package cn.iocoder.yudao.module.mes.service.productprocess;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mes.convert.productprocess.ProductProcessConvert;
import cn.iocoder.yudao.module.mes.dal.mysql.productprocess.ProductProcessMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * 产品工序 Service 实现类
 *
 * @author 墨笔
 */
@Service
@Validated
public class ProductProcessServiceImpl implements ProductProcessService {

    @Resource
    private ProductProcessMapper productProcessMapper;

    @Override
    public Long createProductProcess(ProductProcessCreateReqVO createReqVO) {
        // 插入
        ProductProcessDO productProcess = ProductProcessConvert.INSTANCE.convert(createReqVO);
        productProcessMapper.insert(productProcess);
        // 返回
        return productProcess.getId();
    }

    @Override
    public void updateProductProcess(ProductProcessUpdateReqVO updateReqVO) {
        // 校验存在
        validateProductProcessExists(updateReqVO.getId());
        // 更新
        ProductProcessDO updateObj = ProductProcessConvert.INSTANCE.convert(updateReqVO);
        productProcessMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductProcess(Long id) {
        // 校验存在
        validateProductProcessExists(id);
        // 删除
        productProcessMapper.deleteById(id);
    }

    private void validateProductProcessExists(Long id) {
        if (productProcessMapper.selectById(id) == null) {
            throw exception(PRODUCT_PROCESS_NOT_EXISTS);
        }
    }

    @Override
    public ProductProcessDO getProductProcess(Long id) {
        return productProcessMapper.selectById(id);
    }

    @Override
    public List<ProductProcessDO> getProductProcessList(Collection<Long> ids) {
        return productProcessMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductProcessDO> getProductProcessPage(ProductProcessPageReqVO pageReqVO) {
        return productProcessMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductProcessDO> getProductProcessList(ProductProcessExportReqVO exportReqVO) {
        return productProcessMapper.selectList(exportReqVO);
    }

}
