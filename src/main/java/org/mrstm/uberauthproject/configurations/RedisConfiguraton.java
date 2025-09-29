//package org.mrstm.uberauthproject.configurations;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jmx.export.annotation.ManagedResource;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.JedisPoolConfig;
//import redis.clients.jedis.Protocol;
//
//@Configuration
//public class RedisConfiguraton {
//    @Bean
//    public JedisPool jedisPool(
//            @Value("${spring.data.redis.host}") String host,
//            @Value("${spring.data.redis.port}") int port,
//            @Value("${spring.data.redis.database}") int database
//    ){
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxTotal(50);       // max connections
//        poolConfig.setMaxIdle(10);        // max idle connections
//        poolConfig.setMinIdle(2);         // minimum idle
//        poolConfig.setTestOnBorrow(true); // validate connection
//
//        return new JedisPool(poolConfig , host, port , Protocol.DEFAULT_TIMEOUT, null, database);
//    }
//}
