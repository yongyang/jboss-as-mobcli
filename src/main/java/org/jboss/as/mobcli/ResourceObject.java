package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 * @create 5/23/13 3:11 PM
 */
public class ResourceObject {

    private String name;
    private String value;
    private boolean isLeaf;
    private boolean isGeneric = false;
    private String separator;
    private AttributeDescription attribDesc = null;


    private ModelNode readResourceModelNode;
    private ModelNode readResourceDescriptionModelNode;
    private ModelNode readChildrenTypeModelNode;

    //resourceName=>JSON of ModelNode
    private Map<String, String> genericOperations = new HashMap<String, String>();


    public ResourceObject(ModelNode readResourceModelNode, ModelNode readResourceDescriptionModelNode, ModelNode readChildrenTypeModelNode) {
        
        this.readResourceModelNode = readResourceModelNode;
        this.readResourceDescriptionModelNode = readResourceDescriptionModelNode;
        this.readChildrenTypeModelNode = readChildrenTypeModelNode;
        
        this.attribDesc = new AttributeDescription(readResourceDescriptionModelNode.get("attributes", name));
        
        this.name = name;
        this.value = value;
        this.isLeaf = true;

        if (this.attribDesc.isGraphable()) {
            this.separator = " \u2245 ";
        } else {
            this.separator = " => ";
        }
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
        List<ModelNode> childrenTypesJSONArray = readChildrenTypeModelNode.get("result").asList();
        List<String> childrenTypes = new ArrayList<String>(childrenTypesJSONArray.size());
        for(int i=0; i<childrenTypes.size(); i++){
            childrenTypes.add(childrenTypesJSONArray.get(i).asString());
        }
        return childrenTypes;
    }


    public JSONObject toJSONObject() {
        //TODO: convert to json
        JSONObject json = new JSONObject();
        json.put("address", "/");
        json.put("name", "root");
        json.put("leaf", false);

        JSONArray children = new JSONArray();
        JSONObject childJSON = new JSONObject();
        childJSON.put("address", "/abc");
        childJSON.put("name", "abc");
        childJSON.put("leaf", true);
        children.add(childJSON);

        json.put("children", children);
        return json;
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
        return false;
//        return isRuntime() && isNumeric();
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
