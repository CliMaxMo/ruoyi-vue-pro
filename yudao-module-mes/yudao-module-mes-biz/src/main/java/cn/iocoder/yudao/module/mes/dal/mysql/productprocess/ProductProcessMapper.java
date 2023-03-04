package cn.iocoder.yudao.module.mes.dal.mysql.productprocess;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;

/**
 * 产品工序 Mapper
 *
 * @author 墨笔
 */
@Mapper
public interface ProductProcessMapper extends BaseMapperX<ProductProcessDO> {

    default PageResult<ProductProcessDO> selectPage(ProductProcessPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductProcessDO>()
                .likeIfPresent(ProductProcessDO::getName, reqVO.getName())
                .eqIfPresent(ProductProcessDO::getNumber, reqVO.getNumber())
                .eqIfPresent(ProductProcessDO::getReportingUsers, reqVO.getReportingUsers())
                .eqIfPresent(ProductProcessDO::getChildProcess, reqVO.getChildProcess())
                .eqIfPresent(ProductProcessDO::getReporting, reqVO.getReporting())
                .eqIfPresent(ProductProcessDO::getPiecework, reqVO.getPiecework())
                .eqIfPresent(ProductProcessDO::getPieceworkUnitId, reqVO.getPieceworkUnitId())
                .eqIfPresent(ProductProcessDO::getPieceworkPrice, reqVO.getPieceworkPrice())
                .eqIfPresent(ProductProcessDO::getOutsourcing, reqVO.getOutsourcing())
                .eqIfPresent(ProductProcessDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ProductProcessDO::getQualityInspection, reqVO.getQualityInspection())
                .betweenIfPresent(ProductProcessDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductProcessDO::getId));
    }

    default List<ProductProcessDO> selectList(ProductProcessExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductProcessDO>()
                .likeIfPresent(ProductProcessDO::getName, reqVO.getName())
                .eqIfPresent(ProductProcessDO::getNumber, reqVO.getNumber())
                .eqIfPresent(ProductProcessDO::getReportingUsers, reqVO.getReportingUsers())
                .eqIfPresent(ProductProcessDO::getChildProcess, reqVO.getChildProcess())
                .eqIfPresent(ProductProcessDO::getReporting, reqVO.getReporting())
                .eqIfPresent(ProductProcessDO::getPiecework, reqVO.getPiecework())
                .eqIfPresent(ProductProcessDO::getPieceworkUnitId, reqVO.getPieceworkUnitId())
                .eqIfPresent(ProductProcessDO::getPieceworkPrice, reqVO.getPieceworkPrice())
                .eqIfPresent(ProductProcessDO::getOutsourcing, reqVO.getOutsourcing())
                .eqIfPresent(ProductProcessDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ProductProcessDO::getQualityInspection, reqVO.getQualityInspection())
                .betweenIfPresent(ProductProcessDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductProcessDO::getId));
    }

}
