# nacos
[toc]
### nacos naming 服务注册中心
naming默认使用ap模式,采用阿里自定义协议distro.

#### distro协议
distro协议被定位为临时数据一致性协议.该协议下,不需要把数据存储到磁盘或者数据库,因为临时
数据通常和服务器保持一个session会话,只要会话存在,数据就不丢失.

***distro协议内容如下:***
- 客户端与服务端有两个重要的交互,服务注册与心跳发送
- 客户端以服务为维度向服务端注册,注册后每隔一段时间向服务端发送一次心跳,心跳包需要带上注册服务的全部信息.在客户端看来,服务端节点对等,所以请求的节点时随机的.
- 客户端请求失败则换一个节点重新发起请求
- 服务端节点存储所有数据,但每个节点只负责其中一部分服务,在接收到客户端"写"(注册,上线,下线)请求后,服务端节点判断请求的服务是否为自己负责,
  如果是,则处理,否则交给负责节点处理.
- 每个服务端主动发送健康检查到其他服务节点,有响应的节点被视为健康节点.
- 服务端在收到客户端服务的心跳后,如果该服务不存在,则将该心跳请求视为注册请求来处理
- 服务端如果长时间未收到客户端心跳后,则下线该服务
- 负责的服务端节点在接收到服务注册,服务心跳等写请求后将数据写入后返回,后台异步同步数据到其他节点
- 服务端节点在收到读请求后直接从本机获取后返回,无论数据是否为最新

***从源码中解读distro协议:***
- 服务注册

```NacosNamingService```提供了服务注册接口,最终调用```serverProxy```完成服务注册
```java
public void registerInstance(String serviceName, String groupName, Instance instance) throws NacosException {
    NamingUtils.checkInstanceIsLegal(instance);
    String groupedServiceName = NamingUtils.getGroupedName(serviceName, groupName);
    if (instance.isEphemeral()) {
        BeatInfo beatInfo = beatReactor.buildBeatInfo(groupedServiceName, instance);
        beatReactor.addBeatInfo(groupedServiceName, beatInfo);
    }
    serverProxy.registerService(groupedServiceName, groupName, instance);
}
```

- 服务获取
```java
/**
 * distro协议:服务端节点在收到读请求后直接从本机获取后返回,无论数据是否为最新
 */
```

