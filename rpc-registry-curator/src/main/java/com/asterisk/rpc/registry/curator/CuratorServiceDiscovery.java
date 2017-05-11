package com.asterisk.rpc.registry.curator;

import com.asterisk.rpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.asterisk.rpc.registry.curator.Constants.REGISTRY;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
@Slf4j
public class CuratorServiceDiscovery implements ServiceDiscovery {



    private CuratorFramework client;

    public CuratorServiceDiscovery(String zkAddress,int sessionTimeout,int connTimeout) {
        client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(Integer.MAX_VALUE,3000))
                .connectionTimeoutMs(connTimeout)
                .sessionTimeoutMs(sessionTimeout)
                .connectString(zkAddress).build();
        client.start();
        log.info("zookeeper client started ...");
    }

    @Override
    public String discover(String serviceName) {
        String servicePath = REGISTRY + "/" + serviceName;
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                throw new RuntimeException(String.format("can not find any service node on path :%s",servicePath));
            }
            List<String> addressList = client.getChildren().forPath(servicePath);
            if (addressList == null || addressList.size() == 0) {
                throw new RuntimeException(String.format("can not find any address on path : %s",servicePath));
            }
            String address;
            if (addressList.size() == 1) {
                address = addressList.get(0);
                log.info("only get one address:{}",address);
            } else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
                log.info("random change one address:{}",address);
            }
            String addressPath = servicePath + "/" + address;
            return new String(client.getData().forPath(addressPath));
        } catch (Exception e) {
            log.error("zookeeper error",e);
        }
        return null;
    }
}
