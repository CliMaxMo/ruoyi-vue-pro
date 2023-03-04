package cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 产品工序 Excel VO
 *
 * @author 墨笔
 */
@Data
public class ProductProcessExcelVO {

    @ExcelProperty("供应商ID")
    private Long id;

    @ExcelProperty("供应商表名")
    private String name;

    @ExcelProperty("编码")
    private String number;

    @ExcelProperty("可报工用户")
    private String reportingUsers;

    @ExcelProperty("字工序")
    private String childProcess;

    @ExcelProperty("是否报工")
    private Boolean reporting;

    @ExcelProperty("是否计件")
    private Boolean piecework;

    @ExcelProperty("计件单位")
    private String pieceworkUnitId;

    @ExcelProperty("计件单价")
    private Long pieceworkPrice;

    @ExcelProperty("是否委外")
    private Boolean outsourcing;

    @ExcelProperty("委外供应商")
    private String supplierId;

    @ExcelProperty("是否质检")
    private Boolean qualityInspection;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
