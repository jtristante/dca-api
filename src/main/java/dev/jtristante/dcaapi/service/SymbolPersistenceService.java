package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SymbolPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(SymbolPersistenceService.class);

    private final SymbolRepository symbolRepository;
    private final SymbolMapper symbolMapper;

    public SymbolPersistenceService(SymbolRepository symbolRepository, SymbolMapper symbolMapper) {
        this.symbolRepository = symbolRepository;
        this.symbolMapper = symbolMapper;
    }

    @Transactional
    public List<Symbol> saveFromMarketSearchResults(List<MarketSearchResultDTO> dtos) {
        List<Symbol> entities = symbolMapper.marketSearchResultDtoListToSymbolList(dtos);

        // Defensive filtering: remove any entities with null or blank names
        List<Symbol> validEntities = entities.stream()
                .filter(symbol -> {
                    boolean hasName = StringUtils.isNotBlank(symbol.getName());
                    if (!hasName) {
                        log.warn("Filtering out symbol with no name: ticker={}, instrumentType={}",
                                symbol.getTicker(), symbol.getInstrumentType());
                    }
                    return hasName;
                })
                .toList();

        log.info("Saving {} symbols out of {} received", validEntities.size(), entities.size());
        return symbolRepository.saveAll(validEntities);
    }
}
