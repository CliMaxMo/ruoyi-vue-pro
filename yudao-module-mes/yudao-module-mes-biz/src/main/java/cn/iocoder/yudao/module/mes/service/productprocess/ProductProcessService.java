package cn.iocoder.yudao.module.mes.service.productprocess;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 产品工序 Service 接口
 *
 * @author 墨笔
 */
public interface ProductProcessService {

    /**
     * 创建产品工序
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductProcess(@Valid ProductProcessCreateReqVO createReqVO);

    /**
     * 更新产品工序
     *
     * @param updateReqVO 更新信息
     */
    void updateProductProcess(@Valid ProductProcessUpdateReqVO updateReqVO);

    /**
     * 删除产品工序
     *
     * @param id 编号
     */
    void deleteProductProcess(Long id);

    /**
     * 获得产品工序
     *
     * @param id 编号
     * @return 产品工序
     */
    ProductProcessDO getProductProcess(Long id);

    /**
     * 获得产品工序列表
     *
     * @param ids 编号
     * @return 产品工序列表
     */
    List<ProductProcessDO> getProductProcessList(Collection<Long> ids);

    /**
     * 获得产品工序分页
     *
     * @param pageReqVO 分页查询
     * @return 产品工序分页
     */
    PageResult<ProductProcessDO> getProductProcessPage(ProductProcessPageReqVO pageReqVO);

    /**
     * 获得产品工序列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 产品工序列表
     */
    List<ProductProcessDO> getProductProcessList(ProductProcessExportReqVO exportReqVO);

}
