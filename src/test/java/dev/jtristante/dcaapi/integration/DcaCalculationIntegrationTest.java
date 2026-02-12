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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].weighted_average_price").isNumber())
                .andExpect(jsonPath("$[0].profit").isNumber())
                .andExpect(jsonPath("$[0].roi").isNumber())
                .andExpect(jsonPath("$[0].date").isString());
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].units").value(org.hamcrest.Matchers.greaterThan(0.0)));
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

    @Test
    void calculateDca_detailedTrue_shouldReturnMultipleResults() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("detailed", "true")
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThan(1))))
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].date").isString())
                .andExpect(jsonPath("$[1].total_invested").isNumber())
                .andExpect(jsonPath("$[1].date").isString());
    }

    @Test
    void calculateDca_detailedFalse_shouldReturnSingleResult() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("detailed", "false")
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].date").isString());
    }

    @Test
    void calculateDca_detailedDefault_shouldReturnSingleResult() throws Exception {
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].date").isString());
    }

    @Test
    void calculateDca_detailedTrue_shouldHaveCorrectFields() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("detailed", "true")
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
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].date").isString())
                .andExpect(jsonPath("$[0].total_invested").isNumber())
                .andExpect(jsonPath("$[0].units").isNumber())
                .andExpect(jsonPath("$[0].weighted_average_price").isNumber())
                .andExpect(jsonPath("$[0].profit").isNumber())
                .andExpect(jsonPath("$[0].roi").isNumber());
    }

    @Test
    void calculateDca_detailedTrue_singlePurchase_shouldHaveOneResult() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("detailed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-01-01",
                                "end_date": "2026-01-15"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void calculateDca_detailedTrue_cumulativeValuesShouldBeIncreasing() throws Exception {
        mockMvc.perform(post("/api/v1/dca/calculate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("detailed", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "symbol": "AAPL",
                                "amount": 100.0,
                                "frequency": "monthly",
                                "start_date": "2026-01-01",
                                "end_date": "2026-03-31"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThan(1))))
                .andExpect(jsonPath("$[1].total_invested").value(org.hamcrest.Matchers.greaterThan(0.0)))
                .andExpect(jsonPath("$[1].units").value(org.hamcrest.Matchers.greaterThan(0.0)));
    }
}