- 心跳发送
```java
/**
 * 
 *BeatReactor.java
 *客户端心跳自动任务
 * distro协议:客户端以服务为维度向服务端注册,注册后每隔一段时间向服务端发送一次心跳,心跳包需要带上注册
 服务的全部信息
**/
public void run() {
    if (beatInfo.isStopped()) {
        return;
    }
    long nextTime = beatInfo.getPeriod();
    try {
        //发送心跳,如果lightBeatEnabled为TRUE,则不包含服务信息
        JsonNode result = serverProxy.sendBeat(beatInfo, BeatReactor.this.lightBeatEnabled);
        //获取心跳间隔时间
        long interval = result.get("clientBeatInterval").asLong();
        boolean lightBeatEnabled = false;
        //lightBeatEnabled可以被服务重置
        if (result.has(CommonParams.LIGHT_BEAT_ENABLED)) {
            lightBeatEnabled = result.get(CommonParams.LIGHT_BEAT_ENABLED).asBoolean();
        }
        BeatReactor.this.lightBeatEnabled = lightBeatEnabled;
        if (interval > 0) {
            nextTime = interval;
        }
        int code = NamingResponseCode.OK;
        if (result.has(CommonParams.CODE)) {
            code = result.get(CommonParams.CODE).asInt();
        }
        //如果服务端没有发现该服务,则注册该服务,当lightBeatEnabled为true时触发
        if (code == NamingResponseCode.RESOURCE_NOT_FOUND) {
            Instance instance = new Instance();
            instance.setPort(beatInfo.getPort());
            instance.setIp(beatInfo.getIp());
            instance.setWeight(beatInfo.getWeight());
            instance.setMetadata(beatInfo.getMetadata());
            instance.setClusterName(beatInfo.getCluster());
            instance.setServiceName(beatInfo.getServiceName());
            instance.setInstanceId(instance.getInstanceId());
            //如果切换cp模式,这里写死会有问题.
            instance.setEphemeral(true);
            try {
                serverProxy.registerService(beatInfo.getServiceName(),
                        NamingUtils.getGroupName(beatInfo.getServiceName()), instance);
            } catch (Exception ignore) {
            }
        }
    } catch (NacosException ex) {
        NAMING_LOGGER.error("[CLIENT-BEAT] failed to send beat: {}, code: {}, msg: {}",
                JacksonUtils.toJson(beatInfo), ex.getErrCode(), ex.getErrMsg());
        
    }
    //下一次心跳任务
    executorService.schedule(new BeatTask(beatInfo), nextTime, TimeUnit.MILLISECONDS);
}

/**
*服务端接收心跳
**/
public ObjectNode beat(HttpServletRequest request) throws Exception {

    //略...
    //查询服务    
    Instance instance = serviceManager.getInstance(namespaceId, serviceName, clusterName, ip, port);
    //服务未找到
    if (instance == null) {
        //心跳不包含服务信息,返回not_found
        if (clientBeat == null) {
            result.put(CommonParams.CODE, NamingResponseCode.RESOURCE_NOT_FOUND);
            return result;
        }

        Loggers.SRV_LOG.warn("[CLIENT-BEAT] The instance has been removed for health mechanism, "
        + "perform data compensation operations, beat: {}, serviceName: {}", clientBeat, serviceName);
        //心跳包包含服务信息,则注册服务
        //distro协议:服务端在收到客户端服务的心跳后,如果该服务不存在,则将该心跳请求视为注册请求来处理
        instance = new Instance();
        instance.setPort(clientBeat.getPort());
        instance.setIp(clientBeat.getIp());
        instance.setWeight(clientBeat.getWeight());
        instance.setMetadata(clientBeat.getMetadata());
        instance.setClusterName(clusterName);
        instance.setServiceName(serviceName);
        instance.setInstanceId(instance.getInstanceId());
        instance.setEphemeral(clientBeat.isEphemeral());
    
        serviceManager.registerInstance(namespaceId, serviceName, instance);
    }

    Service service = serviceManager.getService(namespaceId, serviceName);

    if (service == null) {
    throw new NacosException(NacosException.SERVER_ERROR,
    "service not found: " + serviceName + "@" + namespaceId);
    }
    if (clientBeat == null) {
        clientBeat = new RsInfo();
        clientBeat.setIp(ip);
        clientBeat.setPort(port);
        clientBeat.setCluster(clusterName);
    }
    //监控信息更新,更新最后心跳时间
    service.processClientBeat(clientBeat);

    result.put(CommonParams.CODE, NamingResponseCode.OK);
    if (instance.containsMetadata(PreservedMetadataKeys.HEART_BEAT_INTERVAL)) {
        result.put(SwitchEntry.CLIENT_BEAT_INTERVAL, instance.getInstanceHeartBeatInterval());
    }
    result.put(SwitchEntry.LIGHT_BEAT_ENABLED, switchDomain.isLightBeatEnabled());
    return result;
}
```

