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

    // host:SessionObject=>ASInstanceContext
    private final Map<SessionObject, ASCommandContext> cmdCtxMap = new ConcurrentHashMap<SessionObject, ASCommandContext>();

    // scheduled service to terminate idle CommandContext
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private Runnable runnable = new Runnable() {
        public void run() {
            for(Iterator<Map.Entry<SessionObject, ASCommandContext>> it = cmdCtxMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<SessionObject, ASCommandContext> entry = it.next();
                ASCommandContext asCommandContext = entry.getValue();
                if(!asCommandContext.isIdle()) {
                    asCommandContext.close();
                    //TODO: don't remove, will initial again automatically
                    it.remove();
                }
            }
        }};

    {
        // close idle connection every 15m
        scheduledExecutorService.scheduleWithFixedDelay(runnable, 15, 15, TimeUnit.MINUTES);
    }

    private CommandContextProxy() {
    }

    public static CommandContextProxy getInstance() {
        return INSTANCE;
    }
    
    public void connect(SessionObject session) throws Exception{
        initASInstanceContext(session);
    }

    private void initASInstanceContext(SessionObject session) throws CliInitializationException {
        ASCommandContext asCommandContext = new ASCommandContext(session.getHost(),session.getPort(),session.getUser(), session.getPassword());
        cmdCtxMap.put(session, asCommandContext);
    }

    public String execute(SessionObject session, String command) throws Exception {
        return executeModelNode(session, command).toJSONString(false);
    }

    public ModelNode executeModelNode(SessionObject session, String command) throws Exception {
        if(!cmdCtxMap.containsKey(session)) {
            initASInstanceContext(session);
        }
        ASCommandContext asCommandContext = cmdCtxMap.get(session);
        try {
            return asCommandContext.execute(command);
        }
        finally {
            asCommandContext.close();
        }
    }

    public String[] executeBatch(SessionObject session, String[] commands) throws Exception {
        ModelNode[] nodes = executeBatchModelNode(session, commands);
        String[] jsons = new String[nodes.length];
        for(int i=0; i<nodes.length; i++){
            jsons[i] = nodes[i].toJSONString(false);
        }
        return jsons;
    }

    public ModelNode[] executeBatchModelNode(SessionObject session, String[] commands) throws Exception {
        if(!cmdCtxMap.containsKey(session)) {
            initASInstanceContext(session);
        }
        ASCommandContext asCommandContext = cmdCtxMap.get(session);

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
                cmdCtx.terminateSession();
            }
            lastActive = 0;
        }

        public boolean isIdle() {
            return System.currentTimeMillis() - lastActive > 5 * 60 * 1000; //5m
        }

    }

    public static void main(String[] args) throws Exception{
/*
        CommandContextProxy proxy = CommandContextProxy.getInstance();
        System.out.println(proxy.execute("127.0.0.1", 9999, "/:read-resource-description"));
        proxy.destroy();
*/
    }
}
