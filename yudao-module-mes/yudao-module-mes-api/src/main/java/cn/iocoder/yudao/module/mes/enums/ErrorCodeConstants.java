package cn.iocoder.yudao.module.mes.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * member 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode CLIENT_NOT_EXISTS = new ErrorCode(1005001000, "客户信息不存在");
    ErrorCode PRODUCT_PROCESS_NOT_EXISTS = new ErrorCode(1005002000, "产品工序不存在");
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1005003000, "供应商信息不存在");

}
