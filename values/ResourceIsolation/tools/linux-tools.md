# 排查问题工具

## 网络
- iperf3

网络带宽质量测试,常用参数指南:
-c/s：客户端模式/服务端模式

-p：指定iperf测试端口

-i：指定报告间隔

-b：设置UDP的发送带宽，单位bit/s

-t：设置测试的时长，单位为秒，不设置默认10s

-l：指定包大小，TCP默认8k，UDP默认1470字节

- ping

## 内存

1. swap空间

首先获取pid,进入/proc/${pid}目录,找到smaps文件
```shell
cat smaps | egrep '^(Swap|Size)'
```
排查是否有内存数据进入了swap空间.