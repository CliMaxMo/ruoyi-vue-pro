# 1. 概念

## 1 消息模型（Message Model）

RocketMQ主要由 Producer、Broker、Consumer 三部分组成，其中Producer 负责生产消息，Consumer 负责消费消息，Broker 负责存储消息。Broker 在实际部署过程中对应一台服务器，每个 Broker 可以存储多个Topic的消息，每个Topic的消息也可以分片存储于不同的 Broker。Message Queue 用于存储消息的物理地址，每个Topic中的消息地址存储于多个 Message Queue 中。ConsumerGroup 由多个Consumer 实例构成。

## 2 消息生产者（Producer）

负责生产消息，一般由业务系统负责生产消息。一个消息生产者会把业务应用系统里产生的消息发送到broker服务器。RocketMQ提供多种发送方式，同步发送、异步发送、顺序发送、单向发送。同步和异步方式均需要Broker返回确认信息，单向发送不需要。

## 3 消息消费者（Consumer）

负责消费消息，一般是后台系统负责异步消费。一个消息消费者会从Broker服务器拉取消息、并将其提供给应用程序。从用户应用的角度而言提供了两种消费形式：拉取式消费、推动式消费。

## 4 主题（Topic）

表示一类消息的集合，每个主题包含若干条消息，每条消息只能属于一个主题，是RocketMQ进行消息订阅的基本单位。

## 5 代理服务器（Broker Server）

消息中转角色，负责存储消息、转发消息。代理服务器在RocketMQ系统中负责接收从生产者发送来的消息并存储、同时为消费者的拉取请求作准备。代理服务器也存储消息相关的元数据，包括消费者组、消费进度偏移和主题和队列消息等。

## 6 名字服务（Name Server）

名称服务充当路由消息的提供者。生产者或消费者能够通过名字服务查找各主题相应的Broker IP列表。多个Namesrv实例组成集群，但相互独立，没有信息交换。

## 7 拉取式消费（Pull Consumer）

Consumer消费的一种类型，应用通常主动调用Consumer的拉消息方法从Broker服务器拉消息、主动权由应用控制。一旦获取了批量消息，应用就会启动消费过程。

## 8 推动式消费（Push Consumer）

Consumer消费的一种类型，应用不需要主动调用Consumer的拉消息方法，在底层已经封装了拉取的调用逻辑，在用户层面看来是broker把消息推送过来的，其实底层还是consumer去broker主动拉取消息。

## 9 生产者组（Producer Group）

同一类Producer的集合，这类Producer发送同一类消息且发送逻辑一致。如果发送的是事务消息且原始生产者在发送之后崩溃，则Broker服务器会联系同一生产者组的其他生产者实例以提交或回溯消费。

## 10 消费者组（Consumer Group）

同一类Consumer的集合，这类Consumer通常消费同一类消息且消费逻辑一致。消费者组使得在消息消费方面，实现负载均衡和容错的目标变得非常容易。要注意的是，消费者组的消费者实例必须订阅完全相同的Topic。RocketMQ 支持两种消息模式：集群消费（Clustering）和广播消费（Broadcasting）。

## 11 集群消费（Clustering）

集群消费模式下，相同Consumer Group的每个Consumer实例平均分摊消息。

## 12 广播消费（Broadcasting）

广播消费模式下，相同Consumer Group的每个Consumer实例都接收全量的消息。

## 13 普通顺序消息（Normal Ordered Message）

普通顺序消费模式下，消费者通过同一个消息队列（ Topic 分区，称作 Message Queue） 收到的消息是有顺序的，不同消息队列收到的消息则可能是无顺序的。

## 14 严格顺序消息（Strictly Ordered Message）

严格顺序消息模式下，消费者收到的所有消息均是有顺序的。

## 15 消息（Message）

消息系统所传输信息的物理载体，生产和消费数据的最小单位，每条消息必须属于一个主题。RocketMQ中每个消息拥有唯一的Message ID，且可以携带具有业务标识的Key。系统提供了通过Message ID和Key查询消息的功能。

## 16 标签（Tag）

为消息设置的标志，用于同一主题下区分不同类型的消息。来自同一业务单元的消息，可以根据不同业务目的在同一主题下设置不同标签。标签能够有效地保持代码的清晰度和连贯性，并优化RocketMQ提供的查询系统。消费者可以根据Tag实现对不同子主题的不同消费逻辑，实现更好的扩展性。

# 2.特性

## 2 消息顺序

消息有序指的是一类消息消费时，能按照发送的顺序来消费。例如：一个订单产生了三条消息分别是订单创建、订单付款、订单完成。消费时要按照这个顺序消费才能有意义，但是同时订单之间是可以并行消费的。RocketMQ可以严格的保证消息有序。

顺序消息分为全局顺序消息与分区顺序消息，全局顺序是指某个Topic下的所有消息都要保证顺序；部分顺序消息只要保证每一组消息被顺序消费即可。

- 全局顺序 对于指定的一个 Topic，所有消息按照严格的先入先出（FIFO）的顺序进行发布和消费。 适用场景：性能要求不高，所有的消息严格按照 FIFO 原则进行消息发布和消费的场景
- 分区顺序 对于指定的一个 Topic，所有消息根据 sharding key 进行区块分区。 同一个分区内的消息按照严格的 FIFO 顺序进行发布和消费。 Sharding key 是顺序消息中用来区分不同分区的关键字段，和普通消息的 Key 是完全不同的概念。 适用场景：性能要求高，以 sharding key 作为分区字段，在同一个区块中严格的按照 FIFO 原则进行消息发布和消费的场景。

## 3 消息过滤

RocketMQ的消费者可以根据Tag进行消息过滤，也支持自定义属性过滤。消息过滤目前是在Broker端实现的，优点是减少了对于Consumer无用消息的网络传输，缺点是增加了Broker的负担、而且实现相对复杂。

## 4 消息可靠性

RocketMQ支持消息的高可靠，影响消息可靠性的几种情况：

1. Broker非正常关闭
2. Broker异常Crash
3. OS Crash
4. 机器掉电，但是能立即恢复供电情况
5. 机器无法开机（可能是cpu、主板、内存等关键设备损坏）
6. 磁盘设备损坏

1)、2)、3)、4) 四种情况都属于硬件资源可立即恢复情况，RocketMQ在这四种情况下能保证消息不丢，或者丢失少量数据（依赖刷盘方式是同步还是异步）。

5)、6)属于单点故障，且无法恢复，一旦发生，在此单点上的消息全部丢失。RocketMQ在这两种情况下，通过异步复制，可保证99%的消息不丢，但是仍然会有极少量的消息可能丢失。通过同步双写技术可以完全避免单点，同步双写势必会影响性能，适合对消息可靠性要求极高的场合，例如与Money相关的应用。注：RocketMQ从3.0版本开始支持同步双写。

## 5 至少一次

至少一次(At least Once)指每个消息必须投递一次。Consumer先Pull消息到本地，消费完成后，才向服务器返回ack，如果没有消费一定不会ack消息，所以RocketMQ可以很好的支持此特性。

## 6 回溯消费

