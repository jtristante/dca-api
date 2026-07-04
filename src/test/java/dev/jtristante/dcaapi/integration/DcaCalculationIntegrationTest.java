package dev.jtristante.dcaapi.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class DcaCalculationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void calculateDca_validRequest_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-01-01",
                                "end_date": "2026-02-28"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_invested").isNumber())
                .andExpect(jsonPath("$.units").isNumber())
                .andExpect(jsonPath("$.weighted_average_price").isNumber())
                .andExpect(jsonPath("$.current_value").isNumber())
                .andExpect(jsonPath("$.profit").isNumber())
                .andExpect(jsonPath("$.roi").isNumber());
    }

    @Test
    void calculateDca_monthlyFrequency_shouldAccumulateUnits() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-01-01",
                                "end_date": "2026-01-31"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_invested").isNumber())
                .andExpect(jsonPath("$.units").isNumber())
                .andExpect(jsonPath("$.units").value(org.hamcrest.Matchers.greaterThan(0.0)));
    }

    @Test
    void calculateDca_invalidDateRange_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-02-01",
                                "end_date": "2026-01-01"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateDca_invalidDateFormat_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "01-01-2026",
                                "end_date": "2026-02-28"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateDca_unauthorized_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-01-01",
                                "end_date": "2026-02-28"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
