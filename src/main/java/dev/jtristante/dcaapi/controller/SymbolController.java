package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.SymbolsApi;
import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.service.SymbolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SymbolController implements SymbolsApi {

    private final SymbolService symbolService;

    public SymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    @Override
    public ResponseEntity<List<SymbolResponse>> searchSymbols(String name, String ticker) {
        return ResponseEntity.ok(symbolService.search(name, ticker));
    }
}
