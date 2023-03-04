package cn.iocoder.yudao.module.mes.controller.admin.client;

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

import cn.iocoder.yudao.module.mes.controller.admin.client.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.client.ClientDO;
import cn.iocoder.yudao.module.mes.convert.client.ClientConvert;
import cn.iocoder.yudao.module.mes.service.client.ClientService;

@Tag(name = "管理后台 - 客户信息")
@RestController
@RequestMapping("/mes/client")
@Validated
public class ClientController {

    @Resource
    private ClientService clientService;

    @PostMapping("/create")
    @Operation(summary = "创建客户信息")
    @PreAuthorize("@ss.hasPermission('mes:client:create')")
    public CommonResult<Long> createClient(@Valid @RequestBody ClientCreateReqVO createReqVO) {
        return success(clientService.createClient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户信息")
    @PreAuthorize("@ss.hasPermission('mes:client:update')")
    public CommonResult<Boolean> updateClient(@Valid @RequestBody ClientUpdateReqVO updateReqVO) {
        clientService.updateClient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:client:delete')")
    public CommonResult<Boolean> deleteClient(@RequestParam("id") Long id) {
        clientService.deleteClient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:client:query')")
    public CommonResult<ClientRespVO> getClient(@RequestParam("id") Long id) {
        ClientDO client = clientService.getClient(id);
        return success(ClientConvert.INSTANCE.convert(client));
    }

    @GetMapping("/list")
    @Operation(summary = "获得客户信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('mes:client:query')")
    public CommonResult<List<ClientRespVO>> getClientList(@RequestParam("ids") Collection<Long> ids) {
        List<ClientDO> list = clientService.getClientList(ids);
        return success(ClientConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户信息分页")
    @PreAuthorize("@ss.hasPermission('mes:client:query')")
    public CommonResult<PageResult<ClientRespVO>> getClientPage(@Valid ClientPageReqVO pageVO) {
        PageResult<ClientDO> pageResult = clientService.getClientPage(pageVO);
        return success(ClientConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户信息 Excel")
    @PreAuthorize("@ss.hasPermission('mes:client:export')")
    @OperateLog(type = EXPORT)
    public void exportClientExcel(@Valid ClientExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ClientDO> list = clientService.getClientList(exportReqVO);
        // 导出 Excel
        List<ClientExcelVO> datas = ClientConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "客户信息.xls", "数据", ClientExcelVO.class, datas);
    }

}
