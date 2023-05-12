# Linux docker nacos

## 前提要检查jdk，一定要64位的。不然会出现一些bean文件创建失败的错误

## 1. 拉去镜像

```java
docker pull nacos/nacos-server:v2.1.2
```

## 2. 创建挂载目录，用于把配置文件映射到容器目录

```java
mkdir -p nacos/conf
```

## 3. 执行nacos conf里面的mysql

## 4. 同步 application.properties文件

使用mysql 则一定要指定``spring.datasource.platform=mysql``

```properties
# spring
server.servlet.contextPath=${SERVER_SERVLET_CONTEXTPATH:/nacos}
server.contextPath=/nacos
server.port=${NACOS_APPLICATION_PORT:8848}
server.tomcat.accesslog.max-days=30
server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D %{User-Agent}i %{Request-Source}i
server.tomcat.accesslog.enabled=${TOMCAT_ACCESSLOG_ENABLED:false}
server.error.include-message=ALWAYS
# default current work dir
server.tomcat.basedir=file:.
#*************** Config Module Related Configurations ***************#
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
#spring.datasource.platform=${SPRING_DATASOURCE_PLATFORM:}
spring.sql.init.platform=${SPRING_DATASOURCE_PLATFORM:}
nacos.cmdb.dumpTaskInterval=3600
nacos.cmdb.eventTaskInterval=10
nacos.cmdb.labelTaskInterval=300
nacos.cmdb.loadDataAtStart=false
spring.datasource.platform=mysql
db.num=${MYSQL_DATABASE_NUM:1}
db.url.0=jdbc:mysql://172.17.192.20:${MYSQL_SERVICE_PORT:3306}/${MYSQL_SERVICE_DB_NAME:nacos}?${MYSQL_SERVICE_DB_PARAM:characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false}
db.user.0=${MYSQL_SERVICE_USER:root}
db.password.0=${MYSQL_SERVICE_PASSWORD:123456}
### The auth system to use, currently only 'nacos' and 'ldap' is supported:
nacos.core.auth.system.type=${NACOS_AUTH_SYSTEM_TYPE:nacos}
### worked when nacos.core.auth.system.type=nacos
### The token expiration in seconds:
nacos.core.auth.plugin.nacos.token.expire.seconds=${NACOS_AUTH_TOKEN_EXPIRE_SECONDS:18000}
### The default token:
nacos.core.auth.plugin.nacos.token.secret.key=${NACOS_AUTH_TOKEN:}
### Turn on/off caching of auth information. By turning on this switch, the update of auth information would have a 15 seconds delay.
nacos.core.auth.caching.enabled=${NACOS_AUTH_CACHE_ENABLE:false}
nacos.core.auth.enable.userAgentAuthWhite=${NACOS_AUTH_USER_AGENT_AUTH_WHITE_ENABLE:false}
nacos.core.auth.server.identity.key=${NACOS_AUTH_IDENTITY_KEY:}
nacos.core.auth.server.identity.value=${NACOS_AUTH_IDENTITY_VALUE:}
## spring security config
### turn off security
nacos.security.ignore.urls=${NACOS_SECURITY_IGNORE_URLS:/,/error,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/v1/auth/**,/v1/console/health/**,/actuator/**,/v1/console/server/**}
# metrics for elastic search
management.metrics.export.elastic.enabled=false
management.metrics.export.influx.enabled=false
nacos.naming.distro.taskDispatchThreadCount=10
nacos.naming.distro.taskDispatchPeriod=200
nacos.naming.distro.batchSyncKeyCount=1000
nacos.naming.distro.initDataRatio=0.9
nacos.naming.distro.syncRetryDelay=5000
nacos.naming.data.warmup=true

```

## 5. 挂载并运行

```
docker run -d --name nacos-server \
-p 7848:7848 -p 8848:8848 -p 9848:9848 \
-e JVM_XMS=256m \
-e JVM_XMX=256m \
-e MODE=standalone \
--privileged=true \
--restart=always \
-v /root/docker/nacos/conf/application.properties:/home/nacos/conf/application.properties \
nacos/nacos-server:v2.2.2
```

