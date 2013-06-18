package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
public class OperationNamesLoader extends ModelNodeLoader {

    private static final String[] genericOps = {"add", "read-operation-description", "read-resource-description", "read-operation-names"};
    private static final List<String> genericOpList = Arrays.asList(genericOps);
    private static final String[] leafOps = {"write-attribute", "undefine-attribute"};
    private static final List<String> leafOpList = Arrays.asList(leafOps);

    ModelNode readOperationNamesModelNode;
//    private List<ModelNode> operationDescriptionModelNodes = new ArrayList<ModelNode>();

    private List<String> operationNames = new ArrayList<String>();
    
    protected OperationNamesLoader() {
        
    }

    @Override
    protected void loadModelNode()  throws Exception {
        
            if(isLeaf()) {
                operationNames.addAll(leafOpList);
/*
                for(String operName : leafOps) {
                    ModelNode operationDescriptionModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-description(name=\"" + operName + "\")");
                    operationDescriptionModelNodes.add(operationDescriptionModelNode);
                }
*/
            }
            else if(isGeneric()) {
                operationNames.addAll(genericOpList);
/*
                for(String operName : genericOps) {
                    ModelNode operationDescriptionModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-description(name=\"" + operName + "\")");
                    operationDescriptionModelNodes.add(operationDescriptionModelNode);
                }
*/
            }
            else {
                //TODO: 可以只返回operation names列表，点击 operation 时再 read-operation-description
                readOperationNamesModelNode = getProxy().executeModelNode(getSessionObject(), getAddress() + ":read-operation-names");
                if (!readOperationNamesModelNode.get("outcome").asString().equals("failed")) {
                    for (ModelNode name : readOperationNamesModelNode.get("result").asList()) {
                        String operName = name.asString();
                        operationNames.add(operName);
/*
                        ModelNode operationDescriptionModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-description(name=\"" + operName + "\")");
                        operationDescriptionModelNodes.add(operationDescriptionModelNode);
*/
                    }
                }
            }
    }


    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("address", this.getAddress());

        JSONArray operations = new JSONArray();
        
        for(String operationName : operationNames) {
            JSONObject operationJSON = new JSONObject();
            operationJSON.put("operation-name", operationName);
            operations.add(operationJSON);
        }
        
/*
        for(ModelNode operationDescriptionModelNode : operationDescriptionModelNodes) {
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
*/

        json.put("operations", operations);
        return json;
    }
}
