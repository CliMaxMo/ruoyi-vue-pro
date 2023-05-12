# winodws 安装RocketMQ

## 1.打开 https://rocketmq.apache.org/release_notes/ 下载相应的版本

**souce**是源码版本，**Binary**是编译好的版本

## 2.如果下载好的是源码版本

```cmd
# Maven 编译 RocketMQ ，并跳过测试。耐心等待...
$ mvn -Prelease-all -DskipTests clean install -U
```

## 3. 配置 环境变量ROCKETMQ_HOME

变量名：ROCKETMQ_HOME

变量值:**D:\Program\rocketmq-all-4.6.0-source-release\distribution\target\rocketmq-4.6.0\rocketmq-4.6.0\bin**

 ## 4. 修改runserver.cmd

将Java_opt修改小一点

```java
set "JAVA_OPT=%JAVA_OPT% -server -Xms256m -Xmx256m -Xmn256m"
    
```

## 5.修改runbroker.cmd

1. 将Java_opt修改小一点

```java
set "JAVA_OPT=%JAVA_OPT% -server -Xms256m -Xmx256m -Xmn256m"
```

2. 将-XX:MaxDirectMemorySize=15g的值修改小一点
       

## 6. 启动server 和broken

```java
1.start mqnamesrv.cmd
2.start mqbroker.cmd -n 127.0.0.1:9876 -c ../conf/broker.conf autoCreateTopicEnable=true
```

## 7. 添加运行地址的环境变量

在系统变量里添加 

```java
NAMESRV_ADDR: 127.0.0.1:9876
```

## 8.运行消费者和生产者

```java
//测试消费者接受消息
tools.cmd org.apache.rocketmq.example.quickstart.Consumer
//发消息测试
tools.cmd org.apache.rocketmq.example.quickstart.Producer
```

## 9.其他命令或错误 

停止

```java
sh bin/mqshutdown broker
#停止namesvr
sh bin/mqshutdown namesrv
```

错误1

```java
MQBrokerException: CODE: 1  DESC: create mapped file failed, server is busy or broken.
```

解决方法：确认jdk版本是否是32位的。是的话需要换成64位的
