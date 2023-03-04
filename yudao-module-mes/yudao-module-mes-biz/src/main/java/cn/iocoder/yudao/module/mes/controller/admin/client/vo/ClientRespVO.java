package cn.iocoder.yudao.module.mes.controller.admin.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClientRespVO extends ClientBaseVO {

    @Schema(description = "客户ID", required = true, example = "7675")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
