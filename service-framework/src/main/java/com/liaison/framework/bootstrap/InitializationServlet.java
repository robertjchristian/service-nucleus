// TODO check web.xml... should be deprecated


package com.liaison.framework.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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

    }
}
