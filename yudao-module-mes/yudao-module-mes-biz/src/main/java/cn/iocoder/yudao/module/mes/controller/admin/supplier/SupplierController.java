package cn.iocoder.yudao.module.mes.controller.admin.supplier;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;
import cn.iocoder.yudao.module.mes.convert.supplier.SupplierConvert;
import cn.iocoder.yudao.module.mes.service.supplier.SupplierService;

@Tag(name = "管理后台 - 供应商信息")
@RestController
@RequestMapping("/mes/supplier")
@Validated
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商信息")
    @PreAuthorize("@ss.hasPermission('mes:supplier:create')")
    public CommonResult<Long> createSupplier(@Valid @RequestBody SupplierCreateReqVO createReqVO) {
        return success(supplierService.createSupplier(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应商信息")
    @PreAuthorize("@ss.hasPermission('mes:supplier:update')")
    public CommonResult<Boolean> updateSupplier(@Valid @RequestBody SupplierUpdateReqVO updateReqVO) {
        supplierService.updateSupplier(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:supplier:delete')")
    public CommonResult<Boolean> deleteSupplier(@RequestParam("id") Long id) {
        supplierService.deleteSupplier(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:supplier:query')")
    public CommonResult<SupplierRespVO> getSupplier(@RequestParam("id") Long id) {
        SupplierDO supplier = supplierService.getSupplier(id);
        return success(SupplierConvert.INSTANCE.convert(supplier));
    }

    @GetMapping("/list")
    @Operation(summary = "获得供应商信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('mes:supplier:query')")
    public CommonResult<List<SupplierRespVO>> getSupplierList(@RequestParam("ids") Collection<Long> ids) {
        List<SupplierDO> list = supplierService.getSupplierList(ids);
        return success(SupplierConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商信息分页")
    @PreAuthorize("@ss.hasPermission('mes:supplier:query')")
    public CommonResult<PageResult<SupplierRespVO>> getSupplierPage(@Valid SupplierPageReqVO pageVO) {
        PageResult<SupplierDO> pageResult = supplierService.getSupplierPage(pageVO);
        return success(SupplierConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应商信息 Excel")
    @PreAuthorize("@ss.hasPermission('mes:supplier:export')")
    @OperateLog(type = EXPORT)
    public void exportSupplierExcel(@Valid SupplierExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SupplierDO> list = supplierService.getSupplierList(exportReqVO);
        // 导出 Excel
        List<SupplierExcelVO> datas = SupplierConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "供应商信息.xls", "数据", SupplierExcelVO.class, datas);
    }

}