- 客户端失败重试机制
```java
/**
 * 客户端调用服务端最终接口
 * distro协议:在客户端看来,服务端节点对等,所以请求的节点时随机的.
 */
public class NamingProxy implements Closeable {
    public String reqApi(String api, Map<String, String> params, Map<String, String> body, List<String> servers,
                         String method) throws NacosException {

        params.put(CommonParams.NAMESPACE_ID, getNamespaceId());

        if (CollectionUtils.isEmpty(servers) && StringUtils.isBlank(nacosDomain)) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }

        NacosException exception = new NacosException();
        //如果只有一个服务端,nacosDomain不为空,否则为空
        if (StringUtils.isNotBlank(nacosDomain)) {
            //默认会重试3次
            for (int i = 0; i < maxRetry; i++) {
                try {
                    return callServer(api, params, body, nacosDomain, method);
                } catch (NacosException e) {
                    exception = e;
                    if (NAMING_LOGGER.isDebugEnabled()) {
                        NAMING_LOGGER.debug("request {} failed.", nacosDomain, e);
                    }
                }
            }
        } else {
            //随机访问服务端,如果失败,发送给其他服务端
            //distro协议:客户端请求失败则换一个节点重新发起请求
            Random random = new Random(System.currentTimeMillis());
            int index = random.nextInt(servers.size());

            for (int i = 0; i < servers.size(); i++) {
                String server = servers.get(index);
                try {
                    return callServer(api, params, body, server, method);
                } catch (NacosException e) {
                    exception = e;
                    if (NAMING_LOGGER.isDebugEnabled()) {
                        NAMING_LOGGER.debug("request {} failed.", server, e);
                    }
                }
                index = (index + 1) % servers.size();
            }
        }

        NAMING_LOGGER.error("request: {} failed, servers: {}, code: {}, msg: {}", api, servers, exception.getErrCode(),
                            exception.getErrMsg());

        throw new NacosException(exception.getErrCode(),
                                 "failed to req API:" + api + " after all servers(" + servers + ") tried: " + exception.getMessage());

    }
}
```

- 客户端缓存服务端信息刷新机制

在distro协议中,在客户端看来,服务端节点对等,所以请求节点时随机的.那么客户端需要有服务端节点的
所有信息.在```NamingProxy```初始化的时候会请求一份服务端节点信息数据,后续定时更新.
```java
public class NamingProxy implements Closeable {
    public NamingProxy(String namespaceId, String endpoint, String serverList, Properties properties) {

        this.securityProxy = new SecurityProxy(properties, nacosRestTemplate);
        this.properties = properties;
        this.setServerPort(DEFAULT_SERVER_PORT);
        this.namespaceId = namespaceId;
        this.endpoint = endpoint;
        this.maxRetry = ConvertUtils.toInt(properties.getProperty(PropertyKeyConst.NAMING_REQUEST_DOMAIN_RETRY_COUNT,
                                                                  String.valueOf(UtilAndComs.REQUEST_DOMAIN_RETRY_COUNT)));
        if (StringUtils.isNotEmpty(serverList)) {
            this.serverList = Arrays.asList(serverList.split(","));
            if (this.serverList.size() == 1) {
                this.nacosDomain = serverList;
            }
        }
        //在NamingProxy初始化的时候,创建定时任务并获取服务端节点信息
        this.initRefreshTask();
    }

    private void initRefreshTask() {
        //创建定时任务,可以看到,线程池的线程数量是写死的.
        this.executorService = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.naming.updater");
                t.setDaemon(true);
                return t;
            }
        });

        this.executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //默认30s,更新一次
                refreshSrvIfNeed();
            }
        }, 0, vipSrvRefInterMillis, TimeUnit.MILLISECONDS);

        this.executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                securityProxy.login(getServerList());
            }
        }, 0, securityInfoRefreshIntervalMills, TimeUnit.MILLISECONDS);

        //获取服务端节点信息
        refreshSrvIfNeed();
        this.securityProxy.login(getServerList());
    }
}
```

- 服务健康检查任务
```java
/**
 * distro协议:服务端如果长时间未收到客户端心跳后,则下线该服务
 */
public class Service extends com.alibaba.nacos.api.naming.pojo.Service implements Record, RecordListener<Instances> {
    @JsonIgnore
    private ClientBeatCheckTask clientBeatCheckTask = new ClientBeatCheckTask(this);
    
}

public class ClientBeatCheckTask implements Runnable {
    @Override
    public void run() {
        try {
            if (!getDistroMapper().responsible(service.getName())) {
                return;
            }

            if (!getSwitchDomain().isHealthCheckEnabled()) {
                return;
            }

            List<Instance> instances = service.allIPs(true);

            // first set health status of instances:
            for (Instance instance : instances) {
                if (System.currentTimeMillis() - instance.getLastBeat() > instance.getInstanceHeartBeatTimeOut()) {
                    if (!instance.isMarked()) {
                        if (instance.isHealthy()) {
                            instance.setHealthy(false);
                            getPushService().serviceChanged(service);
                            ApplicationUtils.publishEvent(new InstanceHeartbeatTimeoutEvent(this, instance));
                        }
                    }
                }
            }

            if (!getGlobalConfig().isExpireInstance()) {
                return;
            }

            // then remove obsolete instances:
            for (Instance instance : instances) {

                if (instance.isMarked()) {
                    continue;
                }

                if (System.currentTimeMillis() - instance.getLastBeat() > instance.getIpDeleteTimeout()) {
                    // delete instance
                    //ap模式下,每个服务节点负责部分数据的持久化,所以需要调用api来删除数据
                    deleteIp(instance);
                }
            }

        } catch (Exception e) {
            Loggers.SRV_LOG.warn("Exception while processing client beat time out.", e);
        }

    }
}
```

