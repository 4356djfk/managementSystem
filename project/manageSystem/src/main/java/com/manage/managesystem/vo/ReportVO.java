package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 鎶ヨ〃VO
 */
@Data
public class ReportVO {

    private Long id;

    private String reportType;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private String contentJson;

    private String status;

    private Long attachmentId;

    private String attachmentUrl;

    private Long generatedBy;

    private String generatedByName;

    private LocalDateTime generatedAt;
}
