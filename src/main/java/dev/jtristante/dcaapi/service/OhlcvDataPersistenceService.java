package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.model.OhlcvData;
import dev.jtristante.dcaapi.repository.OhlcvDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OhlcvDataPersistenceService {

    private final OhlcvDataRepository ohlcvDataRepository;

    public OhlcvDataPersistenceService(OhlcvDataRepository ohlcvDataRepository) {
        this.ohlcvDataRepository = ohlcvDataRepository;
    }

    @Transactional
    public List<OhlcvData> saveAll(List<OhlcvData> ohlcvDataList) {
        return ohlcvDataRepository.saveAll(ohlcvDataList);
    }
}
