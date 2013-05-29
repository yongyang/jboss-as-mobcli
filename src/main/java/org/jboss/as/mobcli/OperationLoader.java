package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Young Yang</a>
 */
public class OperationLoader extends ModelNodeLoader {

    private static final String[] genericOps = {"add", "read-operation-description", "read-resource-description", "read-operation-names"};
    private static final List<String> genericOpList = Arrays.asList(genericOps);
    private static final String[] leafOps = {"write-attribute", "undefine-attribute"};
    private static final List<String> leafOpList = Arrays.asList(leafOps);

    
    private ModelNode readOperationNamesModelNode;
    private Map<String, ModelNode> operationDescriptions = new HashMap<String, ModelNode>();

    protected OperationLoader() {
        
    }

    @Override
    protected void loadModelNode() {
        
        try {
            readOperationNamesModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-names");
            if (!readOperationNamesModelNode.get("outcome").asString().equals("failed")) {
                for (ModelNode name : readOperationNamesModelNode.get("result").asList()) {
                    String operName = name.asString();
/*
                    if(operName.startsWith("\"")) {
                        operName = operName.substring(1);
                    }
                    if(operName.endsWith("\"")) {
                        operName = operName.substring(0, operName.length()-1);
                    }
*/
                    
                    ModelNode operationDescriptionModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-description(name=" + name + ")");
                    operationDescriptions.put(operName, operationDescriptionModelNode);
                }
            }

        }
        catch (Exception e) {
            throw new RuntimeException("Failed to collect operation info.", e);
        }
    }


    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("address", this.getAddress());

        JSONArray operations = new JSONArray();

        if (readOperationNamesModelNode.get("outcome").asString().equals("success")) {
            for(ModelNode operationDescriptionModelNode : operationDescriptions.values()) {
                if(operationDescriptionModelNode.get("outcome").asString().equals("success")) {
                    String operationName = operationDescriptionModelNode.get("result").get("operation-name").asString();
                    // filter operations
                    if (isGeneric() && !genericOpList.contains(operationName)) continue;
                    if (isLeaf() && !leafOpList.contains(operationName)) continue;
                    if (!isGeneric() && !isLeaf() && operationName.equals("add")) continue;

                    JSONObject operationJSON = (JSONObject)JSONValue.parse(operationDescriptionModelNode.get("result").toJSONString(false));
                    operations.add(operationJSON);
                }
            }
/*
            
            for (ModelNode name : readOperationNamesModelNode.get("result").asList()) {
                String strName = name.asString();

                // filter operations
                if (isGeneric() && !genericOpList.contains(strName)) continue;
                if (isLeaf() && !leafOpList.contains(strName)) continue;
                if (!isGeneric() && !isLeaf() && strName.equals("add")) continue;

                JSONObject operationJSON = new JSONObject();
                operationJSON.put("name", strName);
                //TODO: get description string               
                operationJSON.put("description", operationDescriptions.get(strName).asString());
                operations.add(operationJSON);
            }
*/

        }

        json.put("operations", operations);
        return json;
    }
}
