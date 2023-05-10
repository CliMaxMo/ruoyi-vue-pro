# [Spring Boot 多数据源（读写分离）入门](https://www.iocoder.cn/Spring-Boot/dynamic-datasource/)



# 1. 

## 1.1 配置

**yaml**

```
spring:
  datasource:
    # dynamic-datasource-spring-boot-starter 动态数据源的配置内容
    dynamic:
      primary: users # 设置默认的数据源或者数据源组，默认值即为 master
      datasource:
        # 订单 orders 数据源配置
        orders:
          url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:
        # 用户 users 数据源配置
        users:
          url: jdbc:mysql://127.0.0.1:3306/test_users?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: cn.iocoder.springboot.lab17.dynamicdatasource.dataobject # 配置数据库实体包路径
```

```java
spring.datasource.dynamic 配置项，设置 dynamic-datasource-spring-boot-starter 动态数据源的配置内容。
primary 配置项，设置默认的数据源或者数据源组，默认值即为 master 。
datasource 配置项，配置每个动态数据源。这里，我们配置了 orders、users 两个动态数据源。
mybatis 配置项，设置 mybatis-spring-boot-starter MyBatis 的配置内容。
```

**mapper上使用**

```java
// OrderMapper.java
@Repository
@DS("order")
public interface OrderMapper {

    OrderDO selectById(@Param("id") Integer id);

}
// UserMapper.java
@Repository
@DS("user")
public interface UserMapper {

    UserDO selectById(@Param("id") Integer id);

}
```

## 1.2测试

### 1.2.1 测试1

```java
// OrderService.java

public void method01() {
    // 查询订单
    OrderDO order = orderMapper.selectById(1);
    System.out.println(order);
    // 查询用户
    UserDO user = userMapper.selectById(1);
    System.out.println(user);
}
方法未使用 @Transactional 注解，不会开启事务。
对于 OrderMapper 和 UserMapper 的查询操作，分别使用其接口上的 @DS 注解，找到对应的数据源，执行操作。
这样一看，在未开启事务的情况下，我们已经能够自由的使用多数据源落。
```

### 1.2.2 测试2

```java
// OrderService.java

@Transactional
public void method02() {
    // 查询订单
    OrderDO order = orderMapper.selectById(1);
    System.out.println(order);
    // 查询用户
    UserDO user = userMapper.selectById(1);
    System.out.println(user);
}
```

执行方法，抛出如下异常：

```
Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table 'test_users.orders' doesn't exist
```

1：方法上添加了``@Transactional``注解，Spring事务就会生效。Spring的 ``TransactionInterceptor``会通过AOP拦截该方法，通过``DataSourceTransactionManager``创建事务，并将事务信息通过ThreadLoacl绑定在当前线程。所以还没到``OrderMapper``的查询操作，``Connection``就已经被创建出来了,并且，因为事务信息会和当前线程绑定在一起，所以默认就是当前线程的``Connection.``

2.``DataSourceTransactionManager ``创建时会传入所需要管理的``DataSource`` 数据源。在使用 `dynamic-datasource-spring-boot-starter` 时，它创建了一个 ``DynamicRoutingDataSource ``，传入到 ``DataSourceTransactionManager`` 中。

3.``DynamicRoutingDataSource ``负责管理我们配置的多个数据源，根据``@DS``获得数据原名，从而获得对应的``DataSource``,结果因为我们在Service没有添加``@DS``注解，所以就返回默认数据源，也就是``user``



### 1.2.3 测试3

```java
 private OrderService self() {
        return (OrderService) AopContext.currentProxy();
}
public void method03() {
    // 查询订单
    self().method031();
    // 查询用户
    self().method032();
}

@Transactional // 报错，因为此时获取的是 primary 对应的 DataSource ，即 users 。
public void method031() {
    OrderDO order = orderMapper.selectById(1);
    System.out.println(order);
}

@Transactional
public void method032() {
    UserDO user = userMapper.selectById(1);
    System.out.println(user);
}

异常Table 'test_users.orders' doesn't exist
```

