package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
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
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProcurementService;
import com.manage.managesystem.vo.ContractVO;
import com.manage.managesystem.vo.ProcurementVO;
import com.manage.managesystem.vo.SupplierVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}")
public class ProcurementController {
    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @GetMapping("/suppliers")
    public ApiResponse<PageResult<SupplierVO>> listSuppliers(@PathVariable Long projectId, SupplierQueryDto queryDto) {
        return ApiResponse.success(procurementService.listSuppliers(projectId, queryDto));
    }

    @PostMapping("/suppliers")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<SupplierVO> createSupplier(@PathVariable Long projectId, @Valid @RequestBody CreateSupplierDto dto) {
        return ApiResponse.success(procurementService.createSupplier(projectId, dto));
    }

    @PutMapping("/suppliers/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<SupplierVO> updateSupplier(@PathVariable Long projectId,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdateSupplierDto dto) {
        return ApiResponse.success(procurementService.updateSupplier(projectId, id, dto));
    }

    @DeleteMapping("/suppliers/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteSupplier(@PathVariable Long projectId, @PathVariable Long id) {
        procurementService.deleteSupplier(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/procurements")
    public ApiResponse<PageResult<ProcurementVO>> listProcurements(@PathVariable Long projectId, ProcurementQueryDto queryDto) {
        return ApiResponse.success(procurementService.listProcurements(projectId, queryDto));
    }

    @PostMapping("/procurements")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProcurementVO> createProcurement(@PathVariable Long projectId, @Valid @RequestBody CreateProcurementDto dto) {
        return ApiResponse.success(procurementService.createProcurement(projectId, dto));
    }

    @PutMapping("/procurements/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProcurementVO> updateProcurement(@PathVariable Long projectId,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody UpdateProcurementDto dto) {
        return ApiResponse.success(procurementService.updateProcurement(projectId, id, dto));
    }

    @PatchMapping("/procurements/{id}/status")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProcurementVO> updateProcurementStatus(@PathVariable Long projectId,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody UpdateProcurementStatusDto dto) {
        return ApiResponse.success(procurementService.updateProcurementStatus(projectId, id, dto));
    }

    @DeleteMapping("/procurements/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteProcurement(@PathVariable Long projectId, @PathVariable Long id) {
        procurementService.deleteProcurement(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/contracts")
    public ApiResponse<PageResult<ContractVO>> listContracts(@PathVariable Long projectId, ContractQueryDto queryDto) {
        return ApiResponse.success(procurementService.listContracts(projectId, queryDto));
    }

    @PostMapping("/contracts")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ContractVO> createContract(@PathVariable Long projectId, @Valid @RequestBody CreateContractDto dto) {
        return ApiResponse.success(procurementService.createContract(projectId, dto));
    }

    @PutMapping("/contracts/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ContractVO> updateContract(@PathVariable Long projectId,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdateContractDto dto) {
        return ApiResponse.success(procurementService.updateContract(projectId, id, dto));
    }

    @DeleteMapping("/contracts/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteContract(@PathVariable Long projectId, @PathVariable Long id) {
        procurementService.deleteContract(projectId, id);
        return ApiResponse.success(null);
    }
}
