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
 * @author <a href="mailto:yyang@redhat.com">Young Yang</a>
 */
public class OperationExecutor {

    private ModelControllerProxy proxy = ModelControllerProxy.getInstance();

    private String ip;
    private int port;
    private String address;
    private String operationName;
    private Map<String, Object> paramMap = new HashMap<String, Object>();

    private ModelNode resultModeNode;
    protected OperationExecutor() {
        
    }
    
    public static OperationExecutor newOperationExecutor() {
        return new OperationExecutor();
    }

    public ModelControllerProxy getProxy() {
        return proxy;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public OperationExecutor execute(String ip, int port, String address, String operationName, Map<String, Object> paramMap) throws Exception {
        this.ip = ip;
        this.port = port;
        this.address = address;
        resultModeNode = getProxy().executeModelNode(getIp(), getPort(), getAddress() + ":" + operationName);
        return this;
    }

    public ModelNode getResultModeNode() {
        return resultModeNode;
    }

}
