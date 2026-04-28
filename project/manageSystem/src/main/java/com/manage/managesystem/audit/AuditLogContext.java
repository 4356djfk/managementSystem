package com.manage.managesystem.audit;

public final class AuditLogContext {
    public static final String RESULT_CODE_ATTR = AuditLogContext.class.getName() + ".RESULT_CODE";
    public static final String RESPONSE_DATA_ATTR = AuditLogContext.class.getName() + ".RESPONSE_DATA";
    public static final String OPERATOR_ID_ATTR = AuditLogContext.class.getName() + ".OPERATOR_ID";
    public static final String OPERATOR_NAME_ATTR = AuditLogContext.class.getName() + ".OPERATOR_NAME";

    private AuditLogContext() {
    }
}
