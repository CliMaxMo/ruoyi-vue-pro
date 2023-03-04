package cn.iocoder.yudao.module.mes.convert.productprocess;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;

/**
 * 产品工序 Convert
 *
 * @author 墨笔
 */
@Mapper
public interface ProductProcessConvert {

    ProductProcessConvert INSTANCE = Mappers.getMapper(ProductProcessConvert.class);

    ProductProcessDO convert(ProductProcessCreateReqVO bean);

    ProductProcessDO convert(ProductProcessUpdateReqVO bean);

    ProductProcessRespVO convert(ProductProcessDO bean);

    List<ProductProcessRespVO> convertList(List<ProductProcessDO> list);

    PageResult<ProductProcessRespVO> convertPage(PageResult<ProductProcessDO> page);

    List<ProductProcessExcelVO> convertList02(List<ProductProcessDO> list);

}