- 每个服务节点负责一部分数据

  在ap模式下,每个服务节点负责一部分数据,在nacos中,所有请求由```@CanDistro```拦截,判断哪个节点负责
  这个服务,然后转发.
  触发转发的操作有:注册,取消注册(心跳检测失败),服务更新.

  ```java
  public class ServiceManager implements RecordListener<Service> {
          /**
          *服务注册
          **/
          public void addInstance(String namespaceId, String serviceName, boolean ephemeral, Instance... ips)
              throws NacosException {
          
          String key = KeyBuilder.buildInstanceListKey(namespaceId, serviceName, ephemeral);
          
          Service service = getService(namespaceId, serviceName);
          
          synchronized (service) {
              List<Instance> instanceList = addIpAddresses(service, ephemeral, ips);
              
              Instances instances = new Instances();
              instances.setInstanceList(instanceList);
              //在这里,ephemeral=true,所以是distro协议的持久化,异步发送注册信息给其他服务端节点
              consistencyService.put(key, instances);
          }
      }
      
      /**
      *服务下线
      */
      private void removeInstance(String namespaceId, String serviceName, boolean ephemeral, Service service,
              Instance... ips) throws NacosException {
          
          String key = KeyBuilder.buildInstanceListKey(namespaceId, serviceName, ephemeral);
          
          List<Instance> instanceList = substractIpAddresses(service, ephemeral, ips);
          
          Instances instances = new Instances();
          instances.setInstanceList(instanceList);
          //同服务注册
          consistencyService.put(key, instances);
      }
  }
  
  /**
  *distro协议对应的持久化服务
  */
  public class DistroConsistencyServiceImpl implements EphemeralConsistencyService, DistroDataProcessor {
      @Override
      public void put(String key, Record value) throws NacosException {
          //更新本地
          onPut(key, value);
          //异步同步信息给服务端节点
          //这里的话,任务延迟执行,如果短时间内有相同的任务会合并处理,有效减少task数量,默认延迟1s执行,失败重试延迟3s执行.
          distroProtocol.sync(new DistroKey(key, KeyBuilder.INSTANCE_LIST_KEY_PREFIX), DataOperation.CHANGE,
                  globalConfig.getTaskDispatchPeriod() / 2);
      }
  }
  ```

- nacos集群节点定时报告任务

  nacos通过节点定时报告来同步nacos服务节点的健康状态,超过3次未报告或者异常为```Connection refused```,将节点状态设置为```DOWN```.
  参数```nacos.core.member.fail-access-cnt```可配置失败次数.未超过指定次数,将节点状态设置为```SUSPICIOUS```.
  该定时任务2s运行一次,线程名为```com.alibaba.nacos.core.common```,线程数据为```4```,不可配置.
  pis:每2s向其中一个集群节点报告状态,不是向所有节点
