# Redis功能持久化分析

[toc]

### 持久化
redis支持两种数据持久化方式,分别是RDB(Redis DataBase)和AOF(Append Only File).
#### RDB
RDB是将redis某一时刻的数据持久化到磁盘中,是一种快照方式的持久化.

```ini
#   after 900 sec (15 min) if at least 1 key changed
#   after 300 sec (5 min) if at least 10 keys changed
#   after 60 sec if at least 10000 keys changed
save 900 1
save 300 10
save 60 10000

#bgsave RDB失败,是否对外停止服务
stop-writes-on-bgsave-error yes

#RDB是否压缩数据
rdbcompression yes

#CRC64校验
rdbchecksum yes

#文件名
dbfilename dump.rdb

#db路径
dir ./

#在没有启用持久性的情况下，删除复制实例中使用的RDB文件.
rdb-del-sync-files no
```

#### AOF
AOF是将执行过的写指令记录下来,在恢复的时候按照从前到后的顺序再将指令执行一遍.

```ini
#开启AOF持久化
appendonly no

#AOF文件名
appendfilename "appendonly.aof"

#AOF3种持久化模式
# appendfsync always
appendfsync everysec
# appendfsync no

#在aof重写或者写入rdb文件的时候，会执行大量IO，此时对于everysec和always的aof模式来说，执行fsync会造成阻塞过长时间，no-appendfsync-on-rewrite字段设置为默认设置为no。如果对延迟要求很高的应用，这个字段可以设置为yes，否则还是设置为no，这样对持久化特性来说这是更安全的选择。设置为yes表示rewrite期间对新写操作不fsync,暂时存在内存中,等rewrite完成后再写入，默认为no
no-appendfsync-on-rewrite no

#aof自动重写配置
#当目前aof文件大小超过上一次重写的aof文件大小的百分之多少进行重写，即当aof文件增长到一定大小的时候Redis能够调用bgrewriteaof对日志文件进行重写。当前AOF文件大小是上次日志重写得到AOF文件大小的二倍（设置为100）时，自动启动新的日志重写过程，也可以将此值设置为0，表示禁用重写。
auto-aof-rewrite-percentage 100
#设置允许重写的最小aof文件大小，避免了达到约定百分比但尺寸仍然很小的情况还要重写
auto-aof-rewrite-min-size 64mb

#aof文件可能在尾部是不完整的，当redis启动的时候，aof文件的数据被载入内存。重启可能发生在redis所在的主机操作系统宕机后，尤其在ext4文件系统没有加上data=ordered选项（redis宕机或者异常终止不会造成尾部不完整现象。）出现这种现象，可以选择让redis退出，或者导入尽可能多的数据。如果选择的是yes，当截断的aof文件被导入的时候，会自动发布一个log给客户端然后load。如果是no，用户必须手动redis-check-aof修复AOF文件才可以。一般此值保持默认的yes即可。
aof-load-truncated yes

#开启RDB,AOF混合持久化
aof-use-rdb-preamble yes
```

#### 持久化阻塞问题
如何判断redis已经阻塞?
可通过命令:SLOWLOG get 100,如果出现时间复杂度为O(1)的key,说明整个redis已经发生阻塞了.需要排查具体原因 

- fork阻塞
当RDB和AOF重写的时候,如果fork操作本身耗时长,必然导致主线程阻塞.可通过redis命令:
info stats查看指标latest_fork_usec,最近一次Fork操作阻塞redis进程的耗时数,单位微秒

- AOF刷盘阻塞
当配置了AOF持久化模式为everysec或者always,如果磁盘响应速度慢,也会发生阻塞.
  
- 执行时间复杂度为O(n)的命令
O(n)的命令执行时间不确定,随着n越来越大,执行时间会越来越长,也会造成网络阻塞
- big keys 
key执行时间较长,而且容易引起网络阻塞
可通过命令:./redis-cli --bigkeys,查看bigkeys的分布情况.具体的bigkey需要遍历找出

