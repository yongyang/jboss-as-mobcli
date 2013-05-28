package org.jboss.as.mobcli;

import org.jboss.dmr.ModelNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        processAsync(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processAsync(req, resp);
    }

    private void processAsync(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final AsyncContext asyncContext = req.startAsync();
        asyncContext.addListener(asyncListener);

        String pathInfo = req.getPathInfo();
        if(pathInfo.startsWith("/")) {
            pathInfo=pathInfo.substring(1);
        }

        if(pathInfo.endsWith("/")) {
            pathInfo=pathInfo.substring(0, pathInfo.length()-1);
        }
        
        if(pathInfo.equals("resources")) { // list all resources of an address path
            executor.execute(new Runnable() {
                public void run() {
                    listResource(asyncContext);
                }
            });
        }
        else if(pathInfo.equals("operations")) { // list operations of an resouce node
            executor.execute(new Runnable() {
                public void run() {
                    listOperation(asyncContext);
                }
            });
        }
        else if(pathInfo.equals("command")){ // execute submitted command
            executor.execute(new Runnable() {
                public void run() {
                    executeCommand(asyncContext);
                }
            });
        }
        else {
            throw new IllegalArgumentException(req.getPathInfo());
        }
    }

    private void listResource(AsyncContext asyncContext) {
        ServletResponse resp = asyncContext.getResponse();
        ServletRequest req = asyncContext.getRequest();
        try {
            String address = req.getParameter("addr");
            JSONObject resourceJSON = ResourceParser.newResourceParser("127.0.0.1", 9999, address).toJSONObject();
            writeResponseJSON(resp, resourceJSON);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void listOperation(AsyncContext asyncContext) {
        ServletResponse resp = asyncContext.getResponse();
        ServletRequest req = asyncContext.getRequest();
        try {
            String nodeString = req.getParameter("node");
            //TODO: don't transfer node
            JSONObject nodeJSON = (JSONObject)JSONValue.parse(nodeString);
            OperationParser.newOperationParser("127.0.0.1", 9999, "address").toJSONObject();
            JSONObject operationJSON = new JSONObject();

            JSONArray names = new JSONArray();
            for(int i=0; i<5; i++){
                JSONObject name = new JSONObject();
                name.put("name", "add");
                names.add(name);
            }
            
            operationJSON.put("children",names);
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


    private void executeCommand(AsyncContext asyncContext){

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
