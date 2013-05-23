package org.jboss.as.mobcli;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
public class ModelControllerProxy {

    private final static ModelControllerProxy INSTANCE = new ModelControllerProxy();

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 9999;

    // host:port=>ASInstanceContext
    private Map<String, ASInstanceContext> cmdCtxMap = new ConcurrentHashMap<String, ASInstanceContext>();

    // scheduled service to terminate idle CommandContext
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private Runnable runnable = new Runnable() {
        public void run() {
            for(Iterator<Map.Entry<String, ASInstanceContext>> it = cmdCtxMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, ASInstanceContext> entry = it.next();
                ASInstanceContext asInstanceContext = entry.getValue();
                if(!asInstanceContext.isIdle()) {
                    asInstanceContext.close();
                    it.remove();
                }
            }
        }};

    {
        // close idle connection every 5m
        scheduledExecutorService.scheduleWithFixedDelay(runnable, 5, 5, TimeUnit.MINUTES);
    }

    private ModelControllerProxy() {
    }

    public static ModelControllerProxy getInstance() {
        return INSTANCE;
    }

    private void initASInstanceContext(String host, int port, String username, String password) throws CliInitializationException {
        ASInstanceContext asInstanceContext = new ASInstanceContext(host,port,username,password);
        cmdCtxMap.put(host+":"+port, asInstanceContext);
    }

    // wrapper method for read-resource
    public NodeObject readResourceNode(String ip, int port, String addressPath) throws Exception {
        
        String[] commands = new String[]{
                addressPath + ":read-resource(include-runtime=true,include-defaults=true)",
                addressPath + ":read-resource-description(recursive-depth=1)", //red children's description one time, so the children's tooltip can be displayed after loading
                addressPath + ":read-children-types"
        };

        ModelNode[] result = executeBatchModelNode(ip, port, commands);

        ModelNode readResourceModelNode = result[0];
        ModelNode readResourceDescriptionModelNode = result[1];
        ModelNode readChildrenTypeModelNode = result[2];

        NodeObject nodeObject = new NodeObject(readResourceModelNode,readResourceDescriptionModelNode, readChildrenTypeModelNode);
        
        
        ModelNode resourceResponse = result[0].get("result"); //result modelnode
        if(resourceResponse.isDefined()) {
            for(ModelNode node: resourceResponse.asList()){
                Property prop = node.asProperty();
                String resource = prop.getName();
                ModelNode readOperationNamesModelNode = executeModelNode(ip, port, addressPath + resource + "=*/:read-operation-names");
                nodeObject.addGenericOperationResult(resource, readOperationNamesModelNode);
            }
        }
        return nodeObject;
    }

    public String execute(String ip, int port, String command) throws Exception {
        return executeModelNode(ip, port, command).toJSONString(false);
    }

    private ModelNode executeModelNode(String ip, int port, String command) throws Exception {
        String key = ip + ":" +port;
        if(!cmdCtxMap.containsKey(key)) {
            initASInstanceContext(ip, port, "", "");
        }
        ASInstanceContext asInstanceContext = cmdCtxMap.get(key);
        try {
            return asInstanceContext.execute(command);
        }
        finally {
            asInstanceContext.close();
        }
    }

    public String[] executeBatch(String ip, int port, String[] commands) throws Exception {
        ModelNode[] nodes = executeBatchModelNode(ip, port, commands);
        String[] jsons = new String[nodes.length];
        for(int i=0; i<nodes.length; i++){
            jsons[i] = nodes[i].toJSONString(false);
        }
        return jsons;
    }

    private ModelNode[] executeBatchModelNode(String ip, int port, String[] commands) throws Exception {
        String key = ip + ":" +port;
        if(!cmdCtxMap.containsKey(key)) {
            initASInstanceContext(ip, port, "", "");
        }
        ASInstanceContext asInstanceContext = cmdCtxMap.get(key);

        ModelNode[] responses = new ModelNode[commands.length];
        try {

            for(int i=0; i<commands.length; i++){
                ModelNode response = asInstanceContext.execute(commands[i]);
                responses[i] = response;
            }
            return responses;
        }
        finally {
            asInstanceContext.close();
        }
    }

