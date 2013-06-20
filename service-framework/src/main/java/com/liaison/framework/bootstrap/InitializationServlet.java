package com.liaison.framework.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.framework.audit.AuditStatement;
import com.liaison.framework.audit.DefaultAuditStatement;
import com.liaison.framework.audit.pci.PCIV20Requirement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Initialization Servlet
 * <p/>
 * <P>Bootstrapper
 * <p/>
 * TODO:  Probably not the best place for this.  Should likely move this (and all servlets) to
 * TODO within the guice framework.
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class InitializationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(InitializationServlet.class);

    public void init(ServletConfig config) throws ServletException {
    	// This has to come after any System.setProperty() calls as the
        // configure() method triggers the initialization of the
        // ConfigurationManager
        //logger.info("Initializing Blitz4J...");
        //LoggingConfiguration.getInstance().configure();
    	DefaultAuditStatement audit = new DefaultAuditStatement(PCIV20Requirement.PCI10_2_6, AuditStatement.Status.SUCCEED, "Initialization via servlet");   	
    	logger.info("Servlet Init", audit);
    }
}
