package dev.jtristante.dcaapi.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class YahooFinanceConfig {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceConfig.class);

    @Bean
    public RestClient yahooFinanceRestClient(
            @Value("${rapidapi.yahoo.base-url}") String baseUrl,
            @Value("${rapidapi.yahoo.key}") String apiKey,
            @Value("${rapidapi.yahoo.host}") String host
    ) {
        HttpClient httpClient = HttpClientBuilder.create()
                .disableCookieManagement()
                .build();

        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .requestInterceptor((request, body, execution) -> {
                    log.info("Calling Yahoo Finance: {}", request.getURI());
                    return execution.execute(request, body);
                })
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", host)
                .build();
    }
}
