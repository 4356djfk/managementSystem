package com.manage.managesystem.vo;

import lombok.Data;

import java.util.List;

/**
 * 浠诲姟璇︽儏VO
 */
@Data
public class TaskDetailVO extends TaskListItemVO {

    private String description;

    private Long parentTaskId;

    private Long wbsId;

    private String wbsName;

    private Long milestoneId;

    private String milestoneName;

    private Integer childTaskCount;

    private List<TaskDependencyVO> dependencies;

    private List<CommentVO> comments;
}