```java
class MemberInfoReportTask extends Task {
    private final GenericType<RestResult<String>> reference = new GenericType<RestResult<String>>() {
    };

    private int cursor = 0;

    @Override
    protected void executeBody() {
        List<Member> members = ServerMemberManager.this.allMembersWithoutSelf();

        if (members.isEmpty()) {
            return;
        }

        this.cursor = (this.cursor + 1) % members.size();
        Member target = members.get(cursor);

        Loggers.CLUSTER.debug("report the metadata to the node : {}", target.getAddress());

        final String url = HttpUtils
                .buildUrl(false, target.getAddress(), EnvUtil.getContextPath(), Commons.NACOS_CORE_CONTEXT,
                          "/cluster/report");

        try {
            asyncRestTemplate
                    .post(url, Header.newInstance().addParam(Constants.NACOS_SERVER_HEADER, VersionUtils.version),
                          Query.EMPTY, getSelf(), reference.getType(), new Callback<String>() {
                                @Override
                                public void onReceive(RestResult<String> result) {
                                    if (result.getCode() == HttpStatus.NOT_IMPLEMENTED.value()
                                            || result.getCode() == HttpStatus.NOT_FOUND.value()) {
                                        Loggers.CLUSTER
                                                .warn("{} version is too low, it is recommended to upgrade the version : {}",
                                                      target, VersionUtils.version);
                                        return;
                                    }
                                    if (result.ok()) {
                                        MemberUtil.onSuccess(ServerMemberManager.this, target);
                                    } else {
                                        Loggers.CLUSTER
                                                .warn("failed to report new info to target node : {}, result : {}",
                                                      target.getAddress(), result);
                                        MemberUtil.onFail(ServerMemberManager.this, target);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    Loggers.CLUSTER
                                            .error("failed to report new info to target node : {}, error : {}",
                                                   target.getAddress(),
                                                   ExceptionUtil.getAllExceptionMsg(throwable));
                                    MemberUtil.onFail(ServerMemberManager.this, target, throwable);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
        } catch (Throwable ex) {
            Loggers.CLUSTER.error("failed to report new info to target node : {}, error : {}", target.getAddress(),
                                  ExceptionUtil.getAllExceptionMsg(ex));
        }
    }

    @Override
    protected void after() {
        GlobalExecutor.scheduleByCommon(this, 2_000L);
    }
}
```

- nacos注册服务定时报告任务
  
  在ap模式下,每个nacos服务端负责部分数据,而在获取注册服务的时候是从本地直接返回.所以需要nacos服务端定时向其他服务节点同步数据. 
  虽然nacos节点在服务上下线的时候会异步通知其他节点,但是并不保证一定会通知到,有报错重试机制.
```java
//ServiceManager.java
private class ServiceReporter implements Runnable {
    @Override
    public void run() {
        try {

            Map<String, Set<String>> allServiceNames = getAllServiceNames();

            if (allServiceNames.size() <= 0) {
                //ignore
                return;
            }

            for (String namespaceId : allServiceNames.keySet()) {

                ServiceChecksum checksum = new ServiceChecksum(namespaceId);

                for (String serviceName : allServiceNames.get(namespaceId)) {
                    if (!distroMapper.responsible(serviceName)) {
                        continue;
                    }

                    Service service = getService(namespaceId, serviceName);

                    if (service == null || service.isEmpty()) {
                        continue;
                    }

                    service.recalculateChecksum();

                    checksum.addItem(serviceName, service.getChecksum());
                }

                Message msg = new Message();

                msg.setData(JacksonUtils.toJson(checksum));

                Collection<Member> sameSiteServers = memberManager.allMembers();

                if (sameSiteServers == null || sameSiteServers.size() <= 0) {
                    return;
                }

                for (Member server : sameSiteServers) {
                    if (server.getAddress().equals(NetUtils.localServer())) {
                        continue;
                    }
                    synchronizer.send(server.getAddress(), msg);
                }
            }
        } catch (Exception e) {
            Loggers.SRV_LOG.error("[DOMAIN-STATUS] Exception while sending service status", e);
        } finally {
            //默认5s执行一次,如果单次执行时间过长,下次任务也会延迟
            GlobalExecutor.scheduleServiceReporter(this, switchDomain.getServiceStatusSynchronizationPeriodMillis(),
                                                   TimeUnit.MILLISECONDS);
        }
    } 
}
```  
  服务端在接收到/service/status请求后,会加入```toBeUpdatedServicesQueue```,最终由```com.alibaba.nacos.naming.service.update.http.handler```
线程池执行.如果队列已满,会有warning日志警告,可以留意日志.

### nacos config 配置管理中心


