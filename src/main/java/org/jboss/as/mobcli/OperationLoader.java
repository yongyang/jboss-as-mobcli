package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Young Yang</a>
 */
public class OperationLoader {

    private static final String[] genericOps = {"add", "read-operation-description", "read-resource-description", "read-operation-names"};
    private static final List<String> genericOpList = Arrays.asList(genericOps);
    private static final String[] leafOps = {"write-attribute", "undefine-attribute"};
    private static final List<String> leafOpList = Arrays.asList(leafOps);

    private ModelControllerProxy proxy = ModelControllerProxy.getInstance();
    
    private ModelNode readOperationNamesModelNode;
    private Map<String, ModelNode> operationDescriptions = new HashMap<String, ModelNode>(); 

    private String ip;
    private int port;
    private String address;
    
    private boolean isGeneric;
    private boolean isLeaf;

    public static OperationLoader newOperationLoader(String ip, int port, String address) {
        return new OperationLoader(ip, port, address);
    }

    private OperationLoader(String ip, int port, String address) {
        this.ip = ip;
        this.port = port;
        this.address = address;
        load();
    }

    private void load() {
        try {
            readOperationNamesModelNode = proxy.executeModelNode(ip, port, address + ":read-operation-names");
            if (!readOperationNamesModelNode.get("outcome").asString().equals("failed")) {
                for (ModelNode name : readOperationNamesModelNode.get("result").asList()) {
                    String operName = name.asString();
                    ModelNode operationDescriptionModelNode = proxy.executeModelNode(ip, port, address + ":read-operation-description(name=\"" + name + "\")");
                    operationDescriptions.put(operName, operationDescriptionModelNode);
                }
            }

        }
        catch (Exception e) {
            throw new RuntimeException("Failed to collect operation info.", e);
        }
    }


    public boolean isGeneric() {
        return isGeneric;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("address", this.address);

        JSONArray operations = new JSONArray();

        if (!readOperationNamesModelNode.get("outcome").asString().equals("failed")) {
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

        }

        json.put("operations", operations);
        return json;
    }
}
