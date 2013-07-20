package com.liaison.framework.audit.log4j2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.liaison.framework.audit.AuditStatement;

/**
 * Mainly for serializing in json (via jaxb).
 * 
 * @author jeremyfranklin-ross
 *
 */
@XmlRootElement(name="AuditLogMessage")
public interface AuditLogMessage {

	@XmlElement
	public AuditStatement getAuditStatement();
	
	/**
	 * JSON serialization
     * @return a serialized json version 
	 */
	public String toString();
}
