package cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 产品工序分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductProcessPageReqVO extends PageParam {

    @Schema(description = "供应商表名", example = "李四")
    private String name;

    @Schema(description = "编码")
    private String number;

    @Schema(description = "可报工用户")
    private String reportingUsers;

    @Schema(description = "字工序")
    private String childProcess;

    @Schema(description = "是否报工")
    private Boolean reporting;

    @Schema(description = "是否计件")
    private Boolean piecework;

    @Schema(description = "计件单位", example = "22540")
    private String pieceworkUnitId;

    @Schema(description = "计件单价", example = "15352")
    private Long pieceworkPrice;

    @Schema(description = "是否委外")
    private Boolean outsourcing;

    @Schema(description = "委外供应商", example = "1575")
    private String supplierId;

    @Schema(description = "是否质检")
    private Boolean qualityInspection;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
