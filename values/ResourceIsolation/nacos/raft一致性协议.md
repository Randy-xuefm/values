# raft

[toc]

raft协议是为了解决分布应用下数据一致性的问题.
国外有个动画演示网站,可以学习下.
[raft](http://thesecretlivesofdata.com/raft/)

### 选举
在raft协议下,是通过状态机来实现的.
raft协议中,有3种状态.,分别为leader,follower,candidate.

在初始状态下,所有的节点都处于follower,经过一段时间后(心跳超时时间),如果没有收到leader的心跳包,
在经过一段时间(选举超时时间),则变为candidate.

在设计时应该注意,**选举超时时间**应该是一个范围随机值,防止所有节点都变为candidate,无法完成选举.

candidate如果能收到过半的投票,则变为leader.

leader在指定间隔时间,发送心跳给follower,保持leader身份.

### 日志
在raft协议下,只有leader响应数据变更,然后由leader发送变更日志到follower,leader在收到过半
的follower响应后,正式提交数据,然后返回给客户以及follower.

### 快照
当节点意外宕机后,重新启动,为了快速恢复,节点定时生成快照.这样在节点重启后,可以先读取快照,然后再执行日志,快速恢复.

### 脑裂
当集群环境下出现多个leader的时候,这种现象称为脑裂.
raft协议是如何解决的呢?

Term,每一次选举,Term递增,这样防止脑裂.
当出现两个leader,Term小的自动变为follower.

