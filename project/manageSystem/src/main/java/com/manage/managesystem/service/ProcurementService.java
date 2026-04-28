package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.ContractQueryDto;
import com.manage.managesystem.dto.CreateContractDto;
import com.manage.managesystem.dto.CreateProcurementDto;
import com.manage.managesystem.dto.CreateSupplierDto;
import com.manage.managesystem.dto.ProcurementQueryDto;
import com.manage.managesystem.dto.SupplierQueryDto;
import com.manage.managesystem.dto.UpdateContractDto;
import com.manage.managesystem.dto.UpdateProcurementDto;
import com.manage.managesystem.dto.UpdateProcurementStatusDto;
import com.manage.managesystem.dto.UpdateSupplierDto;
import com.manage.managesystem.entity.ContractEntity;
import com.manage.managesystem.entity.ProcurementEntity;
import com.manage.managesystem.entity.SupplierEntity;
import com.manage.managesystem.enums.ContractStatusEnum;
import com.manage.managesystem.enums.ContractTypeEnum;
import com.manage.managesystem.enums.ProcurementStatusEnum;
import com.manage.managesystem.mapper.CostMapper;
import com.manage.managesystem.mapper.ProcurementMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.vo.ContractVO;
import com.manage.managesystem.vo.ProcurementVO;
import com.manage.managesystem.vo.SupplierVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcurementService {
    private final ProcurementMapper procurementMapper;
    private final ProjectMapper projectMapper;
    private final CostMapper costMapper;
    private final ProjectPermissionService projectPermissionService;

    public ProcurementService(ProcurementMapper procurementMapper,
                              ProjectMapper projectMapper,
                              CostMapper costMapper,
                              ProjectPermissionService projectPermissionService) {
        this.procurementMapper = procurementMapper;
        this.projectMapper = projectMapper;
        this.costMapper = costMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public PageResult<SupplierVO> listSuppliers(Long projectId, SupplierQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(procurementMapper.selectSuppliersByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public SupplierVO createSupplier(Long projectId, CreateSupplierDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        SupplierEntity entity = new SupplierEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setSupplierName(dto.getSupplierName().trim());
        entity.setContactName(normalizeText(dto.getContactName()));
        entity.setContactPhone(normalizeText(dto.getContactPhone()));
        entity.setContactEmail(normalizeText(dto.getContactEmail()));
        entity.setAddress(normalizeText(dto.getAddress()));
        entity.setRemark(normalizeText(dto.getRemark()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        procurementMapper.insertSupplier(entity);
        return requireSupplier(projectId, entity.getId());
    }

    @Transactional
    public SupplierVO updateSupplier(Long projectId, Long id, UpdateSupplierDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        SupplierEntity entity = requireSupplierEntity(projectId, id);
        entity.setSupplierName(dto.getSupplierName().trim());
        entity.setContactName(normalizeText(dto.getContactName()));
        entity.setContactPhone(normalizeText(dto.getContactPhone()));
        entity.setContactEmail(normalizeText(dto.getContactEmail()));
        entity.setAddress(normalizeText(dto.getAddress()));
        entity.setRemark(normalizeText(dto.getRemark()));
        entity.setUpdatedAt(LocalDateTime.now());
        procurementMapper.updateSupplier(entity);
        return requireSupplier(projectId, id);
    }

    @Transactional
    public void deleteSupplier(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireSupplierEntity(projectId, id);
        if (procurementMapper.countProcurementsBySupplierId(projectId, id) > 0) {
            throw new IllegalArgumentException("supplier still has procurements");
        }
        if (procurementMapper.countContractsBySupplierId(projectId, id) > 0) {
            throw new IllegalArgumentException("supplier still has contracts");
        }
        procurementMapper.softDeleteSupplier(projectId, id, LocalDateTime.now());
    }

    public PageResult<ProcurementVO> listProcurements(Long projectId, ProcurementQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(procurementMapper.selectProcurementsByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public ProcurementVO createProcurement(Long projectId, CreateProcurementDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateSupplier(projectId, dto.getSupplierId());
        validateCostPlan(projectId, dto.getCostPlanId());
        LocalDateTime now = LocalDateTime.now();
        ProcurementEntity entity = new ProcurementEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setSupplierId(dto.getSupplierId());
        entity.setCostPlanId(dto.getCostPlanId());
        entity.setItemName(dto.getItemName().trim());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setTotalAmount(calculateTotalAmount(dto.getQuantity(), dto.getUnitPrice()));
        entity.setStatus(normalizeProcurementStatus(dto.getStatus()));
        entity.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        entity.setActualDeliveryDate(normalizeActualDeliveryDate(entity.getStatus(), dto.getActualDeliveryDate()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        procurementMapper.insertProcurement(entity);
        return requireProcurement(projectId, entity.getId());
    }

    @Transactional
    public ProcurementVO updateProcurement(Long projectId, Long id, UpdateProcurementDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateSupplier(projectId, dto.getSupplierId());
        validateCostPlan(projectId, dto.getCostPlanId());
        ProcurementEntity entity = requireProcurementEntity(projectId, id);
        entity.setSupplierId(dto.getSupplierId());
        entity.setCostPlanId(dto.getCostPlanId());
        entity.setItemName(dto.getItemName().trim());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setTotalAmount(calculateTotalAmount(dto.getQuantity(), dto.getUnitPrice()));
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : normalizeProcurementStatus(dto.getStatus()));
        entity.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        entity.setActualDeliveryDate(normalizeActualDeliveryDate(entity.getStatus(), dto.getActualDeliveryDate()));
        entity.setUpdatedAt(LocalDateTime.now());
        procurementMapper.updateProcurement(entity);
        return requireProcurement(projectId, id);
    }

    @Transactional
    public ProcurementVO updateProcurementStatus(Long projectId, Long id, UpdateProcurementStatusDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        ProcurementEntity entity = requireProcurementEntity(projectId, id);
        String status = normalizeProcurementStatus(dto.getStatus());
        LocalDate actualDeliveryDate = normalizeActualDeliveryDate(status, dto.getActualDeliveryDate());
        procurementMapper.updateProcurementStatus(projectId, id, status, actualDeliveryDate, LocalDateTime.now());
        return requireProcurement(projectId, entity.getId());
    }

    @Transactional
    public void deleteProcurement(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireProcurementEntity(projectId, id);
        procurementMapper.softDeleteProcurement(projectId, id, LocalDateTime.now());
    }

    public PageResult<ContractVO> listContracts(Long projectId, ContractQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(procurementMapper.selectContractsByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public ContractVO createContract(Long projectId, CreateContractDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateSupplier(projectId, dto.getSupplierId());
        LocalDateTime now = LocalDateTime.now();
        ContractEntity entity = new ContractEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setSupplierId(dto.getSupplierId());
        entity.setContractNo(dto.getContractNo().trim());
        entity.setContractName(dto.getContractName().trim());
        entity.setContractType(normalizeContractType(dto.getContractType()));
        entity.setSignDate(dto.getSignDate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setDeliverables(normalizeText(dto.getDeliverables()));
        entity.setPaymentTerms(normalizeText(dto.getPaymentTerms()));
        entity.setStatus(normalizeContractStatus(dto.getStatus()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        procurementMapper.insertContract(entity);
        return requireContract(projectId, entity.getId());
    }

    @Transactional
    public ContractVO updateContract(Long projectId, Long id, UpdateContractDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateSupplier(projectId, dto.getSupplierId());
        ContractEntity entity = requireContractEntity(projectId, id);
        entity.setSupplierId(dto.getSupplierId());
        entity.setContractNo(dto.getContractNo().trim());
        entity.setContractName(dto.getContractName().trim());
        entity.setContractType(normalizeContractType(dto.getContractType()));
        entity.setSignDate(dto.getSignDate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setDeliverables(normalizeText(dto.getDeliverables()));
        entity.setPaymentTerms(normalizeText(dto.getPaymentTerms()));
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : normalizeContractStatus(dto.getStatus()));
        entity.setUpdatedAt(LocalDateTime.now());
        procurementMapper.updateContract(entity);
        return requireContract(projectId, id);
    }

    @Transactional
    public void deleteContract(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireContractEntity(projectId, id);
        procurementMapper.softDeleteContract(projectId, id, LocalDateTime.now());
    }

    private void ensureProject(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateSupplier(Long projectId, Long supplierId) {
        if (supplierId != null && procurementMapper.selectSupplierEntityById(projectId, supplierId) == null) {
            throw new IllegalArgumentException("supplier not found");
        }
    }

    private void validateCostPlan(Long projectId, Long costPlanId) {
        if (costPlanId != null && costMapper.selectPlanEntityById(projectId, costPlanId) == null) {
            throw new IllegalArgumentException("cost plan not found");
        }
    }

    private SupplierVO requireSupplier(Long projectId, Long id) {
        SupplierVO vo = procurementMapper.selectSupplierById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("supplier not found");
        }
        return vo;
    }

    private SupplierEntity requireSupplierEntity(Long projectId, Long id) {
        SupplierEntity entity = procurementMapper.selectSupplierEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("supplier not found");
        }
        return entity;
    }

    private ProcurementVO requireProcurement(Long projectId, Long id) {
        ProcurementVO vo = procurementMapper.selectProcurementById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("procurement not found");
        }
        return vo;
    }

    private ProcurementEntity requireProcurementEntity(Long projectId, Long id) {
        ProcurementEntity entity = procurementMapper.selectProcurementEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("procurement not found");
        }
        return entity;
    }

    private ContractVO requireContract(Long projectId, Long id) {
        ContractVO vo = procurementMapper.selectContractById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("contract not found");
        }
        return vo;
    }

    private ContractEntity requireContractEntity(Long projectId, Long id) {
        ContractEntity entity = procurementMapper.selectContractEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("contract not found");
        }
        return entity;
    }

    private String normalizeProcurementStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProcurementStatusEnum.PLANNED.name();
        }
        try {
            return ProcurementStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid procurement status");
        }
    }

    private String normalizeContractType(String contractType) {
        try {
            return ContractTypeEnum.valueOf(contractType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid contract type");
        }
    }

    private String normalizeContractStatus(String status) {
        if (status == null || status.isBlank()) {
            return ContractStatusEnum.DRAFT.name();
        }
        try {
            return ContractStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid contract status");
        }
    }

    private LocalDate normalizeActualDeliveryDate(String status, LocalDate actualDeliveryDate) {
        if (ProcurementStatusEnum.DELIVERED.name().equals(status) || ProcurementStatusEnum.ACCEPTED.name().equals(status)) {
            return actualDeliveryDate == null ? LocalDate.now() : actualDeliveryDate;
        }
        return actualDeliveryDate;
    }

    private BigDecimal calculateTotalAmount(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null) {
            return null;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity.longValue()));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <T> PageResult<T> buildPageResult(List<T> list, Integer page, Integer pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(page == null ? 1 : page);
        pageResult.setPageSize(pageSize == null ? list.size() : pageSize);
        pageResult.setTotal((long) list.size());
        return pageResult;
    }
}
