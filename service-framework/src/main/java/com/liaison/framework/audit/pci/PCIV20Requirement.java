package com.liaison.framework.audit.pci;

import javax.xml.bind.annotation.XmlElement;

import com.liaison.framework.audit.AuditStandardsRequirement;

/**
 * Relevant requirements from PCI DSS 2.0 (Not all requirements)
 *
 * @author jeremyf
 */
public enum PCIV20Requirement implements AuditStandardsRequirement {

    /**
     * All individual accesses to cardholder data
     * <p/>
     * Testing Procedure:
     * Verify all individual access to cardholder data is logged.
     * <p/>
     * Guidance:
     * Malicious individuals could obtain knowledge of a user account with access to systems in the CDE,
     * or they could create a new, unauthorized account in order to access cardholder data. A record of
     * all individual accesses to cardholder data can identify which accounts may have been compromised or misused.
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.1
     */
    PCI10_2_1("All individual accesses to cardholder data"),

    /**
     * Requirement:
     * All actions taken by any individual with root or administrative privileges.
     * <p/>
     * Testing Procedure:
     * Verify actions taken by any individual with root or administrative privileges is logged.
     * <p/>
     * Guidance:
     * Accounts with increased privileges, such as the "administrator" or "root" account, have the potential to
     * greatly impact the security or operational functionality of a system. Without a log of the activities performed,
     * an organization is unable to trace any issues resulting from an administrative mistake or misuse of privilege
     * back to the specific action and individual. QSA Report on Compliance Notes (if any)
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.2
     */
    PCI10_2_2("All actions taken by any individual with root or administrative privileges."),

    /**
     * Requirement:
     * Access to all audit trails
     * <p/>
     * Testing Procedure:
     * Verify access to all audit trails is logged.
     * <p/>
     * Guidance:
     * Malicious users often attempt to alter audit logs to hide their actions, and a record of access allows an
     * organization to trace any inconsistencies or potential tampering of the logs to an individual account.
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.3
     */
    PCI10_2_3("Access to all audit trails"),

    /**
     * Requirement:
     * <p/>
     * Invalid logical access attempts
     * <p/>
     * Testing Procedure:
     * Verify invalid logical access attempts are logged.
     * <p/>
     * Guidance:
     * Malicious individuals will often perform multiple access attempts on targeted systems. Multiple invalid
     * login attempts may be an indication of an unauthorized user's attempts to "brute force" or guess a password.
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.4
     */
    PCI10_2_4("Invalid logical access attempts"),

    /**
     * Requirement:
     * Use of identification and authentication mechanisms
     * <p/>
     * Testing Procedure:
     * Verify use of identification and authentication mechanisms is logged.
     * <p/>
     * Guidance:
     * Without knowing who was logged on at the time of an incident, it is impossible to identify the accounts which
     * may be used. Additionally, malicious users may attempt to manipulate the authentication controls with the intent
     * of bypassing them or impersonating a valid account. Activities including, but not limited to, escalation of
     * privilege or changes to access permissions may indicate unauthorized use of a system's authentication
     * mechanisms.
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.5
     */
    PCI10_2_5("Use of identification and authentication mechanisms"),

    /**
     * Requirement:
     * Initialization of the audit logs
     * <p/>
     * Testing Procedure:
     * Verify initialization of audit logs is logged.
     * <p/>
     * Guidance:
     * Turning the audit logs off prior to performing illicit activities is a common goal for malicious users wishing
     * to avoid detection. Initialization of audit logs could indicate that the log function was disabled by a user
     * to hide their actions.
     * <p/>
     * http://wiki.hs.com:6080/display/PCI/PCI+DSS+10.2.6
     */
    PCI10_2_6("Initialization of the audit logs"),

    /**
     * Requirement:
     * Creation and deletion of system-level objects
     * <p/>
     * Testing Procedure:
     * Verify creation and deletion of system level objects are logged.
     * <p/>
     * Guidance:
     * Malicious software, such as malware, often creates or replaces system level objects on the target system in
     * order to control a particular function or operation on that system.
     * <p/>
     * Please refer to the PCI DSS and PA-DSS Glossary of Terms, Abbreviations, and Acronyms for definitions of
     * "system-level objects".
     */
    PCI10_2_7("Creation and deletion of system-level objects");


    private String description;

    private PCIV20Requirement(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    //TODO make serialize properly (enums are a little wonky w/ jsonification)
    public String toString() {
    	return name() + ": " + getDescription();
    }
    
}