package cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 产品工序 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductProcessRespVO extends ProductProcessBaseVO {

    @Schema(description = "供应商ID", required = true, example = "9378")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
