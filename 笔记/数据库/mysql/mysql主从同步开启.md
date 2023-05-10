### https://www.jianshu.com/p/9bccb68fbc79  该文档有多种mysql同步模式配置

### 1 配置主库

创建一个叫’hongzhi-mo’的用户，密码为’Root@123456’，并且给xiaoming用户授予REPLICATION SLAVE权限。才能建立起从库与主库之间的通信，使得hongzhi-mo可以从主库中复制。 todo 这部分还未测试

```properties
server-id=1
 #开启二进制日志                  
log-bin = mysql-bin    
#需要复制的数据库名，如果复制多个数据库，重复设置这个选项即可                  
binlog-do-db = test        
#将从服务器从主服务器收到的更新记入到从服务器自己的二进制日志文件中                 
log-slave-updates                        
#控制binlog的写入频率。每执行多少次事务写入一次(这个参数性能消耗很大，但可减小MySQL崩溃造成的损失) 
sync_binlog = 1                    
#这个参数一般用在主主同步中，用来错开自增值, 防止键值冲突
auto_increment_offset = 1           
#这个参数一般用在主主同步中，用来错开自增值, 防止键值冲突
auto_increment_increment = 1            
#二进制日志自动删除的天数，默认值为0,表示“没有自动删除”，启动时和二进制日志循环时可能删除  
expire_logs_days = 7                    
#将函数复制到slave  
log_bin_trust_function_creators = 1   
#binlog-ignore-db=information_schema
```

### 2 配置从库

```properties
[mysqld]
server-id=2
log_bin=mysql-bin
log-slave-updates
sync_binlog = 0
#log buffer将每秒一次地写入log file中，并且log file的flush(刷到磁盘)操作同时进行。该模式下在事务提交的时候，不会主动触发写入磁盘的操作
innodb_flush_log_at_trx_commit = 0
#MySQL主从复制的时候，当Master和Slave之间的网络中断，但是Master和Slave无法察觉的情况下（比如防火墙或者路由问题）。Slave会等待slave_net_timeout设置的秒数后，才能认为网络出现故障，然后才会重连并且追赶这段时间主库的数据
slave-net-timeout = 60                    
log_bin_trust_function_creators = 1

```

### 3 从库进行开始同步

```properties
//设置主库的ip 账号密码
CHANGE MASTER TO MASTER_HOST='172.17.192.20', MASTER_PORT=3306, MASTER_USER='root2', MASTER_PASSWORD='123456'
//开始
START SLAVE;
```

### 4 重新同步

```properties
主从数据不一致，重新配置主从同步也是一种解决方法。
 
1.从库停止主从复制
stop slave;
 
2.对主库数据库加锁
flush tables with read lock;
 
3.备份主库数据
mysqldump -uroot -p --databases testdb1 testdb2 > full_bak.sql
 
4.重置主库日志
reset master；
 
5.对主库数据库解锁
unlock tables;
 
6.删除旧数据
drop database testdb1;
drop database testdb2;
 
7.从库导入数据
source /tmp/full_bak.sql
 
8.重置从库日志
reset slave; 或者 reset slave all;
  
清理slave 同步信息：
---reset slave 仅清理master.info 和 relay-log.info 文件
---删除所有的relay log 文件，重启用一个新的relay log 文件。
---重置 MASTER_DELAY  复制延迟间隔为:0
---不清理内存里的同步复制配置信息
---不重置 gtid_executed or gtid_purged 参数值
  
reset slave;
（重启mysqld后，内存里的同步配置信息自动重置）
reset slave all;
---其他功能和reset slave 一样，唯一区别是：会立即清理内存里的同步配置信息。
  
9.开启主从复制
start slave;
  
10.查看主从复制状态
show slave status;
Slave_IO_Running: Yes
Slave_SQL_Running: Yes
```

### 5.查看结果

需要 **Slave_IO_Running 和Slave_SQL_Running全部为Yes**

### 6从库同步状态查询详细说明

<img src="D:\cmw\ruoyi-vue-pro\笔记\数据库\主从同步状态字段详解表.png" alt="img" style="zoom:75%;" />

