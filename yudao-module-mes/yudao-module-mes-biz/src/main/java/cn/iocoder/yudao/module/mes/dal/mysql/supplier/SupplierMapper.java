package cn.iocoder.yudao.module.mes.dal.mysql.supplier;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;

/**
 * 供应商信息 Mapper
 *
 * @author 墨笔
 */
@Mapper
public interface SupplierMapper extends BaseMapperX<SupplierDO> {

    default PageResult<SupplierDO> selectPage(SupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SupplierDO>()
                .likeIfPresent(SupplierDO::getName, reqVO.getName())
                .likeIfPresent(SupplierDO::getCompanyName, reqVO.getCompanyName())
                .eqIfPresent(SupplierDO::getAddress, reqVO.getAddress())
                .eqIfPresent(SupplierDO::getRemark, reqVO.getRemark())
                .eqIfPresent(SupplierDO::getPhone, reqVO.getPhone())
                .eqIfPresent(SupplierDO::getEmail, reqVO.getEmail())
                .eqIfPresent(SupplierDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SupplierDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SupplierDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SupplierDO::getId));
    }

    default List<SupplierDO> selectList(SupplierExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SupplierDO>()
                .likeIfPresent(SupplierDO::getName, reqVO.getName())
                .likeIfPresent(SupplierDO::getCompanyName, reqVO.getCompanyName())
                .eqIfPresent(SupplierDO::getAddress, reqVO.getAddress())
                .eqIfPresent(SupplierDO::getRemark, reqVO.getRemark())
                .eqIfPresent(SupplierDO::getPhone, reqVO.getPhone())
                .eqIfPresent(SupplierDO::getEmail, reqVO.getEmail())
                .eqIfPresent(SupplierDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SupplierDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SupplierDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SupplierDO::getId));
    }

}
