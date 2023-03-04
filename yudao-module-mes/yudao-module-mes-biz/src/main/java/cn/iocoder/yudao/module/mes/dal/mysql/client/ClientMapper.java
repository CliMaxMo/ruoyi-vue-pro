package cn.iocoder.yudao.module.mes.dal.mysql.client;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.client.ClientDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mes.controller.admin.client.vo.*;

/**
 * 客户信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ClientMapper extends BaseMapperX<ClientDO> {

    default PageResult<ClientDO> selectPage(ClientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ClientDO>()
                .likeIfPresent(ClientDO::getName, reqVO.getName())
                .likeIfPresent(ClientDO::getCompanyName, reqVO.getCompanyName())
                .likeIfPresent(ClientDO::getPhone, reqVO.getPhone())
                .eqIfPresent(ClientDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ClientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ClientDO::getId));
    }

    default List<ClientDO> selectList(ClientExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ClientDO>()
                .likeIfPresent(ClientDO::getName, reqVO.getName())
                .likeIfPresent(ClientDO::getCompanyName, reqVO.getCompanyName())
                .likeIfPresent(ClientDO::getPhone, reqVO.getPhone())
                .eqIfPresent(ClientDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ClientDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ClientDO::getId));
    }

}
