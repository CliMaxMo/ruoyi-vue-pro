package cn.iocoder.yudao.module.mes.dal.dataobject.productprocess;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品工序 DO
 *
 * @author 墨笔
 */
@TableName("mes_product_process")
@KeySequence("mes_product_process_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductProcessDO extends BaseDO {

    /**
     * 供应商ID
     */
    @TableId
    private Long id;
    /**
     * 供应商表名
     */
    private String name;
    /**
     * 编码
     */
    private String number;
    /**
     * 可报工用户
     */
    private String reportingUsers;
    /**
     * 字工序
     */
    private String childProcess;
    /**
     * 是否报工
     */
    private Boolean reporting;
    /**
     * 是否计件
     */
    private Boolean piecework;
    /**
     * 计件单位
     */
    private String pieceworkUnitId;
    /**
     * 计件单价
     */
    private Long pieceworkPrice;
    /**
     * 是否委外
     */
    private Boolean outsourcing;
    /**
     * 委外供应商
     */
    private String supplierId;
    /**
     * 是否质检
     */
    private Boolean qualityInspection;

}
