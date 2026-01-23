package dev.jtristante.dcaapi.mapper;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.model.InstrumentType;
import dev.jtristante.dcaapi.model.Symbol;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SymbolMapper {

    SymbolResponse toDto(Symbol symbol);

    List<SymbolResponse> toDtoList(List<Symbol> symbols);

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
}
