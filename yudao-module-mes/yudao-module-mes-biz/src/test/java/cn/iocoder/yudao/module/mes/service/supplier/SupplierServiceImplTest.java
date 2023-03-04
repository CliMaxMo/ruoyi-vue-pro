package cn.iocoder.yudao.module.mes.service.supplier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.mes.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.supplier.SupplierDO;
import cn.iocoder.yudao.module.mes.dal.mysql.supplier.SupplierMapper;
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
* {@link SupplierServiceImpl} 的单元测试类
*
* @author 墨笔
*/
@Import(SupplierServiceImpl.class)
public class SupplierServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SupplierServiceImpl supplierService;

    @Resource
    private SupplierMapper supplierMapper;

    @Test
    public void testCreateSupplier_success() {
        // 准备参数
        SupplierCreateReqVO reqVO = randomPojo(SupplierCreateReqVO.class);

        // 调用
        Long supplierId = supplierService.createSupplier(reqVO);
        // 断言
        assertNotNull(supplierId);
        // 校验记录的属性是否正确
        SupplierDO supplier = supplierMapper.selectById(supplierId);
        assertPojoEquals(reqVO, supplier);
    }

    @Test
    public void testUpdateSupplier_success() {
        // mock 数据
        SupplierDO dbSupplier = randomPojo(SupplierDO.class);
        supplierMapper.insert(dbSupplier);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SupplierUpdateReqVO reqVO = randomPojo(SupplierUpdateReqVO.class, o -> {
            o.setId(dbSupplier.getId()); // 设置更新的 ID
        });

        // 调用
        supplierService.updateSupplier(reqVO);
        // 校验是否更新正确
        SupplierDO supplier = supplierMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, supplier);
    }

    @Test
    public void testUpdateSupplier_notExists() {
        // 准备参数
        SupplierUpdateReqVO reqVO = randomPojo(SupplierUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> supplierService.updateSupplier(reqVO), SUPPLIER_NOT_EXISTS);
    }

    @Test
    public void testDeleteSupplier_success() {
        // mock 数据
        SupplierDO dbSupplier = randomPojo(SupplierDO.class);
        supplierMapper.insert(dbSupplier);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSupplier.getId();

        // 调用
        supplierService.deleteSupplier(id);
       // 校验数据不存在了
       assertNull(supplierMapper.selectById(id));
    }

    @Test
    public void testDeleteSupplier_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> supplierService.deleteSupplier(id), SUPPLIER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSupplierPage() {
       // mock 数据
       SupplierDO dbSupplier = randomPojo(SupplierDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setCompanyName(null);
           o.setAddress(null);
           o.setRemark(null);
           o.setPhone(null);
           o.setEmail(null);
           o.setMobile(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       supplierMapper.insert(dbSupplier);
       // 测试 name 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setName(null)));
       // 测试 companyName 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setCompanyName(null)));
       // 测试 address 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setAddress(null)));
       // 测试 remark 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setRemark(null)));
       // 测试 phone 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setPhone(null)));
       // 测试 email 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setEmail(null)));
       // 测试 mobile 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setMobile(null)));
       // 测试 status 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setCreateTime(null)));
       // 准备参数
       SupplierPageReqVO reqVO = new SupplierPageReqVO();
       reqVO.setName(null);
       reqVO.setCompanyName(null);
       reqVO.setAddress(null);
       reqVO.setRemark(null);
       reqVO.setPhone(null);
       reqVO.setEmail(null);
       reqVO.setMobile(null);
       reqVO.setStatus(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SupplierDO> pageResult = supplierService.getSupplierPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSupplier, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSupplierList() {
       // mock 数据
       SupplierDO dbSupplier = randomPojo(SupplierDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setCompanyName(null);
           o.setAddress(null);
           o.setRemark(null);
           o.setPhone(null);
           o.setEmail(null);
           o.setMobile(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       supplierMapper.insert(dbSupplier);
       // 测试 name 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setName(null)));
       // 测试 companyName 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setCompanyName(null)));
       // 测试 address 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setAddress(null)));
       // 测试 remark 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setRemark(null)));
       // 测试 phone 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setPhone(null)));
       // 测试 email 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setEmail(null)));
       // 测试 mobile 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setMobile(null)));
       // 测试 status 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       supplierMapper.insert(cloneIgnoreId(dbSupplier, o -> o.setCreateTime(null)));
       // 准备参数
       SupplierExportReqVO reqVO = new SupplierExportReqVO();
       reqVO.setName(null);
       reqVO.setCompanyName(null);
       reqVO.setAddress(null);
       reqVO.setRemark(null);
       reqVO.setPhone(null);
       reqVO.setEmail(null);
       reqVO.setMobile(null);
       reqVO.setStatus(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<SupplierDO> list = supplierService.getSupplierList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSupplier, list.get(0));
    }

}
