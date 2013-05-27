package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 * @create 5/23/13 3:11 PM
 */
public class AddressObject {

    private String address;

    private ModelNode readResourceModelNode;
    private ModelNode readResourceDescriptionModelNode;
    private ModelNode readChildrenTypeModelNode;

    //resourceName=>JSON of ModelNode
    private Map<String, String> genericOperations = new HashMap<String, String>();


    public AddressObject(String address, ModelNode readResourceModelNode, ModelNode readResourceDescriptionModelNode, ModelNode readChildrenTypeModelNode) {
        this.address = address;
        this.readResourceModelNode = readResourceModelNode;
        this.readResourceDescriptionModelNode = readResourceDescriptionModelNode;
        this.readChildrenTypeModelNode = readChildrenTypeModelNode;
                
    }

    public String getAddress() {
        return address;
    }

    public ModelNode getReadResourceModelNode() {
        return readResourceModelNode;
    }

    public ModelNode getReadResourceDescriptionModelNode() {
        return readResourceDescriptionModelNode;
    }

    public ModelNode getReadChildrenTypeModelNode() {
        return readChildrenTypeModelNode;
    }

    public void addGenericOperationResult(String resource, ModelNode resultJSON){
        //TODO: 
//        genericOperations.put(resource, resultJSON);
    }

    public boolean hasGenericOperations(String resourceName) {
/*
        String readOperationNamesResponse = genericOperations.get(resourceName);
        if(readOperationNamesResponse == null) {
            return false;
        }
        else {
            JSONValue value = JSONParser.parseLenient(readOperationNamesResponse);
            JSONObject jsonObj = value.isObject();
            if (jsonObj.get("outcome").isString().stringValue().equals("failed")) {
                return false;
            }

            JSONArray array = jsonObj.get("result").isArray();
            for(int i=0; i<array.size(); i++) {
                if(array.get(i).isString().stringValue().equals("add")) {
                    return true;
                }
            }
            return false;
        }
*/
        return false;
    }

    public List<String> getChildrenTypes() {
        List<ModelNode> childrenTypesModelNodes = readChildrenTypeModelNode.get("result").asList();
        List<String> childrenTypes = new ArrayList<String>(childrenTypesModelNodes.size());
        for(ModelNode node: childrenTypesModelNodes){
            childrenTypes.add(node.asString());
        }
        return childrenTypes;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        //TODO: convert to json
        JSONObject json = new JSONObject();
        json.put("address", getAddress());
        json.put("name", "root");
        json.put("leaf", false);

        ModelNode result = readResourceModelNode.get("result");
        ModelNode resourceDesc = readResourceDescriptionModelNode.get("result");
        
        List<String> childrenTypes = getChildrenTypes();
        
        List<JSONObject> attributes = new ArrayList<JSONObject>();
        List<JSONObject> pathes = new ArrayList<JSONObject>();
        
        for (ModelNode node : result.asList()) {           
            Property prop = node.asProperty();
            if (childrenTypes.contains(prop.getName())) { // resource node
/*
                if (hasGenericOperations(addressPath, prop.getName())) {
                    add(new ManagementModelNode(cliGuiCtx, new UserObject(node, prop.getName())));
                }
*/
                if (prop.getValue().isDefined()) { // path, as subsystem=jmx
                    for (ModelNode innerNode : prop.getValue().asList()) {
                        PathNodeObject pathNodeObject = new PathNodeObject(this.getAddress(), prop.getName(), innerNode.asProperty().getName(), false);
                        pathes.add(pathNodeObject.toJSONObject());
                    }
                }
            }
            else { // attribute node
                AttributeNodeObject attributeNodeObject = new AttributeNodeObject(this.getAddress(), prop.getName(), prop.getValue().asString(), new AttributeDescription(resourceDesc.get("attributes", prop.getName())));
                attributes.add(attributeNodeObject.toJSONObject());
            }           
        }

        Collections.sort(attributes, new Comparator<JSONObject>() {
            public int compare(JSONObject o1, JSONObject o2) {
                return ((String)o1.get("name")).compareTo((String)o2.get("name"));
            }
        });

        Collections.sort(pathes, new Comparator<JSONObject>() {
            public int compare(JSONObject o1, JSONObject o2) {
                return ((String)o1.get("name")).compareTo((String)o2.get("name"));
            }
        });

        JSONArray children = new JSONArray();
        children.addAll(attributes);
        children.addAll(pathes);
        json.put("children", children);
        return json;
    }
}

abstract class NodeObject {
    private String name;
    private String value;
    private boolean isLeaf;
    private String separator = "=";
    private String baseAddress;

    protected NodeObject(String baseAddress, String name, String value, boolean leaf, String separator) {
        this.baseAddress = baseAddress;
        this.name = name;
        this.value = value;
        this.isLeaf = leaf;
        this.separator = separator;
    }

    String getBaseAddress() {
        return baseAddress;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isLeaf() {
        return this.isLeaf;
    }

    public String getSeparator() {
        return separator;
    }

    @Override
    public String toString() {
        return this.name + this.separator + this.value;
    }


    public abstract JSONObject toJSONObject();
}

class PathNodeObject extends NodeObject {
    
    private boolean isGeneric = false;

    public PathNodeObject(String baseAddress, String name, String value, boolean generic) {
        super(baseAddress, name, value, false, "=");
        isGeneric = generic;
    }

    public boolean isGeneric() {
        return this.isGeneric;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject childJSON = new JSONObject();
        childJSON.put("address", getBaseAddress() + "/" + getName() + getSeparator() + getValue());
        childJSON.put("name", getName());
        childJSON.put("value", getValue());
        childJSON.put("displayname", getName() + getSeparator() + getValue());
        childJSON.put("leaf", isLeaf());
        return childJSON;
    }
}

class AttributeNodeObject extends NodeObject {
    
    private AttributeDescription attribDesc = null;

    AttributeNodeObject(String baseAddress, String name, String value, AttributeDescription attribDesc) {
        super(baseAddress, name, value, true, attribDesc.isGraphable() ? " \u2245 " : "=>");
        this.attribDesc = attribDesc;
    }

    public AttributeDescription getAttributeDescription() {
        return this.attribDesc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject childJSON = new JSONObject();
        childJSON.put("address", getBaseAddress() + "/" + getName() + getSeparator() + getValue());
        childJSON.put("name", getName());
        childJSON.put("value", getValue());
        childJSON.put("displayname", getName() + getSeparator() + getValue());
        childJSON.put("leaf", isLeaf());
        return childJSON;
    }
}


class AttributeDescription {

    private ModelNode attributes;

    AttributeDescription(ModelNode attributes) {
        this.attributes = attributes;
    }

    // Is this a runtime attribute?
    public boolean isRuntime() {
        return attributes.get("storage").asString().equals("runtime");
    }

    public ModelType getType() {
        return attributes.get("type").asType();
    }

    public boolean isGraphable() {
        return isRuntime() && isNumeric();
    }

    public boolean isNumeric() {
        ModelType type = getType();
        return (type == ModelType.BIG_DECIMAL) ||
                (type == ModelType.BIG_INTEGER) ||
                (type == ModelType.DOUBLE) ||
                (type == ModelType.INT) ||
                (type == ModelType.LONG);
    }
}
