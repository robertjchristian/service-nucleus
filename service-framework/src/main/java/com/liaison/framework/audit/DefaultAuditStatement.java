package com.liaison.framework.audit;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;


/**
 * General use implementation of AuditStatement
 * 
 * Apply to any logger configured with AuditStatement aware appender.
 *
 */
public class DefaultAuditStatement implements AuditStatement {

    private final AuditStandardsRequirement requirement;
    private final Status status;
    private final String message;

    public DefaultAuditStatement(AuditStandardsRequirement requirement, Status status, String message) {
        this.requirement = requirement;
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public AuditStandardsRequirement getAuditStandardsRequirement() {
        return requirement;
    }

    /**
     * makes a JSON version of this audit statement.
     */
    public String toString() {
    	try {
    		return marshallToJson(this);
		} catch (  JAXBException| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return message;
    }
    
    //TODO move to jaxbutil
	@SuppressWarnings("deprecation")
	private String marshallToJson(AuditStatement object) throws JAXBException,
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
