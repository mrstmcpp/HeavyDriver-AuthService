package org.mrstm.uberauthproject.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    @Value("${jwt.expiry}")
    private long seconds;

    public RedisService(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.database}") int database,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(
                poolConfig,
                host,
                port,
                Protocol.DEFAULT_TIMEOUT,
                password.isEmpty() ? null : password,
                database
        );
    }

    public void setValue(String jwtToken, String username, String userId, String role, String name) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> data = new HashMap<>();
            data.put("username", username);
            data.put("userId", userId);
            data.put("role", role);
            data.put("name" , name);
            String json = objectMapper.writeValueAsString(data);
            jedis.set(jwtToken, json);
            jedis.expire(jwtToken, seconds / 1000);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to store JWT in Redis", e);
        }
    }

    public void setValue(String jwtToken, String username, String userId , String name) {
        setValue(jwtToken, username, userId, name);
    }

    public Map<String, String> getValue(String jwtToken) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(jwtToken);
            if (json == null) return null;
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error retrieving JWT from Redis", e);
        }
    }

    public Boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}
