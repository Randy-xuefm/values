# linux 网络问题排查

### TCP
- backlog队列溢出
  netstat -s | grep overflowed
- 判断网卡是否支持多队列