package com.liaison.framework.audit.hipaa;


import com.liaison.framework.audit.AuditStandardsRequirement;

/**
 * Relevant requirements from HIPAA Admin Simplification 2013/03 (Not all requirements)
 *
 * In general HIPAA requires logging for 
 *  * CRUDE attempts of health related data
 *     * note success or failure
 *  * Authentication attempts
 *     * note success or failure
 *  * Automatic Log off
 *  * Detection of reasonably anticipating security breach attempts
 *  
 * Advisable to log for
 *  * encrypt and decrypt of data
 *  * Transmission of data
 *  
 *  
 * @author jeremyf
 */
public enum HIPAAAdminSimplification201303 implements AuditStandardsRequirement {
	/**
	 * Log-in monitoring (Addressable). 
	 * 
	 * Procedures for monitoring log-in attempts and reporting discrepancies.
	 */
	
	HIPAA_AS_C_164_308_5iiC ("Log-in monitoring"),
	
	/** 
	 * Password management (Addressable). 
	 * Procedures for creating, changing, and safeguarding passwords.
	 */
	
	HIPAA_AS_C_164_308_5iiD ("Password management"),

	
	/**
	 * Access control. </p>
	 * 
	 * Implement technical policies and procedures for electronic 
	 * information systems that maintain electronic protected health 
	 * information to allow access only to those persons or software 
	 * programs that have been granted access rights as specified in  164.308(a)(4). 
	 *
	 */
	
	HIPAA_AS_C_164_312_a1 ("Access Control"),
	
	/**
	 * Automatic logoff (Addressable). 
	 * 
	 * Implement electronic procedures that terminate an electronic session after a predetermined time of inactivity.
	 */
	
	HIPAA_AS_C_164_312_a2iii ("Automatic logoff"),
	
	/**
	 *  Encryption and decryption (Addressable). 
	 * 
	 * Implement a mechanism to encrypt and decrypt electronic protected health information.
	 */
	HIPAA_AS_C_164_312_a2iv ("Encryption and decryption"),
	
	/**
	 * Standard: Person or entity authentication. 
	 * Implement procedures to verify that a person or entity seeking access to electronic protected health information is the one claimed.
	 */
	HIPAA_AS_C_164_312_c2d("Person or entity authentication"),
	
	
	
    /**
     * Confidentiality, integrity, and availability of health information<p/>
     * 
     * Ensure the confidentiality, integrity, and availability of all electronic 
     * protected health information the covered entity or business associate creates, 
     * receives, maintains, or transmits
     */
	HIPAA_AS_C_164_306_a1("Confidentiality, integrity, and availability of health information"),
	
	/**
     * security or integrity of health information<p/>
     * 
     * Protect against any reasonably anticipated threats or 
     * hazards to the security or integrity of such information.
     */
	HIPAA_AS_C_164_306_a2("security or integrity of health information"),
	
	/**
     * Protect against anticipated access/disclosure<p/>
     * 
     * Protect against any reasonably anticipated uses or disclosures of such information 
     * that are not permitted or required under subpart E of this part. 
     */
	HIPAA_AS_C_164_306_a3("Protect against anticipated access/disclosure"),
	
	
    /**
     * Risk management <p/>
     * (Required). Implement security measures sufficient to reduce risks and vulnerabilities to a 
     * reasonable and appropriate level to comply with 164.306(a). 
     */
	HIPAA_AS_C_164_308_1iiB("Risk Management"),

    /**
     * Isolating health care clearinghouse functions <p/>
     * 
     * (Required). If a health care clearinghouse is part of a larger organization, the clearinghouse 
     * must implement policies and procedures that protect the electronic protected health 
     * information of the clearinghouse from unauthorized access by the larger organization.
     * 
     */
	HIPAA_AS_C_164_308_4iiA("All actions taken by any individual with root or administrative privileges.");


    private String description;

    private HIPAAAdminSimplification201303(String description) {
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