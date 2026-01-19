package dev.jtristante.dcaapi.controller;

import dev.jtristante.dcaapi.dto.StatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class StatusControllerTest {

    private StatusController statusController;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-01-19T10:00:00Z"), ZoneOffset.UTC);
        statusController = new StatusController(fixedClock);
    }

    @Test
    void getStatus_shouldReturnCorrectResponse() {
        ResponseEntity<StatusResponse> responseEntity = statusController.getStatus();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        StatusResponse body = responseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo("ok");
        assertThat(body.getService()).isEqualTo("dca-api");
        assertThat(body.getTimestamp()).isEqualTo(OffsetDateTime.ofInstant(fixedClock.instant(), ZoneOffset.UTC));
    }
}