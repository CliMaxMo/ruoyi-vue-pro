# 0.部署入门

## 0.1 单机部署

1. 在https://github.com/alibaba/nacos/releases 进行下载

2. ``startup.cmd -m standalone``进行单机部署

## 0.2 集群部署

**① 复制 nacos 文件**

将[「2. 单机部署」](https://www.iocoder.cn/Nacos/install/#) 的 `nacos` 文件，复制 `nacos-01`、`nacos-02`、`nacos-03` 三个文件夹，用于搭建三个 Nacos 节点。操作命令如下：



```
# 复制
$ cp -r nacos nacos-01
$ cp -r nacos nacos-02
$ cp -r nacos nacos-03

# 查看目录
$ ls -ls
     0 drwxr-xr-x  11 yunai  staff       352 Jan 20 23:33 nacos
     0 drwxr-xr-x  11 yunai  staff       352 Jan 21 09:22 nacos-01
     0 drwxr-xr-x  11 yunai  staff       352 Jan 21 09:22 nacos-02
     0 drwxr-xr-x  11 yunai  staff       352 Jan 21 09:22 nacos-03
102408 -rw-r--r--@  1 yunai  staff  52115827 Jan 20 21:58 nacos-server-1.1.4.tar.gz
```



**② 初始化数据库**

继续使用[「2.2 单机部署」](https://www.iocoder.cn/Nacos/install/#)初始化好的数据库 `nacos-example`。

**③ 配置数据库连接**

对 `nacos-01`、`nacos-02`、`nacos-03` 三个文件夹，**都**修改 `conf/application.properties` 配置文件，在尾部额外增加 MySQL 数据库配置如下：



```
# 数据源为 MySQL
spring.datasource.platform=mysql

# 数据源的数量。因为这里我们只配置一个数据源，所以设置为 1。
db.num=1
# 第 0 个数据源的配置
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos-example?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
# 数据库的账号密码
db.user=root
db.password=
```



- 如果使用 MySQL 集群，可以修改 `db.num` 为 MySQL 节点数量，然后在 `db.url.1`、`db.url.2` 不断往下配置。

**④ 配置服务器地址**

因为我们是在本机启动三个 Nacos 服务，所以需要修改其端口，保证能够启动。在 `conf/application.properties` 配置文件，修改 `server.port` 配置项，可以修改 Nacos 服务器端口。这里我们分别修改如下：

- `nacos-01` 对应 18848 端口。
- `nacos-02` 对应 28848 端口。
- `nacos-03` 对应 38848 端口。

**⑤ 配置 Nacos 集群**

在 `nacos-01`、`nacos-02`、`nacos-03` 三个文件夹中，创建 `conf/cluster.conf` 配置文件，配置一个 Nacos 集群的所有节点。具体内容如下：



```
# ip:port
192.168.3.44:18848
192.168.3.44:28848
192.168.3.44:38848
```



- 每一行为 Nacos 节点的服务器地址，格式为 `ip:port`。
- 注意，不要使用 `127.0.0.1` 这个 IP 地址，因为 Nacos 获取的是外部 IP。因此，艿艿这里使用的是 `192.168.3.44`。

通过该配置文件，每个 Nacos 服务可以知道集群中的其它 Nacos 节点。

**⑥ 启动 Nacos 服务**

现在，让我们来启动三个 Nacos 服务。

- 执行 `sh nacos-01/bin/startup.sh` 命令，启动 Nacos 节点 01。
- 执行 `sh nacos-02/bin/startup.sh` 命令，启动 Nacos 节点 02。
- 执行 `sh nacos-03/bin/startup.sh` 命令，启动 Nacos 节点 03。

每个 Nacos 节点是否启动成功，胖友自己去看看 `logs/start.out` 日志文件。

# 一、Spring Boot +Nacos注册中心

## 1.1.注册中心原理

注册中心时，一共有三种角色：服务提供者（Service Provider）、服务消费者（Service Consumer）、注册中心（Registry）。

![注册中心原理](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\01.png)

- Provider：
  - 启动时，向 Registry **注册**自己为一个服务（Service）的实例（Instance）。
  - 同时，定期向 Registry 发送**心跳**，告诉自己还存活。
  - 关闭时，向 Registry **取消注册**。
- Consumer：
  - 启动时，向 Registry **订阅**使用到的服务，并缓存服务的实例列表在内存中。
  - 后续，Consumer 向对应服务的 Provider 发起**调用**时，从内存中的该服务的实例列表选择一个，进行远程调用。
  - 关闭时，向 Registry **取消订阅**。
- Registry：
  - Provider 超过一定时间未**心跳**时，从服务的实例列表移除。
  - 服务的实例列表发生变化（新增或者移除）时，通知订阅该服务的 Consumer，从而让 Consumer 能够刷新本地缓存。

## 1.2 案例

 ###  1.2.1引入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>LabelTest3</artifactId>
    <version>1.0-SNAPSHOT</version>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <dependencies>
        <!-- 实现对 SpringMVC 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 实现对 Nacos 作为注册中心的自动化配置 -->
        <dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>nacos-discovery-spring-boot-starter</artifactId>
            <version>0.2.4</version>
        </dependency>
    </dependencies>
</project>
```

### 1.2.2 配置文件

```yaml
spring:
  application:
    name: demo-application1 # 应用名 注册到nacos就是以此名

nacos:
  # Nacos 配置中心的配置项，对应 NacosDiscoveryProperties 配置类
  discovery:
    server-addr: 127.0.0.1:8848 # Nacos 服务器地址
    auto-register: true # 是否自动注册到 Nacos 中。默认为 false。
    namespace: # 使用的 Nacos 的命名空间，默认为 null。
    register:
      service-name: ${spring.application.name} # 注册到 Nacos 的服务名
      group-name: DEFAULT_GROUP # 使用的 Nacos 服务分组，默认为 DEFAULT_GROUP。
      cluster-name: # 集群名，默认为空。
```

### 1.2.3 创建生产者

```java
@RestController
@RequestMapping("/provider")
public class ProviderController {

    @GetMapping("/demo")
    public String provider() {
        return "echo";
    }

}
```

### 1.2.4 创建消费者

```java
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @NacosInjected //需要通过nacos提供的注解进行注入
    private NamingService namingService;

    private RestTemplate restTemplate = new RestTemplate(); //进行远程调用的案例

    @GetMapping("/demo")
    public String consumer() throws IllegalStateException, NacosException {
        // <1> 获得实例
        Instance instance = null;
        if (false) {
            List<Instance> instances = namingService.getAllInstances("demo-application");
            // 获得首个实例，进行调用
            instance = instances.stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("未找到对应的 Instance"));
        } else {
            instance = namingService.selectOneHealthyInstance("demo-application1");
        }
        // <2> 执行请求
        return restTemplate.getForObject("http://" + instance.toInetAddr() + "/provider/demo",
                String.class);
    }

}
```

### 1.2.5 测试

配置好后nacos会有服务界面就有我们注册上去的生产者

![image-20230508104055244](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230508104055244.png)

接着使用浏览器访问 http://127.0.0.1:8080/consumer/demo

之后就会进入ConsumerController的consumer 通过远程访问到provider的demo方法。

接着查看nacos以下界面就会有消费者访问的记录

![image-20230508104237794](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230508104237794.png)

# 二、Spring Boot +Nacos配置中心

## 2.1 配置中心原理

配置中心远程读取

## 2.2 快速入门

### 2.2.1 引入依赖

```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencies>
    <!-- Spring Boot Starter 基础依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- 实现对 Nacos 作为配置中心的自动化配置 -->
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>nacos-config-spring-boot-starter</artifactId>
        <version>0.2.4</version>
    </dependency>
