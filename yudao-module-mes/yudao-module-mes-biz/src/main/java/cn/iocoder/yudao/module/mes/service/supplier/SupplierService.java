package cn.iocoder.yudao.module.mes.service.supplier;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 供应商信息 Service 接口
 *
 * @author 墨笔
 */
public interface SupplierService {

    /**
     * 创建供应商信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupplier(@Valid SupplierCreateReqVO createReqVO);

    /**
     * 更新供应商信息
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplier(@Valid SupplierUpdateReqVO updateReqVO);

    /**
     * 删除供应商信息
     *
     * @param id 编号
     */
    void deleteSupplier(Long id);

    /**
     * 获得供应商信息
     *
     * @param id 编号
     * @return 供应商信息
     */
    SupplierDO getSupplier(Long id);

    /**
     * 获得供应商信息列表
     *
     * @param ids 编号
     * @return 供应商信息列表
     */
    List<SupplierDO> getSupplierList(Collection<Long> ids);

    /**
     * 获得供应商信息分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商信息分页
     */
    PageResult<SupplierDO> getSupplierPage(SupplierPageReqVO pageReqVO);

    /**
     * 获得供应商信息列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 供应商信息列表
     */
    List<SupplierDO> getSupplierList(SupplierExportReqVO exportReqVO);

}
