package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.QuoteType;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class SymbolService {

    private static final EnumSet<QuoteType> SUPPORTED_QUOTE_TYPES = EnumSet.of(
            QuoteType.EQUITY,
            QuoteType.ETF,
            QuoteType.CRYPTOCURRENCY
    );

    private final SymbolRepository symbolRepository;
    private final SymbolMapper symbolMapper;
    private final YahooFinanceApi yahooFinanceApi;
    private final SymbolPersistenceService symbolPersistenceService;

    public SymbolService(SymbolRepository symbolRepository,
                         SymbolMapper symbolMapper,
                         YahooFinanceApi yahooFinanceApi,
                         SymbolPersistenceService symbolPersistenceService) {
        this.symbolRepository = symbolRepository;
        this.symbolMapper = symbolMapper;
        this.yahooFinanceApi = yahooFinanceApi;
        this.symbolPersistenceService = symbolPersistenceService;
    }

    public List<SymbolResponse> search(String name, String ticker) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name parameter is mandatory");
        }

        String tickerParam = Optional.ofNullable(ticker).filter(t -> !t.isBlank()).orElse(null);

        List<Symbol> symbols = (tickerParam == null)
                ? symbolRepository.findByNameContainingIgnoreCase(name)
                : symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(name, tickerParam);

        if (symbols.isEmpty()) {
            MarketSearchResponseDTO response = yahooFinanceApi.searchMarket(name);
            List<MarketSearchResultDTO> filteredResults = response.body().stream()
                    .filter(dto -> isSupportedQuoteType(dto.quoteType()))
                    .toList();

            if (!filteredResults.isEmpty()) {
                symbols = symbolPersistenceService.saveFromMarketSearchResults(filteredResults);
            }
        }

        return symbolMapper.symbolListToSymbolResponseList(symbols);
    }

    private boolean isSupportedQuoteType(QuoteType quoteType) {
        return quoteType != null && SUPPORTED_QUOTE_TYPES.contains(quoteType);
    }
}
