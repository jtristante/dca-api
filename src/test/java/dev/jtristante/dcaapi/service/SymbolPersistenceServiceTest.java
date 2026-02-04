package dev.jtristante.dcaapi.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymbolPersistenceServiceTest {

    @Mock
    private SymbolRepository symbolRepository;

    @Mock
    private SymbolMapper symbolMapper;

    @Captor
    private ArgumentCaptor<List<Symbol>> symbolsCaptor;

    private SymbolPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new SymbolPersistenceService(symbolRepository, symbolMapper);
    }

    @Nested
    @DisplayName("saveFromMarketSearchResults")
    class SaveFromMarketSearchResultsTests {

        @Test
        @DisplayName("should map DTOs to entities")
        void saveFromMarketSearchResults_shouldMapDtosToEntities() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("AAPL", "Apple Inc.", "EQUITY"),
                    createMockMarketResult("GOOGL", "Alphabet Inc.", "EQUITY")
            );

            List<Symbol> mappedSymbols = List.of(
                    createMockSymbol("AAPL", "Apple Inc.", "STOCKS"),
                    createMockSymbol("GOOGL", "Alphabet Inc.", "STOCKS")
            );
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(mappedSymbols);

            service.saveFromMarketSearchResults(dtos);

            verify(symbolMapper).marketSearchResultDtoListToSymbolList(dtos);
        }

        @Test
        @DisplayName("should filter entities with null name")
        void saveFromMarketSearchResults_shouldFilterEntitiesWithNullName() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("VALID1", "Valid One", "EQUITY"),
                    createMockMarketResult("NONAME1", null, "ETF")
            );

            List<Symbol> mappedSymbols = List.of(
                    createMockSymbol("VALID1", "Valid One", "STOCKS"),
                    createMockSymbol("NONAME1", null, "ETF")
            );
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(List.of(mappedSymbols.get(0)));

            service.saveFromMarketSearchResults(dtos);

            verify(symbolRepository).saveAll(symbolsCaptor.capture());
            List<Symbol> savedSymbols = symbolsCaptor.getValue();
            assertThat(savedSymbols).hasSize(1);
            assertThat(savedSymbols.get(0).getTicker()).isEqualTo("VALID1");
        }

        @Test
        @DisplayName("should filter entities with blank name")
        void saveFromMarketSearchResults_shouldFilterEntitiesWithBlankName() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("VALID1", "Valid One", "EQUITY"),
                    createMockMarketResult("NONAME1", "", "ETF")
            );

            List<Symbol> mappedSymbols = List.of(
                    createMockSymbol("VALID1", "Valid One", "STOCKS"),
                    createMockSymbol("NONAME1", "", "ETF")
            );
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(List.of(mappedSymbols.get(0)));

            service.saveFromMarketSearchResults(dtos);

            verify(symbolRepository).saveAll(symbolsCaptor.capture());
            List<Symbol> savedSymbols = symbolsCaptor.getValue();
            assertThat(savedSymbols).hasSize(1);
        }

        @Test
        @DisplayName("should save all valid entities")
        void saveFromMarketSearchResults_shouldSaveAllValidEntities() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("AAPL", "Apple Inc.", "EQUITY"),
                    createMockMarketResult("BTC", "Bitcoin", "CRYPTOCURRENCY"),
                    createMockMarketResult("TSLA", "Tesla Inc.", "EQUITY")
            );

            List<Symbol> mappedSymbols = List.of(
                    createMockSymbol("AAPL", "Apple Inc.", "STOCKS"),
                    createMockSymbol("BTC", "Bitcoin", "CRYPTO"),
                    createMockSymbol("TSLA", "Tesla Inc.", "STOCKS")
            );
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(mappedSymbols);

            List<Symbol> result = service.saveFromMarketSearchResults(dtos);

            assertThat(result).hasSize(3);
            verify(symbolRepository).saveAll(mappedSymbols);
        }

        @Test
        @DisplayName("should return saved entities")
        void saveFromMarketSearchResults_shouldReturnSavedEntities() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("AAPL", "Apple Inc.", "EQUITY")
            );

            List<Symbol> mappedSymbols = List.of(createMockSymbol("AAPL", "Apple Inc.", "STOCKS"));
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(mappedSymbols);

            List<Symbol> result = service.saveFromMarketSearchResults(dtos);

            assertThat(result).isEqualTo(mappedSymbols);
        }

        @Test
        @DisplayName("should handle empty input list")
        void saveFromMarketSearchResults_shouldHandleEmptyInput() {
            List<MarketSearchResultDTO> dtos = List.of();

            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(List.of());
            when(symbolRepository.saveAll(any())).thenReturn(List.of());

            List<Symbol> result = service.saveFromMarketSearchResults(dtos);

            assertThat(result).isEmpty();
            verify(symbolRepository).saveAll(List.of());
        }

        @Test
        @DisplayName("should handle single valid entity")
        void saveFromMarketSearchResults_shouldHandleSingleValidEntity() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("BTC", "Bitcoin", "CRYPTOCURRENCY")
            );

            List<Symbol> mappedSymbols = List.of(createMockSymbol("BTC", "Bitcoin", "CRYPTO"));
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(mappedSymbols);

            List<Symbol> result = service.saveFromMarketSearchResults(dtos);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTicker()).isEqualTo("BTC");
        }

        @Test
        @DisplayName("should handle mixed valid and invalid entities")
        void saveFromMarketSearchResults_shouldHandleMixedValidAndInvalid() {
            List<MarketSearchResultDTO> dtos = List.of(
                    createMockMarketResult("AAPL", "Apple Inc.", "EQUITY"),
                    createMockMarketResult("BTC", "Bitcoin", "CRYPTOCURRENCY"),
                    createMockMarketResult("NONAME", null, "ETF"),
                    createMockMarketResult("GOOGL", "Alphabet", "EQUITY")
            );

            List<Symbol> mappedSymbols = List.of(
                    createMockSymbol("AAPL", "Apple Inc.", "STOCKS"),
                    createMockSymbol("BTC", "Bitcoin", "CRYPTO"),
                    createMockSymbol("NONAME", null, "ETF"),
                    createMockSymbol("GOOGL", "Alphabet", "STOCKS")
            );
            when(symbolMapper.marketSearchResultDtoListToSymbolList(dtos)).thenReturn(mappedSymbols);
            when(symbolRepository.saveAll(any())).thenReturn(List.of(
                    mappedSymbols.get(0), mappedSymbols.get(1), mappedSymbols.get(3)
            ));

            service.saveFromMarketSearchResults(dtos);

            verify(symbolRepository).saveAll(symbolsCaptor.capture());
            List<Symbol> savedSymbols = symbolsCaptor.getValue();
            assertThat(savedSymbols).hasSize(3);
            assertThat(savedSymbols).extracting(Symbol::getTicker)
                    .containsExactly("AAPL", "BTC", "GOOGL");
        }
    }

    private MarketSearchResultDTO createMockMarketResult(String symbol, String shortname, String quoteType) {
        return new MarketSearchResultDTO(
                shortname,
                quoteType,
                symbol,
                null,
                1.0,
                "Stock",
                null,
                "US",
                null,
                null,
                null,
                null
        );
    }

    private Symbol createMockSymbol(String ticker, String name, String instrumentType) {
        Symbol symbol = new Symbol();
        symbol.setTicker(ticker);
        symbol.setName(name);
        symbol.setInstrumentType(InstrumentType.valueOf(instrumentType));
        return symbol;
    }
}
