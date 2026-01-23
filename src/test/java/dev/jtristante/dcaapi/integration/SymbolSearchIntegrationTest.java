package dev.jtristante.dcaapi.integration;

import dev.jtristante.dcaapi.dto.SymbolResponse;
import dev.jtristante.dcaapi.service.SymbolService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SymbolSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SymbolService symbolService;

    private static final String API_KEY_HEADER = "X-DCA-Internal-Key";
    private static final String API_KEY_VALUE = "test-api-key";

    @Test
    void searchSymbols_byName_shouldReturnMatchingSymbols() throws Exception {
        when(symbolService.search("bit", null))
                .thenReturn(List.of(new SymbolResponse("BTC-EUR", "Bitcoin Euro", SymbolResponse.InstrumentTypeEnum.STOCK)));

        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("name", "bit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("BTC-EUR"))
                .andExpect(jsonPath("$[0].name").value("Bitcoin Euro"));
    }

    @Test
    void searchSymbols_byNameAndTicker_shouldReturnMatchingSymbols() throws Exception {
        when(symbolService.search("Euro", "ETH"))
                .thenReturn(List.of(new SymbolResponse("ETH-EUR", "Ethereum Euro", SymbolResponse.InstrumentTypeEnum.ETF)));

        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("name", "Euro")
                        .param("ticker", "ETH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("ETH-EUR"));
    }

    @Test
    void searchSymbols_missingName_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchSymbols_unauthorized_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .param("name", "bit"))
                .andExpect(status().isUnauthorized());
    }
}
