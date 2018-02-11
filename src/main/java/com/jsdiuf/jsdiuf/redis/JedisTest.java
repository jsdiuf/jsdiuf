package com.jsdiuf.jsdiuf.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author weicc
 * @create 2018-02-06 11:31
 **/
public class JedisTest {
    public static void main(String[] args) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大连接数
        poolConfig.setMaxTotal(1);
        // 最大空闲数
        poolConfig.setMaxIdle(1);
        // 最大允许等待时间，如果超过这个时间还未获取到连接，则会报JedisException异常：
        // Could not get a resource from the pool
        poolConfig.setMaxWaitMillis(1000);
        Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
        nodes.add(new HostAndPort("192.168.154.129", 7005));
      /*  nodes.add(new HostAndPort("192.168.154.128", 7001));
        nodes.add(new HostAndPort("192.168.154.128", 7002));
        nodes.add(new HostAndPort("192.168.154.129", 7003));
        nodes.add(new HostAndPort("192.168.154.129", 7004));
        nodes.add(new HostAndPort("192.168.154.129", 7005));*/
        JedisCluster cluster = new JedisCluster(nodes,10,10,10,"123", poolConfig);

        String name = cluster.get("name");
        System.out.println(name);
        cluster.set("age", "18");
        System.out.println(cluster.get("age"));
        try {
            cluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
