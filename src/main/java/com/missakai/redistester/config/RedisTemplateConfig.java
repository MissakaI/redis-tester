package com.missakai.redistester.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Missaka Iddamalgoda (@MissakaI-ObjectOne)
 */
@Configuration
public class RedisTemplateConfig {

    /**
     * Creates a RedisTemplate with the support for String Keys and Object Values which will be serialized and stored as
     * a JSON and vice versa.
     *
     * @param redisConnectionFactory DI of an available Bean from the Spring Context
     * @param objectMapper DI of the default Spring's ObjectMapper Bean
     * */
    @Bean
    public RedisTemplate<String,Object> redisTemplateStringObject(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper){
        RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return redisTemplate;
    }

}
