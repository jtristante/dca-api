package dev.jtristante.dcaapi.config;

import dev.jtristante.dcaapi.infrastructure.rapidapi.RapidApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(RapidApiProperties.class)
public class RapidApiClientConfig {

    @Bean
    public RestClient rapidApiRestCLient(RapidApiProperties props) {
        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-rapidAPI-key", props.getKey())
                .defaultHeader("x-rapidAPI-host", props.getHost())
                .build();
    }
}
