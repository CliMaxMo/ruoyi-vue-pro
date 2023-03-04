package cn.iocoder.yudao.module.mes.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 供应商信息更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplierUpdateReqVO extends SupplierBaseVO {

    @Schema(description = "供应商ID", required = true, example = "1818")
    @NotNull(message = "供应商ID不能为空")
    private Long id;

}
