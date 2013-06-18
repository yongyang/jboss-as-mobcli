package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
public class OperationExecutor {

    private CommandContextProxy proxy = CommandContextProxy.getInstance();
    
    private SessionObject session;
    private String address;
    private String operationName;
    private Map<String, String[]> paramMap = new HashMap<String, String[]>();

    private String command = null;

    private ModelNode resultModeNode;
    protected OperationExecutor() {
        
    }
    
    public static OperationExecutor newOperationExecutor() {
        return new OperationExecutor();
    }

    public CommandContextProxy getProxy() {
        return proxy;
    }

    public SessionObject getSessionObject() {
        return session;
    }

    public String getAddress() {
        return address;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, String[]> getParamMap() {
        return paramMap;
    }

    public OperationExecutor execute(SessionObject session, String address, String operationName, Map<String, String[]> paramMap) throws Exception {
        this.session = session;
        this.address = address;
        this.operationName = operationName;
        this.paramMap = paramMap;
        constructCommand();
        resultModeNode = getProxy().executeModelNode(getSessionObject(), getCommand());
        return this;
    }

    private String getParameterString() throws Exception {
        String paramString = "";
        ModelNode operationDescriptionModelNode = getProxy().executeModelNode(getSessionObject(), getAddress() + ":read-operation-description(name=\"" + operationName + "\")");
        if(operationDescriptionModelNode.get("outcome").asString().equals("success")) {
            ModelNode requestProperties = operationDescriptionModelNode.get("result", "request-properties");
/*
        "request-properties" : {
            "name" : {
                "type" : {
                    "TYPE_MODEL_VALUE" : "STRING"
                },
                "description" : "The name of the attribute to get the value for under the selected resource",
                "nillable" : false
            },
            "include-defaults" : {
                "type" : {
                    "TYPE_MODEL_VALUE" : "BOOLEAN"
                },
                "description" : "Boolean to enable/disable default reading. In case it is set to false only attribute set by user are returned ignoring undefined.",
                "required" : false,
                "nillable" : true,
                "default" : true
            }
        },
*/
            if(requestProperties.isDefined()) { // has requestProperties
                for(ModelNode requestProperty : requestProperties.asList()) {
                    String propName = requestProperty.asProperty().getName();
                    ModelType type = requestProperty.asProperty().getValue().get("type").asType();
//                    Boolean required = requestProperty.asProperty().getValue().get("required").asBoolean();
//                    Boolean nillable = requestProperty.asProperty().getValue().get("nillable").asBoolean();
                    // deal with null value? the result ModelNode includes needed information of properties
/*
                    if(required && !paramMap.containsKey(propName)) {

                    }
*/
                    if(paramMap.containsKey(propName) && !paramMap.get(propName)[0].isEmpty()) {
                        if(!paramString.isEmpty()) {
                            paramString += ",";
                        }
                        if(isStringyType(type)) {
                            paramString += propName + "=\"" + paramMap.get(propName)[0] + "\"";
                        }
                        else {
                            paramString += propName + "=" + paramMap.get(propName)[0];
                        }
                    }
                }
            }
        }
        return paramString;
    }

    private boolean isStringyType(ModelType type) {
        return (type != ModelType.BIG_DECIMAL) &&
                (type != ModelType.BIG_INTEGER) &&
                (type != ModelType.DOUBLE) &&
                (type != ModelType.INT) &&
                (type != ModelType.LONG) &&
                (type != ModelType.BOOLEAN) &&
                (type != ModelType.LIST);
    }

    private void constructCommand() throws Exception {
        String command = getAddress() + ":" + operationName;
        String paramString = getParameterString();
        if(paramString != null && !paramString.isEmpty()) {
            command += "(" + paramString + ")";
        }
        this.command = command;

    }

    private synchronized String getCommand() {
        return command;
    }

    private ModelNode getResultModeNode() {
        return resultModeNode;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("command", this.getCommand());
        json.put("result", getResultModeNode().toJSONString(false));
        return json;
    }

}
