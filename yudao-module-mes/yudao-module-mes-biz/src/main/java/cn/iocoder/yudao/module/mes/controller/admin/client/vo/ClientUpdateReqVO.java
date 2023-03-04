package cn.iocoder.yudao.module.mes.controller.admin.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 客户信息更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClientUpdateReqVO extends ClientBaseVO {

    @Schema(description = "客户ID", required = true, example = "7675")
    @NotNull(message = "客户ID不能为空")
    private Long id;

}
