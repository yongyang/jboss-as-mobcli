package org.jboss.as.mobcli;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
public class CommandContextProxy {

    private final static CommandContextProxy INSTANCE = new CommandContextProxy();

    // host:port=>ASInstanceContext
    private Map<String, ASCommandContext> cmdCtxMap = new ConcurrentHashMap<String, ASCommandContext>();

    // scheduled service to terminate idle CommandContext
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private Runnable runnable = new Runnable() {
        public void run() {
            for(Iterator<Map.Entry<String, ASCommandContext>> it = cmdCtxMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, ASCommandContext> entry = it.next();
                ASCommandContext asCommandContext = entry.getValue();
                if(!asCommandContext.isIdle()) {
                    asCommandContext.close();
                    it.remove();
                }
            }
        }};

    {
        // close idle connection every 5m
        scheduledExecutorService.scheduleWithFixedDelay(runnable, 5, 5, TimeUnit.MINUTES);
    }

    private CommandContextProxy() {
    }

    public static CommandContextProxy getInstance() {
        return INSTANCE;
    }
    
    public void connect(String host, int port, String user, String password) throws Exception{
        initASInstanceContext(host, port, user, password);
    }

    private void initASInstanceContext(String host, int port, String user, String password) throws CliInitializationException {
        ASCommandContext asCommandContext = new ASCommandContext(host,port,user,password);
        cmdCtxMap.put(host+":"+port, asCommandContext);
    }

    public String execute(String ip, int port, String command) throws Exception {
        return executeModelNode(ip, port, command).toJSONString(false);
    }

    public ModelNode executeModelNode(String ip, int port, String command) throws Exception {
        String key = ip + ":" +port;
        if(!cmdCtxMap.containsKey(key)) {
            initASInstanceContext(ip, port, "", "");
        }
        ASCommandContext asCommandContext = cmdCtxMap.get(key);
        try {
            return asCommandContext.execute(command);
        }
        finally {
            asCommandContext.close();
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
        ASCommandContext asCommandContext = cmdCtxMap.get(key);

        ModelNode[] responses = new ModelNode[commands.length];
        try {

            for(int i=0; i<commands.length; i++){
                ModelNode response = asCommandContext.execute(commands[i]);
                responses[i] = response;
            }
            return responses;
        }
        finally {
            asCommandContext.close();
        }
    }

    public void destroy() {
        runnable.run();
        scheduledExecutorService.shutdownNow();
    }

    class ASCommandContext {
        private String ip;
        private int port;
        private String username;
        private String password;
        private CommandContext cmdCtx;

        private long lastActive = 0;

        ASCommandContext(String ip, int port, String username, String password) throws CliInitializationException {
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
                try {
                    cmdCtx.terminateSession();
                }
                catch (Exception e) {
                    // terminate quietly
                }
            }
            lastActive = 0;
        }

        public boolean isIdle() {
            return System.currentTimeMillis() - lastActive > 5 * 60 * 1000; //5m
        }

    }

    public static void main(String[] args) throws Exception{
        CommandContextProxy proxy = CommandContextProxy.getInstance();
        System.out.println(proxy.execute("127.0.0.1", 9999, "/:read-resource-description"));
        proxy.destroy();
    }
}
