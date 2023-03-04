package cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 产品工序更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductProcessUpdateReqVO extends ProductProcessBaseVO {

    @Schema(description = "供应商ID", required = true, example = "9378")
    @NotNull(message = "供应商ID不能为空")
    private Long id;

}
