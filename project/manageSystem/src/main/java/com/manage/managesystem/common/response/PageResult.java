package com.manage.managesystem.common.response;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Integer page;
    private Integer pageSize;
    private Long total;
}
