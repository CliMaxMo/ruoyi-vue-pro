<img src="https://static.iocoder.cn/images/Redis/2019_09_28/01.png" alt="Spring Data Redis 调用" style="zoom: 50%;" />



# 1. jedis

## 1.1 基本配置

```yaml
spring:
  # 对应 RedisProperties 类
  redis:
    host: 127.0.0.1
    port: 6379
    password: # Redis 服务器密码，默认为空。生产中，一定要设置 Redis 密码！
    database: 0 # Redis 数据库号，默认为 0 。
    timeout: 0 # Redis 连接超时时间，单位：毫秒。
    # 对应 RedisProperties.Jedis 内部类
    jedis:
      pool:
        max-active: 8 # 连接池最大连接数，默认为 8 。使用负数表示没有限制。
        max-idle: 8 # 默认连接数最小空闲的连接数，默认为 8 。使用负数表示没有限制。
        min-idle: 0 # 默认连接池最小空闲的连接数，默认为 0 。允许设置 0 和 正数。
        max-wait: -1 # 连接池最大阻塞等待时间，单位：毫秒。默认为 -1 ，表示不限制。

```

## 1.2 简单使用

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class Test01 {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringSetKey() {
        stringRedisTemplate.opsForValue().set("yunai", "shuai");
        //opsForValue 就是常见的数据库操作类，确认你存储的数据类型
    }
    //常见的如下
Redis 常见数据结构操作类。
ValueOperations 类，提供 Redis String API 操作。
ListOperations 类，提供 Redis List API 操作。
SetOperations 类，提供 Redis Set API 操作。
ZSetOperations 类，提供 Redis ZSet(Sorted Set) API 操作。
GeoOperations 类，提供 Redis Geo API 操作。
HyperLogLogOperations 类，提供 Redis HyperLogLog API 操作。
        
}
```

## 1.3 序列化

``RedisSerializer`` 序列化,通过serialize和deserialize来序列化和反序列化，传输都是二进制。

```java
// RedisSerializer.java
public interface RedisSerializer<T> {

	@Nullable
	byte[] serialize(@Nullable T t) throws SerializationException;

	@Nullable
	T deserialize(@Nullable byte[] bytes) throws SerializationException;

}
```

序列化主要分为如下四类

- JDK 序列化方式
- String 序列化方式
- JSON 序列化方式
- XML 序列化方式

```java
FastJsonRedisSerializer;
GenericFastJsonRedisSerializer;
GenericJackson2JsonRedisSerializer;
GenericToStringSerializer;
Jackson2JsonRedisSerializer;
JdkSerializationRedisSerializer;
OxmSerializer;
StringRedisSerializer;
```

大部分使用的都是``StringRedisSerializer``

```java
// StringRedisSerializer.java

private final Charset charset;

@Override
public String deserialize(@Nullable byte[] bytes) {
	return (bytes == null ? null : new String(bytes, charset));
}

@Override
public byte[] serialize(@Nullable String string) {
	return (string == null ? null : string.getBytes(charset));
}
```

``GenericJackson2JsonRedisSerializer ``

```java
// GenericJackson2JsonRedisSerializer.java

public GenericJackson2JsonRedisSerializer(@Nullable String classPropertyTypeName) {}
传入classPropertyTypeName ，如果不传入就是对象的类全名，这个是为了序列化和反序列化找到相应的对象的;
比如标准的是这样子的;
{
    "id": 1,
    "name": "芋道源码",
    "gender": 1
}
通过jackson default Typing则是这样子的;
{
       "@class": "cn.iocoder.springboot.labs.lab10.springdatarediswithjedis.cacheobject.UserCacheObject",
       "id": 1,
       "name": "芋道源码",
       "gender": 1
}
    
```

### 1.3.1 自定义序列化

```java
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        //开启事务支持
		template.setEnableTransactionSupport(true);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());

        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE 。
        template.setValueSerializer(new GenericFastJsonRedisSerializer());
        return template;
    }

}
```

# 2.Pipeline

## 2.1 原理和性能对比

未使用pipeline

![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20181122105251930.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3cxbGd5,size_16,color_FFFFFF,t_70)

使用了pipeline

![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20181122105343203.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3cxbGd5,size_16,color_FFFFFF,t_70)

两者性能对比

![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20181122105544269.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3cxbGd5,size_16,color_FFFFFF,t_70)

## 2.2 基于Redis Pipeline的函数

```java
// <1> 基于 Session 执行 Pipeline
@Override
public List<Object> executePipelined(SessionCallback<?> session) {
	return executePipelined(session, valueSerializer);
}
@Override
public List<Object> executePipelined(SessionCallback<?> session, @Nullable RedisSerializer<?> resultSerializer) {
    // ... 省略代码
}

