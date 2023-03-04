package cn.iocoder.yudao.module.mes.service.productprocess;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.mes.controller.admin.productprocess.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.productprocess.ProductProcessDO;
import cn.iocoder.yudao.module.mes.dal.mysql.productprocess.ProductProcessMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* {@link ProductProcessServiceImpl} 的单元测试类
*
* @author 墨笔
*/
@Import(ProductProcessServiceImpl.class)
public class ProductProcessServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductProcessServiceImpl productProcessService;

    @Resource
    private ProductProcessMapper productProcessMapper;

    @Test
    public void testCreateProductProcess_success() {
        // 准备参数
        ProductProcessCreateReqVO reqVO = randomPojo(ProductProcessCreateReqVO.class);

        // 调用
        Long productProcessId = productProcessService.createProductProcess(reqVO);
        // 断言
        assertNotNull(productProcessId);
        // 校验记录的属性是否正确
        ProductProcessDO productProcess = productProcessMapper.selectById(productProcessId);
        assertPojoEquals(reqVO, productProcess);
    }

    @Test
    public void testUpdateProductProcess_success() {
        // mock 数据
        ProductProcessDO dbProductProcess = randomPojo(ProductProcessDO.class);
        productProcessMapper.insert(dbProductProcess);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProductProcessUpdateReqVO reqVO = randomPojo(ProductProcessUpdateReqVO.class, o -> {
            o.setId(dbProductProcess.getId()); // 设置更新的 ID
        });

        // 调用
        productProcessService.updateProductProcess(reqVO);
        // 校验是否更新正确
        ProductProcessDO productProcess = productProcessMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, productProcess);
    }

    @Test
    public void testUpdateProductProcess_notExists() {
        // 准备参数
        ProductProcessUpdateReqVO reqVO = randomPojo(ProductProcessUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> productProcessService.updateProductProcess(reqVO), PRODUCT_PROCESS_NOT_EXISTS);
    }

    @Test
    public void testDeleteProductProcess_success() {
        // mock 数据
        ProductProcessDO dbProductProcess = randomPojo(ProductProcessDO.class);
        productProcessMapper.insert(dbProductProcess);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProductProcess.getId();

        // 调用
        productProcessService.deleteProductProcess(id);
       // 校验数据不存在了
       assertNull(productProcessMapper.selectById(id));
    }

    @Test
    public void testDeleteProductProcess_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> productProcessService.deleteProductProcess(id), PRODUCT_PROCESS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProductProcessPage() {
       // mock 数据
       ProductProcessDO dbProductProcess = randomPojo(ProductProcessDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setNumber(null);
           o.setReportingUsers(null);
           o.setChildProcess(null);
           o.setReporting(null);
           o.setPiecework(null);
           o.setPieceworkUnitId(null);
           o.setPieceworkPrice(null);
           o.setOutsourcing(null);
           o.setSupplierId(null);
           o.setQualityInspection(null);
           o.setCreateTime(null);
       });
       productProcessMapper.insert(dbProductProcess);
       // 测试 name 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setName(null)));
       // 测试 number 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setNumber(null)));
       // 测试 reportingUsers 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setReportingUsers(null)));
       // 测试 childProcess 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setChildProcess(null)));
       // 测试 reporting 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setReporting(null)));
       // 测试 piecework 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPiecework(null)));
       // 测试 pieceworkUnitId 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPieceworkUnitId(null)));
       // 测试 pieceworkPrice 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPieceworkPrice(null)));
       // 测试 outsourcing 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setOutsourcing(null)));
       // 测试 supplierId 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setSupplierId(null)));
       // 测试 qualityInspection 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setQualityInspection(null)));
       // 测试 createTime 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setCreateTime(null)));
       // 准备参数
       ProductProcessPageReqVO reqVO = new ProductProcessPageReqVO();
       reqVO.setName(null);
       reqVO.setNumber(null);
       reqVO.setReportingUsers(null);
       reqVO.setChildProcess(null);
       reqVO.setReporting(null);
       reqVO.setPiecework(null);
       reqVO.setPieceworkUnitId(null);
       reqVO.setPieceworkPrice(null);
       reqVO.setOutsourcing(null);
       reqVO.setSupplierId(null);
       reqVO.setQualityInspection(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProductProcessDO> pageResult = productProcessService.getProductProcessPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProductProcess, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProductProcessList() {
       // mock 数据
       ProductProcessDO dbProductProcess = randomPojo(ProductProcessDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setNumber(null);
           o.setReportingUsers(null);
           o.setChildProcess(null);
           o.setReporting(null);
           o.setPiecework(null);
           o.setPieceworkUnitId(null);
           o.setPieceworkPrice(null);
           o.setOutsourcing(null);
           o.setSupplierId(null);
           o.setQualityInspection(null);
           o.setCreateTime(null);
       });
       productProcessMapper.insert(dbProductProcess);
       // 测试 name 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setName(null)));
       // 测试 number 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setNumber(null)));
       // 测试 reportingUsers 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setReportingUsers(null)));
       // 测试 childProcess 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setChildProcess(null)));
       // 测试 reporting 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setReporting(null)));
       // 测试 piecework 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPiecework(null)));
       // 测试 pieceworkUnitId 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPieceworkUnitId(null)));
       // 测试 pieceworkPrice 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setPieceworkPrice(null)));
       // 测试 outsourcing 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setOutsourcing(null)));
       // 测试 supplierId 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setSupplierId(null)));
       // 测试 qualityInspection 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setQualityInspection(null)));
       // 测试 createTime 不匹配
       productProcessMapper.insert(cloneIgnoreId(dbProductProcess, o -> o.setCreateTime(null)));
       // 准备参数
       ProductProcessExportReqVO reqVO = new ProductProcessExportReqVO();
       reqVO.setName(null);
       reqVO.setNumber(null);
       reqVO.setReportingUsers(null);
       reqVO.setChildProcess(null);
       reqVO.setReporting(null);
       reqVO.setPiecework(null);
       reqVO.setPieceworkUnitId(null);
       reqVO.setPieceworkPrice(null);
       reqVO.setOutsourcing(null);
       reqVO.setSupplierId(null);
       reqVO.setQualityInspection(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<ProductProcessDO> list = productProcessService.getProductProcessList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbProductProcess, list.get(0));
    }

}
