package dev.jtristante.dcaapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SymbolSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String API_KEY_HEADER = "X-DCA-Internal-Key";
    private static final String API_KEY_VALUE = "test-api-key";

    @Test
    void searchSymbols_byName_shouldReturnMatchingSymbols() throws Exception {
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
