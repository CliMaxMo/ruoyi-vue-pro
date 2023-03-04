package cn.iocoder.yudao.module.mes.service.supplier;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mes.convert.supplier.SupplierConvert;
import cn.iocoder.yudao.module.mes.dal.mysql.supplier.SupplierMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * 供应商信息 Service 实现类
 *
 * @author 墨笔
 */
@Service
@Validated
public class SupplierServiceImpl implements SupplierService {

    @Resource
    private SupplierMapper supplierMapper;

    @Override
    public Long createSupplier(SupplierCreateReqVO createReqVO) {
        // 插入
        SupplierDO supplier = SupplierConvert.INSTANCE.convert(createReqVO);
        supplierMapper.insert(supplier);
        // 返回
        return supplier.getId();
    }

    @Override
    public void updateSupplier(SupplierUpdateReqVO updateReqVO) {
        // 校验存在
        validateSupplierExists(updateReqVO.getId());
        // 更新
        SupplierDO updateObj = SupplierConvert.INSTANCE.convert(updateReqVO);
        supplierMapper.updateById(updateObj);
    }

    @Override
    public void deleteSupplier(Long id) {
        // 校验存在
        validateSupplierExists(id);
        // 删除
        supplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if (supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public SupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public List<SupplierDO> getSupplierList(Collection<Long> ids) {
        return supplierMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SupplierDO> getSupplierPage(SupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SupplierDO> getSupplierList(SupplierExportReqVO exportReqVO) {
        return supplierMapper.selectList(exportReqVO);
    }

}
