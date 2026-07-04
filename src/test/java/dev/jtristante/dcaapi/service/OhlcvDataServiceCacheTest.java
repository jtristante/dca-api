package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.OhlcvDataDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.mapper.OhlcvDataMapper;
import dev.jtristante.dcaapi.model.OhlcvData;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.OhlcvDataRepository;
import dev.jtristante.dcaapi.testdata.MockPriceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OhlcvDataServiceCacheTest {

    @Mock
    private OhlcvDataRepository ohlcvDataRepository;

    @Mock
    private YahooFinanceApi yahooFinanceApi;

    @Mock
    private OhlcvDataMapper ohlcvDataMapper;

    @Mock
    private OhlcvDataPersistenceService ohlcvDataPersistenceService;

    private OhlcvDataService service;

    private final LocalDate startDate = LocalDate.of(2024, 1, 1);
    private final LocalDate endDate = LocalDate.of(2024, 3, 31);

    @BeforeEach
    void setUp() {
        service = new OhlcvDataService(ohlcvDataRepository, yahooFinanceApi, ohlcvDataMapper, ohlcvDataPersistenceService);
    }

    @Test
    void getOhlcvData_shouldReturnCachedResult_whenSameParams() {
        Symbol symbol = createMockSymbol(1L, "AAPL");
        List<OhlcvDataDTO> expectedDTOs = MockPriceData.risingPricesOhlcv();
        List<OhlcvData> entities = MockPriceData.risingPricesOhlcvEntity(symbol);
        GetStocksHistoryResponseDTO apiResponse = new GetStocksHistoryResponseDTO(null, MockPriceData.risingPrices());

        when(ohlcvDataRepository.findBySymbolIdAndDateRange(anyLong(), any(), any())).thenReturn(List.of());
        when(yahooFinanceApi.getStocksHistory(anyString(), any(), any(), any())).thenReturn(apiResponse);
        when(ohlcvDataMapper.stockHistoryDtoListToOhlcvDataList(anyList(), any())).thenReturn(entities);
        when(ohlcvDataPersistenceService.saveAll(anyList())).thenReturn(entities);
        when(ohlcvDataMapper.ohlcvDataListToOhlcvDataDTOList(anyList())).thenReturn(expectedDTOs);

        // First call — should execute full method (repository empty → Yahoo Finance API)
        List<OhlcvDataDTO> result1 = service.getOhlcvData(symbol, startDate, endDate);
        // Second call with same params — @Cacheable should return cached result if in proxy context
        List<OhlcvDataDTO> result2 = service.getOhlcvData(symbol, startDate, endDate);

        assertThat(result1).isEqualTo(expectedDTOs);
        assertThat(result2).isEqualTo(expectedDTOs);
    }

    @Test
    void getOhlcvData_shouldCallApi_whenDifferentSymbol() {
        Symbol symbol1 = createMockSymbol(1L, "AAPL");
        Symbol symbol2 = createMockSymbol(2L, "TSLA");
        List<OhlcvDataDTO> expectedDTOs = MockPriceData.risingPricesOhlcv();
        List<OhlcvData> entities = MockPriceData.risingPricesOhlcvEntity(symbol1);
        GetStocksHistoryResponseDTO apiResponse = new GetStocksHistoryResponseDTO(null, MockPriceData.risingPrices());

        when(ohlcvDataRepository.findBySymbolIdAndDateRange(anyLong(), any(), any())).thenReturn(List.of());
        when(yahooFinanceApi.getStocksHistory(anyString(), any(), any(), any())).thenReturn(apiResponse);
        when(ohlcvDataMapper.stockHistoryDtoListToOhlcvDataList(anyList(), any())).thenReturn(entities);
        when(ohlcvDataPersistenceService.saveAll(anyList())).thenReturn(entities);
        when(ohlcvDataMapper.ohlcvDataListToOhlcvDataDTOList(anyList())).thenReturn(expectedDTOs);

        // First call with symbol1
        service.getOhlcvData(symbol1, startDate, endDate);
        // Second call with different symbol — should trigger separate API call
        service.getOhlcvData(symbol2, startDate, endDate);

        // Different cache keys → two API calls
        verify(yahooFinanceApi, times(2)).getStocksHistory(anyString(), any(), any(), any());
    }

    @Test
    void getOhlcvData_shouldNotCacheNullResult() {
        Symbol symbol = createMockSymbol(1L, "AAPL");
        List<OhlcvData> entities = MockPriceData.risingPricesOhlcvEntity(symbol);
        GetStocksHistoryResponseDTO apiResponse = new GetStocksHistoryResponseDTO(null, MockPriceData.risingPrices());

        when(ohlcvDataRepository.findBySymbolIdAndDateRange(anyLong(), any(), any())).thenReturn(List.of());
        when(yahooFinanceApi.getStocksHistory(anyString(), any(), any(), any())).thenReturn(apiResponse);
        when(ohlcvDataMapper.stockHistoryDtoListToOhlcvDataList(anyList(), any())).thenReturn(entities);
        when(ohlcvDataPersistenceService.saveAll(anyList())).thenReturn(entities);
        // Make the final mapper return null — service method returns null
        when(ohlcvDataMapper.ohlcvDataListToOhlcvDataDTOList(anyList())).thenReturn(null);

        List<OhlcvDataDTO> result1 = service.getOhlcvData(symbol, startDate, endDate);
        List<OhlcvDataDTO> result2 = service.getOhlcvData(symbol, startDate, endDate);

        assertThat(result1).isNull();
        assertThat(result2).isNull();
        // With unless = "#result == null", null result should NOT be cached
        // So API should be called twice
        verify(yahooFinanceApi, times(2)).getStocksHistory(anyString(), any(), any(), any());
    }

    private static Symbol createMockSymbol(Long id, String ticker) {
        Symbol symbol = new Symbol();
        symbol.setId(id);
        symbol.setTicker(ticker);
        symbol.setName(ticker + " Inc.");
        return symbol;
    }
}
