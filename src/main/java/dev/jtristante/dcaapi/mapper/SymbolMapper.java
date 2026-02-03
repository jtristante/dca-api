package dev.jtristante.dcaapi.mapper;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.MarketSearchResultDTO;
import dev.jtristante.dcaapi.model.InstrumentType;
import dev.jtristante.dcaapi.model.Symbol;
import io.micrometer.common.util.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SymbolMapper {

    SymbolResponse symbolToSymbolResponse(Symbol symbol);

    List<SymbolResponse> symbolListToSymbolResponseList(List<Symbol> symbols);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "symbol", target = "ticker")
    @Mapping(target = "name", expression = "java(mapName(dto.longname(), dto.shortname()))")
    @Mapping(target = "instrumentType", expression = "java(mapQuoteType(dto.quoteType()))")
    Symbol marketSearchResultDtoToSymbol(MarketSearchResultDTO dto);

    List<Symbol> marketSearchResultDtoListToSymbolList(List<MarketSearchResultDTO> dtos);

    default SymbolResponse.InstrumentTypeEnum map(InstrumentType instrumentType) {
        if (instrumentType == null) {
            return null;
        }
        return SymbolResponse.InstrumentTypeEnum.fromValue(instrumentType.name());
    }

    default InstrumentType map(SymbolResponse.InstrumentTypeEnum instrumentTypeEnum) {
        if (instrumentTypeEnum == null) {
            return null;
        }
        return InstrumentType.valueOf(instrumentTypeEnum.getValue());
    }

    default String mapName(String longname, String shortname) {
        return StringUtils.isNotBlank(longname) ? longname : shortname;
    }

    default InstrumentType mapQuoteType(String quoteType) {
        if (quoteType == null) {
            return null;
        }
        return switch (quoteType.toUpperCase()) {
            case "EQUITY" -> InstrumentType.STOCKS;
            case "ETF" -> InstrumentType.ETF;
            case "CRYPTOCURRENCY" -> InstrumentType.CRYPTO;
            default -> null;
        };
    }
}
