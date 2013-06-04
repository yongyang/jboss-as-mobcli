package org.jboss.as.mobcli;

import org.json.simple.JSONObject;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 * @create 5/29/13 2:59 PM
 */
public abstract class ModelNodeLoader {

    private CommandContextProxy proxy = CommandContextProxy.getInstance();

    protected String ip;
    protected int port;
    protected String address;

    private boolean isGeneric;
    private boolean isLeaf;


    protected ModelNodeLoader() {
    }

    public CommandContextProxy getProxy() {
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

    public boolean isGeneric() {
        return isGeneric;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public ModelNodeLoader load(String ip, int port, JSONObject nodeJSON) throws Exception {
        this.ip = ip;
        this.port = port;
        this.address = nodeJSON.get("address").toString();
        this.isGeneric = (Boolean)nodeJSON.get("generic");
        this.isLeaf = (Boolean)nodeJSON.get("leaf");
        loadModelNode();
        return this;
    }
    
    public static OperationNamesLoader newOperationsLoader() {
        return new OperationNamesLoader(); 
    }

    public static OperationDescriptionLoader newOperationDescriptionLoader() {
        return new OperationDescriptionLoader();
    }


    public static ResourceLoader newResourceLoader() {
        return new ResourceLoader();
    }
    
    protected abstract void loadModelNode() throws Exception;
    
    public abstract JSONObject toJSONObject();

}
