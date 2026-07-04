package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.config.CaffeineCacheConfig;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.InstrumentType;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for caching behavior on SymbolService.
 * <p>
 * Uses a minimal Spring context with @EnableCaching to verify that @Cacheable
 * annotations on findByTicker and findOrSearchByTicker correctly cache results,
 * avoid caching empty Optionals, and distinguish different cache keys.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaffeineCacheConfig.class, SymbolServiceCacheTest.TestConfig.class})
class SymbolServiceCacheTest {

    @Configuration
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager("symbolByTicker", "symbolSearch");
        }

        @Bean
        SymbolRepository symbolRepository() {
            return mock(SymbolRepository.class);
        }

        @Bean
        SymbolMapper symbolMapper() {
            return mock(SymbolMapper.class);
        }

        @Bean
        YahooFinanceApi yahooFinanceApi() {
            return mock(YahooFinanceApi.class);
        }

        @Bean
        SymbolPersistenceService symbolPersistenceService() {
            return mock(SymbolPersistenceService.class);
        }

        @Bean
        SymbolService symbolService(SymbolRepository repo, SymbolMapper mapper,
                                    YahooFinanceApi api, SymbolPersistenceService ps) {
            return new SymbolService(repo, mapper, api, ps);
        }
    }

    @Autowired
    private SymbolService symbolService;

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private SymbolMapper symbolMapper;

    @Autowired
    private YahooFinanceApi yahooFinanceApi;

    @Autowired
    private SymbolPersistenceService symbolPersistenceService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(symbolRepository, symbolMapper, yahooFinanceApi, symbolPersistenceService);
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Nested
    @DisplayName("findByTicker")
    class FindByTickerTests {

        @Test
        @DisplayName("should return cached result when same ticker is queried twice")
        void findByTicker_shouldReturnCachedResult_whenSameTicker() {
            Symbol symbol = createMockSymbol("AAPL", "Apple Inc.", "STOCKS");
            when(symbolRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.of(symbol));

            Optional<Symbol> result1 = symbolService.findByTicker("AAPL");
            Optional<Symbol> result2 = symbolService.findByTicker("AAPL");

            assertThat(result1).isPresent();
            assertThat(result1.get().getTicker()).isEqualTo("AAPL");
            assertThat(result2).isPresent();
            assertThat(result2.get().getTicker()).isEqualTo("AAPL");
            verify(symbolRepository, times(1)).findByTickerIgnoreCase("AAPL");
        }

        @Test
        @DisplayName("should call repository when different ticker is queried")
        void findByTicker_shouldCallRepository_whenDifferentTicker() {
            Symbol aapl = createMockSymbol("AAPL", "Apple Inc.", "STOCKS");
            Symbol googl = createMockSymbol("GOOGL", "Alphabet Inc.", "STOCKS");
            when(symbolRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.of(aapl));
            when(symbolRepository.findByTickerIgnoreCase("GOOGL")).thenReturn(Optional.of(googl));

            Optional<Symbol> result1 = symbolService.findByTicker("AAPL");
            Optional<Symbol> result2 = symbolService.findByTicker("GOOGL");

            assertThat(result1).isPresent();
            assertThat(result1.get().getTicker()).isEqualTo("AAPL");
            assertThat(result2).isPresent();
            assertThat(result2.get().getTicker()).isEqualTo("GOOGL");
            verify(symbolRepository, times(1)).findByTickerIgnoreCase("AAPL");
            verify(symbolRepository, times(1)).findByTickerIgnoreCase("GOOGL");
        }

        @Test
        @DisplayName("should not cache empty Optional result")
        void findByTicker_shouldNotCacheEmptyResult() {
            when(symbolRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.empty());

            Optional<Symbol> result1 = symbolService.findByTicker("AAPL");
            Optional<Symbol> result2 = symbolService.findByTicker("AAPL");

            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
            verify(symbolRepository, times(2)).findByTickerIgnoreCase("AAPL");
        }
    }

    @Nested
    @DisplayName("findOrSearchByTicker")
    class FindOrSearchByTickerTests {

        @Test
        @DisplayName("should return cached result when same params are queried twice")
        void findOrSearchByTicker_shouldReturnCachedResult_whenSameParams() {
            Symbol symbol = createMockSymbol("AAPL", "Apple Inc.", "STOCKS");
            when(symbolRepository.findByTickerStartingWithIgnoreCase("AAPL")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = new MarketSearchResponseDTO(null, List.of(
                    MarketSearchResultDTO.build("AAPL", "Apple Inc.", "EQUITY")
            ));
            when(yahooFinanceApi.searchMarket("AAPL")).thenReturn(mockResponse);
            when(symbolPersistenceService.saveFromMarketSearchResults(any())).thenReturn(List.of(symbol));

            Optional<Symbol> result1 = symbolService.findOrSearchByTicker("AAPL");
            Optional<Symbol> result2 = symbolService.findOrSearchByTicker("AAPL");

            assertThat(result1).isPresent();
            assertThat(result1.get().getTicker()).isEqualTo("AAPL");
            assertThat(result2).isPresent();
            assertThat(result2.get().getTicker()).isEqualTo("AAPL");
            verify(yahooFinanceApi, times(1)).searchMarket("AAPL");
            verify(symbolRepository, times(1)).findByTickerStartingWithIgnoreCase("AAPL");
        }

        @Test
        @DisplayName("should call API when different params are queried")
        void findOrSearchByTicker_shouldCallApi_whenDifferentParams() {
            Symbol aapl = createMockSymbol("AAPL", "Apple Inc.", "STOCKS");
            Symbol googl = createMockSymbol("GOOGL", "Alphabet Inc.", "STOCKS");

            when(symbolRepository.findByTickerStartingWithIgnoreCase("AAPL")).thenReturn(List.of());
            when(symbolRepository.findByTickerStartingWithIgnoreCase("GOOGL")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponseAapl = new MarketSearchResponseDTO(null, List.of(
                    MarketSearchResultDTO.build("AAPL", "Apple Inc.", "EQUITY")
            ));
            MarketSearchResponseDTO mockResponseGoogl = new MarketSearchResponseDTO(null, List.of(
                    MarketSearchResultDTO.build("GOOGL", "Alphabet Inc.", "EQUITY")
            ));
            when(yahooFinanceApi.searchMarket("AAPL")).thenReturn(mockResponseAapl);
            when(yahooFinanceApi.searchMarket("GOOGL")).thenReturn(mockResponseGoogl);

            when(symbolPersistenceService.saveFromMarketSearchResults(any()))
                    .thenReturn(List.of(aapl), List.of(googl));

            Optional<Symbol> result1 = symbolService.findOrSearchByTicker("AAPL");
            Optional<Symbol> result2 = symbolService.findOrSearchByTicker("GOOGL");

            assertThat(result1).isPresent();
            assertThat(result1.get().getTicker()).isEqualTo("AAPL");
            assertThat(result2).isPresent();
            assertThat(result2.get().getTicker()).isEqualTo("GOOGL");
            verify(yahooFinanceApi, times(1)).searchMarket("AAPL");
            verify(yahooFinanceApi, times(1)).searchMarket("GOOGL");
            verify(symbolRepository, times(1)).findByTickerStartingWithIgnoreCase("AAPL");
            verify(symbolRepository, times(1)).findByTickerStartingWithIgnoreCase("GOOGL");
        }
    }

    private Symbol createMockSymbol(String ticker, String name, String instrumentType) {
        Symbol symbol = new Symbol();
        symbol.setTicker(ticker);
        symbol.setName(name);
        symbol.setInstrumentType(InstrumentType.valueOf(instrumentType));
        return symbol;
    }
}
