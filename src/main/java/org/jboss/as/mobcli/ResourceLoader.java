package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 * @create 5/23/13 3:11 PM
 */
public class ResourceLoader extends  ModelNodeLoader{


    private ModelNode readResourceModelNode;
    private ModelNode readResourceDescriptionModelNode;
    private ModelNode readChildrenTypeModelNode;

    //resourceName=>ModelNode
    private Map<String, ModelNode> genericOperations = new HashMap<String, ModelNode>();

    private ModelControllerProxy proxy = ModelControllerProxy.getInstance();


    protected ResourceLoader() {
    }

    @Override
    protected void loadModelNode() {
        // wrapper method for read-resource
        String[] commands = new String[]{
                getAddress() + ":read-resource(include-runtime=true,include-defaults=true)",
                //TODO: ":read-resource-description(recursive-depth=1)" read children's description one time, so the children's tooltip can be displayed after loading
                getAddress() + ":read-resource-description(recursive-depth=1)", 
                getAddress() + ":read-children-types"
        };

        try {
            ModelNode[] result = proxy.executeBatchModelNode(getIp(), getPort(), commands);

            this.readResourceModelNode = result[0];
            this.readResourceDescriptionModelNode = result[1];
            this.readChildrenTypeModelNode = result[2];

            ModelNode resourceResponse = result[0].get("result"); //result modelnode
            if (resourceResponse.isDefined()) {
                for (ModelNode node : resourceResponse.asList()) {
                    Property prop = node.asProperty();
                    String resource = prop.getName();
                    ModelNode readOperationNamesModelNode = proxy.executeModelNode(getIp(), getPort(), getAddress() + resource + "=*/:read-operation-names");
                    this.addGenericOperationResult(resource, readOperationNamesModelNode);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to collect resources info.", e);
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

    public void addGenericOperationResult(String resource, ModelNode resultJSON) {
        genericOperations.put(resource, resultJSON);
    }

    private boolean hasGenericOperations(String resourceName) {
        ModelNode response = genericOperations.get(resourceName);
        if (response.get("outcome").asString().equals("failed")) return false;

        for (ModelNode node : response.get("result").asList()) {
            if (node.asString().equals("add")) return true;
        }
        return false;
    }

    private List<String> getChildrenTypes() {
        List<ModelNode> childrenTypesModelNodes = readChildrenTypeModelNode.get("result").asList();
        List<String> childrenTypes = new ArrayList<String>(childrenTypesModelNodes.size());
        for (ModelNode node : childrenTypesModelNodes) {
            childrenTypes.add(node.asString());
        }
        return childrenTypes;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        // convert to json
        JSONObject json = new JSONObject();
        json.put("address", getAddress());

        ModelNode result = readResourceModelNode.get("result");
        ModelNode resourceDesc = readResourceDescriptionModelNode.get("result");

        JSONArray children = new JSONArray();
        List<String> childrenTypes = getChildrenTypes();
        for (ModelNode node : result.asList()) {
            Property prop = node.asProperty();
            if (childrenTypes.contains(prop.getName())) { // resource node
                if (hasGenericOperations(prop.getName())) { // generic path
//                    PathNodeObject pathNodeObject = new PathNodeObject(this.getAddress(), prop.getName(), "*");
//                    children.add(pathNodeObject.toJSONObject());
                    PathNodeObject pathNodeObject = new PathNodeObject(this.getAddress(), prop.getName(), "*");
                    children.add(pathNodeObject.toJSONObject());
                }
                if (prop.getValue().isDefined()) { // path, as subsystem=jmx
                    for (ModelNode innerNode : prop.getValue().asList()) {
                        PathNodeObject pathNodeObject = new PathNodeObject(this.getAddress(), prop.getName(), innerNode.asProperty().getName());
                        children.add(pathNodeObject.toJSONObject());
                    }
                }
            }
            else { // attribute node
                AttributeNodeObject attributeNodeObject = new AttributeNodeObject(this.getAddress(), prop.getName(), prop.getValue().asString(), new AttributeDescription(resourceDesc.get("attributes", prop.getName())));
                children.add(attributeNodeObject.toJSONObject());
            }
        }

        json.put("nodes", children);
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

    public PathNodeObject(String baseAddress, String name, String value) {
        super(baseAddress, name, value, false, "=");
        if (value.equals("*")) {
            isGeneric = true;
        }
    }

    boolean isGeneric() {
        return isGeneric;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("address", getBaseAddress() + getName() + getSeparator() + getValue() + "/");
        json.put("name", getName());
        json.put("value", getValue());
        json.put("displayname", getName() + getSeparator() + getValue() + (isGeneric() ? "" : " /"));
        json.put("leaf", isLeaf());
        json.put("generic", isGeneric());
        return json;
    }
}

class AttributeNodeObject extends NodeObject {

    private AttributeDescription attribDesc = null;

    AttributeNodeObject(String baseAddress, String name, String value, AttributeDescription attribDesc) {
        super(baseAddress, name, value, true, attribDesc.isGraphable() ? " \u2245 " : "=>");
        this.attribDesc = attribDesc;
    }

    AttributeNodeObject(String baseAddress, String name, String value) {
        super(baseAddress, name, value, true, "=");
    }

    public AttributeDescription getAttributeDescription() {
        return this.attribDesc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("address", getBaseAddress() + getName() + getSeparator() + getValue() + "/");
        json.put("name", getName());
        json.put("value", getValue());
        json.put("displayname", getName() + getSeparator() + getValue());
        json.put("leaf", isLeaf());
        json.put("generic", false);
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
