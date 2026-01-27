package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.DcaApi;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import org.springframework.http.ResponseEntity;

public class DcaController implements DcaApi {
    @Override
    public ResponseEntity<DcaResponse> calculateDca(DcaRequest dcaRequest) {
        return null;
    }

}
