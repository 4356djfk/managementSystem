package com.manage.managesystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 閫氱敤瀹炰綋鍩虹被
 */
@Data
public class BaseEntity {
    /**
     * 涓婚敭
     */
    private Long id;

    /**
     * 鍒涘缓锟?
     */
    private Long createdBy;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createdAt;

    /**
     * 鏇存柊锟?
     */
    private Long updatedBy;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updatedAt;

    /**
     * 閫昏緫鍒犻櫎鏍囪锟? 锟?1 锟?
     */
    private Integer deleted;
}
