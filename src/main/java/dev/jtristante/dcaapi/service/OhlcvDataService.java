package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.StockHistoryDTO;
import dev.jtristante.dcaapi.mapper.OhlcvDataMapper;
import dev.jtristante.dcaapi.model.OhlcvData;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.OhlcvDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OhlcvDataService {

    private static final Logger log = LoggerFactory.getLogger(OhlcvDataService.class);

    private final OhlcvDataRepository ohlcvDataRepository;
    private final YahooFinanceApi yahooFinanceApi;
    private final OhlcvDataMapper ohlcvDataMapper;
    private final OhlcvDataPersistenceService ohlcvDataPersistenceService;

    public OhlcvDataService(OhlcvDataRepository ohlcvDataRepository,
                           YahooFinanceApi yahooFinanceApi,
                           OhlcvDataMapper ohlcvDataMapper,
                           OhlcvDataPersistenceService ohlcvDataPersistenceService) {
        this.ohlcvDataRepository = ohlcvDataRepository;
        this.yahooFinanceApi = yahooFinanceApi;
        this.ohlcvDataMapper = ohlcvDataMapper;
        this.ohlcvDataPersistenceService = ohlcvDataPersistenceService;
    }

    public List<OhlcvData> getOhlcvData(Symbol symbol, LocalDate startDate, LocalDate endDate) {
        List<OhlcvData> existingData = ohlcvDataRepository.findBySymbolIdAndDateRange(
                symbol.getId(), startDate, endDate
        );

        if (!existingData.isEmpty()) {
            log.info("Found {} OHLCV records in DB for symbol: {}", existingData.size(), symbol.getTicker());
            return existingData;
        }

        log.info("No OHLCV data found in DB for symbol: {}. Fetching from Yahoo Finance...", symbol.getTicker());
        return fetchAndPersistFromYahooFinance(symbol);
    }

    private List<OhlcvData> fetchAndPersistFromYahooFinance(Symbol symbol) {
        GetStocksHistoryResponseDTO response = yahooFinanceApi.getStocksHistory(
                symbol.getTicker(),
                IntervalType.W1,
                1000,
                true
        );

        List<StockHistoryDTO> priceData = response.body();
        if (priceData == null || priceData.isEmpty()) {
            log.warn("No OHLCV data returned from Yahoo Finance for symbol: {}", symbol.getTicker());
            return List.of();
        }

        List<OhlcvData> ohlcvEntities = ohlcvDataMapper.stockHistoryDtoListToOhlcvDataList(priceData, symbol);

        log.info("Persisting {} OHLCV records for symbol: {}", ohlcvEntities.size(), symbol.getTicker());
        return ohlcvDataPersistenceService.saveAll(ohlcvEntities);
    }
}
