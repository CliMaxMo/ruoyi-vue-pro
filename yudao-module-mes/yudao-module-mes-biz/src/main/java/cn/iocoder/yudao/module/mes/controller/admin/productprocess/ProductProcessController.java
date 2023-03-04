package cn.iocoder.yudao.module.mes.controller.admin.productprocess;

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

import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;
import cn.iocoder.yudao.module.mes.convert.productprocess.ProductProcessConvert;
import cn.iocoder.yudao.module.mes.service.productprocess.ProductProcessService;

@Tag(name = "管理后台 - 产品工序")
@RestController
@RequestMapping("/mes/product-process")
@Validated
public class ProductProcessController {

    @Resource
    private ProductProcessService productProcessService;

    @PostMapping("/create")
    @Operation(summary = "创建产品工序")
    @PreAuthorize("@ss.hasPermission('mes:product-process:create')")
    public CommonResult<Long> createProductProcess(@Valid @RequestBody ProductProcessCreateReqVO createReqVO) {
        return success(productProcessService.createProductProcess(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品工序")
    @PreAuthorize("@ss.hasPermission('mes:product-process:update')")
    public CommonResult<Boolean> updateProductProcess(@Valid @RequestBody ProductProcessUpdateReqVO updateReqVO) {
        productProcessService.updateProductProcess(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品工序")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:product-process:delete')")
    public CommonResult<Boolean> deleteProductProcess(@RequestParam("id") Long id) {
        productProcessService.deleteProductProcess(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品工序")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:product-process:query')")
    public CommonResult<ProductProcessRespVO> getProductProcess(@RequestParam("id") Long id) {
        ProductProcessDO productProcess = productProcessService.getProductProcess(id);
        return success(ProductProcessConvert.INSTANCE.convert(productProcess));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产品工序列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('mes:product-process:query')")
    public CommonResult<List<ProductProcessRespVO>> getProductProcessList(@RequestParam("ids") Collection<Long> ids) {
        List<ProductProcessDO> list = productProcessService.getProductProcessList(ids);
        return success(ProductProcessConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品工序分页")
    @PreAuthorize("@ss.hasPermission('mes:product-process:query')")
    public CommonResult<PageResult<ProductProcessRespVO>> getProductProcessPage(@Valid ProductProcessPageReqVO pageVO) {
        PageResult<ProductProcessDO> pageResult = productProcessService.getProductProcessPage(pageVO);
        return success(ProductProcessConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品工序 Excel")
    @PreAuthorize("@ss.hasPermission('mes:product-process:export')")
    @OperateLog(type = EXPORT)
    public void exportProductProcessExcel(@Valid ProductProcessExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ProductProcessDO> list = productProcessService.getProductProcessList(exportReqVO);
        // 导出 Excel
        List<ProductProcessExcelVO> datas = ProductProcessConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "产品工序.xls", "数据", ProductProcessExcelVO.class, datas);
    }

}
