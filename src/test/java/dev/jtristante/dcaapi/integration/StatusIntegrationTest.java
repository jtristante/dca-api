package dev.jtristante.dcaapi.integration;


import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.YahooFinanceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    YahooFinanceClient yahooFinanceClient;


    @Test
    void statusEndpoint_shouldReturn200AndJson() throws Exception {
        when(yahooFinanceClient.getHistoryRaw(anyString(), anyString(), anyInt(), anyBoolean())).thenReturn("[]");
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("dca-api"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
