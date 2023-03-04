package cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
* 产品工序 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductProcessBaseVO {

    @Schema(description = "供应商表名", required = true, example = "李四")
    @NotNull(message = "供应商表名不能为空")
    private String name;

    @Schema(description = "编码", required = true)
    @NotNull(message = "编码不能为空")
    private String number;

    @Schema(description = "可报工用户", required = true)
    @NotNull(message = "可报工用户不能为空")
    private String reportingUsers;

    @Schema(description = "字工序")
    private String childProcess;

    @Schema(description = "是否报工", required = true)
    @NotNull(message = "是否报工不能为空")
    private Boolean reporting;

    @Schema(description = "是否计件", required = true)
    @NotNull(message = "是否计件不能为空")
    private Boolean piecework;

    @Schema(description = "计件单位", example = "22540")
    private String pieceworkUnitId;

    @Schema(description = "计件单价", required = true, example = "15352")
    @NotNull(message = "计件单价不能为空")
    private Long pieceworkPrice;

    @Schema(description = "是否委外")
    private Boolean outsourcing;

    @Schema(description = "委外供应商", required = true, example = "1575")
    @NotNull(message = "委外供应商不能为空")
    private String supplierId;

    @Schema(description = "是否质检")
    private Boolean qualityInspection;

}
