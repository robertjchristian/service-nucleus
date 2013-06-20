package com.liaison.framework.audit.log4j2;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.message.ParameterizedMessage;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.liaison.framework.audit.AuditStatement;

/**
 * Mainly for serializing in json (via jaxb).
 * 
 * @author jeremyfranklin-ross
 *
 */
@XmlRootElement(name="AuditLogMessage")
public class AuditParameterizedMessage extends ParameterizedMessage implements AuditLogMessage {

	private static final long serialVersionUID = 1138794012807L;

	private AuditParameterizedMessage (String messagePattern, Object[] arguments, Throwable throwable)  {
		super(messagePattern, arguments, throwable);
	}

	/**
	 * Note: does not assure give parameterizedMessage actually contains an audit statement.
	 * @param parameterizedMessage
	 */
	public AuditParameterizedMessage (ParameterizedMessage parameterizedMessage)  {
		super(parameterizedMessage.getFormat(), parameterizedMessage.getParameters(), parameterizedMessage.getThrowable());
	}
	
	
	@Override
	@XmlTransient
	public String getFormat() {
		return super.getFormat();
	}

	
	@Override
	@XmlTransient
	public Object[] getParameters() {
		return super.getParameters();
	}
	
	@XmlElement(name="throwable")	
	public String getThrowableMessage() {
		if (null == getThrowable()) {
			return null;
		}
		return getThrowable().toString();
	}
	
    public AuditStatement getAuditStatement() {
		AuditStatement auditStatement = extractAuditStatement(this);
		if (null == auditStatement) {
			throw new RuntimeException("Expected AuditStatement");
		}
		return auditStatement;
    }
	
	/**
	 * Extracts audit statement from parameters or throwable
	 * @param parameterizedMessage
	 * @return null if no AuditStatement present
	 */
	public static AuditStatement extractAuditStatement(ParameterizedMessage parameterizedMessage) {
		for (Object wrappedObject : parameterizedMessage.getParameters()) {
	    	if (null != wrappedObject && wrappedObject instanceof AuditStatement) {
	    		return (AuditStatement)wrappedObject;
	    	}
		}
		if (null != parameterizedMessage.getThrowable() 
				&& parameterizedMessage.getThrowable() instanceof AuditStatement) {
			return (AuditStatement)parameterizedMessage.getThrowable();
		}
		return null;
	}
	

	/**
	 * JSON serialized form
	 */
	@Override
	public String toString() {
		try {
			return marshallToJson(this);
		} catch ( JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "ERROR SERIALIZING LOG MESSAGE";
	}
	
	/**
	 * TODO move this to a general jaxbutil
	 */
	@SuppressWarnings("deprecation")
	private String marshallToJson(AuditParameterizedMessage object) throws JAXBException,
		JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(
				primary, secondary);

		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().setAnnotationIntrospector(
				introspector);
		// make serializer use JAXB annotations (only)
		mapper.getSerializationConfig().setAnnotationIntrospector(introspector);

		return mapper.writeValueAsString(object);
	}
	
}
