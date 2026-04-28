package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.ArchiveItemEntity;
import com.manage.managesystem.entity.AttachmentEntity;
import com.manage.managesystem.entity.LessonLearnedEntity;
import com.manage.managesystem.entity.ReportRecordEntity;
import com.manage.managesystem.vo.ArchiveItemVO;
import com.manage.managesystem.vo.AttachmentVO;
import com.manage.managesystem.vo.LessonLearnedVO;
import com.manage.managesystem.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProjectClosureMapper {
    List<ArchiveItemVO> selectArchivesByProjectId(@Param("projectId") Long projectId);

    int insertArchive(ArchiveItemEntity entity);

    int softDeleteArchive(@Param("projectId") Long projectId,
                          @Param("id") Long id,
                          @Param("updatedAt") LocalDateTime updatedAt);

    List<LessonLearnedVO> selectLessonsByProjectId(@Param("projectId") Long projectId);

    LessonLearnedVO selectLessonById(@Param("projectId") Long projectId, @Param("id") Long id);

    LessonLearnedEntity selectLessonEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertLesson(LessonLearnedEntity entity);

    int updateLesson(LessonLearnedEntity entity);

    int softDeleteLesson(@Param("projectId") Long projectId,
                         @Param("id") Long id,
                         @Param("updatedAt") LocalDateTime updatedAt);

    List<ReportVO> selectReportsByProjectId(@Param("projectId") Long projectId);

    ReportVO selectReportById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertReport(ReportRecordEntity entity);

    int softDeleteReport(@Param("projectId") Long projectId,
                         @Param("id") Long id);

    AttachmentVO selectAttachmentById(@Param("id") Long id);

    int softDeleteAttachment(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
}
