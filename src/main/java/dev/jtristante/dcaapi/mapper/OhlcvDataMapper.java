package dev.jtristante.dcaapi.mapper;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.StockHistoryDTO;
import dev.jtristante.dcaapi.model.OhlcvData;
import dev.jtristante.dcaapi.model.OhlcvDataId;
import dev.jtristante.dcaapi.model.Symbol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OhlcvDataMapper {

    @Mapping(target = "id", expression = "java(createOhlcvDataId(symbol, dto))")
    @Mapping(target = "symbol", expression = "java(symbol)")
    @Mapping(target = "dividend", constant = "BigDecimal.ZERO")
    OhlcvData stockHistoryDtoToOhlcvData(StockHistoryDTO dto, Symbol symbol);

    default List<OhlcvData> stockHistoryDtoListToOhlcvDataList(List<StockHistoryDTO> dtos, Symbol symbol) {
        return dtos.stream()
                .map(dto -> stockHistoryDtoToOhlcvData(dto, symbol))
                .toList();
    }

    static OhlcvDataId createOhlcvDataId(Symbol symbol, StockHistoryDTO dto) {
        LocalDate date = LocalDate.ofEpochDay(dto.timestampUnix() / 86400);
        return new OhlcvDataId(symbol.getId(), date);
    }
}
