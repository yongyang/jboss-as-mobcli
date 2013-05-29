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

    public String execute(String ip, int port, String command) throws Exception {
        return executeModelNode(ip, port, command).toJSONString(false);
    }

    public ModelNode executeModelNode(String ip, int port, String command) throws Exception {
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

    public ModelNode[] executeBatchModelNode(String ip, int port, String[] commands) throws Exception {
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
