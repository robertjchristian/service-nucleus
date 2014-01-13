package com.liaison.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Framework Filter
 * <p/>
 * <P>Filters incoming requests for audit and handling of propagated exceptions
 *
 * @author Robert.Christian
 * @version 1.0
 */

public class FrameworkFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing FrameworkFilter...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        int pid = ProcessManager.initTransaction();

        logger.info("Processing pid: " + pid);

        try {
            chain.doFilter(request, response);

        } catch (Throwable t) {

            // log
            String msg = "Error processing PID " + pid;
            //logger.error("msg", t);

            // audit
            // TODO PID is thread local.  Should be able to audit with PID alone and correlate later?

            // respond in a terse fashion
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            // finish filtering
            return;
        }
    }

    @Override
    public void destroy() {
        logger.debug("FrameworkFilter Destroyed");
    }

}