package com.manage.managesystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class ImportResultVO {
    private Integer successCount;

    private Integer failCount;

    private List<String> errors;
}