回溯消费是指Consumer已经消费成功的消息，由于业务上需求需要重新消费，要支持此功能，Broker在向Consumer投递成功消息后，消息仍然需要保留。并且重新消费一般是按照时间维度，例如由于Consumer系统故障，恢复后需要重新消费1小时前的数据，那么Broker要提供一种机制，可以按照时间维度来回退消费进度。RocketMQ支持按照时间回溯消费，时间维度精确到毫秒。

## 7 事务消息

RocketMQ事务消息（Transactional Message）是指应用本地事务和发送消息操作可以被定义到全局事务中，要么同时成功，要么同时失败。RocketMQ的事务消息提供类似 X/Open XA 的分布事务功能，通过事务消息能达到分布式事务的最终一致。

## 8 定时消息

定时消息（延迟队列）是指消息发送到broker后，不会立即被消费，等待特定时间投递给真正的topic。 broker有配置项messageDelayLevel，默认值为“1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h”，18个level。可以配置自定义messageDelayLevel。注意，messageDelayLevel是broker的属性，不属于某个topic。发消息时，设置delayLevel等级即可：msg.setDelayLevel(level)。level有以下三种情况：

- level == 0，消息为非延迟消息
- 1<=level<=maxLevel，消息延迟特定时间，例如level==1，延迟1s
- level > maxLevel，则level== maxLevel，例如level==20，延迟2h

定时消息会暂存在名为SCHEDULE_TOPIC_XXXX的topic中，并根据delayTimeLevel存入特定的queue，queueId = delayTimeLevel – 1，即一个queue只存相同延迟的消息，保证具有相同发送延迟的消息能够顺序消费。broker会调度地消费SCHEDULE_TOPIC_XXXX，将消息写入真实的topic。

需要注意的是，定时消息会在第一次写入和调度写入真实topic时都会计数，因此发送数量、tps都会变高。

## 9 消息重试

Consumer消费消息失败后，要提供一种重试机制，令消息再消费一次。Consumer消费消息失败通常可以认为有以下几种情况：

- 由于消息本身的原因，例如反序列化失败，消息数据本身无法处理（例如话费充值，当前消息的手机号被注销，无法充值）等。这种错误通常需要跳过这条消息，再消费其它消息，而这条失败的消息即使立刻重试消费，99%也不成功，所以最好提供一种定时重试机制，即过10秒后再重试。
- 由于依赖的下游应用服务不可用，例如db连接不可用，外系统网络不可达等。遇到这种错误，即使跳过当前失败的消息，消费其他消息同样也会报错。这种情况建议应用sleep 30s，再消费下一条消息，这样可以减轻Broker重试消息的压力。

RocketMQ会为每个消费组都设置一个Topic名称为“%RETRY%+consumerGroup”的重试队列（这里需要注意的是，这个Topic的重试队列是针对消费组，而不是针对每个Topic设置的），用于暂时保存因为各种异常而导致Consumer端无法消费的消息。考虑到异常恢复起来需要一些时间，会为重试队列设置多个重试级别，每个重试级别都有与之对应的重新投递延时，重试次数越多投递延时就越大。RocketMQ对于重试消息的处理是先保存至Topic名称为“SCHEDULE_TOPIC_XXXX”的延迟队列中，后台定时任务按照对应的时间进行Delay后重新保存至“%RETRY%+consumerGroup”的重试队列中。

## 10 消息重投

生产者在发送消息时，同步消息失败会重投，异步消息有重试，oneway没有任何保证。消息重投保证消息尽可能发送成功、不丢失，但可能会造成消息重复，消息重复在RocketMQ中是无法避免的问题。消息重复在一般情况下不会发生，当出现消息量大、网络抖动，消息重复就会是大概率事件。另外，生产者主动重发、consumer负载变化也会导致重复消息。如下方法可以设置消息重试策略：

- retryTimesWhenSendFailed：同步发送失败重投次数，默认为2，因此生产者会最多尝试发送retryTimesWhenSendFailed + 1次。不会选择上次失败的broker，尝试向其他broker发送，最大程度保证消息不丢。超过重投次数，抛出异常，由客户端保证消息不丢。当出现RemotingException、MQClientException和部分MQBrokerException时会重投。
- retryTimesWhenSendAsyncFailed：异步发送失败重试次数，异步重试不会选择其他broker，仅在同一个broker上做重试，不保证消息不丢。
- retryAnotherBrokerWhenNotStoreOK：消息刷盘（主或备）超时或slave不可用（返回状态非SEND_OK），是否尝试发送到其他broker，默认false。十分重要消息可以开启。

## 11 流量控制

生产者流控，因为broker处理能力达到瓶颈；消费者流控，因为消费能力达到瓶颈。

生产者流控：

- commitLog文件被锁时间超过osPageCacheBusyTimeOutMills时，参数默认为1000ms，返回流控。
- 如果开启transientStorePoolEnable == true，且broker为异步刷盘的主机，且transientStorePool中资源不足，拒绝当前send请求，返回流控。
- broker每隔10ms检查send请求队列头部请求的等待时间，如果超过waitTimeMillsInSendQueue，默认200ms，拒绝当前send请求，返回流控。
- broker通过拒绝send 请求方式实现流量控制。

注意，生产者流控，不会尝试消息重投。

消费者流控：

- 消费者本地缓存消息数超过pullThresholdForQueue时，默认1000。
- 消费者本地缓存消息大小超过pullThresholdSizeForQueue时，默认100MB。
- 消费者本地缓存消息跨度超过consumeConcurrentlyMaxSpan时，默认2000。

消费者流控的结果是降低拉取频率。

## 12 死信队列

死信队列用于处理无法被正常消费的消息。当一条消息初次消费失败，消息队列会自动进行消息重试；达到最大重试次数后，若消费依然失败，则表明消费者在正常情况下无法正确地消费该消息，此时，消息队列 不会立刻将消息丢弃，而是将其发送到该消费者对应的特殊队列中。

RocketMQ将这种正常情况下无法被消费的消息称为死信消息（Dead-Letter Message），将存储死信消息的特殊队列称为死信队列（Dead-Letter Queue）。在RocketMQ中，可以通过使用console控制台对死信队列中的消息进行重发来使得消费者实例再次进行消费。

# 3.  RocketMQ-Spring概述

