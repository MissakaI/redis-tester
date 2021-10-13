package com.missakai.redistester.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Missaka Iddamalgoda (@MissakaI-ObjectOne)
 */
@Configuration
public class RedisCacheConfig {

    /**
     * Creates a RedisCacheConfiguration with the support for String Keys and Object Values which will be serialized and stored as
     * a JSON and vice versa.
     *
     * @param objectMapper DI of the default Spring's ObjectMapper Bean
     * */
    @Bean
    @Primary
    public RedisCacheConfiguration defaultCacheConfig(ObjectMapper objectMapper){
        GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
    }
}
