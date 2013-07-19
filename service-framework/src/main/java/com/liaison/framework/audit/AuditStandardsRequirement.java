package com.liaison.framework.audit;
import org.codehaus.jackson.annotate.JsonValue;



/**
 * Super class of all standards requirements... presently that means PCI 2.0
 *
 * @author jeremyfranklin-ross
 * @see com.liaison.framework.audit.pci.PCIV20Requirement
 */

public interface AuditStandardsRequirement {

    String getDescription();
    
    /**
     * TODO this is kind of a hack, would be better to have this be a subobject.. but enums are treated a little differently
     * recommend name() + getDescription();
     * @return a serialized json version 
     */
    @JsonValue
    public String toString();

}