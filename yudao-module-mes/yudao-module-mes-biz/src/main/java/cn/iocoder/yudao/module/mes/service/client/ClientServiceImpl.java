package cn.iocoder.yudao.module.mes.service.client;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.mes.controller.admin.client.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.client.ClientDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mes.convert.client.ClientConvert;
import cn.iocoder.yudao.module.mes.dal.mysql.client.ClientMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * 客户信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ClientServiceImpl implements ClientService {

    @Resource
    private ClientMapper clientMapper;

    @Override
    public Long createClient(ClientCreateReqVO createReqVO) {
        // 插入
        ClientDO client = ClientConvert.INSTANCE.convert(createReqVO);
        clientMapper.insert(client);
        // 返回
        return client.getId();
    }

    @Override
    public void updateClient(ClientUpdateReqVO updateReqVO) {
        // 校验存在
        validateClientExists(updateReqVO.getId());
        // 更新
        ClientDO updateObj = ClientConvert.INSTANCE.convert(updateReqVO);
        clientMapper.updateById(updateObj);
    }

    @Override
    public void deleteClient(Long id) {
        // 校验存在
        validateClientExists(id);
        // 删除
        clientMapper.deleteById(id);
    }

    private void validateClientExists(Long id) {
        if (clientMapper.selectById(id) == null) {
            throw exception(CLIENT_NOT_EXISTS);
        }
    }

    @Override
    public ClientDO getClient(Long id) {
        return clientMapper.selectById(id);
    }

    @Override
    public List<ClientDO> getClientList(Collection<Long> ids) {
        return clientMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ClientDO> getClientPage(ClientPageReqVO pageReqVO) {
        return clientMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ClientDO> getClientList(ClientExportReqVO exportReqVO) {
        return clientMapper.selectList(exportReqVO);
    }

}
