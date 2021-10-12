package com.missakai.redistester.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.missakai.redistester.dto.KeyValuePairDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author Missaka Iddamalgoda (@MissakaI-ObjectOne)
 */
@RestController
@RequestMapping("/api")
public class RedisController {

    RedisTemplate<String,Object> redisTemplate;

    public RedisController(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PutMapping
    public void setEx(@RequestBody KeyValuePairDto dto){
        redisTemplate.opsForValue().set(dto.getKey(),dto.getValue());
    }

    @GetMapping("/{key}")
    public JsonNode get(@PathVariable String key){
        return (JsonNode) redisTemplate.opsForValue().get(key);
    }

}
