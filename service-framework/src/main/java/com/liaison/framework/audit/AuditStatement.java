package com.liaison.framework.audit;

/**
 * Objects, including throwable implementing AuditStatement will be caught via the AuditSyslogAppender
 * 
 * @author jeremyfranklin-ross
 */
public interface AuditStatement {

    public enum Status {
        FAILED,
        SUCCEED,
        ATTEMPT,
        POTENTIAL
    }

    public String getMessage();

    public Status getStatus();

    public AuditStandardsRequirement getAuditStandardsRequirement();

}