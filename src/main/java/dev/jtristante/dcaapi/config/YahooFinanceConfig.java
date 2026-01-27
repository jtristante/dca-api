package dev.jtristante.dcaapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class YahooFinanceConfig {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceConfig.class);

    HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Bean
    public RestClient yahooFinanceRestClient(
            @Value("${rapidapi.yahoo.base-url}") String baseUrl,
            @Value("${rapidapi.yahoo.key}") String apiKey,
            @Value("${rapidapi.yahoo.host}") String host
    ) {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor((request, body, execution) -> {
                    log.info("Calling Yahoo Finance: {}", request.getURI());
                    return execution.execute(request, body);
                })
                .baseUrl(baseUrl)
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .defaultHeader("X-RapidAPI-Host", host)
                .build();
    }
}
