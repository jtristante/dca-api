package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SymbolPersistenceService {

    private final SymbolRepository symbolRepository;
    private final SymbolMapper symbolMapper;

    public SymbolPersistenceService(SymbolRepository symbolRepository, SymbolMapper symbolMapper) {
        this.symbolRepository = symbolRepository;
        this.symbolMapper = symbolMapper;
    }

    @Transactional
    public List<Symbol> saveFromMarketSearchResults(List<MarketSearchResultDTO> dtos) {
        List<Symbol> entities = symbolMapper.marketSearchResultDtoListToSymbolList(dtos);
        return symbolRepository.saveAll(entities);
    }
}
