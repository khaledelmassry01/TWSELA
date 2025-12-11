package com.twsela.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.cache.type:redis}")
    private String cacheType;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        if ("simple".equals(cacheType)) {
            // Use simple cache for development when Redis is not available
            return new ConcurrentMapCacheManager("users", "roles", "zones", "shipmentStatuses", "pricing", "dashboard", "statistics");
        }
        
        // Use Redis cache for production
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - 30 minutes
        cacheConfigurations.put("users", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Role cache - 1 hour
        cacheConfigurations.put("roles", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
        
        // Zone cache - 1 hour
        cacheConfigurations.put("zones", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
        
        // Shipment status cache - 1 hour
        cacheConfigurations.put("shipmentStatuses", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
        
        // Pricing cache - 2 hours
        cacheConfigurations.put("pricing", defaultCacheConfig.entryTtl(Duration.ofHours(2)));
        
        // Dashboard data cache - 5 minutes
        cacheConfigurations.put("dashboard", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Statistics cache - 10 minutes
        cacheConfigurations.put("statistics", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
