package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.DcaApi;
import dev.jtristante.dcaapi.dto.CalculateDca200Response;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.OhlcvDataDTO;
import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.service.DcaCalculationService;
import dev.jtristante.dcaapi.service.OhlcvDataService;
import dev.jtristante.dcaapi.service.SymbolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
public class DcaController implements DcaApi {

    private final DcaCalculationService dcaCalculationService;
    private final SymbolService symbolService;
    private final OhlcvDataService ohlcvDataService;

    public DcaController(DcaCalculationService dcaCalculationService,
                         SymbolService symbolService,
                         OhlcvDataService ohlcvDataService) {
        this.dcaCalculationService = dcaCalculationService;
        this.symbolService = symbolService;
        this.ohlcvDataService = ohlcvDataService;
    }

    @Override
    public ResponseEntity<CalculateDca200Response> calculateDca(DcaRequest dcaRequest, Boolean detailed) {
        validateDateRange(dcaRequest);

        Symbol symbol = symbolService.findOrSearchByTicker(dcaRequest.getSymbol())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Symbol not found"));

        List<OhlcvDataDTO> ohlcvData = ohlcvDataService.getOhlcvData(
                symbol,
                dcaRequest.getStartDate(),
                dcaRequest.getEndDate()
        );

        if (Boolean.TRUE.equals(detailed)) {
            return ResponseEntity.ok(dcaCalculationService.calculateDetailed(dcaRequest, ohlcvData));
        } else {
            return ResponseEntity.ok(dcaCalculationService.calculate(dcaRequest, ohlcvData));
        }
    }

    private void validateDateRange(DcaRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start_date must be before end_date");
        }
    }
}