[RocketMQ-Spring](https://github.com/apache/rocketmq-spring) 项目，RocketMQ 对 Spring 的集成支持。主要有两方面的功能：

- 功能一：支持 Spring Message 规范，方便开发者从其它 MQ 快速切换到 RocketMQ 。
- 功能二：帮助开发者在 [Spring Boot](http://projects.spring.io/spring-boot/) 中快速集成 [RocketMQ](http://rocketmq.apache.org/) 。

# 4. 快速入门

## 4.1 引入依赖

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
<dependencies>
    <!-- 实现对 RocketMQ 的自动化配置 -->
    <dependency>
        <groupId>org.apache.rocketmq</groupId>
        <artifactId>rocketmq-spring-boot-starter</artifactId>
        <version>2.0.4</version>
    </dependency>

    <!-- 方便等会写单元测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 4.2 应用配置文件

 ```yaml
 # rocketmq 配置项，对应 RocketMQProperties 配置类
 rocketmq:
   name-server: 127.0.0.1:9876 # RocketMQ Namesrv
   # Producer 配置项
   producer:
     group: demo-producer-group # 生产者分组
     send-message-timeout: 3000 # 发送消息超时时间，单位：毫秒。默认为 3000 。
     compress-message-body-threshold: 4096 # 消息压缩阀值，当消息体的大小超过该阀值后，进行消息压缩。默认为 4 * 1024B
     max-message-size: 4194304 # 消息体的最大允许大小。。默认为 4 * 1024 * 1024B
     retry-times-when-send-failed: 2 # 同步发送消息时，失败重试次数。默认为 2 次。
     retry-times-when-send-async-failed: 2 # 异步发送消息时，失败重试次数。默认为 2 次。
     retry-next-server: false # 发送消息给 Broker 时，如果发送失败，是否重试另外一台 Broker 。默认为 false
     access-key: # Access Key ，可阅读 https://github.com/apache/rocketmq/blob/master/docs/cn/acl/user_guide.md 文档
     secret-key: # Secret Key
     enable-msg-trace: true # 是否开启消息轨迹功能。默认为 true 开启。可阅读 https://github.com/apache/rocketmq/blob/master/docs/cn/msg_trace/user_guide.md 文档
     customized-trace-topic: RMQ_SYS_TRACE_TOPIC # 自定义消息轨迹的 Topic 。默认为 RMQ_SYS_TRACE_TOPIC 。
   # Consumer 配置项
   consumer:
     listeners: # 配置某个消费分组，是否监听指定 Topic 。结构为 Map<消费者分组, <Topic, Boolean>> 。默认情况下，不配置表示监听。一般情况下，只有我们在想不监听消费某个消费分组的某个 Topic 时，才需要配 listener 配置。
       test-consumer-group:
         topic1: false # 关闭 test-consumer-group 对 topic1 的监听消费
 ```

 ## 4.3 Application

```java
// Application.java

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

## 4.4 Demo01Message

```
//消息类 
public class Demo01Message {

    public static final String TOPIC = "DEMO_01";

    /**
     * 编号
     */
    private Integer id;

    // ... 省略 set/get/toString 方法

}
```

## 4.5 Demo01Producer

```java
// Demo01Producer.java
//生产类
@Component
public class Demo01Producer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public SendResult syncSend(Integer id) {
        // 创建 Demo01Message 消息
        Demo01Message message = new Demo01Message();
        message.setId(id);
        // 同步发送消息
        return rocketMQTemplate.syncSend(Demo01Message.TOPIC, message);
    }

    public void asyncSend(Integer id, SendCallback callback) {
        // 创建 Demo01Message 消息
        Demo01Message message = new Demo01Message();
        message.setId(id);
        // 异步发送消息
        rocketMQTemplate.asyncSend(Demo01Message.TOPIC, message, callback);
    }

    public void onewaySend(Integer id) {
        // 创建 Demo01Message 消息
        Demo01Message message = new Demo01Message();
        message.setId(id);
        // oneway 发送消息
        rocketMQTemplate.sendOneWay(Demo01Message.TOPIC, message);
    }

}
我们来简单聊下 RocketMQTemplate 类，它继承 Spring Messaging 定义的 AbstractMessageSendingTemplate 抽象类，以达到融入 Spring Messaging 体系中。
```

## 4.6 Demo01Consumer

```java
// Demo01Consumer.java
消费者类
@Component
@RocketMQMessageListener(
        topic = Demo01Message.TOPIC,
        consumerGroup = "demo01-consumer-group-" + Demo01Message.TOPIC
)
public class Demo01Consumer implements RocketMQListener<Demo01Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Demo01Message message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
    }

}
在类上，添加了 @RocketMQMessageListener 注解，声明消费的 Topic 是 "DEMO_01" ，消费者分组是 "demo01-consumer-group-DEMO_01" 。一般情况下，我们建议一个消费者分组，仅消费一个 Topic 。这样做会有两个好处：
每个消费者分组职责单一，只消费一个 Topic 。
每个消费者分组是独占一个线程池，这样能够保证多个 Topic 隔离在不同线程池，保证隔离性，从而避免一个 Topic 消费很慢，影响到另外的 Topic 的消费。
实现 RocketMQListener 接口，在 T 泛型里，设置消费的消息对应的类。此处，我们就设置了 Demo01Message 类。
```

## 4.7 Demo01AConsumer

```java
@Component
@RocketMQMessageListener(
        topic = Demo01Message.TOPIC,
        consumerGroup = "demo01-A-consumer-group-" + Demo01Message.TOPIC
)
public class Demo01AConsumer implements RocketMQListener<MessageExt> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(MessageExt message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
    }

}
```

**差异一**，在类上，添加了 [`@RocketMQMessageListener`](https://github.com/apache/rocketmq-spring/blob/master/rocketmq-spring-boot/src/main/java/org/apache/rocketmq/spring/annotation/RocketMQMessageListener.java) 注解，声明消费的 Topic **还是** `"DEMO_01"` ，消费者分组修**改成**了 `"demo01-A-consumer-group-DEMO_01"` 。这样，我们就可以测试 RocketMQ 集群消费的特性。

> 集群消费（Clustering）：集群消费模式下，相同 Consumer Group 的每个 Consumer 实例平均分摊消息。

- 也就是说，如果我们发送一条 Topic 为 `"DEMO_01"` 的消息，可以分别被 `"demo01-A-consumer-group-DEMO_01"` 和 `"demo01-consumer-group-DEMO_01"` 都消费一次。
- 但是，如果我们启动两个该示例的实例，则消费者分组 `"demo01-A-consumer-group-DEMO_01"` 和 `"demo01-consumer-group-DEMO_01"` 都会有多个 Consumer 示例。此时，我们再发送一条 Topic 为 `"DEMO_01"` 的消息，只会被 `"demo01-A-consumer-group-DEMO_01"` 的其中一个Consumer 消费一次，也同样只会被 `"demo01-A-consumer-group-DEMO_01"` 的一个 Consumer 消费一次。

**总结就是同一个Topic 会发给每一个不同的consumerGroup，但是相同的consumerGroup只有其中一个会接收到**

通过**集群消费**的机制，我们可以实现针对相同 Topic ，不同消费者分组实现各自的业务逻辑。例如说：用户注册成功时，发送一条 Topic 为 `"USER_REGISTER"` 的消息。然后，不同模块使用不同的消费者分组，订阅该 Topic ，实现各自的拓展逻辑：

- 积分模块：判断如果是手机注册，给用户增加 20 积分。
- 优惠劵模块：因为是新用户，所以发放新用户专享优惠劵。
- 站内信模块：因为是新用户，所以发送新用户的欢迎语的站内信。
- ... 等等

这样，我们就可以将注册成功后的业务拓展逻辑，实现业务上的解耦，未来也更加容易拓展。同时，也提高了注册接口的性能，避免用户需要等待业务拓展逻辑执行完成后，才响应注册成功。

**差异二**，实现 RocketMQListener 接口，在 `T` 泛型里，设置消费的消息对应的类不是 Demo01Message 类，而是 RocketMQ 内置的 [MessageExt](https://github.com/apache/rocketmq/blob/master/common/src/main/java/org/apache/rocketmq/common/message/MessageExt.java) 类。通过 MessageExt 类，我们可以获取到消费的消息的更多信息，例如说消息的所属队列、创建时间等等属性，不过消息的内容(`body`)就需要自己去反序列化。当然，一般情况下，我们不会使用 MessageExt 类。

## 4.8 简单测试

```java
// Demo01ProducerTest.java

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Demo01ProducerTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Demo01Producer producer;

    @Test
    public void testSyncSend() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        SendResult result = producer.syncSend(id);
        logger.info("[testSyncSend][发送编号：[{}] 发送结果：[{}]]", id, result);

        // 阻塞等待，保证消费
        new CountDownLatch(1).await();
    }

    @Test
    public void testASyncSend() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        producer.asyncSend(id, new SendCallback() {

            @Override
            public void onSuccess(SendResult result) {
                logger.info("[testASyncSend][发送编号：[{}] 发送成功，结果为：[{}]]", id, result);
            }

            @Override
            public void onException(Throwable e) {
                logger.info("[testASyncSend][发送编号：[{}] 发送异常]]", id, e);
            }

        });

        // 阻塞等待，保证消费
        new CountDownLatch(1).await();
    }

    @Test
    public void testOnewaySend() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        producer.onewaySend(id);
        logger.info("[testOnewaySend][发送编号：[{}] 发送完成]", id);

        // 阻塞等待，保证消费
        new CountDownLatch(1).await();
    }

}
```

我们来执行 `#testSyncSend()` 方法，测试同步发送消息。控制台输出如下：



