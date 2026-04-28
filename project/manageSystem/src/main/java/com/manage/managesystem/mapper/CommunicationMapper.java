package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.CommunicationMatrixQueryDto;
import com.manage.managesystem.dto.CommunicationRecordQueryDto;
import com.manage.managesystem.dto.MeetingQueryDto;
import com.manage.managesystem.entity.CommunicationMatrixEntity;
import com.manage.managesystem.entity.CommunicationRecordEntity;
import com.manage.managesystem.entity.MeetingEntity;
import com.manage.managesystem.vo.CommunicationMatrixVO;
import com.manage.managesystem.vo.CommunicationRecordVO;
import com.manage.managesystem.vo.MeetingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommunicationMapper {
    List<CommunicationMatrixVO> selectCommunicationMatrixByProjectId(@Param("projectId") Long projectId,
                                                                     @Param("query") CommunicationMatrixQueryDto query);

    CommunicationMatrixVO selectCommunicationMatrixById(@Param("projectId") Long projectId, @Param("id") Long id);

    CommunicationMatrixEntity selectCommunicationMatrixEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertCommunicationMatrix(CommunicationMatrixEntity entity);

    int updateCommunicationMatrix(CommunicationMatrixEntity entity);

    int deleteCommunicationMatrix(@Param("projectId") Long projectId, @Param("id") Long id);

    List<MeetingVO> selectMeetingsByProjectId(@Param("projectId") Long projectId,
                                              @Param("query") MeetingQueryDto query);

    MeetingVO selectMeetingById(@Param("projectId") Long projectId, @Param("id") Long id);

    MeetingEntity selectMeetingEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertMeeting(MeetingEntity entity);

    int updateMeeting(MeetingEntity entity);

    int deleteMeeting(@Param("projectId") Long projectId, @Param("id") Long id);

    int countCommunicationRecordsByMeetingId(@Param("projectId") Long projectId, @Param("meetingId") Long meetingId);

    List<CommunicationRecordVO> selectCommunicationRecordsByProjectId(@Param("projectId") Long projectId,
                                                                      @Param("query") CommunicationRecordQueryDto query);

    CommunicationRecordVO selectCommunicationRecordById(@Param("projectId") Long projectId, @Param("id") Long id);

    CommunicationRecordEntity selectCommunicationRecordEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertCommunicationRecord(CommunicationRecordEntity entity);

    int updateCommunicationRecord(CommunicationRecordEntity entity);

    int softDeleteCommunicationRecord(@Param("projectId") Long projectId,
                                      @Param("id") Long id,
                                      @Param("updatedAt") LocalDateTime updatedAt);
}
