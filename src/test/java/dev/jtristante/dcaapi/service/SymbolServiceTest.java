package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.SymbolResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymbolServiceTest {

    @Mock
    private SymbolRepository symbolRepository;

    @Mock
    private SymbolMapper symbolMapper;

    @Mock
    private YahooFinanceApi yahooFinanceApi;

    @Mock
    private SymbolPersistenceService symbolPersistenceService;

    private SymbolService service;

    @BeforeEach
    void setUp() {
        service = new SymbolService(symbolRepository, symbolMapper, yahooFinanceApi, symbolPersistenceService);
    }

    @Nested
    @DisplayName("search")
    class SearchTests {

        @Test
        @DisplayName("should return empty list when both parameters are blank")
        void search_shouldReturnEmptyList_whenBothParamsAreBlank() {
            List<SymbolResponse> result = service.search(null, "");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should query database by ticker when only ticker provided")
        void search_shouldQueryByTickerOnly_whenOnlyTickerProvided() {
            List<Symbol> symbols = List.of(createMockSymbol("AAPL", "Apple Inc.", "STOCKS"));
            when(symbolRepository.findByTickerStartingWithIgnoreCase("AAPL")).thenReturn(symbols);
            when(symbolMapper.symbolListToSymbolResponseList(symbols)).thenReturn(List.of(createMockResponse("AAPL")));

            List<SymbolResponse> result = service.search("AAPL", null);

            assertThat(result).hasSize(1);
            verify(symbolRepository).findByTickerStartingWithIgnoreCase("AAPL");
        }

        @Test
        @DisplayName("should query database by name when only name provided")
        void search_shouldQueryByNameOnly_whenOnlyNameProvided() {
            List<Symbol> symbols = List.of(createMockSymbol("TSLA", "Tesla Inc.", "STOCKS"));
            when(symbolRepository.findByNameStartingWithIgnoreCase("Tesla")).thenReturn(symbols);
            when(symbolMapper.symbolListToSymbolResponseList(symbols)).thenReturn(List.of(createMockResponse("TSLA")));

            List<SymbolResponse> result = service.search(null, "Tesla");

            assertThat(result).hasSize(1);
            verify(symbolRepository).findByNameStartingWithIgnoreCase("Tesla");
        }

        @Test
        @DisplayName("should query database with both parameters when both provided")
        void search_shouldQueryWithBothParams_whenBothProvided() {
            List<Symbol> symbols = List.of(createMockSymbol("AAPL", "Apple Inc.", "STOCKS"));
            when(symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase("Apple", "AAPL"))
                    .thenReturn(symbols);
            when(symbolMapper.symbolListToSymbolResponseList(symbols)).thenReturn(List.of(createMockResponse("AAPL")));

            List<SymbolResponse> result = service.search("AAPL", "Apple");

            assertThat(result).hasSize(1);
            verify(symbolRepository).findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase("Apple", "AAPL");
        }

        @Test
        @DisplayName("should return cached results when database has results")
        void search_shouldReturnCachedResults_whenDatabaseHasResults() {
            List<Symbol> symbols = List.of(createMockSymbol("BTC", "Bitcoin", "CRYPTO"));
            when(symbolRepository.findByTickerStartingWithIgnoreCase("BTC")).thenReturn(symbols);
            when(symbolMapper.symbolListToSymbolResponseList(symbols)).thenReturn(List.of(createMockResponse("BTC")));

            List<SymbolResponse> result = service.search("BTC", null);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTicker()).isEqualTo("BTC");
        }

        @Test
        @DisplayName("should call Yahoo API when database is empty")
        void search_shouldCallYahooApi_whenDatabaseIsEmpty() {
            when(symbolRepository.findByTickerStartingWithIgnoreCase("UNKNOWN")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = createMockYahooResponse(
                    createMockMarketResult("UNKNOWN", "Unknown Coin", "CRYPTOCURRENCY")
            );
            when(yahooFinanceApi.searchMarket("UNKNOWN")).thenReturn(mockResponse);

            List<Symbol> savedSymbols = List.of(createMockSymbol("UNKNOWN", "Unknown Coin", "CRYPTO"));
            when(symbolPersistenceService.saveFromMarketSearchResults(any())).thenReturn(savedSymbols);
            when(symbolMapper.symbolListToSymbolResponseList(savedSymbols)).thenReturn(List.of(createMockResponse("UNKNOWN")));

            List<SymbolResponse> result = service.search("UNKNOWN", null);

            assertThat(result).hasSize(1);
            verify(yahooFinanceApi).searchMarket("UNKNOWN");
        }

        @Test
        @DisplayName("should filter unsupported quote types from Yahoo API results")
        void search_shouldFilterUnsupportedQuoteTypes() {
            when(symbolRepository.findByTickerStartingWithIgnoreCase("TEST")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = createMockYahooResponse(
                    createMockMarketResult("STOCK1", "Stock One", "EQUITY"),
                    createMockMarketResult("FUND1", "Fund One", "MUTUAL_FUND"),
                    createMockMarketResult("ETF1", "ETF One", "ETF"),
                    createMockMarketResult("CRYPTO1", "Crypto One", "CRYPTOCURRENCY"),
                    createMockMarketResult("INDEX1", "Index One", "INDEX")
            );
            when(yahooFinanceApi.searchMarket("TEST")).thenReturn(mockResponse);

            List<Symbol> savedSymbols = List.of(
                    createMockSymbol("STOCK1", "Stock One", "STOCKS"),
                    createMockSymbol("ETF1", "ETF One", "ETF"),
                    createMockSymbol("CRYPTO1", "Crypto One", "CRYPTO")
            );
            when(symbolPersistenceService.saveFromMarketSearchResults(any())).thenReturn(savedSymbols);
            when(symbolMapper.symbolListToSymbolResponseList(savedSymbols)).thenReturn(List.of(
                    createMockResponse("STOCK1"),
                    createMockResponse("ETF1"),
                    createMockResponse("CRYPTO1")
            ));

            List<SymbolResponse> result = service.search("TEST", null);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("should filter results with no valid name")
        void search_shouldFilterResultsWithNoName() {
            when(symbolRepository.findByTickerStartingWithIgnoreCase("NONAME")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = createMockYahooResponse(
                    createMockMarketResult("VALID1", "Valid One", "EQUITY"),
                    createMockMarketResult("NONAME1", null, "EQUITY"),
                    createMockMarketResult("NONAME2", "", "ETF")
            );
            when(yahooFinanceApi.searchMarket("NONAME")).thenReturn(mockResponse);

            List<Symbol> savedSymbols = List.of(createMockSymbol("VALID1", "Valid One", "STOCKS"));
            when(symbolPersistenceService.saveFromMarketSearchResults(any())).thenReturn(savedSymbols);
            when(symbolMapper.symbolListToSymbolResponseList(savedSymbols)).thenReturn(List.of(createMockResponse("VALID1")));

            List<SymbolResponse> result = service.search("NONAME", null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when Yahoo API returns no valid results")
        void search_shouldReturnEmptyList_whenYahooApiReturnsNoValidResults() {
            when(symbolRepository.findByTickerStartingWithIgnoreCase("EMPTY")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = createMockYahooResponse(
                    createMockMarketResult("FUND1", "Fund One", "MUTUAL_FUND"),
                    createMockMarketResult("INDEX1", null, "INDEX")
            );
            when(yahooFinanceApi.searchMarket("EMPTY")).thenReturn(mockResponse);
            when(symbolMapper.symbolListToSymbolResponseList(List.of())).thenReturn(List.of());

            List<SymbolResponse> result = service.search("EMPTY", null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should prioritize ticker over name in Yahoo API search query")
        void search_shouldPrioritizeTickerInYahooQuery() {
            lenient().when(symbolRepository.findByTickerStartingWithIgnoreCase("BTC")).thenReturn(List.of());

            MarketSearchResponseDTO mockResponse = createMockYahooResponse(
                    createMockMarketResult("BTC", "Bitcoin", "CRYPTOCURRENCY")
            );
            when(yahooFinanceApi.searchMarket("BTC")).thenReturn(mockResponse);

            List<Symbol> savedSymbols = List.of(createMockSymbol("BTC", "Bitcoin", "CRYPTO"));
            when(symbolPersistenceService.saveFromMarketSearchResults(any())).thenReturn(savedSymbols);
            when(symbolMapper.symbolListToSymbolResponseList(savedSymbols)).thenReturn(List.of(createMockResponse("BTC")));

            List<SymbolResponse> result = service.search("BTC", "Bitcoin");

            assertThat(result).hasSize(1);
            verify(yahooFinanceApi).searchMarket("BTC");
        }
    }

    private Symbol createMockSymbol(String ticker, String name, String instrumentType) {
        Symbol symbol = new Symbol();
        symbol.setTicker(ticker);
        symbol.setName(name);
        symbol.setInstrumentType(InstrumentType.valueOf(instrumentType));
        return symbol;
    }

    private SymbolResponse createMockResponse(String ticker) {
        SymbolResponse response = new SymbolResponse();
        response.setTicker(ticker);
        response.setName("Mock Name");
        response.setInstrumentType(SymbolResponse.InstrumentTypeEnum.STOCKS);
        return response;
    }

    private MarketSearchResultDTO createMockMarketResult(String symbol, String shortname, String quoteType) {
        return MarketSearchResultDTO.build(symbol, shortname, quoteType);
    }

    private MarketSearchResponseDTO createMockYahooResponse(MarketSearchResultDTO... results) {
        return new MarketSearchResponseDTO(null, List.of(results));
    }
}
