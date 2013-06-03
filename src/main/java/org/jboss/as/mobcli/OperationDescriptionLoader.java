package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:yyang@redhat.com">Young Yang</a>
 */
public class OperationDescriptionLoader extends ModelNodeLoader {
    
    private String operationName;
    private ModelNode operationDescriptionModelNode;

    protected OperationDescriptionLoader() {
        
    }

    @Override
    public ModelNodeLoader load(String ip, int port, JSONObject nodeJSON) throws Exception {
        this.ip = ip;
        this.port = port;
        this.address = nodeJSON.get("address").toString();
        this.operationName = nodeJSON.get("name").toString();
        loadModelNode();
        return this;
    }

    public String getOperationName() {
        return operationName;
    }

    @Override
    protected void loadModelNode()  throws Exception {
        operationDescriptionModelNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":read-operation-description(name=\"" + getOperationName() + "\")");
    }


    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {       
        JSONObject json = new JSONObject();
        if(operationDescriptionModelNode.get("outcome").asString().equals("success")) {
            ModelNode resultModelNode = operationDescriptionModelNode.get("result");
            json.put("operationDesc", (JSONObject)JSONValue.parse(resultModelNode.toJSONString(true)));
            json.put("operationDescString", resultModelNode.toJSONString(false));
        }
        return json;
    }
}
