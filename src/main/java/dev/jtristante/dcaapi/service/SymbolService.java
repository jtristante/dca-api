package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SymbolService {

    private final SymbolRepository symbolRepository;
    private final SymbolMapper symbolMapper;

    public SymbolService(SymbolRepository symbolRepository, SymbolMapper symbolMapper) {
        this.symbolRepository = symbolRepository;
        this.symbolMapper = symbolMapper;
    }

    @Transactional(readOnly = true)
    public List<SymbolResponse> search(String name, String ticker) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name parameter is mandatory");
        }

        String tickerParam = (ticker == null || ticker.isBlank()) ? null : ticker;

        List<Symbol> symbols = (tickerParam == null)
                ? symbolRepository.findByNameContainingIgnoreCase(name)
                : symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(name, tickerParam);

        return symbolMapper.toDtoList(symbols);
    }
}
