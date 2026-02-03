package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.exception.YahooFinanceException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("!test")
public class YahooFinanceClient implements YahooFinanceApi {

    private final RestClient restClient;

    public YahooFinanceClient(@Qualifier("yahooFinanceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public GetStocksHistoryResponseDTO getStocksHistory(@NotNull String symbol, @NotNull IntervalType interval, Integer limit, Boolean dividend) {

        var uriBuilder = UriComponentsBuilder.fromPath("/api/v2/markets/stock/history")
                .queryParam("symbol", symbol)
                .queryParam("interval", interval.getCode());

        if (limit != null) {
            uriBuilder.queryParam("limit", limit);
        }
        if (dividend != null) {
            uriBuilder.queryParam("dividend", dividend);
        }

        return restClient.get()
                .uri(uriBuilder::build)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (_, res) -> {
                    throw new YahooFinanceException(
                            "Error 4xx from Yahoo Finance: " + res.getStatusCode()
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    throw new YahooFinanceException(
                            "Error 5xx from Yahoo Finance: " + res.getStatusCode()
                    );
                })
                .body(GetStocksHistoryResponseDTO.class);
    }

    @Override
    public MarketSearchResponseDTO searchMarket(@NotNull String search) {

        var uriBuilder = UriComponentsBuilder.fromPath("/api/v1/markets/search")
                .queryParam("search", search);

        return restClient.get()
                .uri(uriBuilder::build)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (_, res) -> {
                    throw new YahooFinanceException(
                            "Error 4xx from Yahoo Finance: " + res.getStatusCode()
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    throw new YahooFinanceException(
                            "Error 5xx from Yahoo Finance: " + res.getStatusCode()
                    );
                })
                .body(MarketSearchResponseDTO.class);
    }
}
