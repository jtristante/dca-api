package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!test")
public class YahooFinanceClient {

    private final RestClient restClient;

    public YahooFinanceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String getHistoryRaw(String symbol, String interval, int limit, boolean dividend) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/markets/stock/history")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", interval)
                        .queryParam("limit", limit)
                        .queryParam("dividend", dividend)
                        .build())
                .retrieve()
                .body(String.class);
    }
}
