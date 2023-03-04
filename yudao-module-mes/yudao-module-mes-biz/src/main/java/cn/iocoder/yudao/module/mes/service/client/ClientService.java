package cn.iocoder.yudao.module.mes.service.client;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.mes.controller.admin.client.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.client.ClientDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 客户信息 Service 接口
 *
 * @author 芋道源码
 */
public interface ClientService {

    /**
     * 创建客户信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createClient(@Valid ClientCreateReqVO createReqVO);

    /**
     * 更新客户信息
     *
     * @param updateReqVO 更新信息
     */
    void updateClient(@Valid ClientUpdateReqVO updateReqVO);

    /**
     * 删除客户信息
     *
     * @param id 编号
     */
    void deleteClient(Long id);

    /**
     * 获得客户信息
     *
     * @param id 编号
     * @return 客户信息
     */
    ClientDO getClient(Long id);

    /**
     * 获得客户信息列表
     *
     * @param ids 编号
     * @return 客户信息列表
     */
    List<ClientDO> getClientList(Collection<Long> ids);

    /**
     * 获得客户信息分页
     *
     * @param pageReqVO 分页查询
     * @return 客户信息分页
     */
    PageResult<ClientDO> getClientPage(ClientPageReqVO pageReqVO);

    /**
     * 获得客户信息列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 客户信息列表
     */
    List<ClientDO> getClientList(ClientExportReqVO exportReqVO);

}
