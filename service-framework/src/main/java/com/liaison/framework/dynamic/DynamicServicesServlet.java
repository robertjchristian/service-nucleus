package com.liaison.framework.dynamic;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.liaison.framework.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Dynamic Services Servlet
 * <p/>
 * <P>Entry point, router, and processor of dynamic service requests
 * <p/>
 * Note:  This is a very quick and dirty implementation for proof of concept only.  Next
 * iteration, this will be evolved heavily and include richer support, and WADL.
 *
 * @author Robert.Christian
 * @version 1.0
 */
@Singleton
public class DynamicServicesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DynamicServicesServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // TODO:  Probably shouldn't build and load every time, but don't want to cache and hold indefinitely either
        // needs to be updatable in real-time
        // for now, always expect com.liaison.service config here under classpath
        String rawBindingConfiguration = ServiceUtils.readFileFromClassPath("/bindings.json");

        // parse com.liaison.service configuration
        DynamicBindings serviceBindings = new Gson().fromJson(rawBindingConfiguration, DynamicBindings.class);

        // ie if http://localhost:8989/hello-world/dyn/foo/bar/baz, then /foo/bar/baz
        String path = request.getPathInfo();

        // if no path info, show landing page information
        if (path == null || path.equals("/")) {

            // TODO: Probably shouldn't build every time...
            String html = DynamicServicesWebPageBuilder.buildHTMLPageFromBindings(rawBindingConfiguration, serviceBindings);
            response.getWriter().print(html);

            return;
        }

        // match incoming url with service
        DynamicServiceMatcher dsm = new DynamicServiceMatcher(serviceBindings, path);

        // read in JavaScript contents
        String scriptContents = ServiceUtils.readFileFromClassPath(dsm.matchedBinding.scriptLocation);
        logger.info("Script contents:\n" + scriptContents);

        ScriptEngineManager manager = new ScriptEngineManager();

        // Obtain a ScriptEngine that supports the JavaScript short name.
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        // Evaluate the script.
        Object result = null;
        try {
            engine.eval(scriptContents);

            Invocable invocable = (Invocable) engine;

            result = invocable.invokeFunction(dsm.matchedFunction, dsm.matchedParameters[0], dsm.matchedParameters[1]);

            logger.info("Result of invocable is " + result);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //String html = "<html><body>URL:  {pathInfo}<br>Method:  {Method}<br></body></html>";
        //html = html.replace("{pathInfo}", "Path info:  " + path);
        //html = html.replace("{match}", "false");

        String json = "[\n" + "  {\"Function\": \"" + dsm.matchedFunction + "\"},\n";
        json += "  {\"Parameters\": \"" + ServiceUtils.formatArrayAsString(dsm.matchedParameters, ",") + "\"},\n";
        json += "  {\"Result\": \"" + result + "\"}\n";
        json += "]";


        response.getWriter().print(json);

    }


    // TODO this is a quick and dirty (and buggy) way to match inbound dynamic service urls to a service specification
    // TODO and implementation for the purpose of demonstrating a Proof Of Concept for dynamic services.  Need to move
    // TODO towards using a WADL to leverage existing tools, documentation, and knowledge.  NetFlix has a similar notion
    // TODO of dynamic endpoints but it doesn't seem to have been released as open source as of this time.
    class DynamicServiceMatcher {

        DynamicBinding matchedBinding = null;
        String matchedFunction = null;
        String[] matchedParameters = null;

        private final Logger logger = LoggerFactory.getLogger(DynamicServicesServlet.class);

        public DynamicServiceMatcher(DynamicBindings serviceBindings, String path) {

            // lazy load of service match
            // TODO prefer eager-loading/parsing of service binding information
            // TODO also note this is a quick and dirty proof of concept... there are bugs.
            // TODO need to move to WADL based url matching and validation, and use bindings
            // TODO only for binding wadl-to-JS and security

            for (DynamicBinding db : serviceBindings.bindings) {
                // pathInfo = /v1/math/multiply/1/2
                if (path.startsWith(db.baseURI)) {
                    logger.info("Found possible match (" + db.serviceName + ") for " + path + "... checking available operations.");
                    for (Operation o : db.operations) {
                        String spec = db.baseURI + o.operationUrl;
                        // ... as a quick and dirty, just remove the params to see if we have a match
                        // TODO this is just to demo a POC... use WADL to match and apply template parameters
                        spec = spec.substring(0, spec.indexOf('{'));

                        if (path.startsWith(spec)) {
                            logger.info("Matched " + path + " with spec " + db.baseURI + o.operationUrl);
                            // now check for parameters
                            String tail = path.replaceFirst(spec, ""); // should be empty or parameters
                            String[] params = tail.split("/");

                            matchedBinding = db;
                            matchedParameters = params;

                            // hacky... just for POC  (much cleaner to separate function and template parameters in configuration)
                            logger.info(o.operationUrl + ", " + o.operationUrl.split("/")[1]);
                            matchedFunction = o.operationUrl.split("/")[1];
                            logger.info("Function: " + matchedFunction);


                        }
                    }
                }

            }


        }

    }

}