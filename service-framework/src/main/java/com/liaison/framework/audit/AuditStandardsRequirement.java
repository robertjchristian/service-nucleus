package com.liaison.framework.audit;

/**
 * Super class of all standards requirements... presently that means PCI 2.0
 *
 * @author jeremyf
 * @see com.liaison.framework.audit.pci.PCIV20Requirement
 */
public interface AuditStandardsRequirement {

    String getDescription();

}