**如果将上述的self改成this，就不会抛出异常**

因为@Transactional是通过AOP实现的，即在运行时动态生成代理对象，对目标对象进行增强。当在一个类的方法中调用另外一个当前类，而其中一个方法上有@Transaction注解，那么被调用的方法并不会被代理，因为代理只会对外部调用产生作用

但是Gclib是全程代理的，所以他是可以调用到的。因为spring aop他保存着原类，当类内方法调用类内方法时，调用的是原类。

解决方法就是用 AopContext.currentProxy()来调用，获取的是当前的代理类

### 1.2.4 测试4

```
// OrderService.java

public void method04() {
    // 查询订单
    self().method041();
    // 查询用户
    self().method042();
}

@Transactional
@DS(DBConstants.DATASOURCE_ORDERS)
public void method041() {
    OrderDO order = orderMapper.selectById(1);
    System.out.println(order);
}

@Transactional
@DS(DBConstants.DATASOURCE_USERS)
public void method042() {
    UserDO user = userMapper.selectById(1);
    System.out.println(user);
}
```

- 和 `@method03()` 方法，差异在于，`#method041()` 和 `#method042()` 方法上，添加 `@DS` 注解，声明对应使用的 DataSource 。
- 执行方法，正常结束，未抛出异常。是不是觉得有点奇怪？
- 在执行 `#method041()` 方法前，因为有 `@Transactional` 注解，所以 Spring 事务机制触发。DynamicRoutingDataSource 根据 `@DS` 注解，获得对应的 `orders` 的 DataSource ，从而获得 Connection 。所以后续 OrderMapper 执行查询操作时，即使使用的是线程绑定的 Connection ，也可能不会报错。😈 嘿嘿，实际上，此时 OrderMapper 上的 `@DS` 注解，也没有作用。
- 对于 `#method042()` ，也是同理。但是，我们上面不是提了 Connection 会绑定在当前线程么？那么，在 `#method042()` 方法中，应该使用的是 `#method041()` 的 `orders` 对应的 Connection 呀。在 Spring 事务机制中，在一个事务执行完成后，会将事务信息和当前线程解绑。所以，在执行 `#method042()` 方法前，又可以执行一轮事务的逻辑。
- 【重要】总的来说，对于声明了 `@Transactional` 的 Service 方法上，也同时通过 `@DS` 声明对应的数据源。

### 1.2.5 测试5

```
// OrderService.java

@Transactional
@DS(DBConstants.DATASOURCE_ORDERS)
public void method05() {
    // 查询订单
    OrderDO order = orderMapper.selectById(1);
    System.out.println(order);
    // 查询用户
    self().method052();
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
@DS(DBConstants.DATASOURCE_USERS)
public void method052() {
    UserDO user = userMapper.selectById(1);
    System.out.println(user);
}
```

- 和 `@method04()` 方法，差异在于，我们直接在 `#method05()` 方法中，**此时处于一个事务中**，直接调用了 `#method052()` 方法。

- 执行方法，正常结束，未抛出异常。是不是觉得有点奇怪？

- 我们仔细看看

  ```
  #method052()
  ```

  方法，我们添加的

  ```
  @Transactionl
  ```

  注解，使用的事务传播级别是

  ```
  Propagation.REQUIRES_NEW
  ```

  。此时，在执行

  ```
  #method052()
  ```

  方法之前，TransactionInterceptor 会将原事务

  挂起

  ，暂时性的将原事务信息和当前线程解绑。

  - 所以，在执行 `#method052()` 方法前，又可以执行一轮事务的逻辑。
  - 之后，在执行 `#method052()` 方法完成后，会将原事务**恢复**，重新将原事务信息和当前线程绑定。

- 编写这个场景的目的，是想告诉胖友，如果在使用方案一【基于 Spring AbstractRoutingDataSource 做拓展】，在事务中时，如何切换数据源。当然，一旦切换数据源，可能产生多个事务，就会碰到多个事务一致性的问题，也就是分布式事务。😈

# 2. baomidou 读写分离
