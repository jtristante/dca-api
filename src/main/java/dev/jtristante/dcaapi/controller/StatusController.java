package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.api.StatusApi;
import dev.jtristante.dcaapi.dto.StatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1")
public class StatusController implements StatusApi {

    private static final String SERVICE_NAME = "dca-api";
    private final Clock clock;

    public StatusController(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ResponseEntity<StatusResponse> getStatus() {
        StatusResponse response = new StatusResponse(
                "ok",
                SERVICE_NAME,
                OffsetDateTime.now(clock)
        );
        return ResponseEntity.ok(response);
    }
}
