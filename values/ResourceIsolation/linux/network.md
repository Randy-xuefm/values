# linux 网络问题排查

### TCP

#### backlog
TCP通讯是可靠的通讯方式,可靠是通过面向连接来实现的.也就是说双方需要维护一定的数据结构来保证面向连接.
TCP通讯在创建连接时有两个数据结构:
```
半连接队列(SYN queue),全连接队列(accept queue)
```

从TCP的三次握手说起,
第一次客户端发送SYN到服务端,服务端会将这个连接保存在```SYN queue```这个队列,
服务端返回SYN ACK给客户端,
客户端收到ACK后,在发送ACK给服务端,服务端会将这个连接保存在```accept queue```,同时移除```SYN queue```中的记录
![img.png](img.png)

在这个过程中可能存在``` SYN queue 或者 accept queue```队列已满的情况,我们在创建TCP连接时backlog其实就是```accept queue```队列长度.
相关的Linux系统参数有:
```ini
#accept queue队列长度,最终取somaxconn,backlog中的最小值生效
#/proc/sys/net/core/somaxconn
somaxconn

#accept queue队列满后,后续连接处理方式,默认为0,丢弃,等待客户端重试
#/proc/sys/net/ipv4/tcp_abort_on_overflow
tcp_abort_on_overflow

#SYN queue队列长度
tcp_max_syn_backlog

#SYN超时重传次数
#/proc/sys/net/ipv4/tcp_syn_retries
tcp_syn_retries

#SYN ACK超时重传次数
#/proc/sys/net/ipv4/tcp_synack_retries
tcp_synack_retries
```
- backlog队列溢出

    内核中记录了两个计数器：
    
    ListenOverflows：当 socket 的 listen queue 已满，当新增一个连接请求时，应用程序来不及处理；
    
    ListenDrops：包含上面的情况，除此之外，当内存不够无法为新的连接分配 socket 相关的数据结构时，也会加 1，当然还有别的异常情况下会增加 1。
    
    分别对应下面文件中的第 21 列（ListenOverflows）和第 22 列（ListenDrops），可以通过如下命令查看：
    
    ```shell
    cat /proc/net/netstat | awk '/TcpExt/ { print $21,$22 }'
    ```
    也可以用命令
    ```shell
    netstat -s | grep overflowed
    ```
    查看accept queue队列长度
    ```shell
    ss -lnt|grep port
    ```
- SYN queue队列溢出
  可以用命令```netstat -s |grep "SYNs to LISTEN"```来查看,但是如果要调整,accept queue也要调整
  
#### 四次挥手

#### 数据传输