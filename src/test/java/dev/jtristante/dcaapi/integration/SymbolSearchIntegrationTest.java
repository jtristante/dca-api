package dev.jtristante.dcaapi.integration;

import dev.jtristante.dcaapi.model.Symbol;
import dev.jtristante.dcaapi.repository.SymbolRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class SymbolSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SymbolRepository symbolRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    private static final String API_KEY_HEADER = "X-DCA-Internal-Key";
    private static final String API_KEY_VALUE = "test-api-key";

    @Test
    void searchSymbols_byTicker_shouldReturnMatchingSymbols() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("ticker", "BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("BTC-EUR"))
                .andExpect(jsonPath("$[0].name").value("Bitcoin Euro"));
    }

    @Test
    void searchSymbols_byName_shouldReturnMatchingSymbols() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("name", "Bitcoin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("BTC-EUR"))
                .andExpect(jsonPath("$[0].name").value("Bitcoin Euro"));
    }

    @Test
    void searchSymbols_byTickerAndName_shouldReturnMatchingSymbols() throws Exception {

        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("ticker", "ETH")
                        .param("name", "Euro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticker").value("ETH-EUR"));
    }

    @Test
    void searchSymbols_noParameters_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchSymbols_unauthorized_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/symbols")
                        .param("ticker", "BTC"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchSymbols_emptyDb_shouldCallYahooFinanceAndSaveToDb() throws Exception {
        // Verify test symbols are not in DB initially
        List<Symbol> existingSymbols = symbolRepository.findAll().stream()
                .filter(s -> s.getTicker().startsWith("TEST-"))
                .toList();
        assertThat(existingSymbols).isEmpty();

        // Call endpoint - this should trigger YahooFinance API call and save to DB
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("ticker", "TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].ticker").value(org.hamcrest.Matchers.hasItems("TEST-BTC-USD", "TEST-MSFT")));

        // Verify data was saved to DB - check both mock results were persisted
        List<Symbol> allSymbols = symbolRepository.findAll();
        Symbol savedBtc = allSymbols.stream()
                .filter(s -> s.getTicker().equals("TEST-BTC-USD"))
                .findFirst()
                .orElseThrow();
        assertThat(savedBtc.getName()).isEqualTo("Bitcoin USD");
        assertThat(savedBtc.getInstrumentType().name()).isEqualTo("CRYPTO");

        Symbol savedMsft = allSymbols.stream()
                .filter(s -> s.getTicker().equals("TEST-MSFT"))
                .findFirst()
                .orElseThrow();
        assertThat(savedMsft.getName()).isEqualTo("Microsoft Corporation");
        assertThat(savedMsft.getInstrumentType().name()).isEqualTo("STOCKS");
    }

    @Test
    void searchSymbols_filtersOutSymbolsWithNoName() throws Exception {
        // Verify symbol with no name is not in DB
        List<Symbol> existingSymbols = symbolRepository.findAll().stream()
                .filter(s -> s.getTicker().equals("1OPEN.MI"))
                .toList();
        assertThat(existingSymbols).isEmpty();

        // Call endpoint - mock returns symbol with no name
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("ticker", "noname"))
                .andExpect(status().isOk());

        // Verify the symbol with no name was NOT saved to DB
        List<Symbol> savedSymbols = symbolRepository.findAll();
        boolean hasSymbolWithNoName = savedSymbols.stream()
                .anyMatch(s -> s.getTicker().equals("1OPEN.MI"));
        assertThat(hasSymbolWithNoName).isFalse();
    }

    @Test
    void searchSymbols_filtersOutUnsupportedQuoteTypes() throws Exception {
        // Verify mutual funds and futures are not saved to DB
        List<Symbol> existingVtsmx = symbolRepository.findAll().stream()
                .filter(s -> s.getTicker().equals("VTSMX"))
                .toList();
        assertThat(existingVtsmx).isEmpty();

        List<Symbol> existingGold = symbolRepository.findAll().stream()
                .filter(s -> s.getTicker().equals("GC=F"))
                .toList();
        assertThat(existingGold).isEmpty();

        // Call endpoint - mock returns unsupported quote types
        mockMvc.perform(get("/api/v1/symbols")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("ticker", "unsupported"))
                .andExpect(status().isOk());

        // Verify mutual fund (VTSMX) and future (GC=F) were NOT saved to DB
        List<Symbol> savedSymbols = symbolRepository.findAll();
        boolean hasMutualFund = savedSymbols.stream()
                .anyMatch(s -> s.getTicker().equals("VTSMX"));
        boolean hasFuture = savedSymbols.stream()
                .anyMatch(s -> s.getTicker().equals("GC=F"));
        assertThat(hasMutualFund).isFalse();
        assertThat(hasFuture).isFalse();
    }
}
