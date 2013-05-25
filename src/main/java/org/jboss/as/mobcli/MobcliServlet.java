package org.jboss.as.mobcli;

import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
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
        
        if(pathInfo.equals("resource")) { // list all resources of an address path
            executor.execute(new Runnable() {
                public void run() {
                    listResource(asyncContext);
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
        asyncContext.getRequest();
        ServletResponse resp = asyncContext.getResponse();
        try {
            JSONObject resourceJSON = proxy.readResourceNode("127.0.0.1", 9999, "/").toJSONObject();
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("success", true);
            resultJSON.put("data", resourceJSON);
            writeJSON(resp, resultJSON);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeCommand(AsyncContext asyncContext){

    }

    private void writeJSON(ServletResponse resp, JSONObject json) throws IOException{
        try {
            resp.setContentType("application/json");
            json.writeJSONString(resp.getWriter());
            resp.flushBuffer();
            resp.getWriter().close();
        }
        finally {
            resp.getWriter().close();
        }
    }

}