</dependencies>
```

### 2.2.2 配置文件

```yaml
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:18848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example # 使用的 Nacos 配置集的 dataId。
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: # 使用的 Nacos 的命名空间，默认为 null。
```

`nacos.config` 配置项，为 Nacos 作为配置中心的配置，对应 [NacosConfigProperties](https://github.com/nacos-group/nacos-spring-boot-project/blob/master/nacos-config-spring-boot-autoconfigure/src/main/java/com/alibaba/boot/nacos/config/properties/NacosConfigProperties.java) 配置类。

- `server-addr`：Nacos 服务器地址。

- `bootstrap.enable`：是否开启 Nacos 配置预加载功能。默认为 `false`。😈 这里，我们设置为 `true`，保证使用 `@Value` 和 `@ConfigurationProperties` 注解，可以读取到来自 Nacos 的配置项。

- `bootstrap.log-enable`：是否开启 Nacos 支持日志级别的加载时机。默认为 `false`。😈 这里，我们设置为 `true`，保证 Spring Boot 应用的 Logger 能够使用来自 Nacos 的配置项。

- `data-id`：使用的 Nacos 配置集的 dataId。😈 这里，我们设置为 `example`，稍后会去创建。

  > FROM [《Nacos 文档 —— Nacos 概念》](https://nacos.io/zh-cn/docs/concepts.html)
  >
  > **配置集**
  > 一组相关或者不相关的配置项的集合称为配置集。在系统中，一个配置文件通常就是一个配置集，包含了系统各个方面的配置。例如，一个配置集可能包含了数据源、线程池、日志级别等配置项。
  >
  > **配置集 ID**
  > Nacos 中的某个配置集的 ID。配置集 ID 是组织划分配置的维度之一。Data ID 通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个配置集都可以被一个有意义的名称标识。Data ID 通常采用类 Java 包（如 `com.taobao.tc.refund.log.level`）的命名规则保证全局唯一性。此命名规则非强制。

- `type`：使用的 Nacos 配置集的配置格式。默认为 `PROPERTIES`。这里，我们设置 `YAML`，对应 YAML 格式的配置格式。

- `group`：使用的 Nacos 配置分组，默认为 `DEFAULT_GROUP`。😈 这里，我们设置为 `DEFAULT_GROUP`，就是默认值。

  > FROM [《Nacos 文档 —— Nacos 概念》](https://nacos.io/zh-cn/docs/concepts.html)
  >
  > **配置分组**
  > Nacos 中的一组配置集，是组织配置的维度之一。通过一个有意义的字符串（如 Buy 或 Trade ）对配置集进行分组，从而区分 Data ID 相同的配置集。当您在 Nacos 上创建一个配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 `DEFAULT_GROUP` 。配置分组的常见场景：不同的应用或组件使用了相同的配置类型，如 `database_url` 配置和 `MQ_topic` 配置。

- `namespace`：使用的 Nacos 的命名空间，默认为 `null`。😈 稍后，我们会通过 `namespace` 配置项，基于 Nacos 的多环境不同配置的功能。

  > FROM [《Nacos 文档 —— Nacos 概念》](https://nacos.io/zh-cn/docs/concepts.html)
  >
  > **命名空间**
  > 用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置。Namespace 的常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。



### 2.2.3 创建 Nacos 配置集

① 打开 Nacos UI 界面的「配置列表」菜单，进入「配置管理」功能。如下图所示：![配置管理](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\02.png)

② 点击列表右上角的➕号，进入「新建配置」界面，创建一个 Nacos 配置集。输入如下内容，并点击「发布」按钮，完成创建。如下图所示：![新建配置](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\03.png)

其中，YAML 配置文件如下：

```
order:
  pay-timeout-seconds: 120 # 订单支付超时时长，单位：秒。
  create-frequency-seconds: 10 # 订单创建频率，单位：秒
