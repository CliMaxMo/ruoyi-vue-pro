package cn.iocoder.yudao.module.mes.dal.dataobject.client;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 客户信息 DO
 *
 * @author 芋道源码
 */
@TableName("mes_client")
@KeySequence("mes_client_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDO extends BaseDO {

    /**
     * 客户ID
     */
    @TableId
    private Long id;
    /**
     * 客户代表名
     */
    private String name;
    /**
     * 公司名
     */
    private String companyName;
    /**
     * 地址
     */
    private String address;
    /**
     * 备注
     */
    private String remark;
    /**
     * 手机号码
     */
    private String phone;
    /**
     * 电子邮件
     */
    private String email;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 帐号状态（0正常 1停用）
     */
    private Byte status;

}