```
# Producer 同步发送消息成功
2019-12-05 13:48:57.342  INFO 79342 --- [           main] c.i.s.l.r.producer.Demo01ProducerTest    : [testSyncSend][发送编号：[1575438537] 发送结果：[SendResult [sendStatus=SEND_OK, msgId=C0A8032C35EE18B4AAC2126A02770000, offsetMsgId=C0A8032C00002A9F000000000010E628, messageQueue=MessageQueue [topic=DEMO_01, brokerName=broker-a, queueId=0], queueOffset=255]]]

# Demo01AConsumer 消费了一次该消息
2019-12-05 13:48:57.347  INFO 79342 --- [MessageThread_1] c.i.s.l.r.consumer.Demo01AConsumer       : [onMessage][线程编号:45 消息内容：MessageExt [queueId=0, storeSize=284, queueOffset=255, sysFlag=0, bornTimestamp=1575438537338, bornHost=/192.168.3.44:57823, storeTimestamp=1575438537340, storeHost=/192.168.3.44:10911, msgId=C0A8032C00002A9F000000000010E628, commitLogOffset=1107496, bodyCRC=1962202087, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='DEMO_01', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=256, CONSUME_START_TIME=1575438537347, id=b0e72a1c-cb11-5152-7d0d-c034b118a3e5, UNIQ_KEY=C0A8032C35EE18B4AAC2126A02770000, CLUSTER=DefaultCluster, WAIT=false, contentType=application/json, timestamp=1575438537333}, body=[123, 34, 105, 100, 34, 58, 49, 53, 55, 53, 52, 51, 56, 53, 51, 55, 125], transactionId='null'}]]

# Demo01Consumer 消费了一次该消息
2019-12-05 13:49:00.150  INFO 79342 --- [MessageThread_1] c.i.s.l.r.consumer.Demo01Consumer        : [onMessage][线程编号:51 消息内容：Demo01Message{id=1575438537}]
```



- 通过日志我们可以看到，我们发送的消息，分别被 Demo01AConsumer 和 Demo01Consumer 两个消费者（消费者分组）都消费了一次。
- 同时，两个消费者在不同的线程池中，消费了这条消息。虽然说，我们看到两条日志里，我们都看到了线程名为 `"MessageThread_1"` ，但是线程编号分别是 45 和 51 。😈 因为，每个 RocketMQ Consumer 的消费线程池创建的线程都是以 `"MessageThread_"` 开头，同时这里相同的线程名结果不同的线程编号，很容易判断出时候用了两个不同的消费线程池。

我们来执行 `#testASyncSend()` 方法，测试异步发送消息。控制台输出如下：

> 友情提示：注意，不要关闭 `#testSyncSend()` 单元测试方法，因为我们要模拟每个消费者集群，都有多个 Consumer 节点。



```
// Producer 异步发送消息成功
2019-12-05 13:56:34.366  INFO 79642 --- [ublicExecutor_4] c.i.s.l.r.producer.Demo01ProducerTest    : [testASyncSend][发送编号：[1575438994] 发送成功，结果为：[SendResult [sendStatus=SEND_OK, msgId=C0A8032C371A18B4AAC21270FBB70000, offsetMsgId=C0A8032C00002A9F000000000010E8CA, messageQueue=MessageQueue [topic=DEMO_01, brokerName=broker-a, queueId=3], queueOffset=256]]]

# Demo01AConsumer 消费了一次该消息
2019-12-05 13:56:34.370  INFO 79642 --- [MessageThread_1] c.i.s.l.r.consumer.Demo01AConsumer       : [onMessage][线程编号:47 消息内容：MessageExt [queueId=3, storeSize=284, queueOffset=256, sysFlag=0, bornTimestamp=1575438994361, bornHost=/192.168.3.44:57926, storeTimestamp=1575438994364, storeHost=/192.168.3.44:10911, msgId=C0A8032C00002A9F000000000010E8CA, commitLogOffset=1108170, bodyCRC=412662346, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='DEMO_01', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=257, CONSUME_START_TIME=1575438994370, id=80b9f381-febe-6cda-02e7-43bf8f8a5c8a, UNIQ_KEY=C0A8032C371A18B4AAC21270FBB70000, CLUSTER=DefaultCluster, WAIT=false, contentType=application/json, timestamp=1575438994356}, body=[123, 34, 105, 100, 34, 58, 49, 53, 55, 53, 52, 51, 56, 57, 57, 52, 125], transactionId='null'}]]

# Demo01Consumer 消费了一次该消息
2019-12-05 13:56:34.402  INFO 79642 --- [MessageThread_1] c.i.s.l.r.consumer.Demo01Consumer        : [onMessage][线程编号:46 消息内容：Demo01Message{id=1575438994}]
```



- 和 `#testSyncSend()` 方法执行的结果，是一致的。此时，我们打开 `#testSyncSend()` 方法所在的控制台，不会看到有消息消费的日志。说明，符合集群消费的机制：**集群消费模式下，相同 Consumer Group 的每个 Consumer 实例平均分摊消息。**。
- 😈 不过如上的日志，也可能出现在 `#testSyncSend()` 方法所在的控制台，而不在 `#testASyncSend()` 方法所在的控制台。

`#testOnewaySend()` 方法，胖友自己执行，比较简单。

## 3.9 RocketMQMessageListener

`@RocketMQMessageListener` 注解的**常用**属性如下：

