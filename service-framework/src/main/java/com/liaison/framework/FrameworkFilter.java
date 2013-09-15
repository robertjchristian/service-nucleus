package com.liaison.framework;

import com.liaison.framework.audit.AuditLogger;
import com.liaison.framework.audit.AuditStatement;
import com.liaison.framework.audit.DefaultAuditStatement;
import com.liaison.framework.audit.pci.PCIV20Requirement;
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
        //logger.info("Initializing FrameworkFilter...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //logger.info("Info!");

        //AuditLogger.log(PCIV20Requirement.PCI10_2_2, AuditStatement.Status.ATTEMPT, "Attempting to create PID");

        int pid = ProcessManager.initTransaction();

        // TODO audit with PID

        // threads are pooled and reused, so here we note not only the thread id but also the process id
        //logger.debug("Filtering " + chain.toString() + " [thread=" + Thread.currentThread().getId() + ", PID=" + pid + "]");

        try {

            // audit
            chain.doFilter(request, response);


            // audit
            // TODO audit with PID

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