package cn.iocoder.yudao.module.mes.convert.client;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mes.controller.admin.client.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.client.ClientDO;

/**
 * 客户信息 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ClientConvert {

    ClientConvert INSTANCE = Mappers.getMapper(ClientConvert.class);

    ClientDO convert(ClientCreateReqVO bean);

    ClientDO convert(ClientUpdateReqVO bean);

    ClientRespVO convert(ClientDO bean);

    List<ClientRespVO> convertList(List<ClientDO> list);

    PageResult<ClientRespVO> convertPage(PageResult<ClientDO> page);

    List<ClientExcelVO> convertList02(List<ClientDO> list);

}
