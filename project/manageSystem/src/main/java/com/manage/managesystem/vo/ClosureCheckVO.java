package com.manage.managesystem.vo;

import lombok.Data;

@Data
public class ClosureCheckVO {
    private Boolean acceptanceCompleted;

    private Boolean archiveCompleted;

    private Integer openRisks;

    private Integer pendingChanges;

    private Boolean requiredReportsReady;
}
