package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.ContractQueryDto;
import com.manage.managesystem.dto.ProcurementQueryDto;
import com.manage.managesystem.dto.SupplierQueryDto;
import com.manage.managesystem.entity.ContractEntity;
import com.manage.managesystem.entity.ProcurementEntity;
import com.manage.managesystem.entity.SupplierEntity;
import com.manage.managesystem.vo.ContractVO;
import com.manage.managesystem.vo.ProcurementVO;
import com.manage.managesystem.vo.SupplierVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProcurementMapper {
    List<SupplierVO> selectSuppliersByProjectId(@Param("projectId") Long projectId,
                                                @Param("query") SupplierQueryDto query);

    SupplierVO selectSupplierById(@Param("projectId") Long projectId, @Param("id") Long id);

    SupplierEntity selectSupplierEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertSupplier(SupplierEntity entity);

    int updateSupplier(SupplierEntity entity);

    int softDeleteSupplier(@Param("projectId") Long projectId,
                           @Param("id") Long id,
                           @Param("updatedAt") LocalDateTime updatedAt);

    int countProcurementsBySupplierId(@Param("projectId") Long projectId, @Param("supplierId") Long supplierId);

    int countContractsBySupplierId(@Param("projectId") Long projectId, @Param("supplierId") Long supplierId);

    List<ProcurementVO> selectProcurementsByProjectId(@Param("projectId") Long projectId,
                                                      @Param("query") ProcurementQueryDto query);

    ProcurementVO selectProcurementById(@Param("projectId") Long projectId, @Param("id") Long id);

    ProcurementEntity selectProcurementEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertProcurement(ProcurementEntity entity);

    int updateProcurement(ProcurementEntity entity);

    int updateProcurementStatus(@Param("projectId") Long projectId,
                                @Param("id") Long id,
                                @Param("status") String status,
                                @Param("actualDeliveryDate") LocalDate actualDeliveryDate,
                                @Param("updatedAt") LocalDateTime updatedAt);

    int softDeleteProcurement(@Param("projectId") Long projectId,
                              @Param("id") Long id,
                              @Param("updatedAt") LocalDateTime updatedAt);

    List<ContractVO> selectContractsByProjectId(@Param("projectId") Long projectId,
                                                @Param("query") ContractQueryDto query);

    ContractVO selectContractById(@Param("projectId") Long projectId, @Param("id") Long id);

    ContractEntity selectContractEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertContract(ContractEntity entity);

    int updateContract(ContractEntity entity);

    int softDeleteContract(@Param("projectId") Long projectId,
                           @Param("id") Long id,
                           @Param("updatedAt") LocalDateTime updatedAt);
}
