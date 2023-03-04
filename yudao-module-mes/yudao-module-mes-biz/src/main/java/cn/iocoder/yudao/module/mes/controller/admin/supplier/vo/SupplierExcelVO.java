package cn.iocoder.yudao.module.mes.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 供应商信息 Excel VO
 *
 * @author 墨笔
 */
@Data
public class SupplierExcelVO {

    @ExcelProperty("供应商ID")
    private Long id;

    @ExcelProperty("供应商表名")
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
