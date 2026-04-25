package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 闄勪欢淇℃伅瀹炰綋
 * 瀵瑰簲琛細attachment_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentEntity extends BaseEntity {
    /**
     * 鏂囦欢锟?
     */
    private String fileName;

    /**
     * 鏂囦欢鍦板潃
     */
    private String fileUrl;

    /**
     * 鏂囦欢澶у皬
     */
    private Long fileSize;

    /**
     * 鏂囦欢绫诲瀷
     */
    private String fileType;

    /**
     * 涓氬姟绫诲瀷
     */
    private String bizType;

    /**
     * 涓氬姟 ID
     */
    private Long bizId;

    /**
     * 涓婁紶锟?
     */
    private Long uploadedBy;

    /**
     * 涓婁紶鏃堕棿
     */
    private LocalDateTime uploadedAt;
}
