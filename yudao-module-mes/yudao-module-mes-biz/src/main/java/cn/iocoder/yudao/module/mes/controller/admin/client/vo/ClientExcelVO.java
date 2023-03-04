package cn.iocoder.yudao.module.mes.controller.admin.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 客户信息 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ClientExcelVO {

    @ExcelProperty("客户ID")
    private Long id;

    @ExcelProperty("客户代表名")
    private String name;

    @ExcelProperty("公司名")
    private String companyName;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("手机号码")
    private String phone;

    @ExcelProperty("电子邮件")
    private String email;

    @ExcelProperty("手机号码")
    private String mobile;

    @ExcelProperty("帐号状态（0正常 1停用）")
    private Byte status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