```java
/**
 * Consumer 所属消费者分组
 *
 * Consumers of the same role is required to have exactly same subscriptions and consumerGroup to correctly achieve
 * load balance. It's required and needs to be globally unique.
 *
 * See <a href="http://rocketmq.apache.org/docs/core-concept/">here</a> for further discussion.
 */
String consumerGroup();

/**
 * 消费的 Topic
 *
 * Topic name.
 */
String topic();

/**
 * 选择器类型。默认基于 Message 的 Tag 选择。
 *
 * Control how to selector message.
 *
 * @see SelectorType
 */
SelectorType selectorType() default SelectorType.TAG;
/**
 * 选择器的表达式。
 * 设置为 * 时，表示全部。
 *
 * 如果使用 SelectorType.TAG 类型，则设置消费 Message 的具体 Tag 。
 * 如果使用 SelectorType.SQL92 类型，可见 https://rocketmq.apache.org/rocketmq/filter-messages-by-sql92-in-rocketmq/ 文档
 *
 * Control which message can be select. Grammar please see {@link SelectorType#TAG} and {@link SelectorType#SQL92}
 */
String selectorExpression() default "*";

/**
 * 消费模式。可选择并发消费，还是顺序消费。
 *
 * Control consume mode, you can choice receive message concurrently or orderly.
 */
ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

/**
 * 消息模型。可选择是集群消费，还是广播消费。
 *
 * Control message mode, if you want all subscribers receive message all message, broadcasting is a good choice.
 */
MessageModel messageModel() default MessageModel.CLUSTERING;

/**
 * 消费的线程池的最大线程数
 *
 * Max consumer thread number.
 */
int consumeThreadMax() default 64;

/**
 * 消费单条消息的超时时间
 *
 * Max consumer timeout, default 30s.
 */
long consumeTimeout() default 30000L;
```



`@RocketMQMessageListener` 注解的**不常用**属性如下：



```java
// 默认从配置文件读取的占位符
String NAME_SERVER_PLACEHOLDER = "${rocketmq.name-server:}";
String ACCESS_KEY_PLACEHOLDER = "${rocketmq.consumer.access-key:}";
String SECRET_KEY_PLACEHOLDER = "${rocketmq.consumer.secret-key:}";
String TRACE_TOPIC_PLACEHOLDER = "${rocketmq.consumer.customized-trace-topic:}";
String ACCESS_CHANNEL_PLACEHOLDER = "${rocketmq.access-channel:}";

/**
 * The property of "access-key".
 */
 String accessKey() default ACCESS_KEY_PLACEHOLDER;
 /**
 * The property of "secret-key".
 */
String secretKey() default SECRET_KEY_PLACEHOLDER;

/**
 * Switch flag instance for message trace.
 */
boolean enableMsgTrace() default true;
/**
 * The name value of message trace topic.If you don't config,you can use the default trace topic name.
 */
String customizedTraceTopic() default TRACE_TOPIC_PLACEHOLDER;

/**
 * Consumer 连接的 RocketMQ Namesrv 地址。默认情况下，使用 `rocketmq.name-server` 配置项即可。
 *
 * 如果一个项目中，Consumer 需要使用不同的 RocketMQ Namesrv ，则需要配置该属性。
 *
 * The property of "name-server".
 */
String nameServer() default NAME_SERVER_PLACEHOLDER;

/**
 * 访问通道。目前有 LOCAL 和 CLOUD 两种通道。
 *
 * LOCAL ，指的是本地部署的 RocketMQ 开源项目。
 * CLOUD ，指的是阿里云的 ONS 服务。具体可见 https://help.aliyun.com/document_detail/128585.html 文档。
 *
 * The property of "access-channel".
 */
String accessChannel() default ACCESS_CHANNEL_PLACEHOLDER;
```

## 3.10 ExtRocketMQTemplateConfiguration

