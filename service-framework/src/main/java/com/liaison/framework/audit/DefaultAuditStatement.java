package com.liaison.framework.audit;

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
        return this.message;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public AuditStandardsRequirement getAuditStandardsRequirement() {
        return this.getAuditStandardsRequirement();
    }

    public String toString() {
      return getAuditStandardsRequirement().toString() + getAuditStandardsRequirement().getDescription() + "::" + getStatus() + "::" + getMessage();
    }


}
