package cn.iocoder.yudao.module.mes.convert.supplier;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;

/**
 * 供应商信息 Convert
 *
 * @author 墨笔
 */
@Mapper
public interface SupplierConvert {

    SupplierConvert INSTANCE = Mappers.getMapper(SupplierConvert.class);

    SupplierDO convert(SupplierCreateReqVO bean);

    SupplierDO convert(SupplierUpdateReqVO bean);

    SupplierRespVO convert(SupplierDO bean);

    List<SupplierRespVO> convertList(List<SupplierDO> list);

    PageResult<SupplierRespVO> convertPage(PageResult<SupplierDO> page);

    List<SupplierExcelVO> convertList02(List<SupplierDO> list);

}
