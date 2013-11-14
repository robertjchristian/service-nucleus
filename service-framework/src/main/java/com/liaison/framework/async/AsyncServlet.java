package com.liaison.framework.async;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.servlet.annotation.WebServlet(urlPatterns = { "/async" }, asyncSupported = true, initParams = { @WebInitParam(name = "threadpoolsize", 
value = "100") })
public class AsyncServlet extends HttpServlet {

    //private static final Logger logger = Logger.getLogger(AsyncServlet.class);
    private static final Logger logger = LoggerFactory.getLogger(AsyncServlet.class);

    public static final int CALLBACK_TIMEOUT = 10000; // ms

    /** executor service */
    private ExecutorService exec;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        int size = Integer.parseInt(getInitParameter("threadpoolsize"));
        exec = Executors.newFixedThreadPool(size);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    	
	logger.info("calling service() method from AsyncServlet ");


        // TODO Idea is resolving 2.5 rather than 3.0 here and therefore are seeing a compile error
        // need to resolve

        final AsyncContext ctx = req.startAsync();
        //final AsyncContext ctx = null;




        final HttpSession session = req.getSession();

        // set the timeout
        ctx.setTimeout(CALLBACK_TIMEOUT);

        // attach listener to respond to lifecycle events of this AsyncContext
        ctx.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                logger.info("onComplete called");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                logger.info("onTimeout called");
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                logger.info("onError called: " + event.toString());
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                logger.info("onStartAsync called");
            }
        });

        enqueLongRunningTask(ctx, session);
    }

    /**
     * if something goes wrong in the task, it simply causes timeout condition that causes the async context listener to be invoked (after the fact)
     * <p/>
     * if the {@link AsyncContext#getResponse()} is null, that means this context has already timed out (and context listener has been invoked).
     */
    private void enqueLongRunningTask(final AsyncContext ctx, final HttpSession session) {

        exec.execute(new Runnable() {

            @Override
            public void run() {

                String some_big_data = "blah";  // getSomeBigData();

                try {

                    ServletResponse response = ctx.getResponse();
                    if (response != null) {
                        response.getWriter().write(some_big_data);
                        ctx.complete();
                    } else {
                        throw new IllegalStateException(); // this is caught below
                    }
                } catch (IllegalStateException ex) {
                    logger.error("Request object from context is null! (nothing to worry about.)"); // just means the context was already timeout, timeout listener already called.
                } catch (Exception e) {
                    logger.error("ERROR IN AsyncServlet", e);
                }
            }
        });
    }

    /** destroy the executor */
    @Override
    public void destroy() {

        exec.shutdown();
    }
}