// <2> 直接执行 Pipeline
@Override
public List<Object> executePipelined(RedisCallback<?> action) {
	return executePipelined(action, valueSerializer);
}
@Override
public List<Object> executePipelined(RedisCallback<?> action, @Nullable RedisSerializer<?> resultSerializer) {
    // ... 省略代码
}
```

### 2.2.1 源码解读

```JAVA
// RedisTemplate.java
//1.action方法就是实现改方法，就是需要执行的内容
@Override
public List<Object> executePipelined(RedisCallback<?> action, @Nullable RedisSerializer<?> resultSerializer) {
	// <1> 执行 Redis 方法
	return execute((RedisCallback<List<Object>>) connection -> {
		// 自动打开 Pipeline 模式。这样，我们就不需要手动去打开了。
		connection.openPipeline();
		boolean pipelinedClosed = false; // 标记 pipeline 是否关闭
		try {
			//调用我们传入的实现的 RedisCallback#doInRedis(RedisConnection connection) 方法，执行在 Pipeline 中，想要执行的 Redis 操作。
			Object result = action.doInRedis(connection);
			// 不要返回结果。因为 RedisCallback 是统一定义的接口，所以可以返回一个结果。但是在 Pipeline 中，未提交执行时，显然是没有结果，返回也没有意思。简单来说，就是我们在实现 RedisCallback#doInRedis(RedisConnection connection) 方法时，返回 null 即可。
			if (result != null) {
				throw new InvalidDataAccessApiUsageException(
						"Callback cannot return a non-null value as it gets overwritten by the pipeline");
			}
			// 调用 RedisConnection#closePipeline() 方法，自动提交 Pipeline 执行，并返回执行结果。
			List<Object> closePipeline = connection.closePipeline();
			pipelinedClosed = true;
			// <6> 反序列化结果，并返回
			return deserializeMixedResults(closePipeline, resultSerializer, hashKeySerializer, hashValueSerializer);
		} finally {
			if (!pipelinedClosed) {
				connection.closePipeline();
			}
		}
	});
}
```

例子实现：

```java
   @Test
    public  void  test03(){
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // set 写入
            for (int i = 0; i < 3; i++) {
                connection.set(String.format("ccc:%d", i).getBytes(), "shuai".getBytes());
            }
            // get
            for (int i = 0; i < 3; i++) {
                connection.get(String.format("ccc:%d", i).getBytes());
            }
            // 返回 null 即可
            return null;
        });
        // 打印结果
        System.out.println(results);
    }

结果如下
[true, true, true, shuai, shuai, shuai]
```

# 3. Transaction

## 3.1 原理

实现思路：

在 Spring Data Redis 中，实现 Redis Transaction 也是这个思路。通过 SessionCallback 操作 Redis 时，会从当前线程获得 Redis Connection ，如果获取不到，则会去“创建”一个 Redis Connection 并绑定到当前线程中。这样，我们在该 Redis Connection 开启 Redis Transaction 后，在该线程的所有操作，都可以在这个 Transaction 中，最后交由 Spring 事务管理器统一提供或回滚 Transaction 。

## 3.2 源码分析

```java
// RedisTemplate.java

public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {

	Assert.isTrue(initialized, "template not initialized; call afterPropertiesSet() before using it");
	Assert.notNull(action, "Callback object must not be null");

	RedisConnectionFactory factory = getRequiredConnectionFactory();
	RedisConnection conn = null;
	try {
		// <1.1>
		if (enableTransactionSupport) {
			// only bind resources in case of potential transaction synchronization
			conn = RedisConnectionUtils.bindConnection(factory, enableTransactionSupport);
		} else {
			// 如果是创建的 Redis Connection ，会绑定到当前啊线程中。因为 Transaction 是需要在 Connection 打开，然后后续的 Redis 的操作，都需要在其上。并且，还有一个非常重要的操作，打开 Redis Transaction ，会在该方法中，通过调用 RedisConnectionUtils#potentiallyRegisterTransactionSynchronisation(RedisConnectionHolder connHolder, final RedisConnectionFactory factory) 。
			conn = RedisConnectionUtils.getConnection(factory);
		}

		// ... 省略中间，执行 Redis 命令的代码。
	} finally {
		// 放 Redis Connection 。当然，这是有一个前提，整个 Transaction 已经完成。如果未完成，实际 Redis Connection 不会释放。
		RedisConnectionUtils.releaseConnection(conn, factory);
	}
}

