package com.shield.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxTotal(5);
//        poolConfig.setTestOnBorrow(true);
//        poolConfig.setTestOnReturn(true);
//
//        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(poolConfig);
//        connectionFactory.setUsePool(true);
//        connectionFactory.setHostName("localhost");
//        connectionFactory.setPort(6379);
//
//        return connectionFactory;
//    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean("redisLongTemplate")
    public RedisTemplate<String, Long> redisLongTemplate() {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}