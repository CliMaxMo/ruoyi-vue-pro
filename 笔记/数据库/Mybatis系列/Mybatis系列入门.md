## 1. 概述

艿艿自己在 [知识星球](http://www.iocoder.cn/zhishixingqiu/?self) 中，做过一个简单的调研，看看大家使用哪个为主。结果是 MyBatis > JPA > JDBC 。这个也符合在知乎上看到的两篇文章：

- [《MyBatis 为什么在国内相当流行?》](https://www.zhihu.com/question/50729231)
- [《为什么阿里巴巴的持久层采用 iBatis 框架,而不使用 hibernate 框架呢？感觉 hibernate 更厉害的样子？》](https://www.zhihu.com/question/22098131)

而每个团队使用 MyBatis 方式还有不同，主要是如下：

> 注意，几种方式可以组合使用。

- MyBatis + [XML](https://mybatis.org/mybatis-3/zh/sqlmap-xml.html)
- MyBatis + [注解](https://blog.csdn.net/myNameIssls/article/details/21610249)
- [MyBatis-Plus](https://mp.baomidou.com/)
- [tkmybatis](http://www.mybatis.tk/)

艿艿的团队，最终我们使用 XML 的方式，因为 XML 便于可以看到每个表使用到的 SQL ，方便做优化和管理。 后来，考虑到提高开发效率，很多标准的数据库的 CRUD 操作，编写还是比较枯燥乏味浪费时间，所以使用 MyBatis-Plus 简化。当然，一些相对复杂的 SQL ，还是会考虑使用 XML 。

## 2. MyBatis + XML

### 2.1 引入依赖

在 [`pom.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/pom.xml) 文件中，引入相关依赖。



```java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>lab-12-mybatis</artifactId>

    <dependencies>
        <!-- 实现对数据库连接池的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency> <!-- 本示例，我们使用 MySQL -->
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.48</version>
        </dependency>

        <!-- 实现对 MyBatis 的自动化配置 -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.1</version>
        </dependency>

        <!-- 方便等会写单元测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

</project>
```

### 2.2 Application

创建 [`Application.java`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/java/cn/iocoder/springboot/lab12/mybatis/Application.java) 类，配置 `@MapperScan` 注解，扫描对应 Mapper 接口所在的包路径。代码如下：

```
// Application.java

@SpringBootApplication
@MapperScan(basePackages = "cn.iocoder.springboot.lab12.mybatis.mapper")
public class Application {
}
```

- [`cn.iocoder.springboot.lab12.mybatis.mapper`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/mapper) 包路径下，就是我们 Mapper 接口所在的包路径。
- 建议 1 ：因为这里是做示例。实际项目中，可以考虑创建一个 MyBatisConfig 配置类，将 `@MapperScan` 注解添加到其上。

```java
@AutoConfiguration
@MapperScan(value = "${yudao.info.base-package}", annotationClass = Mapper.class,
        lazyInitialization = "${mybatis.lazy-initialization:false}") // Mapper 懒加载，目前仅用于单元测试
public class YudaoMybatisAutoConfiguration {

}
```

### 2.3 应用配置文件

在 [`resources`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources) 目录下，创建 [`application.yaml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/application.yaml) 配置文件。配置如下：



```yaml
spring:
  # datasource 数据源配置内容
  datasource:
    url: jdbc:mysql://47.112.193.81:3306/testb5f4?useSSL=false&useUnicode=true&characterEncoding=UTF-8
    driver-class-name: com.mysql.jdbc.Driver
    username: testb5f4
    password: F4df4db0ed86@11

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: cn.iocoder.springboot.lab12.mybatis.dataobject # 配置数据库实体包路径
```

### 2.4 MyBatis 配置文件

在 [`resources`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources) 目录下，创建 [`mybatis-config.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/mybatis-config.xml) 配置文件。配置如下：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <settings>
        <!-- 使用驼峰命名法转换字段。 -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

    <typeAliases>
        <typeAlias alias="Integer" type="java.lang.Integer"/>
        <typeAlias alias="Long" type="java.lang.Long"/>
        <typeAlias alias="HashMap" type="java.util.HashMap"/>
        <typeAlias alias="LinkedHashMap" type="java.util.LinkedHashMap"/>
        <typeAlias alias="ArrayList" type="java.util.ArrayList"/>
        <typeAlias alias="LinkedList" type="java.util.LinkedList"/>
    </typeAliases>

</configuration>
```



因为在数据库中的表的字段，我们是使用下划线风格，而数据库实体的字段使用驼峰风格，所以通过 `mapUnderscoreToCamelCase = true` 来自动转换。

### 2.5 UserDO

在 [`cn.iocoder.springboot.lab12.mybatis.dataobject`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/java/cn/iocoder/springboot/lab12/mybatis/dataobject) 包路径下，创建 [UserDO.java](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/java/cn/iocoder/springboot/lab12/mybatis/dataobject/UserDO.java) 类，用户 DO 。代码如下：



```
// UserDO.java

public class UserDO {

    /**
     * 用户编号
     */
    private Integer id;
    /**
     * 账号
     */
    private String username;
    /**
     * 密码（明文）
     *
     * ps：生产环境下，千万不要明文噢
     */
    private String password;
    /**
     * 创建时间
     */
    private Date createTime;

    // ... 省略 setting/getting 方法

}
```



对应的创建表的 SQL 如下：

```
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `username` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '账号',
  `password` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### 2.6 UserMapper

在 [`cn.iocoder.springboot.lab12.mybatis.mapper`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/java/cn/iocoder/springboot/lab12/mybatis/mapper) 包路径下，创建 [UserMapper](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/java/cn/iocoder/springboot/lab12/mybatis/mapper/UserMapper.java) 接口。代码如下：

```
// UserMapper.java

@Repository
public interface UserMapper {

    int insert(UserDO user);

    int updateById(UserDO user);

    int deleteById(@Param("id") Integer id); // 生产请使用标记删除，除非有点想不开，嘿嘿。

    UserDO selectById(@Param("id") Integer id);

    UserDO selectByUsername(@Param("username") String username);

    List<UserDO> selectByIds(@Param("ids")Collection<Integer> ids);

}
```

- `@Repository` 注解，用于标记是数据访问 Bean 对象。在 MyBatis 的接口，实际**非必须**，只是为了避免在 Service 中，`@Autowired` 注入时无需报警。

- ```
  @Param
  ```

  注解，声明变量名。

  - 在方法为单参数时，**非必须**。
  - 在方法为多参数时，**必须**。艿艿自己的编程习惯，**禁止**使用 Map 作为查询参数，因为无法通过方法的定义，很直观的看懂具体的用途。

- 细心的胖友，肯定会发现例如说 `#selectByUsername(@Param("username") String username)` 等方法，是使用 *By* 字段结尾，这是为什么呢？一般情况下，在 SQL 中的 `WHERE` 条件字段，我们建议能够带在方法名后。原因无它，简单明了。如果是多个字段，可以使用 *AND* 分隔。当然，如果查询字段比较多，可能方法名会比较长。

在 [`resources/mapper`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/mapper) 路径下，创建 [`UserMapper.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/mapper/UserMapper.xml) 配置文件。代码如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.iocoder.springboot.lab12.mybatis.mapper.UserMapper">

    <sql id="FIELDS">
        id, username, password, create_time
    </sql>

    <insert id="insert" parameterType="UserDO" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO users (
          username, password, create_time
        ) VALUES (
          #{username}, #{password}, #{createTime}
        )
    </insert>

    <update id="updateById" parameterType="UserDO">
        UPDATE users
        <set>
            <if test="username != null">
                , username = #{username}
            </if>
            <if test="password != null">
                , password = #{password}
            </if>
        </set>
        WHERE id = #{id}
    </update>

    <delete id="deleteById" parameterType="Integer">
        DELETE FROM users
        WHERE id = #{id}
    </delete>

    <select id="selectById" parameterType="Integer" resultType="UserDO">
        SELECT
            <include refid="FIELDS" />
        FROM users
        WHERE id = #{id}
    </select>

    <select id="selectByUsername" parameterType="String" resultType="UserDO">
        SELECT
            <include refid="FIELDS" />
        FROM users
        WHERE username = #{username}
        LIMIT 1
    </select>

    <select id="selectByIds" resultType="UserDO">
        SELECT
            <include refid="FIELDS" />
        FROM users
        WHERE id IN
            <foreach item="id" collection="ids" separator="," open="(" close=")" index="">
                #{id}
            </foreach>
    </select>

</mapper>
```

- 建议 1 ：对于绝大多数查询，我们是返回统一字段，所以可以使用 `<sql />` 标签，定义 SQL 段。对于性能或者查询字段比较大的查询，按需要的字段查询。
- 建议 2 ：对于数据库的关键字，使用大写。例如说，`SELECT`、`WHERE` 等等。
- 建议 3 ：基本是每“块”数据库关键字占用一行，胖友可以看看艿艿写的每一行示例。一定要排版干净，毕竟我们是有代码洁癖的男孩子。

## 3. MyBatis + 注解

为最初设计时，MyBatis 是一个 XML 驱动的框架。配置信息是基于 XML 的，而且映射语句也是定义在 XML 中的。而到了 MyBatis 3，就有新选择了。MyBatis 3 构建在全面且强大的基于 Java 语言的配置 API 之上。这个配置 API 是基于 XML 的 MyBatis 配置的基础，也是新的基于注解配置的基础。注解提供了一种简单的方式来实现简单映射语句，而不会引入大量的开销。

**不幸的是，Java 注解的的表达力和灵活性十分有限。**尽管很多时间都花在调查、设计和试验上，最强大的 MyBatis 映射并不能用注解来构建——并不是在开玩笑，的确是这样。比方说，C#属性就没有这些限制，因此 MyBatis.NET 将会比 XML 有更丰富的选择。也就是说，基于 Java 注解的配置离不开它的特性。

**1、application.yaml**

在 [`application.yaml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-annotation/src/main/resources/application.yaml) 配置文件中，我们可以删除 `mapper-locations` 配置项。

当然，如果胖友想 XML 和注解一起使用，可以保留该配置项。

**2、UserMapper.xml**

可以删除 `UserMapper.xml` 配置文件。不过需要在 [UserMapper](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-annotation/src/main/java/cn/iocoder/springboot/lab12/mybatis/mapper/UserMapper.java) 接口上，通过注解声明 SQL 操作。具体见 [「3.2 UserMapper」](https://www.iocoder.cn/Spring-Boot/MyBatis/?yudao#) 。

```java
// UserMapper.java

@Repository
public interface UserMapper {

    @Insert("INSERT INTO users(username, password, create_time) VALUES(#{username}, #{password}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserDO user);

    @Update(value = {
            "<script>",
            "UPDATE users",
            "<set>",
            "<if test='username != null'>, username = #{username}</if>",
            "<if test='password != null'>, password = #{password}</if>",
            "</set>",
            "</script>"
    })
    int updateById(UserDO user);

    @Insert("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Integer id); // 生产请使用标记删除，除非有点想不开，嘿嘿。

    @Select("SELECT username, password, create_time FROM users WHERE id = #{id}")
    UserDO selectById(@Param("id") Integer id);

    @Select("SELECT username, password, create_time FROM users WHERE username = #{username}")
    UserDO selectByUsername(@Param("username") String username);

    @Select(value = {
            "<script>",
            "SELECT username, password, create_time FROM users",
            "WHERE id IN",
            "<foreach item='id' collection='ids' separator=',' open='(' close=')' index=''>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<UserDO> selectByIds(@Param("ids") Collection<Integer> ids);

}
```

## 4. MyBatis-Plus

### 4.1 引入依赖

在 [`pom.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/pom.xml) 文件中，引入相关依赖。



```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>lab-12-mybatis-plus</artifactId>

    <dependencies>
        <!-- 实现对数据库连接池的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency> <!-- 本示例，我们使用 MySQL -->
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.48</version>
        </dependency>

        <!-- 实现对 MyBatis Plus 的自动化配置 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.2.0</version>
        </dependency>

        <!-- 方便等会写单元测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

</project>
```

- 相比来说，将 `mybatis-spring-boot-starter` 替换成 `mybatis-plus-boot-starter` 。

### 4.2 Application

创建 [`Application.java`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/java/cn/iocoder/springboot/lab12/mybatis/Application.java) 类，配置 `@MapperScan` 注解，扫描对应 Mapper 接口所在的包路径。代码如下：



```
// Application.java

@SpringBootApplication
@MapperScan(basePackages = "cn.iocoder.springboot.lab12.mybatis.mapper")
public class Application {
}
```

- 和 [「2.2 Application」](https://www.iocoder.cn/Spring-Boot/MyBatis/?yudao#) 一致。

### 4.3 应用配置文件

在 [`resources`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/resources) 目录下，创建 [`application.yaml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/resources/application.yaml) 配置文件。配置如下：



```
spring:
  # datasource 数据源配置内容
  datasource:
    url: jdbc:mysql://47.112.193.81:3306/testb5f4?useSSL=false&useUnicode=true&characterEncoding=UTF-8
    driver-class-name: com.mysql.jdbc.Driver
    username: testb5f4
    password: F4df4db0ed86@11

# mybatis-plus 配置内容
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true # 虽然默认为 true ，但是还是显示去指定下。
  global-config:
    db-config:
      id-type: auto # ID 主键自增
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: cn.iocoder.springboot.lab12.mybatis.dataobject # 配置数据库实体包路径

# logging
logging:
  level:
    # dao 开启 debug 模式 mybatis 输入 sql
    cn:
      iocoder:
        springboot:
          lab12:
            mybatis:
              mapper: debug
```



- 将 `mybatis` 替换成 `mybatis-plus` 配置项目。实际上，如果老项目在用 `mybatis-spring-boot-starter` 的话，直接将 `mybatis` 修改成 `mybatis-plus` 即可。
- 相比 `mybatis` 配置项来说，`mybatis-plus` 增加了更多配置项，也因此我们无需在配置 [`mybatis-config.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-xml/src/main/resources/mybatis-config.xml) 配置文件。
- 更多的 MyBatis-Plus 配置项，可以看看 [MyBatis-Plus 使用配置](https://mybatis.plus/config/#mapperlocations) 。
- 配置 `logging` 的原因是，方便我们看到 MyBatis-Plus 自动生成的 SQL 。生产环境下，记得关闭噢。

### 4.4 UserDO

在 [`cn.iocoder.springboot.lab12.mybatis.dataobject`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/java/cn/iocoder/springboot/lab12/mybatis/dataobject) 包路径下，创建 [UserDO.java](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/java/cn/iocoder/springboot/lab12/mybatis/dataobject/UserDO.java) 类，用户 DO 。代码如下：



```
// UserDO.java
@TableName(value = "users")
public class UserDO {

    /**
     * 用户编号
     */
    private Integer id;
    /**
     * 账号
     */
    private String username;
    /**
     * 密码（明文）
     *
     * ps：生产环境下，千万不要明文噢
     */
    private String password;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;

    // ... 省略 setting/getting 方法

}
```



- 相比 [「2.5 UserDO」](https://www.iocoder.cn/Spring-Boot/MyBatis/?yudao#) 来说，主要有两点差别。

- 增加了 [`@TableName`](https://mybatis.plus/guide/annotation.html#tablename) 注解，设置了 UserDO 对应的表名是 `users` 。毕竟，我们要使用 MyBatis-Plus 给咱自动生成 CRUD 操作。

- 增加了``deleted``字段，并添加了`@TableLogic`

  注解，设置该字段为逻辑删除的标记。

  - 在 `application.yaml` 配置文件中，我们配置了删除（`logic-delete-value = 1`）和未删除（`logic-not-delete-value = 0`）。当然，也可以通过注解的 `value` 和 `delval` 来定义未删除和删除。
  - 具体关于 MyBatis-Plus 的逻辑删除功能，看下 [逻辑删除](https://mybatis.plus/guide/logic-delete.html) 部分的文档。

对应的创建表的 SQL 如下：



```
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `username` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '账号',
  `password` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `deleted` bit(1) DEFAULT NULL COMMENT '是否删除。0-未删除；1-删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### 4.5 UserMapper

在 [`cn.iocoder.springboot.lab12.mybatis.mapper`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/java/cn/iocoder/springboot/lab12/mybatis/mapper) 包路径下，创建 [UserMapper](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/java/cn/iocoder/springboot/lab12/mybatis/mapper/UserMapper.java) 接口。代码如下：



```
// UserMapper.java

@Repository
public interface UserMapper extends BaseMapper<UserDO> {

    default UserDO selectByUsername(@Param("username") String username) {
        return selectOne(new QueryWrapper<UserDO>().eq("username", username));
    }

    List<UserDO> selectByIds(@Param("ids") Collection<Integer> ids);

    default IPage<UserDO> selectPageByCreateTime(IPage<UserDO> page, @Param("createTime") Date createTime) {
        return selectPage(page,
                new QueryWrapper<UserDO>().gt("create_time", createTime));
    }

}
```



- 还是老样子，我们来比较下 [「2.6 UserMapper」](https://www.iocoder.cn/Spring-Boot/MyBatis/?yudao#) 接口。

- 继承了

   

  ```
  com.baomidou.mybatisplus.core.mapper.BaseMapper<T>
  ```

   

  接口，这样常规的 CRUD 操作，MyBatis-Plus 就可以替我们自动生成。一般来说，开发 CRUD 业务的时候，最枯燥的就是要写 CRUD 的常用 SQL ，完全跟不上艿艿的思绪哈。

  - 因为继承了 BaseMapper 接口，所以我们删除了 `#insert(UserDO user)`、`#updateById(UserDO user)`、`#deleteById(@Param("id") Integer id)`、`#selectById(@Param("id") Integer id)` 四个 CRUD 方法。
  - 更多 BaseMapper 已经提供好的接口方法，可以看看 [《MyBatis-Plus 文档 —— CRUD 接口》](https://mybatis.plus/guide/crud-interface.html#mapper-crud-接口) 。

- 对于

   

  ```
  #selectByUsername(@Param("username") String username)
  ```

   

  方法，我们使用了

   

  ```
  com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>
  ```

   

  构造相对灵活的条件，这样一些动态 SQL 我们就无需在 XML 中编写。

  - 更多 QueryWrapper 已经提供好的拼接方法，可以看看 [《MyBatis-Plus 文档 —— 条件构造器》](https://mybatis.plus/guide/wrapper.html#abstractwrapper) 。
  - 建议 1 ：不要在 Service 中，使用 QueryWrapper 拼接动态条件。因为 BaseMapper 提供了 `#selectList(Wrapper<T> queryWrapper)` 等方法，促使我们能够在 Service 层的逻辑中，使用 QueryWrapper 拼接动态条件，这样会导致逻辑里遍布了各种查询，使我们无法对实际有哪些查询条件做统一的管理。碰到这种情况，建议封装到对应的 Mapper 中，这样会更加简洁干净可管理。
  - 建议 2 ：因为 QueryWrapper 暂时不支持一些类似 `<if />` 等 MyBatis 的 OGNL 表达式，所以艿艿在 [onemall](https://github.com/YunaiV/onemall) 中，通过继承 QueryWrapper 类，封装了 [QueryWrapperX](https://github.com/YunaiV/onemall/blob/master/common/common-framework/src/main/java/cn/iocoder/common/framework/mybatis/QueryWrapperX.java) 类。

- 对于 `#selectByIds(@Param("ids")` 方法，实际也可以使用 MyBatis-Plus 的 QueryWrapper 很方便的实现，这里仅仅是为了演示在 MyBatis-Plus 混合使用 XML 。

- 对于

   

  ```
  #selectPageByCreateTime(IPage<UserDO> page, @Param("createTime") Date createTime)
  ```

   

  方法，是我们额外添加的，用于演示 MyBatis-Plus 提供的分页插件。

  - 更多 IPage 的内容，可以看看 [《MyBatis-Plus 文档 —— 分页插件》](https://mybatis.plus/guide/page.html) 。

在 [`resources/mapper`](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/resources/mapper) 路径下，创建 [`UserMapper.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/main/resources/mapper/UserMapper.xml) 配置文件。代码如下：



```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.iocoder.springboot.lab12.mybatis.mapper.UserMapper">

    <sql id="FIELDS">
        id, username, password, create_time
    </sql>

    <select id="selectByIds" resultType="UserDO">
        SELECT
            <include refid="FIELDS" />
        FROM users
        WHERE id IN
            <foreach item="id" collection="ids" separator="," open="(" close=")" index="">
                #{id}
            </foreach>
    </select>

</mapper>
```



- 是不是一下子，瘦了！

### 4.6 简单测试

创建 [UserMapperTest](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-12-mybatis/lab-12-mybatis-plus/src/test/java/cn/iocoder/springboot/lab12/mybatis/mapper/UserMapperTest.java) 测试类，我们来测试一下简单的 UserMapper 的每个操作。代码如下：



```
// UserMapperTest.java

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testInsert() {
        UserDO user = new UserDO().setUsername(UUID.randomUUID().toString())
                .setPassword("nicai").setCreateTime(new Date())
                .setDeleted(0); // 一般情况下，是否删除，可以全局枚举下。
        userMapper.insert(user);
    }

    @Test
    public void testUpdateById() {
        UserDO updateUser = new UserDO().setId(1)
                .setPassword("wobucai");
        userMapper.updateById(updateUser);
    }

    @Test
    public void testDeleteById() {
        userMapper.deleteById(2);
    }

    @Test
    public void testSelectById() {
        userMapper.selectById(1);
    }

    @Test
    public void testSelectByUsername() {
        userMapper.selectByUsername("yunai");
    }

    @Test
    public void testSelectByIds() {
        List<UserDO> users = userMapper.selectByIds(Arrays.asList(1, 3));
        System.out.println("users：" + users.size());
    }

    @Test
    public void testSelectPageByCreateTime() {
        IPage<UserDO> page = new Page<>(1, 10);
        Date createTime = new Date(2018 - 1990, Calendar.FEBRUARY, 24); // 临时 Demo ，实际不建议这么写
        page = userMapper.selectPageByCreateTime(page, createTime);
        System.out.println("users：" + page.getRecords().size());
    }

}
```