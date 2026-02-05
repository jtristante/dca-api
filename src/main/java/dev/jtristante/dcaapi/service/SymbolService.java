package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SymbolService {

    private static final Set<String> SUPPORTED_QUOTE_TYPES = Set.of(
            "EQUITY",
            "ETF",
            "CRYPTOCURRENCY"
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

    public List<SymbolResponse> search(String ticker, String name) {
        List<Symbol> symbols = findOrSearchSymbols(ticker, name);
        return symbolMapper.symbolListToSymbolResponseList(symbols);
    }

    public Optional<Symbol> findOrSearchByTicker(String ticker) {
        List<Symbol> symbols = findOrSearchSymbols(ticker, null);
        return symbols.stream()
                .filter(s -> s.getTicker().equalsIgnoreCase(ticker))
                .findFirst();
    }

    private List<Symbol> findOrSearchSymbols(String ticker, String name) {
        if (StringUtils.isBlank(ticker) && StringUtils.isBlank(name)) {
            return Collections.emptyList();
        }

        List<Symbol> symbols;
        boolean hasTicker = StringUtils.isNotBlank(ticker);
        boolean hasName = StringUtils.isNotBlank(name);

        if (hasTicker && hasName) {
            symbols = symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(name, ticker);
        } else if (hasTicker) {
            symbols = symbolRepository.findByTickerStartingWithIgnoreCase(ticker);
        } else {
            symbols = symbolRepository.findByNameStartingWithIgnoreCase(name);
        }

        if (symbols.isEmpty()) {
            String searchQuery = hasTicker ? ticker : name;
            MarketSearchResponseDTO response = yahooFinanceApi.searchMarket(searchQuery);
            List<MarketSearchResultDTO> filteredResults = response.body().stream()
                    .filter(dto -> isSupportedQuoteType(dto.quoteType()))
                    .filter(this::hasValidName)
                    .toList();

            if (!filteredResults.isEmpty()) {
                symbols = symbolPersistenceService.saveFromMarketSearchResults(filteredResults);
            }
        }

        return symbols;
    }

    public Optional<Symbol> findByTicker(String ticker) {
        return symbolRepository.findByTickerIgnoreCase(ticker);
    }

    private boolean isSupportedQuoteType(String quoteType) {
        return quoteType != null && SUPPORTED_QUOTE_TYPES.contains(quoteType.toUpperCase());
    }

    private boolean hasValidName(MarketSearchResultDTO dto) {
        return StringUtils.isNotBlank(dto.shortname()) || StringUtils.isNotBlank(dto.longname());
    }
}
