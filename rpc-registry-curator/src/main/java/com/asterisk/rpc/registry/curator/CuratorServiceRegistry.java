package com.asterisk.rpc.registry.curator;

import com.asterisk.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import static com.asterisk.rpc.registry.curator.Constants.REGISTRY;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
@Slf4j
public class CuratorServiceRegistry implements ServiceRegistry {


    private CuratorFramework client;


    public CuratorServiceRegistry(String zkAddress, int timeout) {
        client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 3000))
                .connectionTimeoutMs(timeout).connectString(zkAddress).build();
        client.start();
        log.info("zookeeper started ...");

    }


    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            if (client.checkExists().forPath(REGISTRY) == null) {
                client.create().forPath(REGISTRY);
                log.info("create registry node {}", REGISTRY);
            }
            String servicePath = REGISTRY + "/" + serviceName;
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
                log.info("create service node:{}", servicePath);
            }
            String addressPath = servicePath + "/address-";
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(addressPath, serviceAddress.getBytes());
            log.info("create address node:{}", addressPath);
        } catch (Exception e) {
            log.error("zookeeper occurred error", e);
        }
    }
}