``` properties
参数详解：

1. Slave_IO_State

这里显示了当前slave I/O线程的状态(slave连接到master的状态)。状态信息和使用show processlist | grep "system user"(会显示两条信息，一条slave I/O线程的，一条是slave SQL线程的)显示的内容一样。

slave I/O线程的状态，有以下几种：

       1) waiting for master update

           这是connecting to master状态之前的状态

       2) connecting to master

          I/O线程正尝试连接到master

       3) checking master version

          在与master建立连接后，会出现该状态。该状态出现的时间非常短暂。

       4) registering slave on master

          在与master建立连接后，会出现该状态。该状态出现的时间非常短暂。  

       5) requesting binlog dump

           在与master建立连接后，会出现该状态。该状态出现的时间非常短暂。在这个状态下，I/O线程向master发送请求，请求binlog，位置从指定的binglog 名字和binglog的position位置开始。

       6) waiting to reconnect after a failed binlog dump request

           如果因为连接断开，导致binglog的请求失败，I/O线程会进入睡眠状态。然后定期尝试重连。尝试重连的时间间隔，可以使用命令"change master to master_connect_trt=X;"改变。

       7) reconnecting after a failed binglog dump request

           I/O进程正在尝试连接master

       8) waiting for master to send event

           说明，已经成功连接到master，正等待二进制日志时间的到达。如果master 空闲，这个状态会持续很长时间。如果等待的时间超过了slave_net_timeout(单位是秒)的值，会出现连接超时。在这种状态下，I/O线程会人为连接失败，并开始尝试重连

       9) queueing master event to the relay log

          此时，I/O线程已经读取了一个event，并复制到了relay log 中。这样SQL 线程可以执行此event

       10) waiting to reconnect after a failed master event read

          读取时出现的错误(因为连接断开)。在尝试重连之前，I/O线程进入sleep状态，sleep的时间是master_connect_try的值(默认是60秒)

       11) reconnecting after a failed master event read

          I/O线程正尝试重连master。如果连接建立，状态会变成"waiting for master to send event"

       12) waiting for the slave sql thread to free enough relay log space

         这是因为设置了relay_log_space_limit，并且relay log的大小已经整张到了最大值。I/O线程正在等待SQL线程通过删除一些relay log，来释放relay log的空间。

       13) waiting for slave mutex on exit

          I/O线程停止时会出现的状态，出现的时间非常短。

2. Master_Host: 192.168.1.100

mysql主库的ip地址

3. Master_User: mysync

这个是master上面的一个用户。用来负责主从复制的用户，创建主从复制的时候建立的（具有reolication slave权限）。

4. Master_Port: 3306

master服务器的端口  一般是3306

5. Connect_Retry: 60

连接中断后，重新尝试连接的时间间隔。默认值是60秒。

#与master相关的日志的信息

6. Master_Log_File: mysql-bin.001822

当前I/O线程正在读取的主服务器二进制日志文件的名称。

7. Read_Master_Log_Pos: 290072815

当前I/O线程正在读取的二进制日志的位置。

#与relay log相关的信息

8. Relay_Log_File: mysqld-relay-bin.005201

当前slave SQL线程正在读取并执行的relay log的文件名。

9. Relay_Log_Pos: 256529594

当前slave SQL线程正在读取并执行的relay log文件中的位置；（Relay_Log_File下的Relay_Log_Pos其实一一对应着Relay_Master_Log_File的Exec_Master_Log_Pos。）

10. Relay_Master_Log_File: mysql-bin.001821

当前slave SQL线程读取并执行的relay log的文件中多数近期事件，对应的主服务器二进制日志文件的名称。（说白点就是我SQL线程从relay日志中读取的正在执行的sql语句，对应主库的sql语句记录在主库的哪个binlog日志中）

#slave I/O和SQL线程的状态（重要）

11. Slave_IO_Running: Yes

I/O线程是否被启动并成功地连接到主服务器上。

12. Slave_SQL_Running: Yes

SQL线程是否被启动。

13. Replicate_Do_DB

  Replicate_Ignore_DB

  Replicate_Do_Table

  Replicate_Ignore_Table

  Replicate_Wild_Do_Table

  Replicate_Wild_Ignore_Table

这些参数都是为了用来指明哪些库或表在复制的时候不要同步到从库，但是这些参数用的时候要小心，因为 当跨库使用的时候 可能会出现问题。

一般情况下 ，限制的时候都用Replicate_Wild_Ignore_Table这个参数。

14. Last_Errno: 0

  Last_Error

slave的SQL线程读取日志参数的的错误数量和错误消息。错误数量为0并且消息为空字符串表示没有错误。

如果Last_Error值不是空值，它也会在从属服务器的错误日志中作为消息显示。 

15. Skip_Counter: 0

SQL_SLAVE_SKIP_COUNTER的值，用于设置跳过sql执行步数。

16. Exec_Master_Log_Pos: 256529431

slave SQL线程当前执行的事件，对应在master相应的二进制日志中的position。（结合Relay_Master_Log_File理解，而且在Relay_Master_Log_File这个值等于Master_Log_File值的时候，Exec_Master_Log_Pos是不可能超过Read_Master_Log_Pos的。）

17. Relay_Log_Space: 709504534

所有原有的中继日志结合起来的总大小。

18. Until_Condition: None

  Until_Log_File:

  Until_Log_Pos: 0

在START SLAVE语句的UNTIL子句中指定的值。

Until_Condition具有以下值：

1) 如果没有指定UNTIL子句，则没有值

2) 如果从属服务器正在读取，直到达到主服务器的二进制日志的给定位置为止，则值为Master

3) 如果从属服务器正在读取，直到达到其中继日志的给定位置为止，则值为Relay

Until_Log_File和Until_Log_Pos用于指示日志文件名和位置值。日志文件名和位置值定义了SQL线程在哪个点中止执行。

19. Master_SSL_Allowed: No

  Master_SSL_CA_File: 

  Master_SSL_CA_Path: 

  Master_SSL_Cert: 

  Master_SSL_Cipher: 

  Master_SSL_Key:

  Master_SSL_Verify_Server_Cert: No 

  Master_SSL_Crl: 

  Master_SSL_Crlpath:

这些字段显示了被从属服务器使用加密相关的参数。这些参数用于连接主服务器。

Master_SSL_Allowed具有以下值：

1) 如果允许对主服务器进行SSL连接，则值为Yes

2) 如果不允许对主服务器进行SSL连接，则值为No

3) 如果允许SSL连接，但是从属服务器没有让SSL支持被启用，则值为Ignored。

与SSL有关的字段的值对应于–master-ca,–master-capath,–master-cert,–master-cipher和–master-key选项的值。

20. seconds_Behind_Master: 2923

这个值是时间戳的差值。是slave当前的时间戳和master记录该事件时的时间戳的差值。

21. Last_IO_Errno: 0

  Last_IO_Error: 

  Last_SQL_Errno: 0

  Last_SQL_Error: 

  最后一次I/O线程或者SQL线程的错误号和错误消息。

22. Replicate_Ignore_Server_Ids: 

主从复制，从库忽略的主库服务器Id号。就是不以这些服务器Id为主库。

23. Master_Server_Id: 1

  Master_UUID: 13ee75bb-99e2-11e6-be4d-b499baa80e6e

  Master_Info_File: /home/data/mysql/master.info

  分别表示主库服务器id号，主库服务器的UUID好，还有在从库中保存主库服务器相关的目录位置。

24. SQL_Delay: 0

一个非负整数，表示秒数，Slave滞后多少秒于master。

25. SQL_Remaining_Delay: NULL

当 Slave_SQL_Running_State 等待，直到MASTER_DELAY秒后，Master执行的事件，此字段包含一个整数，表示有多少秒左右的延迟。在其他时候，这个字段是NULL。

26. Slave_SQL_Running_State: Reading event from the relay log

SQL线程运行状态：

 1) Reading event from the relay log

线程已经从中继日志读取一个事件，可以对事件进行处理了。

 2) Has read all relay log; waiting for the slave I/O thread to update it

线程已经处理了中继日志文件中的所有事件，现在正等待I/O线程将新事件写入中继日志。

 3) Waiting for slave mutex on exit

线程停止时发生的一个很简单的状态。

27. Master_Retry_Count: 86400

连接主库失败最多的重试次数。

28. Master_Bind: 

slave从库在多网络接口的情况下使用，以确定用哪一个slave网络接口连接到master。

29. Last_IO_Error_Timestamp: 

  Last_SQL_Error_Timestamp: 

    最后一次I/O线程或者SQL线程错误时的时间戳。

#GTID模式相关

30. Retrieved_Gtid_Set: 

  获取到的GTID<IO线程>

  Executed_Gtid_Set: 

  执行过的GTID<SQL线程>

  Auto_Position: 0

```