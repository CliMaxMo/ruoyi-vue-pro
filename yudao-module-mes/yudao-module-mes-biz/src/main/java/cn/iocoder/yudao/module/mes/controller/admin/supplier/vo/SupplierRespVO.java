package cn.iocoder.yudao.module.mes.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 供应商信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplierRespVO extends SupplierBaseVO {

    @Schema(description = "供应商ID", required = true, example = "1818")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