```

那么，此时会有胖友有疑问，Redis Transaction 的提交和回滚在哪呢？答案在 RedisConnectionUtils 的内部类 RedisTransactionSynchronizer 中。代码如下：



```
// RedisConnectionUtils.java

private static class RedisTransactionSynchronizer extends TransactionSynchronizationAdapter {

	private final RedisConnectionHolder connHolder;
	private final RedisConnection connection;
	private final RedisConnectionFactory factory;

	@Override
	public void afterCompletion(int status) {

		try {
			switch (status) {
				// 提交
				case TransactionSynchronization.STATUS_COMMITTED:
					connection.exec();
					break;
				// 回滚
				case TransactionSynchronization.STATUS_ROLLED_BACK:
				case TransactionSynchronization.STATUS_UNKNOWN:
				default:
					connection.discard();
			}
		} finally {
			connHolder.setTransactionSyncronisationActive(false);
			connection.close();
			TransactionSynchronizationManager.unbindResource(factory);
		}
	}
}
```



- 根据事务结果的状态，进行 Redis Transaction 提交或回滚。😈 如果想进一步的深入，胖友就需要去了解 Spring Transaction 的源码。

# 4 Session

所有 Redis 操作都使用通过同一个 Redis Connection ，因为我们会将获得到的 Connection 绑定到当前线程中

但是不在一个Transaction中时，每次执行Redis操作的时候，每一次都会获取一次RedisConnection。这样子会存在一定的竞争，造成资源上的消耗。

就需要通过Seesion避免

当我们要执行在同一个 Session 里的操作时，我们通过实现 [`org.springframework.data.redis.core.SessionCallback`](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/core/SessionCallback.java) 接口，其代码如下：



```
// SessionCallback.java

public interface SessionCallback<T> {

	@Nullable
	<K, V> T execute(RedisOperations<K, V> operations) throws DataAccessException;
}
```

- 相比 RedisCallback 来说，总比是比较相似的。但是比较友好的是，它的入参 `operations` 是 [org.springframework.data.redis.core.RedisOperations](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/core/RedisOperations.java) 接口类型，而 RedisTemplate 的各种操作，实际就是在 RedisOperations 接口中定义，由 RedisTemplate 来实现。所以使用上也会更加便利。
- 实际上，我们在实现 RedisCallback 接口，也能实现在同一个 Connection 执行一系列的 Redis 操作，因为 RedisCallback 的入参本身就是一个 Redis Connection 。

## 4.2 源码解析

```java
// RedisTemplate.java

@Override
public <T> T execute(SessionCallback<T> session) {

	Assert.isTrue(initialized, "template not initialized; call afterPropertiesSet() before using it");
	Assert.notNull(session, "Callback object must not be null");

	RedisConnectionFactory factory = getRequiredConnectionFactory();
	// bind connection
	// 调用 RedisConnectionUtils#bindConnection(RedisConnectionFactory factory, boolean enableTranactionSupport) 方法，实际和我们开启 //enableTranactionSupport 事务时候，获取 Connection 和处理的方式，是一模一样的。也就是说：
//如果当前线程已经有一个绑定的 Connection 则直接使用（例如说，当前正在 Redis Transaction 事务中）；
//如果当前线程未绑定一个 Connection ，则进行创建并绑定到当前线程。甚至，如果此时是配置开启 enableTranactionSupport 事务的，那么此处就会触发 Redis //Transaction 的开启。
	RedisConnectionUtils.bindConnection(factory, enableTransactionSupport);
	try {
	   // <2> 执行定义的一系列 Redis 操作
		return session.execute(this);
	} finally {
		// <3> 释放并解绑 Connection 。
		RedisConnectionUtils.unbindConnection(factory);
	}
}
```

## 4.3 例子

```
// SessionTest.java

@RunWith(SpringRunner.class)
@SpringBootTest
public class SessionTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test01() {
        String result = stringRedisTemplate.execute(new SessionCallback<String>() {

            @Override
            public String execute(RedisOperations operations) throws DataAccessException {
                for (int i = 0; i < 100; i++) {
                    operations.opsForValue().set(String.format("yunai:%d", i), "shuai02");
                }
                return (String) operations.opsForValue().get(String.format("yunai:%d", 0));
            }

        });

        System.out.println("result:" + result);
    }

}
```

执行 `#test01()` 方法，结果如下：

```
result:shuai02
```

# 5 Pub/Sub

**实现 MessageListener 类**

