package cn.iocoder.yudao.module.mes.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
* 供应商信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SupplierBaseVO {

    @Schema(description = "供应商表名", required = true, example = "李四")
    @NotNull(message = "供应商表名不能为空")
    private String name;

    @Schema(description = "公司名", required = true, example = "赵六")
    @NotNull(message = "公司名不能为空")
    private String companyName;

    @Schema(description = "地址", required = true)
    @NotNull(message = "地址不能为空")
    private String address;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "电子邮件")
    private String email;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "帐号状态（0正常 1停用）", required = true, example = "1")
    @NotNull(message = "帐号状态（0正常 1停用）不能为空")
    private Byte status;

}
