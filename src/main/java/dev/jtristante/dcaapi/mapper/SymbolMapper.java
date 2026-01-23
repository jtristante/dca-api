package dev.jtristante.dcaapi.mapper;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.model.Symbol;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SymbolMapper {

    SymbolResponse toDto(Symbol symbol);

    List<SymbolResponse> toDtoList(List<Symbol> symbols);
}