```java
public class TestPatternTopicMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("收到 PatternTopic 消息：");
        System.out.println("线程编号：" + Thread.currentThread().getName());
        System.out.println("message：" + message);
        System.out.println("pattern：" + new String(pattern));
    }

}
```

- `message` 参数，可获得到具体的消息内容，不过是二进制数组，需要我们自己序列化。具体可以看下 [`org.springframework.data.redis.connection.DefaultMessage`](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/connection/DefaultMessage.java) 类。
- `pattern` 参数，发布的 Topic 的内容。

有一点要注意，默认的 RedisMessageListenerContainer 情况下，MessageListener 是**并发**消费，在线程池中执行(具体见[传送门](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/listener/RedisMessageListenerContainer.java#L982-L988)代码)。所以如果想相同 MessageListener **串行**消费，可以在方法上加 `synchronized` 修饰，来实现同步。

**创建 RedisMessageListenerContainer Bean**

```java
// RedisConfiguration.java

@Bean
public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory) {
    // 创建 RedisMessageListenerContainer 对象
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();

    // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
    container.setConnectionFactory(factory);

    // 添加监听器
    container.addMessageListener(new TestChannelTopicMessageListener(), new ChannelTopic("TEST"));
//        container.addMessageListener(new TestChannelTopicMessageListener(), new ChannelTopic("AOTEMAN"));
//        container.addMessageListener(new TestPatternTopicMessageListener(), new PatternTopic("TEST"));
    return container;
}
```

要注意，虽然 RedisConnectionFactory 可以多次调用 [`#addMessageListener(MessageListener listener, Topic topic)`](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/listener/RedisMessageListenerContainer.java#L375-L396) 方法，但是一定要都是相同的 Topic 类型。例如说，添加了 ChannelTopic 类型，就不能添加 PatternTopic 类型。为什么呢？因为 RedisMessageListenerContainer 是基于**一次** [SUBSCRIBE](http://redis.cn/commands/subscribe.html) 或 [PSUBSCRIBE](http://redis.cn/commands/psubscribe.html) 命令，所以不支持**不同类型**的 Topic 。当然，如果是**相同类型**的 Topic ，多个 MessageListener 是支持的。

那么，可能会有胖友会问，如果我添加了 `"Test"` 给 MessageListener**A** ，`"AOTEMAN"` 给 MessageListener**B** ，两个 Topic 是怎么分发（Dispatch）的呢？在 RedisMessageListenerContainer 中，有个 [DispatchMessageListener](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/listener/RedisMessageListenerContainer.java#L961-L988) 分发器，负责将不同的 Topic 分发到配置的 MessageListener 中。看到此处，有木有想到 Spring MVC 的 DispatcherServlet 分发不同的请求到对应的 `@RequestMapping` 方法。

**使用 RedisTemplate 发布消息**

```
RunWith(SpringRunner.class)
@SpringBootTest
public class PubSubTest {

    public static final String TOPIC = "TEST";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test01() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            stringRedisTemplate.convertAndSend(TOPIC, "yunai:" + i);
            Thread.sleep(1000L);
        }
    }

}
```



 

- 通过 `RedisTemplate#convertAndSend(String channel, Object message)` 方法，PUBLISH 消息。

执行 `#test01()` 方法，运行结果如下：

```
收到 ChannelTopic 消息：
线程编号：listenerContainer-2
message：yunai:0
pattern：TEST
收到 ChannelTopic 消息：
线程编号：listenerContainer-3
message：yunai:1
pattern：TEST
收到 ChannelTopic 消息：
线程编号：listenerContainer-4
message：yunai:2
pattern：TEST
```

- 整整齐齐，发送和订阅都成功了。注意，**线程编号**。

Redis 提供了 PUB/SUB 订阅功能，实际我们在使用时，一定要注意，它提供的**不是一个可靠的**订阅系统。例如说，有消息 PUBLISH 了，Redis Client 因为网络异常断开，无法订阅到这条消息。等到网络恢复后，Redis Client 重连上后，是无法获得到该消息的。相比来说，成熟的消息队列提供的订阅功能，因为消息会进行持久化（Redis 是不持久化 Publish 的消息的），并且有客户端的 ACK 机制做保障，所以即使网络断开重连，消息一样不会丢失。

当然，不能说 Redis Pub/Sub 毫无使用的场景，以下艿艿来列举几个：

- 1、在使用 Redis Sentinel 做高可用时，Jedis 通过 Redis Pub/Sub 功能，实现对 Redis 主节点的故障切换，刷新 Jedis 客户端的主节点的缓存。如果出现 Redis Connection 订阅的异常断开，会重新**主动**去 Redis Sentinel 的最新主节点信息，从而解决 Redis Pub/Sub 可能因为网络问题，丢失消息。
- 2、Redis Sentinel 节点之间的部分信息同步，通过 Redis Pub/Sub 订阅发布。
- 3、在我们实现 Redis 分布式锁时，如果获取不到锁，可以通过 Redis 的 Pub/Sub 订阅锁释放消息，从而实现其它获得不到锁的线程，快速抢占锁。当然，Redis Client 释放锁时，需要 PUBLISH 一条释放锁的消息。在 Redisson 实现分布式锁的源码中，我们可以看到。
- 4、Dubbo 使用 Redis 作为注册中心时，使用 Redis Pub/Sub 实现注册信息的同步。

# 6.Redssion

## 6.1 基础使用配置

**pom**

```xml
 <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.11.3</version>
  </dependency>
```

基本使用上方法不变，因为依然调用的时相同的Spring Data Redis提供的API，无需感知是Redisson还是Jedis的存在

## 6.2 Redis分布式锁

Redisson提供了八种不同的锁，具体可以查看如下

https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8

接下来是例子是用可重入锁

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class LockTest {

    private static final String LOCK_KEY = "anylock";

    @Autowired // <1>
    private RedissonClient redissonClient;

    @Test
    public void test() throws InterruptedException {
        // <2.1> 启动一个线程 A ，去占有锁
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 加锁以后 10 秒钟自动解锁
                // 无需调用 unlock 方法手动解锁
                final RLock lock = redissonClient.getLock(LOCK_KEY);
                lock.lock(10, TimeUnit.SECONDS);
            }
        }).start();
        // <2.2> 简单 sleep 1 秒，保证线程 A 成功持有锁
        Thread.sleep(1000L);

        // <3> 尝试加锁，最多等待 100 秒，上锁以后 10 秒自动解锁
        System.out.println(String.format("准备开始获得锁时间：%s", new SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(new Date())));
        final RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        if (res) {
            System.out.println(String.format("实际获得锁时间：%s", new SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(new Date())));
        } else {
            System.out.println("加锁失败");
        }
    }

}
```

- 整个测试用例，意图是：1）启动一个线程 A ，先去持有锁 10 秒然后释放；2）主线程，也去尝试去持有锁，因为线程 A 目前正在占用着该锁，所以需要等待线程 A 释放到该锁，才能持有成功。

```
准备开始获得锁时间：2019-10-274 00:44:08
实际获得锁时间：2019-10-274 00:44:17
```

- 9 秒后（因为我们 sleep 了 1 秒），主线程成功获得到 Redis 分布式锁，符合预期。

## 6.3 限流器

**Redisson 提供的是基于滑动窗口 RateLimiter 的实现。**

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class RateLimiterTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test() throws InterruptedException {
        // 创建 RRateLimiter 对象
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("myRateLimiter");
        // 初始化：最大流速 = 每 1 秒钟产生 2 个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
//        rateLimiter.trySetRate(RateType.PER_CLIENT, 50, 1, RateIntervalUnit.MINUTES);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 5; i++) {
            System.out.println(String.format("%s：获得锁结果(%s)", simpleDateFormat.format(new Date()),
                    rateLimiter.tryAcquire()));
            Thread.sleep(250L);
        }
    }

}

执行 #test() 测试用例，结果如下：

2019-10-02 22:46:40：获得锁结果(true)
2019-10-02 22:46:40：获得锁结果(true)
2019-10-02 22:46:41：获得锁结果(false)
2019-10-02 22:46:41：获得锁结果(false)
2019-10-02 22:46:41：获得锁结果(true)

```

有一点要纠正一下。Redisson 提供的限流器不是**严格且完整**的滑动窗口的限流器实现。举个例子，我们创建了一个每分钟允许 3 次操作的限流器。整个执行过程如下：



```
00:00:00 获得锁，剩余令牌 2 。
00:00:20 获得锁，剩余令牌 1 。
00:00:40 获得锁，剩余令牌 0 。
```



- 那么，00:01:00 时，锁的数量会恢复，按照 Redisson 的限流器来说。
- 如果是**严格且完整**的滑动窗口的限流器，此时在 00:01:00 剩余可获得的令牌数为 1 ，也就是说，起始点应该变成 00:00:20 。

如果基于 Redis **严格且完整**的滑动窗口的限流器，可以通过基于 Redis [Zset](http://redis.cn/commands.html#sorted_set) 实现。