```

### 2.2.4 OrderProperties

创建 [OrderProperties](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/OrderProperties.java) 配置类，读取 `order` 配置项。代码如下：

```java
@Component
@ConfigurationProperties(prefix = "order")
// @NacosConfigurationProperties(prefix = "order", dataId = "${nacos.config.data-id}", type = ConfigType.YAML)
public class OrderProperties {

    /**
     * 订单支付超时时长，单位：秒。
     */
    private Integer payTimeoutSeconds;

    /**
     * 订单创建频率，单位：秒
     */
    private Integer createFrequencySeconds;

    // ... 省略 set/get 方法

}
```

- 在类上，添加 `@Component` 注解，保证该配置类可以作为一个 Bean 被扫描到。
- 在类上，添加 `@ConfigurationProperties` 注解，并设置 `prefix = "order"` 属性，这样它就可以读取**前缀**为 `order` 配置项，设置到配置类对应的属性上。

😈 这里，我们注释了一段 [`@NacosConfigurationProperties`](https://github.com/nacos-group/nacos-spring-boot-project/blob/master/nacos-config-spring-boot-autoconfigure/src/main/java/com/alibaba/boot/nacos/config/properties/NacosConfigProperties.java) 注解的代码，该注解在功能上是对标 `@ConfigurationProperties` 注解，用于将 Nacos 配置注入 POJO 配置类中。为什么我们这里注释掉了呢？因为我们在[「2.2 配置文件」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)中，设置了 `nacos.config.bootstrap.enable=true`，Spring Boot 应用在启动时，预加载了来自 Nacos 配置，所以可以直接使用 `@ConfigurationProperties` 注解即可。这样的好处，是可以更加通用，而无需和 Nacos 有耦合与依赖。

### 2.2.5 Application

创建 [`Application.java`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/Application.java) 类，配置 `@SpringBootApplication` 注解即可。代码如下：



```java
@SpringBootApplication
// @NacosPropertySource(dataId = "example", type = ConfigType.YAML)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    public class OrderPropertiesCommandLineRunner implements CommandLineRunner {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private OrderProperties orderProperties;

        @Override
        public void run(String... args) {
            logger.info("payTimeoutSeconds:" + orderProperties.getPayTimeoutSeconds());
            logger.info("createFrequencySeconds:" + orderProperties.getCreateFrequencySeconds());
        }

    }

    @Component
    public class ValueCommandLineRunner implements CommandLineRunner {

        private final Logger logger = LoggerFactory.getLogger(getClass());

//        @NacosValue(value = "${order.pay-timeout-seconds}")
        @Value(value = "${order.pay-timeout-seconds}")
        private Integer payTimeoutSeconds;

//        @NacosValue(value = "${order.create-frequency-seconds}")
        @Value(value = "${order.create-frequency-seconds}")
        private Integer createFrequencySeconds;

        @Override
        public void run(String... args) {
            logger.info("payTimeoutSeconds:" + payTimeoutSeconds);
            logger.info("createFrequencySeconds:" + createFrequencySeconds);
        }
    }

}
```

① 在 Application 类上，我们注释了一段 [`@NacosPropertySource`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-context/src/main/java/com/alibaba/nacos/spring/core/env/NacosPropertySource.java) 注解，该注解用于声明从 Nacos 读取的配置集。为什么我们整列注释掉了呢？因为我们在[「2.2 配置文件」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)中，通过 `nacos.config.data-id`、`nacos.config.type` 等配置项，已经设置从 Nacos 读取的配置集。该配置一般用于在**纯** Spring 应用中，使用 Nacos 作为配置中心。

② 在 OrderPropertiesCommandLineRunner 类中，我们测试了使用 `@ConfigurationProperties` 注解的 OrderProperties 配置类，读取 `order` 配置项的效果。

③ 在 ValueCommandLineRunner 类中，我们测试了使用 `@Value` 注解，读取 `order` 配置项的效果。😈 这里，我们注释了一段 [`@NacosValue`](https://github.com/alibaba/nacos/blob/409838fe1f2dc53f7f5c63c660cd69b0fae1d49e/api/src/main/java/com/alibaba/nacos/api/config/annotation/NacosValue.java) 注解的代码，该注解在功能上是对标 `@Value` 注解，用于将 Nacos 配置注入属性种。为什么我们这里注释掉了呢？原因同 `@NacosConfigurationProperties` 注解。

> 友情提示：
>
> - `@Value` 注解，是 Spring 所提供。
> - `@ConfigurationProperties` 注解，是 Spring Boot 所提供。

下面，我们来执行 Application 的 `#main(String[] args)` 方法，启动 Spring Boot 应用。输出日志如下：

