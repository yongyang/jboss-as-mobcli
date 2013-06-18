package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
@WebServlet(urlPatterns = "/cliservlet/*", asyncSupported = false)
public class MobcliServlet extends HttpServlet {

    private static final CommandContextProxy proxy = CommandContextProxy.getInstance();

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    private static final String SESSION_KEY="__MOBCLI__";
    
    //TODO: httpsession record current ip port username password

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 9999;

    private static final AsyncListener asyncListener = new AsyncListener() {
        public void onComplete(AsyncEvent event) throws IOException {

        }

        public void onTimeout(AsyncEvent event) throws IOException {
            System.out.println("Timeout: " + event.toString());
        }

        public void onError(AsyncEvent event) throws IOException {
            Throwable t = event.getThrowable();
            if(t != null) {
                t.printStackTrace();
            }
        }

        public void onStartAsync(AsyncEvent event) throws IOException {

        }
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
        //processAsync(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if(pathInfo.startsWith("/")) {
                pathInfo=pathInfo.substring(1);
            }

            if(pathInfo.endsWith("/")) {
                pathInfo=pathInfo.substring(0, pathInfo.length()-1);
            }

            if(pathInfo.equals("resources")) { // list all resources of an address path
                listResource(req, resp);
            }
            else if(pathInfo.equals("operations")) { // list operations of an resouce node
                listOperation(req, resp);
            }
            else if(pathInfo.equals("operationDesc")) { // read one operation description
                showOperationDescription(req, resp);
            }
            else if(pathInfo.equals("execute")){ // execute submitted command
                executeCommand(req, resp);
            }
            else if(pathInfo.equals("connect")) {
                connect(req,resp);
            }
            else {
                throw new ServletException(new IllegalArgumentException(req.getPathInfo()));
            }
        }
        catch (Exception e) {
            if(e instanceof ServletException) {
                throw (ServletException)e;
            }
            if(e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else {
                throw new ServletException(e);
            }
            
        }        
    }

    private void processAsync(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final AsyncContext asyncContext = req.startAsync();
        asyncContext.addListener(asyncListener);

        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                process((HttpServletRequest) asyncContext.getRequest(), (HttpServletResponse) asyncContext.getResponse());
                return (Void)null;
            }
        });

    }

    private void connect(ServletRequest req,  ServletResponse resp) throws Exception{
        String host = req.getParameter("host");
        String port = req.getParameter("port");
        String user = req.getParameter("user");
        String password = req.getParameter("password");
        
        SessionObject session = new SessionObject(host, Integer.parseInt(port), user, password);
        
        CommandContextProxy.getInstance().connect(session);
        
        HttpSession httpSession = ((HttpServletRequest)req).getSession(true);
        httpSession.setAttribute(SESSION_KEY, session);
        JSONObject resultJSON = new JSONObject();
        writeResponseJSON(resp, resultJSON);
    }

    private void listResource(ServletRequest req,  ServletResponse resp) throws Exception{
        SessionObject session = checkSession(req);
        String nodeString = req.getParameter("node");
        JSONObject nodeJSON = (JSONObject)JSONValue.parse(nodeString);
        JSONObject resourceJSON = ModelNodeLoader.newResourceLoader().load(session, nodeJSON).toJSONObject();
        writeResponseJSON(resp, resourceJSON);
    }

    private void listOperation(ServletRequest req,  ServletResponse resp)  throws Exception {
        SessionObject session = checkSession(req);
        String nodeString = req.getParameter("node");
        JSONObject nodeJSON = (JSONObject)JSONValue.parse(nodeString);
        JSONObject operationJSON = ModelNodeLoader.newOperationsLoader().load(session, nodeJSON).toJSONObject();
        writeResponseJSON(resp, operationJSON);        
    }

    private void showOperationDescription(ServletRequest req, ServletResponse resp)  throws Exception {
        SessionObject session = checkSession(req);
        JSONObject fakeNodeJSON = new JSONObject();
        fakeNodeJSON.put("name", req.getParameter("name"));
        fakeNodeJSON.put("address", req.getParameter("address"));
        JSONObject operationJSON = ModelNodeLoader.newOperationDescriptionLoader().load(session, fakeNodeJSON).toJSONObject();
        writeResponseJSON(resp, operationJSON);
    }


    private void executeCommand(ServletRequest req,  ServletResponse resp) throws Exception {
        SessionObject session = checkSession(req);
        JSONObject json = OperationExecutor.newOperationExecutor().execute(session, req.getParameter("address"), req.getParameter("operation"), req.getParameterMap()).toJSONObject();
         writeResponseJSON(resp, json);
    }

    private void writeResponseJSON(ServletResponse resp, JSONObject resultJSON) throws IOException{
        try {
            resp.setContentType("application/json");
            JSONObject responseJSON = new JSONObject();
            responseJSON.put("success", true);
            responseJSON.put("data", resultJSON);

            responseJSON.writeJSONString(resp.getWriter());
            resp.flushBuffer();
            resp.getWriter().close();
        }
        finally {
            resp.getWriter().close();
        }
    }


    private void writeResponseModelNode(ServletResponse resp, ModelNode resultModelNode) throws IOException{
        try {
            resp.setContentType("application/json");
            JSONObject responseJSON = new JSONObject();
            responseJSON.put("success", true);
            responseJSON.put("data", resultModelNode.toJSONString(false));
            responseJSON.writeJSONString(resp.getWriter());
            resp.flushBuffer();
            resp.getWriter().close();
        }
        finally {
            resp.getWriter().close();
        }
    }

    private SessionObject checkSession(ServletRequest req) throws ServletException{
        HttpSession session = ((HttpServletRequest)req).getSession(false);
        if(session == null) {
            throw new ServletException("Session Timeout!");
        }
        return (SessionObject)session.getAttribute(SESSION_KEY);
    }
    
    private String getHost(ServletRequest req) {
        HttpSession httpSession = ((HttpServletRequest)req).getSession(true);
        return (String)httpSession.getAttribute("host");        
    }

    private int getPort(ServletRequest req) {
        HttpSession httpSession = ((HttpServletRequest)req).getSession(true);
        return (Integer)httpSession.getAttribute("port");
    }

    private String getUser(ServletRequest req) {
        HttpSession httpSession = ((HttpServletRequest)req).getSession(true);
        return (String)httpSession.getAttribute("user");
    }

    private String getPassword(ServletRequest req) {
        HttpSession httpSession = ((HttpServletRequest)req).getSession(true);
        return (String)httpSession.getAttribute("password");
    }
    
    
}

class SessionObject implements Serializable {
    private String host;
    private int port;
    private String user;
    private String password;

    SessionObject(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionObject that = (SessionObject) o;

        if (port != that.port) return false;
        if (!host.equals(that.host)) return false;
        if (!password.equals(that.password)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        result = 31 * result + user.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}

