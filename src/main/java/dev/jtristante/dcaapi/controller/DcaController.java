package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.DcaApi;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.service.DcaCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DcaController implements DcaApi {

    private final DcaCalculationService dcaCalculationService;

    public DcaController(DcaCalculationService dcaCalculationService) {
        this.dcaCalculationService = dcaCalculationService;
    }

    @Override
    public ResponseEntity<DcaResponse> calculateDca(DcaRequest dcaRequest) {
        return ResponseEntity.ok(dcaCalculationService.calculate(dcaRequest));
    }

}