```java
2023-05-08 10:08:21.411  INFO 38296 --- [           main] o.e.r.Application$ValueCommandLineRunner : payTimeoutSeconds:120
2023-05-08 10:08:21.411  INFO 38296 --- [           main] o.e.r.Application$ValueCommandLineRunner : createFrequencySeconds:10
2023-05-08 10:08:21.411  INFO 38296 --- [           main] ication$OrderPropertiesCommandLineRunner : payTimeoutSeconds:120
2023-05-08 10:08:21.411  INFO 38296 --- [           main] ication$OrderPropertiesCommandLineRunner : createFrequencySeconds:10
```

## 2.3. 多环境配置

### 2.3.1 创建Nacos命名空间

① 打开 Nacos UI 界面的「命名空间」菜单，进入「命名空间」功能。如下图所示：![命名空间](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\4.png)

② 点击列表右上角的「新建命名空间」按钮，弹出「新建命名空间」窗口，创建一个 `dev` 命名空间。输入如下内容，并点击「确定」按钮，完成创建。如下图所示：![新建命名空间](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\7.png)

③ 重复该操作，继续创建一个 `prod` 命名空间。最终 `dev` 和 `prod` 信息如下图：![命名空间列表](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\5.png)

### 2.3.2 创建Nacos配置集

![配置管理](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\6.png)

② 点击 `dev` 命名空间，然后点击列表右上角的➕号，进入「新建配置」界面，创建一个 Nacos 配置集。输入如下内容，并点击「发布」按钮，完成创建。如下图所示：![新建配置](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\85.png)

③ 点击 `prod` 命名空间，然后点击列表右上角的➕号，进入「新建配置」界面，创建一个 Nacos 配置集。输入如下内容，并点击「发布」按钮，完成创建。如下图所示：![新建配置](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\9.png)

如此，我们在 Nacos 中有 `dev`、`prod` 命名空间。而这两命名空间下，都有一个 `example` 配置集。而这两配置集都有 `server.port` 配置项，用于启动不同端口的服务器。😈 为什么选择 `server.port` 配置呢？因为 Spring Boot 项目启动后，从日志中就可以看到生效的服务器端口

### 2.3.3 引入依赖

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencies>
    <!-- 实现对 SpringMVC 的自动化配置 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 实现对 Nacos 作为注册中心的自动化配置 -->
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>nacos-discovery-spring-boot-starter</artifactId>
        <version>0.2.4</version>
    </dependency>
</dependencies>
```

### 2.3.4 配置文件

**application**

```
spring:
  application:
    name: demo-application
  profiles:
    active: pro
```

**application-dev**

```
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:8848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example # 使用的 Nacos 配置集的 dataId。
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: 7b430164-53bf-454a-b8b8-358f1ac8b8ae # 使用的 Nacos 的命名空间，默认为 null。
```

**application-pro**

```
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:8848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example # 使用的 Nacos 配置集的 dataId。
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: 2e5aaec0-cb90-4be0-84cf-c1d1319bfa17 # 使用的 Nacos 的命名空间，默认为 null。
```

### 2.3.6 测试结果

根据配置的选项一样

## 2.4.自动刷新

### 2.4.1 引入依赖

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencies>
    <!-- 实现对 SpringMVC 的自动化配置 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 实现对 Nacos 作为注册中心的自动化配置 -->
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>nacos-discovery-spring-boot-starter</artifactId>
        <version>0.2.4</version>
    </dependency>
</dependencies>
```

### 2.4.2 创建Nacos配置集

![image-20230508135120817](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\10.png)

### 2.4.3 配置文件

