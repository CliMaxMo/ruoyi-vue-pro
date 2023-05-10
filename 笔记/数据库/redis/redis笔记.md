注解所有

- `@Cacheable`
- `@CachePut`
- `@CacheEvict`
- `@CacheConfig`
- `@Caching`
- `@EnableCaching`

## 1.1 Cacheable

被改注解注释的方法会先去缓存中查找有没有之前这个方法的返回值，如果有就直接返回缓存。如果没有则调用方法，然后将返回值存入

负责查询缓存，适合读

- `cacheNames` 属性：缓存名。**必填**。`[]` 数组，可以填写多个缓存名。

- `values` 属性：和 `cacheNames` 属性相同，是它的别名。

- ```
  key
  ```

  属性：缓存的 key 。允许空。

  - 如果为空，则默认方法的所有参数进行组合。
  - 如果非空，则需要按照 [SpEL(Spring Expression Language)](http://itmyhome.com/spring/expressions.html) 来配置。例如说，`@Cacheable(value = "users", key = "#id")` ，使用方法参数 `id` 的值作为缓存的 key 。

- ```
  condition
  ```

  属性：基于方法

  入参

  ，判断要缓存的条件。允许空。

  - 如果为空，则不进行入参的判断。
  - 如果非空，则需要按照 [SpEL(Spring Expression Language)](http://itmyhome.com/spring/expressions.html) 来配置。例如说，`@Cacheable(condition="#id > 0")` ，需要传入的 `id` 大于零。

- ```
  unless
  ```

  属性：基于方法

  返回

  ，判断不缓存的条件。允许空。

  - 如果为空，则不进行入参的判断。
  - 如果非空，则需要按照 [SpEL(Spring Expression Language)](http://itmyhome.com/spring/expressions.html) 来配置。例如说，`@Cacheable(unless="#result == null")` ，如果返回结果为 `null` ，则不进行缓存。
  - 要注意，`condition` 和 `unless` 都是条件属性，差别在于前者针对入参，后者针对结果。

- `keyGenerator` 属性：自定义 key 生成器 [KeyGenerator](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/interceptor/KeyGenerator.java) Bean 的名字。允许空。如果设置，则 `key` 失效。

- `cacheManager` 属性：自定义缓存管理器 [CacheManager](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/CacheManager.java) Bean 的名字。允许空。一般不填写，除非有多个 CacheManager Bean 的情况下。

- `cacheResolver` 属性：自定义缓存解析器 [CacheResolver](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/interceptor/CacheResolver.java) Bean 的名字。允许空。

- ```
  sync
  ```

  属性，在获得不到缓存的情况下，是否同步执行方法。

  - 默认为 `false` ，表示无需同步。
  - 如果设置为 `true` ，则执行方法时，会进行加锁，保证同一时刻，有且仅有一个方法在执行，其它线程阻塞等待。通过这样的方式，避免重复执行方法。注意，该功能的实现，需要参考第三方缓存的具体实现。

## 1.2 CachePut

会直接将改方法的结果直接放入缓存之中。

负责增加缓存，适合写

一般来说，使用方式如下：

- `@Cacheable`：搭配**读**操作，实现缓存的**被动**写。
- `@CachePut`：配置**写**操作，实现缓存的**主动**写。

`@Cacheable` 注解的属性，和 `@Cacheable` 注解的属性，基本**一致**，只少一个 `sync` 属性。

## 1.3 CacheEvict

[`@CacheEvict`](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/annotation/CacheEvict.java) 注解，添加在方法上，删除缓存。

相比 `@CachePut` 注解，它额外多了两个属性：

- `allEntries` 属性，是否删除缓存名( `cacheNames` )下，所有 key 对应的缓存。默认为 `false` ，只删除指定 key 的缓存。
- `beforeInvocation` 属性，是否在方法执行**前**删除缓存。默认为 `false` ，在方法执行**后**删除缓存。

## 1.4 Caching

[`@Caching`](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/annotation/CacheConfig.java) 注解，添加在方法上，可以组合使用多个 `@Cacheable`、`@CachePut`、`@CacheEvict` 注解。不太常用，可以暂时忽略。

## 1.5 CacheConfig

[`@CacheConfig`](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/annotation/CacheConfig.java) 注解，添加在类上，共享如下四个属性的配置：

- `cacheNames`
- `keyGenerator`
- `cacheManager`
- `cacheResolver`

## 1.6 @EnableCaching

[`@EnableCaching`](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/annotation/EnableCaching.java) 注解，标记开启 Spring Cache 功能，所以一定要添加。代码如下：

```
// EnableCaching.java

boolean proxyTargetClass() default false;

AdviceMode mode() default AdviceMode.PROXY;

int order() default Ordered.LOWEST_PRECEDENCE;
```

# Ehcache 示例

pom

```xml
        <!-- 实现对 Caches 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- Ehcache 依赖 -->
        <dependency>
            <groupId>net.sf.ehcache</groupId>
            <artifactId>ehcache</artifactId>
        </dependency>
```

yaml

```yaml
# cache 缓存配置内容
cache:
  type: ehcache
  ehcache:
    config: classpath:ehcache.xml
```

ehcache.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd">

    <!-- users 缓存 -->
    <!-- name：缓存名 -->
    <!-- maxElementsInMemory：最大缓存 key 数量 -->
    <!-- timeToLiveSeconds：缓存过期时长，单位：秒 -->
    <!-- memoryStoreEvictionPolicy：缓存淘汰策略 -->
    <cache name="users"
           maxElementsInMemory="1000"
           timeToLiveSeconds="60"
           memoryStoreEvictionPolicy="LRU"/>  <!-- 缓存淘汰策略 -->

</ehcache>
```

Application

- 配置 `@EnableCaching` 注解，开启 Spring Cache 功能。
- 配置 `@MapperScan` 注解，扫描对应 Mapper 接口所在的包路径。

需要增删改查的地方，要么mapper要么service。以下举例为mapper

```java
// UserMapper.java

@Repository
@CacheConfig(cacheNames = "users")
public interface UserMapper extends BaseMapper<UserDO> {

    @Cacheable(key = "#id")
    UserDO selectById(Integer id);

    @CachePut(key = "#user.id")
    default UserDO insert0(UserDO user) {
        // 插入记录
        this.insert(user);
        // 返回用户
        return user;
    }

    @CacheEvict(key = "#id")
    int deleteById(Integer id);
}
```

- 在类上，我们添加了 `@CacheConfig(cacheNames = "users")` 注解，统一配置该 UserMapper 使用的缓存名为 `users` 。

- `#selectById(Integer id)` 方法，添加了 `@Cacheable(key = "#id")` 注解，优先读缓存。

- `#insert0(UserDO user)` 方法，添加了 `@CachePut(key = "#user.id")` 注解，主动写缓存。

  > 注意，此处我们并没有使用 MyBatis-Plus 自带的插入方法，而是包装了一层，因为原插入方法返回的是 `int` 结果，无法进行缓存。

- `#deleteById(Integer id)` 方法，添加了 `@CacheEvict(key = "#id")` 注解，删除缓存。

UserMapperTest

```
// UserMapperTest.java

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserMapperTest {

    private static final String CACHE_NAME_USER = "users";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CacheManager cacheManager;
// UserMapperTest.java

@Test
public void testSelectById() {
    // 这里，胖友事先插入一条 id = 1 的记录。
    Integer id = 1;

    // <1.1> 查询 id = 1 的记录
    UserDO user = userMapper.selectById(id);
    System.out.println("user：" + user);
    // <1.2> 判断缓存中，是不是存在
    Assert.assertNotNull("缓存为空", cacheManager.getCache(CACHE_NAME_USER).get(user.getId(), UserDO.class));

    // <2> 查询 id = 1 的记录
    user = userMapper.selectById(id);
    System.out.println("user：" + user);
}
    // ... 省略每个单元测试。
}
```

redis也是同理

