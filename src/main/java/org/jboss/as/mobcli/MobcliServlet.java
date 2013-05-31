package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
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
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

/**
 * @author <a href="mailto:yyang@redhat.com">Yong Yang</a>
 */
@WebServlet(urlPatterns = "/cliservlet/*", asyncSupported = true)
public class MobcliServlet extends HttpServlet {

    private static final ModelControllerProxy proxy = ModelControllerProxy.getInstance();

    private static final ExecutorService executor = Executors.newCachedThreadPool();

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
        else if(pathInfo.equals("operation")) { // read one operation's resource
            listOperation(req, resp);
        }
        else if(pathInfo.equals("execute")){ // execute submitted command
            executeCommand(req, resp);
        }
        else {
            throw new ServletException(new IllegalArgumentException(req.getPathInfo()));
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

    private void listResource(ServletRequest req,  ServletResponse resp) {
        try {
            String nodeString = req.getParameter("node");
            JSONObject nodeJSON = (JSONObject)JSONValue.parse(nodeString);
            JSONObject resourceJSON = ModelNodeLoader.newResourceLoader().load("127.0.0.1", 9999, nodeJSON).toJSONObject();
            writeResponseJSON(resp, resourceJSON);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void listOperation(ServletRequest req,  ServletResponse resp) {
        try {
            String nodeString = req.getParameter("node");
            JSONObject nodeJSON = (JSONObject)JSONValue.parse(nodeString);
            JSONObject operationJSON = ModelNodeLoader.newOperationLoader().load("127.0.0.1", 9999, nodeJSON).toJSONObject();
/*
            JSONObject operationJSON = new JSONObject();

            JSONArray names = new JSONArray();
            for(int i=0; i<5; i++){
                JSONObject name = new JSONObject();
                name.put("name", "add");
                names.add(name);
            }
            
            operationJSON.put("operations",names);
*/
            writeResponseJSON(resp, operationJSON);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    private ModelNode getResourceDescription(String addressPath, String name) {
        try {
            return proxy.executeModelNode("127.0.0.1", 9999, addressPath + ":read-operation-description(name=\"" + name + "\")");
        } catch (Exception e) {
            return null;
        }
    }


    private void executeCommand(ServletRequest req,  ServletResponse resp){

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

}
