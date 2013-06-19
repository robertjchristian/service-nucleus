package com.liaison.framework.audit.log4j2;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.SyslogAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttr;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.RFC5424Layout;
import org.apache.logging.log4j.core.layout.SyslogLayout;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.EnglishEnums;


//TODO Research: We might be able to do this with the new "Filter" mechanism of log4j2 -- jfr
@Plugin(name = "AuditSyslogAppender", category = "Core", elementType = "appender", printObject = true)
public class AuditSyslogAppender <T extends Serializable> extends SyslogAppender<T>  {

	private static final String RFC5424 = "RFC5424";
    protected AuditSyslogAppender(String name, Layout<T> layout, Filter filter,
			boolean handleException, boolean immediateFlush,
			AbstractSocketManager manager, Advertiser advertiser) {
		super(name, layout, filter, handleException, immediateFlush, manager,
				advertiser);
	}


    /**
     * This is a clean rip off of Log4J SyslogAppender PluginFactory Appender. -jfr
     * TODO will need to be updated when out of log4j2 is out of beta - jfr
     * 
     * Create a SyslogAppender.
     * @param host The name of the host to connect to.
     * @param portNum The port to connect to on the target host.
     * @param protocol The Protocol to use.
     * @param delay The interval in which failed writes should be retried.
     * @param immediateFail True if the write should fail if no socket is immediately available.
     * @param name The name of the Appender.
     * @param immediateFlush "true" if data should be flushed on each write.
     * @param suppress "true" if exceptions should be hidden from the application, "false" otherwise.
     * The default is "true".
     * @param facility The Facility is used to try to classify the message.
     * @param id The default structured data id to use when formatting according to RFC 5424.
     * @param ein The IANA enterprise number.
     * @param includeMDC Indicates whether data from the ThreadContextMap will be included in the RFC 5424 Syslog
     * record. Defaults to "true:.
     * @param mdcId The id to use for the MDC Structured Data Element.
     * @param mdcPrefix The prefix to add to MDC key names.
     * @param eventPrefix The prefix to add to event key names.
     * @param includeNL If true, a newline will be appended to the end of the syslog record. The default is false.
     * @param escapeNL String that should be used to replace newlines within the message text.
     * @param appName The value to use as the APP-NAME in the RFC 5424 syslog record.
     * @param msgId The default value to be used in the MSGID field of RFC 5424 syslog records.
     * @param excludes A comma separated list of mdc keys that should be excluded from the LogEvent.
     * @param includes A comma separated list of mdc keys that should be included in the FlumeEvent.
     * @param required A comma separated list of mdc keys that must be present in the MDC.
     * @param format If set to "RFC5424" the data will be formatted in accordance with RFC 5424. Otherwise,
     * it will be formatted as a BSD Syslog record.
     * @param filter A Filter to determine if the event should be handled by this Appender.
     * @param config The Configuration.
     * @param charsetName The character set to use when converting the syslog String to a byte array.
     * @param exceptionPattern The converter pattern to use for formatting exceptions.
     * @return A SyslogAppender.
     */
    @PluginFactory
    public static <S extends Serializable> AuditSyslogAppender<S> createAppender(@PluginAttr("host") final String host,
                                                @PluginAttr("port") final String portNum,
                                                @PluginAttr("protocol") final String protocol,
                                                @PluginAttr("reconnectionDelay") final String delay,
                                                @PluginAttr("immediateFail") final String immediateFail,
                                                @PluginAttr("name") final String name,
                                                @PluginAttr("immediateFlush") final String immediateFlush,
                                                @PluginAttr("suppressExceptions") final String suppress,
                                                @PluginAttr("facility") final String facility,
                                                @PluginAttr("id") final String id,
                                                @PluginAttr("enterpriseNumber") final String ein,
                                                @PluginAttr("includeMDC") final String includeMDC,
                                                @PluginAttr("mdcId") final String mdcId,
                                                @PluginAttr("mdcPrefix") final String mdcPrefix,
                                                @PluginAttr("eventPrefix") final String eventPrefix,
                                                @PluginAttr("newLine") final String includeNL,
                                                @PluginAttr("newLineEscape") final String escapeNL,
                                                @PluginAttr("appName") final String appName,
                                                @PluginAttr("messageId") final String msgId,
                                                @PluginAttr("mdcExcludes") final String excludes,
                                                @PluginAttr("mdcIncludes") final String includes,
                                                @PluginAttr("mdcRequired") final String required,
                                                @PluginAttr("format") final String format,
                                                @PluginElement("filters") final Filter filter,
                                                @PluginConfiguration final Configuration config,
                                                @PluginAttr("charset") final String charsetName,
                                                @PluginAttr("exceptionPattern") final String exceptionPattern,
                                                @PluginElement("LoggerFields") Logger loggerFields,
                                                @PluginAttr("advertise") final String advertise) {

        final boolean isFlush = immediateFlush == null ? true : Boolean.valueOf(immediateFlush);
        final boolean handleExceptions = suppress == null ? true : Boolean.valueOf(suppress);
        final int reconnectDelay = delay == null ? 0 : Integer.parseInt(delay);
        final boolean fail = immediateFail == null ? true : Boolean.valueOf(immediateFail);
        final int port = portNum == null ? 0 : Integer.parseInt(portNum);
        boolean isAdvertise = advertise == null ? false : Boolean.valueOf(advertise);
        @SuppressWarnings("unchecked")
        final Layout<S> layout = (Layout<S>)(RFC5424.equalsIgnoreCase(format) ?
        	RFC5424Layout.createLayout(facility, id, ein, includeMDC, mdcId, mdcPrefix, eventPrefix, includeNL, 
        		escapeNL, appName, msgId, excludes, includes, required, exceptionPattern, config) :
            SyslogLayout.createLayout(facility, includeNL, escapeNL, charsetName));

        if (name == null) {
            LOGGER.error("No name provided for SyslogAppender");
            return null;
        }
        
        @SuppressWarnings("unused") //This is from beta7 release... I expect this method to change -jfr
		final String prot = protocol != null ? protocol : Protocol.UDP.name();
        final Protocol p = EnglishEnums.valueOf(Protocol.class, protocol);
        final AbstractSocketManager manager = createSocketManager(p, host, port, reconnectDelay, fail, layout);
        if (manager == null) {
            return null;
        }

        return new AuditSyslogAppender<S>(name, layout, filter, handleExceptions, isFlush, manager,
                isAdvertise ? config.getAdvertiser() : null);
    }
    
    
    /**
     * Preflight logic for append method
     * @return true if everything is ok
     */
    private boolean preflightCheckAppendIsOK(Message message) {
    	//Preflight check. kill nulls
    	if (null == message ) {
    		return false; 
    	}
    	
    	//Preflight check if it's a parameterized message with no auditstatement, kill it.
    	if (message instanceof ParameterizedMessage 
    			&& null == AuditParameterizedMessage.extractAuditStatement((ParameterizedMessage) message)) {
    		return false; 
    	}
    	
    	return true;
    }
    
    /**
     * Kills any non-auditstatement bearing messages
     * Passes along new event wrapping a JSON message to actual syslog appender (super class).
     */
	@Override
	public void append(LogEvent event) {
    	Message message = event.getMessage();

    	//Assures we have ParameterizedMessage bearing an AuditParameterizedMessage
    	if (!preflightCheckAppendIsOK(message)) {
    		return; //NOP
    	}

   		//Extract serialized JSON message and jam into SimpleMessage.
    	AuditLogMessage auditMessage = new AuditParameterizedMessage(((ParameterizedMessage)message));
   		SimpleMessage newMessage = new SimpleMessage(auditMessage.toString());

   		//copy LogEvent, but wrap new SimpleMessage
    	Log4jLogEvent newEvent =new Log4jLogEvent(event.getLoggerName(), event.getMarker(), event.getFQCN(), 
    			event.getLevel(), newMessage, event.getThrown());
    	
    	//Finally we're really just trying to use the syslog appender
    	superAppend(newEvent);
    }
	
	/**
	 * Provided as monkey patch point for testing
	 * @param event
	 */
	void superAppend(LogEvent event) {
		super.append(event);		
	}
	
	

}