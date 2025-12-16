package com.yourname.mind.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yourname.mind.common.constant.RedisConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Random;

@Configuration
public class StringRedisTemplateConfig {


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        
        // 强制指定String序列化（避免乱码）
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();
        return template;
    }

    // 2. 封装缓存工具类（核心：用Hutool JSONUtil实现序列化）
    @Bean
    public RedisCacheUtils redisCacheUtils(StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheUtils(stringRedisTemplate);
    }


    public static class RedisCacheUtils {
        private final StringRedisTemplate stringRedisTemplate;
        private final Random random = new Random();

        public RedisCacheUtils(StringRedisTemplate stringRedisTemplate) {
            this.stringRedisTemplate = stringRedisTemplate;
        }


        public <T> void setWithRandomExpire(String key, T obj, long baseExpireSeconds) {
            if (obj == null) {
                throw new RuntimeException("缓存对象不能为空：" + key);
            }

            String json = JSONUtil.toJsonStr(obj);
            // 基础过期 + 0-10分钟随机值（防雪崩核心）
            long randomExpire = baseExpireSeconds + random.nextInt(10 * 60);
            stringRedisTemplate.opsForValue().set(key, json, randomExpire);
        }

        public <T> void setWithKeep(String key, T obj) {
            if (obj == null) {
                throw new RuntimeException("缓存对象不能为空：" + key);
            }
            String json = JSONUtil.toJsonStr(obj);
            stringRedisTemplate.opsForValue().set(key, json);
        }


        public <T> T get(String key, Class<T> clazz) {
            String json = stringRedisTemplate.opsForValue().get(key);

            if (StrUtil.isBlank(json) || RedisConstant.CACHE_NULL_OBJECT.equals(json)) {
                return null;
            }
            return JSONUtil.toBean(json, clazz);
        }

        public <T> T get(String key, Type type) {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isBlank(json) || "null".equals(json)) {
                return null;
            }

            return JSONUtil.toBean(json,type,false);
        }


        public void setEmptyValue(String key, long expireSeconds) {
            stringRedisTemplate.opsForValue().set(key, RedisConstant.CACHE_NULL_OBJECT, expireSeconds);
        }


        public boolean exists(String key) {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        }

        public void delete(String key) {
            stringRedisTemplate.delete(key);
        }

        public void deleteBatch(Collection<String> keys) {
            stringRedisTemplate.delete(keys);
        }
    }
}