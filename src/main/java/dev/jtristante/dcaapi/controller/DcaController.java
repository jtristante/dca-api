package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.DcaApi;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.service.DcaCalculationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Controller
@RequestMapping("/api/v1")
public class DcaController implements DcaApi {

    private final DcaCalculationService dcaCalculationService;

    public DcaController(DcaCalculationService dcaCalculationService) {
        this.dcaCalculationService = dcaCalculationService;
    }

    @Override
    public ResponseEntity<DcaResponse> calculateDca(DcaRequest dcaRequest) {
        validateDateRange(dcaRequest);
        return ResponseEntity.ok(dcaCalculationService.calculate(dcaRequest));
    }

    private void validateDateRange(DcaRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start_date must be before end_date");
        }
    }
}
