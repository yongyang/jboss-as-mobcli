package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:yyang@redhat.com">Young Yang</a>
 */
public class OperationParser {

    private static final String[] genericOps = {"add", "read-operation-description", "read-resource-description", "read-operation-names"};
    private static final List<String> genericOpList = Arrays.asList(genericOps);
    private static final String[] leafOps = {"write-attribute", "undefine-attribute"};
    private static final List<String> leafOpList = Arrays.asList(leafOps);

    private ModelControllerProxy proxy = ModelControllerProxy.getInstance();

    private String ip;
    private int port;
    private String address;

    public static OperationParser newOperationParser(String ip, int port, String address) {
        return new OperationParser(ip, port, address);
    }

    private OperationParser(String ip, int port, String address) {
        this.ip = ip;
        this.port = port;
        this.address = address;
        load();
    }

    private void load() {
        try {
/*
            ModelNode opNames = proxy.executeModelNode("127.0.0.1", 9999, address + ":read-operation-names");
            if (opNames.get("outcome").asString().equals("failed")) return;
*/

/*
        for (ModelNode name : opNames.get("result").asList()) {
            String strName = name.asString();

            // filter operations
            if (node.isGeneric() && !genericOpList.contains(strName)) continue;
            if (node.isLeaf() && !leafOpList.contains(strName)) continue;
            if (!node.isGeneric() && !node.isLeaf() && strName.equals("add")) continue;

            ModelNode opDescription = getResourceDescription(addressPath, strName);
            add(new OperationAction(node, strName, opDescription));
        }
*/

        }
        catch (Exception e) {
            throw new RuntimeException("Failed to collect operation info.", e);
        }
    }

    public JSONObject toJSONObject() {
        return null;
    }
}
