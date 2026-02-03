package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.QuoteType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.MarketSearchMetadataDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.metadata.StockHistoryMetadataDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class TestYahooFinanceClient implements YahooFinanceApi {

    @Override
    public GetStocksHistoryResponseDTO getStocksHistory(@NotNull String symbol, @NotNull IntervalType interval, Integer limit, Boolean dividend) {
        return new GetStocksHistoryResponseDTO(
                new StockHistoryMetadataDTO("v1.0", 200, symbol, interval.getCode(), dividend),
                List.of()
        );
    }

    @Override
    public MarketSearchResponseDTO searchMarket(@NotNull String search) {
        return new MarketSearchResponseDTO(
                new MarketSearchMetadataDTO("v1.0", 200, "https://steadyapi.com", search, "2026-02-03T00:00:00Z"),
                List.of(
                        new MarketSearchResultDTO(
                                "Bitcoin USD",
                                QuoteType.CRYPTOCURRENCY,
                                "BTC-USD",
                                "quotes",
                                43153.6,
                                "Cryptocurrency",
                                "Bitcoin USD",
                                "CCC",
                                null,
                                null,
                                null,
                                null
                        ),
                        new MarketSearchResultDTO(
                                "Microsoft Corp",
                                QuoteType.EQUITY,
                                "MSFT",
                                "quotes",
                                20076.0,
                                "Equity",
                                "Microsoft Corporation",
                                "NASDAQ",
                                "Technology",
                                "Technology",
                                "Software",
                                "Software"
                        )
                )
        );
    }
}
