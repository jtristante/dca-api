package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.testdata.MockPriceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DcaCalculationServiceTest {

    @Mock
    private YahooFinanceApi yahooFinanceApi;

    private DcaCalculationService service;

    @BeforeEach
    void setUp() {
        service = new DcaCalculationService(yahooFinanceApi);
    }

    @Nested
    @DisplayName("calculate integration")
    class CalculateIntegrationTests {

        @Test
        @DisplayName("should return correct DCA calculation with multiple purchases")
        void calculate_shouldReturnCorrectResult_withMultiplePurchases() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, MockPriceData.risingPrices());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.MO1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(300.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getWeightedAveragePrice()).isGreaterThan(0.0);
            assertThat(result.getCurrentValue()).isGreaterThan(0.0);
            assertThat(result.getProfit()).isGreaterThan(0.0);
            assertThat(result.getRoi()).isNotNull();
        }

        @Test
        @DisplayName("should return empty response when no price data")
        void calculate_shouldReturnEmptyResponse_whenNoPriceData() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, List.of());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.MO1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getUnits()).isEqualTo(0.0);
            assertThat(result.getWeightedAveragePrice()).isEqualTo(0.0);
            assertThat(result.getCurrentValue()).isEqualTo(0.0);
            assertThat(result.getProfit()).isEqualTo(0.0);
            assertThat(result.getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return empty response when price data is null")
        void calculate_shouldReturnEmptyResponse_whenPriceDataIsNull() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, null);
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.W1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should calculate positive ROI when current price is higher than average")
        void calculate_shouldReturnPositiveRoi_whenPriceAppreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, MockPriceData.appreciationPrices());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.Q1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isGreaterThan(0.0);
            assertThat(result.getRoi()).isNotNull();
        }

        @Test
        @DisplayName("should calculate negative ROI when current price is lower than average")
        void calculate_shouldReturnNegativeRoi_whenPriceDepreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, MockPriceData.fallingPrices());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.MO1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isLessThan(0.0);
            assertThat(result.getRoi()).isLessThan(0.0);
        }

        @Test
        @DisplayName("should skip dates outside investment period")
        void calculate_shouldSkipDatesOutsideInvestmentPeriod() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 30));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, MockPriceData.mixedPrices());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.MO1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should handle single purchase within period")
        void calculate_shouldHandleSinglePurchase() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

            GetStocksHistoryResponseDTO response = new GetStocksHistoryResponseDTO(null, MockPriceData.singlePurchasePrices());
            when(yahooFinanceApi.getStocksHistory(eq("BTC-EUR"), eq(IntervalType.MO1), any(), any()))
                    .thenReturn(response);

            DcaResponse result = service.calculate(request);

            assertThat(result.getTotalInvested()).isEqualTo(100.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getWeightedAveragePrice()).isEqualTo(45000.0);
            assertThat(result.getCurrentValue()).isCloseTo(111.11, offset(0.01));
            assertThat(result.getProfit()).isCloseTo(11.11, offset(0.01));
            assertThat(result.getRoi()).isCloseTo(0.111, offset(0.001));
        }
    }
}
