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
        // No parameters provided - return empty list
        if (StringUtils.isBlank(ticker) && StringUtils.isBlank(name)) {
            return Collections.emptyList();
        }

        List<Symbol> symbols;

        // Determine which parameters are provided
        boolean hasTicker = StringUtils.isNotBlank(ticker);
        boolean hasName = StringUtils.isNotBlank(name);

        if (hasTicker && hasName) {
            // Both provided: AND logic in DB
            symbols = symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(name, ticker);
        } else if (hasTicker) {
            // Only ticker provided
            symbols = symbolRepository.findByTickerStartingWithIgnoreCase(ticker);
        } else {
            // Only name provided
            symbols = symbolRepository.findByNameStartingWithIgnoreCase(name);
        }

        if (symbols.isEmpty()) {
            // Yahoo API call: prioritize ticker if available, otherwise use name
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

        return symbolMapper.symbolListToSymbolResponseList(symbols);
    }

    private boolean isSupportedQuoteType(String quoteType) {
        return quoteType != null && SUPPORTED_QUOTE_TYPES.contains(quoteType.toUpperCase());
    }

    private boolean hasValidName(MarketSearchResultDTO dto) {
        return StringUtils.isNotBlank(dto.shortname()) || StringUtils.isNotBlank(dto.longname());
    }
}
