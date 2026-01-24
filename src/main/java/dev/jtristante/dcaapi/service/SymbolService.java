package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.mapper.SymbolMapper;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name parameter is mandatory");
        }

        String tickerParam = Optional.ofNullable(ticker).filter(t -> !t.isBlank()).orElse(null);

        List<Symbol> symbols = (tickerParam == null)
                ? symbolRepository.findByNameContainingIgnoreCase(name)
                : symbolRepository.findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(name, tickerParam);

        return symbolMapper.symbolListToSymbolResponseList(symbols);
    }
}
