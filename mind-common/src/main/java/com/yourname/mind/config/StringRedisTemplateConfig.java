package com.yourname.mind.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yourname.mind.aop.config.CacheContextHolder;
import com.yourname.mind.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Random;

@Slf4j
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
            try {
                String json = stringRedisTemplate.opsForValue().get(key);

                if (!StrUtil.isBlank(json) && !RedisConstant.CACHE_NULL_OBJECT.equals(json)) {
                    CacheContextHolder.setCacheHit(true);
                    CacheContextHolder.setCacheNull(false);
                    return JSONUtil.toBean(json, clazz);
                }
                if (RedisConstant.CACHE_NULL_OBJECT.equals(json)) {
                    CacheContextHolder.setCacheHit(true);
                    CacheContextHolder.setCacheNull(true);
                }else {
                    CacheContextHolder.setCacheHit(false);
                }
                return null;
            } catch (Exception e) {
                if (e instanceof RedisConnectionFailureException ||
                        e instanceof SerializationException ||
                        e instanceof RedisSystemException) {

                    // 2. 记录缓存异常状态（监控用）
                    CacheContextHolder.setCacheHit(false); // 异常视为未命中
                    CacheContextHolder.setCacheNull(false);
                    CacheContextHolder.setCacheException(true); // 新增：标记缓存异常

                    // 3. 打日志（记录关键信息，便于排查）
                    log.error("Redis GET操作异常，key={}", key, e);

                    // 4. 返回null（缓存层降级，不影响业务层）
                    return null;
                }
            }
            return null;
        }

        public <T> T get(String key, Type type) {
            try {
                String json = stringRedisTemplate.opsForValue().get(key);

                if (!StrUtil.isBlank(json) && !RedisConstant.CACHE_NULL_OBJECT.equals(json)) {
                    CacheContextHolder.setCacheHit(true);
                    CacheContextHolder.setCacheNull(false);
                    return JSONUtil.toBean(json, type, false);
                }
                if (RedisConstant.CACHE_NULL_OBJECT.equals(key)) {
                    CacheContextHolder.setCacheHit(true);
                    CacheContextHolder.setCacheNull(true);
                }else {
                    CacheContextHolder.setCacheHit(false);
                }
                return null;
            } catch (Exception e) {
                if (e instanceof RedisConnectionFailureException ||
                        e instanceof SerializationException ||
                        e instanceof RedisSystemException) {

                    // 2. 记录缓存异常状态（监控用）
                    CacheContextHolder.setCacheHit(false); // 异常视为未命中
                    CacheContextHolder.setCacheNull(false);
                    CacheContextHolder.setCacheException(true); // 新增：标记缓存异常

                    // 3. 打日志（记录关键信息，便于排查）
                    log.error("Redis GET操作异常，key={}", key, e);

                    // 4. 返回null（缓存层降级，不影响业务层）
                    return null;
                }
            }
            return null;
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