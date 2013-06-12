package com.liaison.framework.audit;

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