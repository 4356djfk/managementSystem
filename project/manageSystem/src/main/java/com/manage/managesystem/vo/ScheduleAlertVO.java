package com.manage.managesystem.vo;

import lombok.Data;

@Data
public class ScheduleAlertVO {
    private Long taskId;

    private String taskName;

    private String alertLevel;

    private String alertMessage;
}
