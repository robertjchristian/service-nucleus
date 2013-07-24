/*
 * Copyright 2005 Anders Nyman.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.j2ep;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.j2ep.factories.MethodNotAllowedException;
import net.sf.j2ep.factories.RequestHandlerFactory;
import net.sf.j2ep.factories.ResponseHandlerFactory;
import net.sf.j2ep.model.AllowedMethodHandler;
import net.sf.j2ep.model.RequestHandler;
import net.sf.j2ep.model.ResponseHandler;
import net.sf.j2ep.model.Server;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A reverse proxy using a set of Rules to identify which resource to proxy.
 * 
 * At first the rule chain is traversed trying to find a matching rule.
 * When the rule is found it is given the option to rewrite the URL.
 * The rewritten URL is then sent to a Server creating a Response Handler
 * that can be used to process the response with streams and headers.
 * 
 * The rules and servers are created dynamically and are specified in the
 * XML data file. This allows the proxy to be easily extended by creating
 * new rules and new servers.
 * 
 * @author Anders Nyman
 */
public class ProxyFilter implements Filter {

    /** 
     * The server chain, will be traversed to find a matching server.
     */
    private ServerChain serverChain;
    
    /** 
     * Logging element supplied by commons-logging.
     */
    private static Log log;
    
    /** 
     * The httpclient used to make all connections with, supplied by commons-httpclient.
     */
    private HttpClient httpClient;

    /**
     * Implementation of a reverse-proxy. All request go through here. This is
     * the main class where are handling starts.
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        Server server = (Server) httpRequest.getAttribute("proxyServer");  
        if (server == null) {
            server = serverChain.evaluate(httpRequest);
        }
        
        if (server == null) {
            filterChain.doFilter(request, response);
        } else {
            String uri = server.getRule().process(getURI(httpRequest));
            String url = request.getScheme() + "://" + server.getDomainName() + server.getPath() + uri;
            log.debug("Connecting to " + url);
            
            ResponseHandler responseHandler = null;
            
            try {
                httpRequest = server.preExecute(httpRequest);
                responseHandler = executeRequest(httpRequest, url);
                httpResponse = server.postExecute(httpResponse);

                responseHandler.process(httpResponse);
            } catch (HttpException e) {
                log.error("Problem while connecting to server", e);
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                server.setConnectionExceptionRecieved(e);
            } catch (UnknownHostException e) {
                log.error("Could not connection to the host specified", e);
                httpResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                server.setConnectionExceptionRecieved(e);
            } catch (IOException e) {
                log.error( "Problem probably with the input being send, either with a Header or the Stream", e);
                httpResponse .setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (MethodNotAllowedException e) {
                log.error("Incoming method could not be handled", e);
                httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpResponse.setHeader("Allow", e.getAllowedMethods());
            } finally {
                if (responseHandler != null) {
                    responseHandler.close();
                }
            }
        }
    }
    
    /**
     * Will build a URI but including the Query String. That means that it really
     * isn't a URI, but quite near.
     * 
     * @param httpRequest Request to get the URI and query string from
     * @return The URI for this request including the query string
     */
    private String getURI(HttpServletRequest httpRequest) {
        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI().substring(contextPath.length());
        if (httpRequest.getQueryString() != null) {
            uri += "?" + httpRequest.getQueryString();
        }
        return uri;
    }

    /**
     * Will create the method and execute it. After this the method
     * is sent to a ResponseHandler that is returned.
     * 
     * @param httpRequest Request we are receiving from the client
     * @param url The location we are proxying to
     * @return A ResponseHandler that can be used to write the response
     * @throws MethodNotAllowedException If the method specified by the request isn't handled
     * @throws IOException When there is a problem with the streams
     * @throws HttpException The httpclient can throw HttpExcetion when executing the method
     */
    private ResponseHandler executeRequest(HttpServletRequest httpRequest,
            String url) throws MethodNotAllowedException, IOException,
            HttpException {
        RequestHandler requestHandler = RequestHandlerFactory
                .createRequestMethod(httpRequest.getMethod());

        HttpMethod method = requestHandler.process(httpRequest, url);
        method.setFollowRedirects(false);

        /*
         * Why does method.validate() return true when the method has been
         * aborted? I mean, if validate returns true the API says that means
         * that the method is ready to be executed. 
         * TODO I don't like doing type casting here, see above.
         */
        if (!((HttpMethodBase) method).isAborted()) {
            httpClient.executeMethod(method);

            if (method.getStatusCode() == 405) {
                Header allow = method.getResponseHeader("allow");
                String value = allow.getValue();
                throw new MethodNotAllowedException(
                        "Status code 405 from server", AllowedMethodHandler
                                .processAllowHeader(value));
            }
        }

        return ResponseHandlerFactory.createResponseHandler(method);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     * 
     * Called upon initialization, Will create the ConfigParser and get the
     * RuleChain back. Will also configure the httpclient.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        log = LogFactory.getLog(ProxyFilter.class);
        AllowedMethodHandler.setAllowedMethods("OPTIONS,GET,HEAD,POST,PUT,DELETE,TRACE");
        
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getParams().setBooleanParameter(HttpClientParams.USE_EXPECT_CONTINUE, false);
        httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        
        String data = filterConfig.getInitParameter("dataUrl");
        if (data == null) {
            serverChain = null;
        } else {
            try {
                File dataFile = new File(filterConfig.getServletContext().getRealPath(data));
                ConfigParser parser = new ConfigParser(dataFile);
                serverChain = parser.getServerChain();               
            } catch (Exception e) {
                throw new ServletException(e);
            } 
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     * 
     * Called when this filter is destroyed.
     * Releases the fields.
     */
    public void destroy() {
        log = null;
        httpClient = null;
        serverChain = null;
    }
}