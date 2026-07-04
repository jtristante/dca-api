package dev.jtristante.dcaapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> {
            cacheManager.registerCustomCache("ohlcvData",
                    Caffeine.newBuilder()
                            .maximumSize(500)
                            .expireAfterWrite(24, TimeUnit.HOURS)
                            .recordStats()
                            .build());
            cacheManager.registerCustomCache("symbolByTicker",
                    Caffeine.newBuilder()
                            .maximumSize(1000)
                            .expireAfterWrite(1, TimeUnit.HOURS)
                            .recordStats()
                            .build());
            cacheManager.registerCustomCache("symbolSearch",
                    Caffeine.newBuilder()
                            .maximumSize(500)
                            .expireAfterWrite(24, TimeUnit.HOURS)
                            .recordStats()
                            .build());
        };
    }
}