RocketMQ-Spring 考虑到开发者可能需要连接多个不同的 RocketMQ 集群，所以提供了 [`@ExtRocketMQTemplateConfiguration`](https://github.com/apache/rocketmq-spring/blob/master/rocketmq-spring-boot/src/main/java/org/apache/rocketmq/spring/annotation/ExtRocketMQTemplateConfiguration.java) 注解，实现配置连接不同 RocketMQ 集群的 Producer 的 RocketMQTemplate Bean 对象。

`@ExtRocketMQTemplateConfiguration` 注解的具体属性，和我们在 [「3.2 应用配置文件」](https://www.iocoder.cn/Spring-Boot/RocketMQ/#) 的 `rocketmq.producer` 配置项是一致的，就不重复赘述啦。

`@ExtRocketMQTemplateConfiguration` 注解的简单使用示例，代码如下：



```
@ExtRocketMQTemplateConfiguration(nameServer = "${demo.rocketmq.extNameServer:demo.rocketmq.name-server}")
public class ExtRocketMQTemplate extends RocketMQTemplate {
}
```



- 在类上，添加 `@ExtRocketMQTemplateConfiguration` 注解，并设置连接的 RocketMQ Namesrv 地址。
- 同时，需要继承 RocketMQTemplate 类，从而使我们可以直接使用 `@Autowire` 或 `@Resource` 注解，注入 RocketMQTemplate Bean 属性。

# 5. 批量发送信息

```java
public <T extends Message> SendResult syncSend(String destination, Collection<T> messages, long timeout) {
    // ... 省略具体代码实现
}
通过方法参数 destination 可知，必须发送相同 Topic 的消息。
要注意方法参数 messages ，每个集合的元素必须是 Spring Messaging 定义的 Message 消息。😈 RocketMQTemplate 重载了非常多的 #syncSend(...) 方法，一定要小心哟。
timeout 超时时间
通过方法名可知，这个是同步批量发送消息。
```

有一点要注意，虽然是批量发送多条消息，但是是以所有消息加起来的大小，不能超过消息的最大大小的限制，而不是按照单条计算。😈 所以，一次性发送的消息特别多，还是需要**分批的**进行批量发送。

测试代码:

```
 public SendResult sendBatch(Collection<Integer> ids) {
        // <X> 创建多条 Demo02Message 消息
        List<Message> messages = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            // 创建 Demo02Message 消息
            Demo02Message message = new Demo02Message().setId(id);
            // 构建 Spring Messaging 定义的 Message 消息
            messages.add(MessageBuilder.withPayload(message).build());
        }
        // 同步批量发送消息
        return rocketMQTemplate.syncSend(Demo02Message.TOPIC, messages, 30 * 1000L);
    }
```

接受到就还是多条

# 6. 定时信息

在 RocketMQ 中，提供定时消息的功能。

> **定时消息**，是指消息发到 Broker 后，不能立刻被 Consumer 消费，要到特定的时间点或者等待特定的时间后才能被消费。

不过，RocketMQ 暂时不支持任意的时间精度的延迟，而是固化了 18 个延迟级别。如下表格：

| 延迟级别 | 时间 | 延迟级别 | 时间 | 延迟级别 | 时间 |
| :------- | :--- | :------- | :--- | :------- | :--- |
| 1        | 1s   | 7        | 3m   | 13       | 9m   |
| 2        | 5s   | 8        | 4m   | 14       | 10m  |
| 3        | 10s  | 9        | 5m   | 15       | 20m  |
| 4        | 30s  | 10       | 6m   | 16       | 30m  |
| 5        | 1m   | 11       | 7m   | 17       | 1h   |
| 6        | 2m   | 12       | 8m   | 18       | 2h   |

代码

```java
    public SendResult syncSendDelay(Integer id, int delayLevel) {
        // 创建 Demo03Message 消息
        Message message = MessageBuilder.withPayload(new Demo03Message().setId(id))
                .build();
        // 同步发送消息
        return rocketMQTemplate.syncSend(Demo03Message.TOPIC, message, 30 * 1000,
                delayLevel);
    }

    public void asyncSendDelay(Integer id, int delayLevel, SendCallback callback) {
        // 创建 Demo03Message 消息
        Message message = MessageBuilder.withPayload(new Demo03Message().setId(id))
                .build();
        // 同步发送消息
        rocketMQTemplate.asyncSend(Demo03Message.TOPIC, message, callback, 30 * 1000,
                delayLevel);
    }
```

目前支持同步和异步

# 7. 消息重试

RocketMQ 提供**消费重试**的机制。在消息**消费失败**的时候，RocketMQ 会通过**消费重试**机制，重新投递该消息给 Consumer ，让 Consumer 有机会重新消费消息，实现消费成功。

当然，RocketMQ 并不会无限重新投递消息给 Consumer 重新消费，而是在默认情况下，达到 16 次重试次数时，Consumer 还是消费失败时，该消息就会进入到**死信队列**。

> 死信队列用于处理无法被正常消费的消息。当一条消息初次消费失败，消息队列会自动进行消息重试；达到最大重试次数后，若消费依然失败，则表明消费者在正常情况下无法正确地消费该消息，此时，消息队列不会立刻将消息丢弃，而是将其发送到该消费者对应的特殊队列中。
>
> RocketMQ 将这种正常情况下无法被消费的消息称为死信消息（Dead-Letter Message），将存储死信消息的特殊队列称为死信队列（Dead-Letter Queue）。在 RocketMQ 中，可以通过使用 console 控制台对死信队列中的消息进行重发来使得消费者实例再次进行消费。

每条消息的失败重试，是有一定的间隔时间。实际上，消费重试是基于[「5. 定时消息」](https://www.iocoder.cn/Spring-Boot/RocketMQ/#) 来实现，第一次重试消费按照延迟级别为 **3** 开始。😈 所以，默认为 16 次重试消费，也非常好理解，毕竟延迟级别最高为 18 呀。

不过要注意，只有**集群消费**模式下，才有消息重试。

**测试方法 在接受处抛出异常**

```
@Component
@RocketMQMessageListener(
        topic = Demo04Message.TOPIC,
        consumerGroup = "demo04-consumer-group-" + Demo04Message.TOPIC
)
public class Demo04Consumer implements RocketMQListener<Demo04Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Demo04Message message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
        // <X> 注意，此处抛出一个 RuntimeException 异常，模拟消费失败
        throw new RuntimeException("我就是故意抛出一个异常");
    }

}
```

# 8. 广播消费

在上述的示例中，我们看到的都是使用集群消费。而在一些场景下，我们需要使用**广播消费**。

> 广播消费模式下，相同 Consumer Group 的每个 Consumer 实例都接收全量的消息。

例如说，在应用中，缓存了数据字典等配置表在内存中，可以通过 RocketMQ 广播消费，实现每个应用节点都消费消息，刷新本地内存的缓存。

**代码 与其他唯一不同就是增加了messageModel = MessageModel.BROADCASTING**

```java
@Component
@RocketMQMessageListener(
        topic = Demo05Message.TOPIC,
        consumerGroup = "demo05-consumer-group-" + Demo05Message.TOPIC,
        messageModel = MessageModel.BROADCASTING // 设置为广播消费
)
public class Demo05Consumer implements RocketMQListener<Demo05Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Demo05Message message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
    }

}
```

**测试结果 相同consumerGroup下的所有consumer都会收到，与集群不同，他在相同的consumerGroup下只有其中一个consumer会收到**

# 9 顺序消息

https://blog.csdn.net/weixin_43767015/article/details/121028059 可以查看这篇博客 将的rocket消息

- 普通顺序消费模式下，消费者通过同一个消费队列收到的消息是有顺序的，不同消息队列收到的消息则可能是无顺序的。
- 严格顺序消息模式下，消费者收到的所有消息均是有顺序的。

生产者

```java
 public SendResult syncSendOrderly(Integer id) {
        // 创建 Demo06Message 消息
        Demo06Message message = new Demo06Message();
        message.setId(id);
        // 同步发送消息
        return rocketMQTemplate.syncSendOrderly(Demo06Message.TOPIC, message, String.valueOf(id));
    }

    public void asyncSendOrderly(Integer id, SendCallback callback) {
        // 创建 Demo06Message 消息
        Demo06Message message = new Demo06Message();
        message.setId(id);
        // 异步发送消息
        rocketMQTemplate.asyncSendOrderly(Demo06Message.TOPIC, message, String.valueOf(id), callback);
    }

    public void onewaySendOrderly(Integer id) {
        // 创建 Demo06Message 消息
        Demo06Message message = new Demo06Message();
        message.setId(id);
        // 异步发送消息
        rocketMQTemplate.sendOneWayOrderly(Demo06Message.TOPIC, message, String.valueOf(id));
    }

```

消费者

```java
在RocketMQMessageListener 里面添加  consumeMode = ConsumeMode.ORDERLY
@Component
@RocketMQMessageListener 里面添加(
        topic = Demo06Message.TOPIC,
        consumerGroup = "demo06-consumer-group-" + Demo06Message.TOPIC,
        consumeMode = ConsumeMode.ORDERLY // 设置为顺序消费
)
public class Demo06Consumer implements RocketMQListener<Demo06Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Demo06Message message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);

        // sleep 2 秒，用于查看顺序消费的效果
        try {
            Thread.sleep(2 * 1000L);
        } catch (InterruptedException ignore) {
        }
    }

}
```

# 10 事务信息

``Producer ``

```java
// Demo07Producer.java

@Component
public class Demo07Producer {

    private static final String TX_PRODUCER_GROUP = "demo07-producer-group";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public TransactionSendResult sendMessageInTransaction(Integer id) {
        // <1> 创建 Demo07Message 消息
        Message message = MessageBuilder.withPayload(new Demo07Message().setId(id))
                .build();
        // <2> 发送事务消息
        return rocketMQTemplate.sendMessageInTransaction(TX_PRODUCER_GROUP, Demo07Message.TOPIC, message,
                id);
    }

}
```

``TransactionListenerImpl``

```java
// Demo07Producer.java

@RocketMQTransactionListener(txProducerGroup = TX_PRODUCER_GROUP)
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // ... local transaction process, return rollback, commit or unknown
        logger.info("[executeLocalTransaction][执行本地事务，消息：{} arg：{}]", msg, arg);
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // ... check transaction status and return rollback, commit or unknown
        logger.info("[checkLocalTransaction][回查消息：{}]", msg);
        return RocketMQLocalTransactionState.COMMIT;
    }

}
```

- 在类上，添加 `@RocketMQTransactionListener` 注解，声明监听器的是生产者分组是 `"demo07-producer-group"` 的 Producer 发送的事务消息。

- 实现 [RocketMQLocalTransactionListener](https://github.com/apache/rocketmq/blob/master/client/src/main/java/org/apache/rocketmq/client/producer/TransactionListener.java) 接口，实现执行本地事务和检查本地事务的方法。

- 实现

   

  ```
  #executeLocalTransaction(...)
  ```

   

  方法，实现执行本地事务。

  - 注意，这是一个**模板方法**。在调用这个方法之前，RocketMQTemplate 已经使用 Producer 发送了一条事务消息。然后根据该方法执行的返回的 [RocketMQLocalTransactionState](https://github.com/apache/rocketmq-spring/blob/3f89080df8f797cad0d1f9eb8badb5050b09a553/rocketmq-spring-boot/src/main/java/org/apache/rocketmq/spring/core/RocketMQLocalTransactionState.java) 结果，提交还是回滚该事务消息。
  - 这里，我们为了模拟 RocketMQ 回查 Producer 来获得事务消息的状态，所以返回了 `RocketMQLocalTransactionState.UNKNOWN` 未知状态。

- 实现

   

  ```
  #checkLocalTransaction(...)
  ```

   

  方法，检查本地事务。

  - 在事务消息长事件未被提交或回滚时，RocketMQ 会回查事务消息对应的生产者分组下的 Producer ，获得事务消息的状态。此时，该方法就会被调用。
  - 这里，我们直接返回 `RocketMQLocalTransactionState.COMMIT` 提交状态。

一般来说，有两种方式实现本地事务回查时，返回事务消息的状态。

**第一种**，通过 `msg` 消息，获得某个业务上的标识或者编号，然后去数据库中查询业务记录，从而判断该事务消息的状态是提交还是回滚。

**第二种**，记录 `msg` 的事务编号，与事务状态到数据库中。

- 第一步，在 `#executeLocalTransaction(...)` 方法中，先存储一条 `id` 为 `msg` 的事务编号，状态为 `RocketMQLocalTransactionState.UNKNOWN` 的记录。
- 第二步，调用带有**事务的**业务 Service 的方法。在该 Service 方法中，在逻辑都执行成功的情况下，更新 `id` 为 `msg` 的事务编号，状态变更为 `RocketMQLocalTransactionState.COMMIT` 。这样，我们就可以伴随这个事务的提交，更新 `id` 为 `msg` 的事务编号的记录的状为 `RocketMQLocalTransactionState.COMMIT` ，美滋滋。。
- 第三步，要以 `try-catch` 的方式，调用业务 Service 的方法。如此，如果发生异常，回滚事务的时候，可以在 `catch` 中，更新 `id` 为 `msg` 的事务编号的记录的状态为 `RocketMQLocalTransactionState.ROLLBACK` 。😭 极端情况下，可能更新失败，则打印 error 日志，告警知道，人工介入。
- 如此三步之后，我们在 `#executeLocalTransaction(...)` 方法中，就可以通过查找数据库，`id` 为 `msg` 的事务编号的记录的状态，然后返回。

**相比来说**，实现更加简单通用，对于业务开发者，更加友好。和有几个朋友沟通了下，他们也是采用第二种。

``Consumer``

```
@Component
@RocketMQMessageListener(
        topic = Demo07Message.TOPIC,
        consumerGroup = "demo07-consumer-group-" + Demo07Message.TOPIC
)
public class Demo07Consumer implements RocketMQListener<Demo07Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Demo07Message message) {
        logger.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), message);
    }

}
```

测试

```java

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Demo07ProducerTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Demo07Producer producer;

    @Test
    public void testSendMessageInTransaction() throws InterruptedException {
        int id = (int) (System.currentTimeMillis() / 1000);
        SendResult result = producer.sendMessageInTransaction(id);
        logger.info("[testSendMessageInTransaction][发送编号：[{}] 发送结果：[{}]]", id, result);

        // 阻塞等待，保证消费
        new CountDownLatch(1).await();
    }

}
```

我们来执行 `#testSendMessageInTransaction()` 方法，测试发送**事务**消息。控制台输出如下：



```
# TransactionListenerImpl 执行 executeLocalTransaction 方法，先执行本地事务的逻辑
2019-12-06 01:23:00.928  INFO 3205 --- [           main] p.Demo07Producer$TransactionListenerImpl : [executeLocalTransaction][执行本地事务，消息：GenericMessage [payload=byte[17], headers={rocketmq_TOPIC=DEMO_07, rocketmq_FLAG=0, id=ce85ed2a-d7ae-9cc6-226d-a8beb2e219ab, contentType=application/json, rocketmq_TRANSACTION_ID=0AAB01730C8518B4AAC214E570BD0002, timestamp=1575480180928}] arg：1575480180]

# Producer 发送事务消息成功，但是因为 executeLocalTransaction 方法返回的是 UNKOWN 状态，所以事务消息并未提交或者回滚
2019-12-06 01:23:00.930  INFO 3205 --- [           main] c.i.s.l.r.producer.Demo07ProducerTest    : [testSendMessageInTransaction][发送编号：[1575480180] 发送结果：[SendResult [sendStatus=SEND_OK, msgId=0AAB01730C8518B4AAC214E570BD0002, offsetMsgId=null, messageQueue=MessageQueue [topic=DEMO_07, brokerName=broker-a, queueId=3], queueOffset=38]]]

# RocketMQ Broker 在发送事务消息 30 秒后，发现事务消息还未提交或是回滚，所以回查 Producer 。此时，checkLocalTransaction 方法返回 COMMIT ，所以该事务消息被提交
2019-12-06 01:23:35.155  INFO 3205 --- [pool-1-thread-1] p.Demo07Producer$TransactionListenerImpl : [checkLocalTransaction][回查消息：GenericMessage [payload=byte[17], headers={rocketmq_QUEUE_ID=3, TRANSACTION_CHECK_TIMES=1, rocketmq_BORN_TIMESTAMP=1575480180925, rocketmq_TOPIC=DEMO_07, rocketmq_FLAG=0, rocketmq_MESSAGE_ID=0AAB017300002A9F0000000000132AC3, rocketmq_TRANSACTION_ID=0AAB01730C8518B4AAC214E570BD0002, rocketmq_SYS_FLAG=0, id=0fc2f199-25fb-5911-d577-f81b8003f0f8, CLUSTER=DefaultCluster, rocketmq_BORN_HOST=10.171.1.115, contentType=application/json, timestamp=1575480215155}]]

# 事务消息被提交，所以该消息被 Consumer 消费
2019-12-06 01:23:35.160  INFO 3205 --- [MessageThread_1] c.i.s.l.r.consumer.Demo07Consumer        : [onMessage][线程编号:89 消息内容：Demo07Message{id=1575480180}]
```

## 10.1 RocketMQTransactionListener

```
// RocketMQTransactionListener.java

public @interface RocketMQTransactionListener {

    /**
     * 事务的生产者分组
     *
     * Declare the txProducerGroup that is used to relate callback event to the listener, rocketMQTemplate must send a
     * transactional message with the declared txProducerGroup.
     * <p>
     * <p>It is suggested to use the default txProducerGroup if your system only needs to define a TransactionListener class.
     */
    String txProducerGroup() default RocketMQConfigUtils.ROCKETMQ_TRANSACTION_DEFAULT_GLOBAL_NAME;

    /**
     * Set ExecutorService params -- corePoolSize
     */
    int corePoolSize() default 1;
    /**
     * Set ExecutorService params -- maximumPoolSize
     */
    int maximumPoolSize() default 1;
    /**
     * Set ExecutorService params -- keepAliveTime
     */
    long keepAliveTime() default 1000 * 60; //60ms
    /**
     * Set ExecutorService params -- blockingQueueSize
     */
    int blockingQueueSize() default 2000;

    /**
     * The property of "access-key"
     */
    String accessKey() default "${rocketmq.producer.access-key}";
    /**
     * The property of "secret-key"
     */
    String secretKey() default "${rocketmq.producer.secret-key}";
}
```

# 11 接入阿里云的消息队列 RocketMQ

在阿里云上，提供消息队列 [RocketMQ](https://help.aliyun.com/product/29530.html) 服务。那么，我们是否能够使用 RocketMQ-Spring 实现阿里云 RocketMQ 的消息的发送与消费呢？

答案是**可以**。在 [《阿里云 —— 消息队列 MQ —— 开源 Java SDK 接入说明》](https://help.aliyun.com/document_detail/128602.html) 中，提到目前开源的 Java SDK 可以接入阿里云 RocketMQ 服务。

> 如果您已使用开源 Java SDK 进行生产，只需参考方法，重新配置参数，即可实现无缝上云。
>
> **前提条件**
>
> - 已在阿里云 MQ 控制台创建资源，包括 Topic、Group ID（GID）、接入点（Endpoint），以及 AccessKeyId 和 AccessKeySecret。
> - 已下载开源 RocketMQ 4.5.1 或以上版本，以支持连接阿里云 MQ。

这里，艿艿创建了 [lab-31-rocketmq-ons](https://github.com/YunaiV/SpringBoot-Labs/tree/master/lab-31/lab-31-rocketmq-ons) 示例项目，使用 RocketMQ-Spring 接入阿里云。重点的差异，就在 [`application.yaml`](https://github.com/YunaiV/SpringBoot-Labs/blob/master/lab-31/lab-31-rocketmq-ons/src/main/resources/application.yaml) 配置文件，配置如下：



```
# rocketmq 配置项，对应 RocketMQProperties 配置类
rocketmq:
  name-server: http://onsaddr.mq-internet-access.mq-internet.aliyuncs.com:80 # 阿里云 RocketMQ Namesrv
  access-channel: CLOUD # 设置使用阿里云
  # Producer 配置项
  producer:
    group: GID_PRODUCER_GROUP_YUNAI_TEST # 生产者分组
    access-key: # 设置阿里云的 RocketMQ 的 access key ！！！这里涉及到隐私，所以这里艿艿没有提供
    secret-key: # 设置阿里云的 RocketMQ 的 secret key ！！！这里涉及到隐私，所以这里艿艿没有提供
```

- 重点，就是设置了 `rocketmq.access-channel=CLOUD` ，访问阿里云 RocketMQ 服务。

- [《RocketMQ 用户指南》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408164726/RocketMQ_userguide.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 原理简介》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408165024/RocketMQ_design.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 最佳实践》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408164929/RocketMQ_experience.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 开发者指南》](https://github.com/apache/rocketmq/tree/master/docs/cn) 基于 RocketMQ 4 的版本。
- [《阿里云 —— 消息队列 MQ》](https://help.aliyun.com/product/29530.html?spm=a2c4g.11186623.6.540.68cc5b3aZYDU2Y) 阿里云的消息队列，就是 RocketMQ 的云服务。

# 彩蛋

# 666. 彩蛋

想写点彩蛋，又发现没有什么好写的。咳咳咳。

从个人使用感受上来说，RocketMQ 提供的特性，可能是最为丰富的，可以说是最适合业务团队的分布式消息队列。艿艿是从 2013 年开始用 RocketMQ 的，主要踩的坑，都是自己错误使用导致的。例如说：

- 刚开始略微抠门，只搭建了 RocketMQ 一主一从集群，结果恰好倒霉，不小心挂了主。
- 多个 Topic 公用一个消费者集群，导致使用相同线程池。结果，嘿~有个消费逻辑需要调用第三方服务，某一天突然特别慢，导致消费积压，进而整个线程池堵塞。
- 相同消费者分组，订阅了不同的 Topic ，导致相互覆盖。

如果胖友在使用阿里云的话，建议量级较小的情况下，可以考虑先使用 [阿里云 —— 消息队列 MQ 服务](https://help.aliyun.com/product/29530.html) 。毕竟搭建一个高可用的 RocketMQ 量主两从的集群，最最最起码要两个 ECS 节点。同时，需要一定的维护和监控成本。😈 我们目前有个项目，就是直接使用阿里云的消息队列服务。

消息队列是非常重要的组件，推荐阅读下 RocketMQ 的最佳实践：

- [《阿里云 —— 消息队列 MQ 服务 —— 最佳实践》](https://help.aliyun.com/document_detail/95837.html)
- [《RocketMQ 官方文档 —— 最佳实践》](https://github.com/apache/rocketmq/blob/master/docs/cn/best_practice.md)

另外，如下**官方**文档，建议通读 + 通读 + 通断：

- [《RocketMQ 用户指南》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408164726/RocketMQ_userguide.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 原理简介》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408165024/RocketMQ_design.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 最佳实践》](http://gd-rus-public.cn-hangzhou.oss-pub.aliyun-inc.com/attachment/201604/08/20160408164929/RocketMQ_experience.pdf) 基于 RocketMQ 3 的版本。
- [《RocketMQ 开发者指南》](https://github.com/apache/rocketmq/tree/master/docs/cn) 基于 RocketMQ 4 的版本。
- [《阿里云 —— 消息队列 MQ》](https://help.aliyun.com/product/29530.html?spm=a2c4g.11186623.6.540.68cc5b3aZYDU2Y) 阿里云的消息队列，就是 RocketMQ 的云服务。

这里，在额外推荐一些内容：

- [《RocketMQ 入门 —— 原理与实践》](http://www.iocoder.cn/RocketMQ/start/Principle-and-practice/?self) ，一文快速了解 RocketMQ 的原理与实践，非常不错，篇幅也在可接受的范围之内。
- [《性能测试 —— RocketMQ 基准测试》](http://www.iocoder.cn/Performance-Testing/RocketMQ-benchmark/?self) ，消息消息队列是我们非常重要的性能优化手段，那么到底它的性能有多强，何不上手测试一波~
- [《RocketMQ 源码解析系列》](http://www.iocoder.cn/categories/RocketMQ/?self) ，知其然，知其所以然。RocketMQ 是艿艿第一个特别完全看完的开源中间件，收获颇丰。