在 [`application.yml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-auto-refresh/src/main/resources/application.yaml) 中，添加 Nacos 配置，如下：

```
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:18848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example-auto-refresh # 使用的 Nacos 配置集的 dataId。
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: # 使用的 Nacos 的命名空间，默认为 null。
    auto-refresh: true # 是否自动刷新，默认为 false。
```

`nacos.config.auto-refresh` 配置项为 `true`，开启 Nacos 自动刷新配置的功能。

### 2.4.4  TestProperties

```java
@Component
@NacosConfigurationProperties(prefix = "", dataId = "${nacos.config.data-id}", type = ConfigType.YAML, autoRefreshed = true)
public class TestProperties {
    /**
     * 测试属性
     */
    private String test;

    public String getTest() {
        return test;
    }
    public void setTest(String test) {
        this.test = test;
    }
    
}
```

这里有一点要注意，`nacos.config.auto-refresh` 配置项开启的是**全局**的，必须为 `true` 时，才能使用自动刷新配置的功能。同时，每个 `@NacosConfigurationProperties` 或 `@NacosValue` 注解，也需要设置 `autoRefreshed` 属性为 `true` 时，对应的配置项才会自动刷新。

### 2.4.5 DemoController

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

    //    @Value("${test}")
    @NacosValue(value = "${test}", autoRefreshed = true)
    private String test;

    @GetMapping("/test")
    public String test() {
        return test;
    }

    @Autowired
    private TestProperties testProperties;

    @GetMapping("/test_properties")
    public TestProperties testProperties() {
        return testProperties;
    }

}
```

- `/demo/test` 接口，测试 `@NacosValue` 注解。注意，这里为了实现自动刷新配置的功能，我们也无法使用 `@Value` 注解，而是使用 `@NacosValue` 替代。同时，设置其 `autoRefreshed` 属性为 `true`。
- `/demo/test_properties` 接口，测试 `@NacosConfigurationProperties` 注解的 TestProperties 配置类。

### 2.4.6

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

### 2.4.7 测试

① 分别请求 `/demo/test`、`/demo/test_properties` 接口，响应结果如下：

```
# /demo/test 接口
ryan

# /demo/test_properties 接口
{
    "test": "ryan"
}
```

② 修改 Nacos 配置集 `example-auto-refresh`，将 `test` 配置项设置为 `ryan 帅`。

重新请求，数据马上进行更改

### 2.4.8 配置监听器

通过 `@NacosValue` 和 `@NacosConfigurationProperties` 注解，已经能够满足我们绝大多数场景下的自动刷新配置的功能。但是，在一些场景下，我们仍然需要**自定义 Nacos 配置监听器**，实现对 Nacos 配置的监听，执行自定义的逻辑。

例如说，当数据库连接的配置发生变更时，我们需要通过监听该配置的变更，重新初始化应用中的数据库连接，从而访问到新的数据库地址。

又例如说，当日志级别发生变更时，我们需要通过监听该配置的变更，设置应用中的 Logger 的日志级别，从而后续的日志打印可以根据新的日志级别。

可能这么说，胖友会觉得有点抽象，我们来搭建一个日志级别的示例。

在 [`cn.iocoder.springboot.lab44.nacosdemo.listener`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-auto-refresh/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/listener/) 包下，创建 [LoggingSystemConfigListener](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-auto-refresh/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/listener/LoggingSystemConfigListener.java) 类，监听 `logging.level` 配置项的变更，修改 Logger 的日志级别。代码如下：



```
@Component
public class LoggingSystemConfigListener {

    /**
     * 日志配置项的前缀
     */
    private static final String LOGGER_TAG = "logging.level.";

    @Resource
    private LoggingSystem loggingSystem;

    @NacosConfigListener(dataId = "${nacos.config.data-id}", type = ConfigType.YAML, timeout = 5000)
    public void onChange(String newLog) throws Exception {
        // <X> 使用 DefaultYamlConfigParse 工具类，解析配置
        Properties properties = new DefaultYamlConfigParse().parse(newLog);
        // <Y> 遍历配置集的每个配置项，判断是否是 logging.level 配置项
        for (Object t : properties.keySet()) {
            String key = String.valueOf(t);
            // 如果是 logging.level 配置项，则设置其对应的日志级别
            if (key.startsWith(LOGGER_TAG)) {
                // 获得日志级别
                String strLevel = properties.getProperty(key, "info");
                LogLevel level = LogLevel.valueOf(strLevel.toUpperCase());
                // 设置日志级别到 LoggingSystem 中
                loggingSystem.setLogLevel(key.replace(LOGGER_TAG, ""), level);
            }
        }
    }

}
```



- `loggingSystem` 属性，是 Spring Boot Logger 日志系统，通过 [LoggingSystem](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/LoggingSystem.java) 可以进行日志级别的修改。

- 在 `#onChange(String newLog)` 方法上，我们添加了 [`@NacosConfigListener`](https://github.com/alibaba/nacos/blob/develop/api/src/main/java/com/alibaba/nacos/api/config/annotation/NacosConfigListener.java) 注解，声明该方法处理指定配置集的配置变化。

- ```
  <X>
  ```

   

  处，使用 Nacos 提供的

   

  DefaultYamlConfigParse

   

  解析 YAML 格式的配置。示例如下图所示：

  

  - Nacos 还提供了 [DefaultPropertiesConfigParse](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-context/src/main/java/com/alibaba/nacos/spring/util/parse/DefaultPropertiesConfigParse.java) 解析 PROPERTIES 格式，[DefaultXmlConfigParse](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-context/src/main/java/com/alibaba/nacos/spring/util/parse/DefaultXmlConfigParse.java) 解析 XML 格式，[DefaultJsonConfigParse](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-context/src/main/java/com/alibaba/nacos/spring/util/parse/DefaultJsonConfigParse.java) 解析 JSON 格式。

- `<Y>` 处，遍历配置集的每个配置项，判断如果是 `logging.level` 配置项，则设置到 LoggingSystem 中，从而修改日志级别。详细的整个过程，胖友看看艿艿的详细的注释，嘿嘿~

### 2.4.9 再次测试

① 在 DemoController 类中，增加如下 API 接口。代码如下：



```
private Logger logger = LoggerFactory.getLogger(getClass());

@GetMapping("/logger")
public void logger() {
    logger.debug("[logger][测试一下]");
}
```



- 如果 DemoController 对应的 Logger 日志级别是 DEBUG 以上，则无法打印出日志。

② 在 Nacos 中，修改测试自动刷新配置的配置集 `example-auto-refresh`，具体内容如下图：![修改 Nacos 配置集](https://static.iocoder.cn/images/Spring-Boot/2020-07-01/25.png)

其中，配置内容如下，方便胖友复制：

```
test: 好帅

logging:
  level:
    cn:
      iocoder:
        springboot:
          lab44:
            nacosdemo:
              controller:
                DemoController: INFO
```



③ 启动 Spring Boot 应用，开始我们本轮的测试。

④ 请求 `/demo/logger` 接口，控制台并未打印日志，因为当前日志级别是 INFO。

⑤ 在 Nacos 中，修改测试自动刷新配置的配置集 `example-auto-refresh`，具体内容如下图：![修改 Nacos 配置集 02](https://static.iocoder.cn/images/Spring-Boot/2020-07-01/26.png)

⑥ 请求 `/demo/logger` 接口，控制台打印日志，因为当前日志级别是 DEBUG。日志内容如下：



```
2020-01-23 14:40:41.484 DEBUG 23501 --- [nio-8080-exec-5] c.i.s.l.n.controller.DemoController      : [logger][测试一下]
```



- 符合预期。

## 2.5 配置加密

> 示例代码对应仓库：[lab-44-nacos-config-demo-jasypt](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-44/lab-44-nacos-config-demo-jasypt)。

考虑到安全性，我们可能最好将配置文件中的敏感信息进行加密。例如说，MySQL 的用户名密码、第三方平台的 Token 令牌等等。不过，Nacos 暂时未内置配置加密的功能。官方文档说明如下：

> FROM https://nacos.io/zh-cn/docs/faq.html
>
> **Nacos如何对配置进行加密**
> Nacos 计划在 1.X 版本提供加密的能力，目前还不支持加密，只能靠 sdk 做好了加密再存到 nacos 中。

因此，我们暂时只能在客户端进行配置的加解密。这里，我们继续采用在[《芋道 Spring Boot 配置文件入门》](http://www.iocoder.cn/Spring-Boot/config-file/?self)的[「8. 配置加密」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)小节中使用的 [Jasypt](https://github.com/jasypt/jasypt)。

下面，我们来使用 Nacos + Jasypt 搭建一个配置加密的示例。

## 2.5.1 引入依赖

在 [`pom.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-jasypt/pom.xml) 文件中，引入相关依赖。



```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>lab-44-nacos-config-demo-jasypt</artifactId>

    <dependencies>
        <!-- 实现对 SpringMVC 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 实现对 Nacos 作为配置中心的自动化配置 -->
        <dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>nacos-config-spring-boot-starter</artifactId>
            <version>0.2.4</version>
        </dependency>

        <!-- 实现对 Jasypt 实现自动化配置 -->
        <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>3.0.2</version>
<!--            <scope>test</scope>-->
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



- 引入 [`jasypt-spring-boot-starter`](https://mvnrepository.com/artifact/com.github.ulisesbocchio/jasypt-spring-boot-starter) 依赖，实现对 Jasypt 的自动化配置。

## 5.2 创建 Nacos 配置集

在 Nacos 中，创建一个用于测试配置加密的配置集 `example-jasypt`，具体内容如下图：![创建 Nacos 配置集](https://static.iocoder.cn/images/Spring-Boot/2020-07-01/31.png)

这里为了测试简便，我们直接添加加密秘钥 `jasypt.encryptor.password` 配置项在该 Nacos 配置集中。如果为了安全性更高，实际建议把加密秘钥和配置隔离。不然，如果配置泄露，岂不是可以拿着加密秘钥，直接进行解密。

## 2.5.3 配置文件

在 [`application.yml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-43/lab-43-demo-jasypt/src/main/resources/application.yaml) 中，添加 Nacos 配置，如下：



```
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:18848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example-jasypt # 使用的 Nacos 配置集的 dataId。
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: # 使用的 Nacos 的命名空间，默认为 null。
    auto-refresh: true # 是否自动刷新，默认为 false。
```



- 和[「4.3 配置文件」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)一样，就是换了一个配置集为 `example-jasypt`。

## 2.5.4 Application

创建 [`Application.java`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-jasypt/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/Application.java) 类，配置 `@SpringBootApplication` 注解即可。代码如下：



```
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

## 2.5.5 简单测试

下面，我们进行下简单测试。

- 首先，我们会使用 Jasypt 将 `demo-application` 进行加密，获得加密结果。
- 然后，将加密结果，赋值到 Nacos 配置集 `example-jasypt` 的 `spring.application.name` 配置项。
- 最后，我们会使用 Jasypt 将 `spring.application.name` 配置项解密。

创建 [JasyptTest](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-jasypt/src/test/java/cn/iocoder/springboot/lab44/nacosdemo/JasyptTest.java) 测试类，编写测试代码如下：



```
@RunWith(SpringRunner.class)
@SpringBootTest
public class JasyptTest {

    @Autowired
    private StringEncryptor encryptor;

    @Test
    public void encode() {
        String applicationName = "demo-application";
        System.out.println(encryptor.encrypt(applicationName));
    }

    @Value("${spring.application.name}")
//    @NacosValue("${spring.application.name}")
    private String applicationName;

    @Test
    public void print() {
        System.out.println(applicationName);
    }

}
```



- 首先，执行

   

  ```
  #encode()
  ```

   

  方法，

  手动

  使用 Jasypt 将

   

  ```
  demo-application
  ```

   

  进行加密，获得加密结果。加密结果如下：

  ```
  nFVlMl4ZJ4vJLJ68X4x+a3CIerdaG0488LpZHKyoGxPoJkgemJT/nw==
  ```

- 然后，将加密结果，赋值到 Nacos 配置集 `example-jasypt` 的 `spring.application.name` 配置项。如下图所示：![修改 Nacos 配置集](https://static.iocoder.cn/images/Spring-Boot/2020-07-01/32.png)

- 最后，执行 `#print()` 方法，**自动**使用 Jasypt 将 `spring.application.name` 配置项解密。解密结果如下：

  ```
  demo-application
  ```

  

  - 成功正确解密，符合预期。

## 2.5.6 补充说明

目前测试下来，在将 Jasypt 集成进来时，Nacos 的[「4. 自动配置刷新」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)功能，竟然失效了。

- 具体的验证，胖友可以将 `jasypt-spring-boot-starter` 依赖设置成 `<scope>test</scope>`，并是使用 [DemoController](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-jasypt/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/controller/DemoController.java) 进行测试。
- 具体的原因，艿艿暂时没去调试与研究，有了解的胖友，麻烦告知下哟。

如果说，胖友暂时不需要自动配置刷新功能的话，可以考虑选择使用 Jasypt 集成。如果需要的话，那么就等待官方支持吧，暂时不要考虑使用 Jasypt 咧。

## 2.6. 监控端点

> 示例代码对应仓库：[lab-44-nacos-config-demo-actuator](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-44/lab-44-nacos-config-demo-actuator)。

> 在[《芋道 Spring Boot 监控端点 Actuator 入门》](http://www.iocoder.cn/Spring-Boot/Actuator/?self)中，我们学习了 Spring Boot Actuator 内置的监控端点。而 Nacos 有个 [nacos-config-spring-boot-actuator](https://github.com/nacos-group/nacos-spring-boot-project/tree/master/nacos-config-spring-boot-actuator) 子项目，提供了 Nacos 作为 Spring Boot 配置中心时的监控端点。

下面，我们从[「4. 自动刷新配置」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)的 [lab-44-nacos-config-demo-auto-refresh](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-44/lab-44-nacos-config-demo-auto-refresh) 示例项目，复制出 [lab-44-nacos-config-demo-actuator](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-actuator/) 作为本小节的示例。当然，我们还需要将 Actuator 集成到其中。

### 2.6.1 引入依赖

修改 [`pom.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-actuator/pom.xml) 文件中，额外引入 Actuator 相关依赖。

```
<!-- 实现对 Actuator 的自动化配置 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<!-- 实现对 Nacos 作为配置中心的 Actuator 的自动化配置 -->
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-config-spring-boot-actuator</artifactId>
    <version>0.2.4</version>
</dependency>
```

### 2.6.2 配置文件

修改 [`application.yaml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-actuator/src/main/resources/application.yaml) 配置文件，额外引入 Actuator 相关配置。



```
management:
  endpoint:
    # Health 端点配置项，对应 HealthProperties 配置类
    health:
      show-details: ALWAYS # 何时显示完整的健康信息。默认为 NEVER 都不展示。可选 WHEN_AUTHORIZED 当经过授权的用户；可选 ALWAYS 总是展示。
  endpoints:
    # Actuator HTTP 配置项，对应 WebEndpointProperties 配置类
    web:
      exposure:
        include: '*' # 需要开放的端点。默认值只打开 health 和 info 两个端点。通过设置 * ，可以开放所有端点。
```

- 每个配置项的用途，胖友看下艿艿添加的详细注释。

配置完成后，启动 Spring Boot 应用，我们可以开始测试 Nacos 提供的监控端点了。

### 2.6.3 health 端点

`health` 端点，是 Spring Boot Actuator 内置的健康状态端点。而 Nacos 自定义了 HealthIndicator 实现类 [NacosConfigHealthIndicator](https://github.com/nacos-group/nacos-spring-boot-project/blob/master/nacos-config-spring-boot-actuator/src/main/java/com/alibaba/boot/nacos/actuate/health/NacosConfigHealthIndicator.java)，获取应用连接 Nacos 的健康状态。

请求 `actuator/health` 地址，获取健康状态结果如下图：![health 端点](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\11.png)

### 2.6.4 nacos-config 端点

`nacos-config` 端点，是 Nacos 自定义端点 [NacosConfigEndpoint](https://github.com/nacos-group/nacos-spring-boot-project/blob/master/nacos-config-spring-boot-actuator/src/main/java/com/alibaba/boot/nacos/actuate/endpoint/NacosConfigEndpoint.java)，获得 Nacos 在 Spring Boot 的配置信息。

请求 `actuator/nacos-config` 地址，获取健康状态结果如下图：![nacos-config 端点](D:\cmw\ruoyi-vue-pro\笔记\spring\nacos\12.png)

## 2.7. 配置加载顺序

> 示例代码对应仓库：[lab-44-nacos-config-demo-multi](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-44/lab-44-nacos-config-demo-multi)。

在[《芋道 Spring Boot 配置文件入门》](http://www.iocoder.cn/Spring-Boot/config-file/?self)的[「9. 配置加载顺序」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)小节，我们了解了 Spring Boot 自带的配置加载顺序。本小节，我们来看看来自 Nacos 的配置，在其中的顺序。同时，我们将配置多个 Nacos 配置集，看看它们互相之间的加载顺序。

下面，我们来搭建一个用于测试配置加载顺序的示例。

### 2.7.1 引入依赖

在 [`pom.xml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-multi/pom.xml) 文件中，引入相关依赖。



```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>lab-44-nacos-config-demo-multi</artifactId>

    <dependencies>
        <!-- 实现对 SpringMVC 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 实现对 Nacos 作为配置中心的自动化配置 -->
        <dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>nacos-config-spring-boot-starter</artifactId>
            <version>0.2.4</version>
        </dependency>
    </dependencies>

</project>
```



- 和[「2.1 引入依赖」](https://www.iocoder.cn/Spring-Boot/config-nacos/?self#)是一致的。

### 2.7.2 配置文件

在 [`application.yml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo/src/main/resources/application.yaml) 中，添加 Nacos 配置，如下：



```
nacos:
  # Nacos 配置中心的配置项，对应 NacosConfigProperties 配置类
  config:
    server-addr: 127.0.0.1:18848 # Nacos 服务器地址
    bootstrap:
      enable: true # 是否开启 Nacos 配置预加载功能。默认为 false。
      log-enable: true # 是否开启 Nacos 支持日志级别的加载时机。默认为 false。
    data-id: example-multi-01 # 使用的 Nacos 配置集的 dataId。
#    data-ids: example-multi-02
    type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
    group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
    namespace: # 使用的 Nacos 的命名空间，默认为 null。
    auto-refresh: true # 是否自动刷新，默认为 false。
    ext-config:
      - server-addr: 127.0.0.1:18848 # Nacos 服务器地址
#        data-id: example-multi-11 # 使用的 Nacos 配置集的 dataId。
        data-ids: example-multi-11, example-multi-12
        type: YAML # 使用的 Nacos 配置集的配置格式。默认为 PROPERTIES。
        group: DEFAULT_GROUP # 使用的 Nacos 配置分组，默认为 DEFAULT_GROUP。
        namespace: # 使用的 Nacos 的命名空间，默认为 null。
        auto-refresh: true # 是否自动刷新，默认为 false。
#      - # 这里，可以继续添加。
```



- 在 `nacos.config` 配置项下，可以通过 `data-id` 和 `data-ids` 来设置使用的 Nacos 配置集。不过要注意，这两者只能二选一。
- 在 `nacos.config.ext-config` 配置项下，它是 [Config](https://github.com/nacos-group/nacos-spring-boot-project/blob/master/nacos-config-spring-boot-autoconfigure/src/main/java/com/alibaba/boot/nacos/config/properties/NacosConfigProperties.java) 数组，可以配置多个配置集。实际上，Config 的属性和 `nacos.config` 是基本类似的，从艿艿这里给出来的示例，是不是已经可以发现啦。不过要注意，`nacos.config` 配置项下的优先级高于 `nacos.config.ext-config`。

这里，Nacos 配置集 `example-multi-01`、`example-multi-11`、`example-multi-12` 需要创建下，具体的配置内容随意哈。

### 2.7.3 Application

创建 [`Application.java`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-44/lab-44-nacos-config-demo-jasypt/src/main/java/cn/iocoder/springboot/lab44/nacosdemo/Application.java) 类，配置 `@SpringBootApplication` 注解即可。代码如下：



```
@SpringBootApplication
// @NacosPropertySource(dataId = "example", type = ConfigType.YAML)
public class Application {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // 查看 Environment
        Environment environment = context.getEnvironment();
        System.out.println(environment);
    }

}
```



在代码中，我们去获取了 Spring [Environment](https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/env/Environment.java) 对象，因为我们要从其中获取到 [PropertySource](https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/env/PropertySource.java) 配置来源。**DEBUG** 运行 Application，并记得在 `System.out.println(environment);` 代码块打一个断点，可以看到如下图的调试信息：![调试信息](https://static.iocoder.cn/images/Spring-Boot/2020-07-01/51.png)

- 每一个 Nacos 配置集，对应一个 PropertySource 对象，并且 `nacos.config` 配置项下的优先级高于 `nacos.config.ext-config`。
- 所有 Nacos 配置集的 PropertySource 对象，排在 `application.yaml` 配置文件的 PropertySource 对象后面，也就是优先级最低。

### 2.7.4 补充说明

搞懂配置加载顺序的作用，很多时候是解决多个配置来源，里面配置了相同的配置项。艿艿建议的话，尽量避免出现相同配置项，排查起来还挺麻烦的。

不过所幸，在日常开发中，我们也很少会设置相同的配置项 😜。