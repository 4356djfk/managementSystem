package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.BaselineQueryDto;
import com.manage.managesystem.dto.ConfigItemQueryDto;
import com.manage.managesystem.entity.BaselineRecordEntity;
import com.manage.managesystem.entity.ConfigItemEntity;
import com.manage.managesystem.vo.BaselineRecordVO;
import com.manage.managesystem.vo.ConfigItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ConfigurationMapper {
    List<ConfigItemVO> selectConfigItemsByProjectId(@Param("projectId") Long projectId,
                                                    @Param("query") ConfigItemQueryDto query);

    ConfigItemVO selectConfigItemById(@Param("projectId") Long projectId, @Param("id") Long id);

    ConfigItemEntity selectConfigItemEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertConfigItem(ConfigItemEntity entity);

    int updateConfigItem(ConfigItemEntity entity);

    int softDeleteConfigItem(@Param("projectId") Long projectId,
                             @Param("id") Long id,
                             @Param("updatedBy") Long updatedBy,
                             @Param("updatedAt") LocalDateTime updatedAt);

    int softDeleteConfigItemsByProjectId(@Param("projectId") Long projectId,
                                         @Param("updatedBy") Long updatedBy,
                                         @Param("updatedAt") LocalDateTime updatedAt);

    List<BaselineRecordVO> selectBaselineRecordsByProjectId(@Param("projectId") Long projectId,
                                                            @Param("query") BaselineQueryDto query);

    BaselineRecordVO selectBaselineRecordById(@Param("projectId") Long projectId, @Param("id") Long id);

    BaselineRecordEntity selectBaselineRecordEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int countBaselineRecords(@Param("projectId") Long projectId, @Param("baselineType") String baselineType);

    int insertBaselineRecord(BaselineRecordEntity entity);

    int deleteBaselineRecord(@Param("projectId") Long projectId, @Param("id") Long id);
}