    public void destroy() {
        runnable.run();
        scheduledExecutorService.shutdownNow();
    }

    class ASInstanceContext {
        private String ip;
        private int port;
        private String username;
        private String password;
        private CommandContext cmdCtx;
        private ModelNode rootModelNode;

        private long lastActive = 0;

        ASInstanceContext(String ip, int port, String username, String password) throws CliInitializationException {
            this.ip = ip;
            this.port = port;
            this.username = username;
            this.password = password;
            initCommandContext();
            lastActive = System.currentTimeMillis();
        }

        private void initCommandContext() throws CliInitializationException {
            if(cmdCtx != null && cmdCtx.getModelControllerClient() != null && !cmdCtx.isTerminated()) {
                return;
            }
            cmdCtx = CommandContextFactory.getInstance().newCommandContext(ip, port, username, password.toCharArray());
            try {
                cmdCtx.connectController();
            } catch (CommandLineException e) {
                throw new CliInitializationException("Failed to connect to the controller", e);
            }
        }

        private ModelNode getRootModelNode() {
            //TODO: SEE org.jboss.as.cli.gui.ManagementModelNode.explore()
/*
            try {
                String addressPath = "/";
                ModelNode resourceDesc = execute(addressPath + ":read-resource-description");
                resourceDesc = resourceDesc.get("result");
                ModelNode response = execute(addressPath + ":read-resource(include-runtime=true,include-defaults=true)");
                ModelNode result = response.get("result");
                if (!result.isDefined()) return null;

                List<String> childrenTypes = getChildrenTypes(addressPath);
                for (ModelNode node : result.asList()) {
                    Property prop = node.asProperty();
                    if (childrenTypes.contains(prop.getName())) { // resource node
                        if (hasGenericOperations(addressPath, prop.getName())) {
                            add(new ManagementModelNode(cliGuiCtx, new UserObject(node, prop.getName())));
                        }
                        if (prop.getValue().isDefined()) {
                            for (ModelNode innerNode : prop.getValue().asList()) {
                                UserObject usrObj = new UserObject(innerNode, prop.getName(), innerNode.asProperty().getName());
                                add(new ManagementModelNode(cliGuiCtx, usrObj));
                            }
                        }
                    } else { // attribute node
                        UserObject usrObj = new UserObject(node, resourceDesc, prop.getName(), prop.getValue().asString());
                        add(new ManagementModelNode(cliGuiCtx, usrObj));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

*/
            return null;
        }

        public ModelNode execute(String command) throws CommandFormatException, IOException, CliInitializationException {
            if(cmdCtx.isTerminated()) {
                initCommandContext();
            }
            lastActive = System.currentTimeMillis();
            ModelControllerClient mcc = cmdCtx.getModelControllerClient();
            ModelNode request = cmdCtx.buildRequest(command);
            ModelNode modelNode = mcc.execute(request);
            lastActive = System.currentTimeMillis();
            return modelNode;
        }

        private List<String> getChildrenTypes(String addressPath) throws Exception {
            List<String> childrenTypes = new ArrayList<String>();
            ModelNode readChildrenTypes = execute(addressPath + ":read-children-types");
            for (ModelNode type : readChildrenTypes.get("result").asList()) {
                childrenTypes.add(type.asString());
            }
            return childrenTypes;
        }

        private boolean hasGenericOperations(String addressPath, String resourceName) throws Exception {
            ModelNode response = execute(addressPath + resourceName + "=*/:read-operation-names");
            if (response.get("outcome").asString().equals("failed")) return false;

            for (ModelNode node : response.get("result").asList()) {
                if (node.asString().equals("add")) return true;
            }
            return false;
        }

        public void close() {
            if(!cmdCtx.isTerminated()) {
                cmdCtx.terminateSession();
            }
            lastActive = 0;
        }

        public boolean isIdle() {
            return System.currentTimeMillis() - lastActive > 5 * 60 * 1000; //5m
        }

    }

    public static void main(String[] args) throws Exception{
        ModelControllerProxy proxy = ModelControllerProxy.getInstance();
        System.out.println(proxy.execute("127.0.0.1", 9999, "/:read-resource-description"));
        proxy.destroy();
    }
}
