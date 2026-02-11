package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.dto.OhlcvDataDTO;
import dev.jtristante.dcaapi.testdata.MockPriceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DcaCalculationServiceTest {

    private DcaCalculationService service;

    @BeforeEach
    void setUp() {
        service = new DcaCalculationService();
    }

    @Nested
    @DisplayName("calculate integration")
    class CalculateIntegrationTests {

        @Test
        @DisplayName("should return correct DCA calculation with multiple purchases")
        void calculate_shouldReturnCorrectResult_withMultiplePurchases() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

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

            List<OhlcvDataDTO> priceData = MockPriceData.emptyPricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

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

            DcaResponse result = service.calculate(request, null);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should calculate positive ROI when current price is higher than average")
        void calculate_shouldReturnPositiveRoi_whenPriceAppreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.appreciationPricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isGreaterThan(0.0);
            assertThat(result.getRoi()).isNotNull();
        }

        @Test
        @DisplayName("should calculate negative ROI when current price is lower than average")
        void calculate_shouldReturnNegativeRoi_whenPriceDepreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isLessThan(0.0);
            assertThat(result.getRoi()).isLessThan(0.0);
        }

        @Test
        @DisplayName("should skip dates outside investment period")
        void calculate_shouldSkipDatesOutsideInvestmentPeriod() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.mixedPricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should handle single purchase within period")
        void calculate_shouldHandleSinglePurchase() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.singlePurchasePricesOhlcv();

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(100.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getWeightedAveragePrice()).isEqualTo(45000.0);
            assertThat(result.getCurrentValue()).isCloseTo(111.11, offset(0.01));
            assertThat(result.getProfit()).isCloseTo(11.11, offset(0.01));
            assertThat(result.getRoi()).isCloseTo(0.111, offset(0.001));
        }
    }

    @Nested
    @DisplayName("frequency filtering tests")
    class FrequencyFilteringTests {

        @Test
        @DisplayName("WEEKLY frequency should use all data points")
        void calculate_withWeeklyFrequency_shouldUseAllDataPoints() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 8 weekly data points in the list

            DcaResponse result = service.calculate(request, priceData);

            // All 8 points should be used (100 * 8 = 800)
            assertThat(result.getTotalInvested()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("MONTHLY frequency should select only first point per month")
        void calculate_withMonthlyFrequency_shouldSelectFirstPointPerMonth() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 8 weekly points across 3 months (Jan, Feb, Mar)
            // Should use only first point of each month: Jan 7, Feb 4, Mar 3

            DcaResponse result = service.calculate(request, priceData);

            // Only 3 purchases (one per month)
            assertThat(result.getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("QUARTERLY frequency should select only first point per quarter")
        void calculate_withQuarterlyFrequency_shouldSelectFirstPointPerQuarter() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();
            // 6 weekly points across 2 quarters (Q1, Q2)
            // Should use only first point of each quarter: Jan 7, Apr 7

            DcaResponse result = service.calculate(request, priceData);

            // Only 2 purchases (one per quarter)
            assertThat(result.getTotalInvested()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("should handle unsorted data correctly")
        void calculate_withUnsortedData_shouldSortBeforeFiltering() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.unsortedWeeklyPoints();
            // Unsorted data but still 3 months present

            DcaResponse result = service.calculate(request, priceData);

            // Should still pick 3 points (one per month) after sorting
            assertThat(result.getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("MONTHLY frequency with single month should use only first point")
        void calculate_withMonthlyFrequencyAndSingleMonth_shouldUseFirstPoint() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 3 points in January, but should only use the first one (Jan 7)

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("QUARTERLY frequency with single quarter should use only first point")
        void calculate_withQuarterlyFrequencyAndSingleQuarter_shouldUseFirstPoint() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();
            // 3 points in Q1, but should only use the first one (Jan 7)

            DcaResponse result = service.calculate(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("empty data should return empty response for all frequencies")
        void calculate_withEmptyData_shouldReturnEmptyResponse() {
            DcaRequest weeklyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
            DcaRequest monthlyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
            DcaRequest quarterlyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<OhlcvDataDTO> emptyData = MockPriceData.emptyPricesOhlcv();

            DcaResponse weeklyResult = service.calculate(weeklyRequest, emptyData);
            DcaResponse monthlyResult = service.calculate(monthlyRequest, emptyData);
            DcaResponse quarterlyResult = service.calculate(quarterlyRequest, emptyData);

            assertThat(weeklyResult.getTotalInvested()).isEqualTo(0.0);
            assertThat(monthlyResult.getTotalInvested()).isEqualTo(0.0);
            assertThat(quarterlyResult.getTotalInvested()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should handle data spanning multiple quarters correctly")
        void calculate_withMultipleQuarters_shouldFilterCorrectly() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            // Create data spanning all 4 quarters
            List<OhlcvDataDTO> priceData = List.of(
                    createOhlcvDataDTO("2024-01-07", 40000.0),  // Q1
                    createOhlcvDataDTO("2024-01-14", 41000.0),  // Q1 - should be ignored
                    createOhlcvDataDTO("2024-04-07", 42000.0),  // Q2
                    createOhlcvDataDTO("2024-07-07", 43000.0),  // Q3
                    createOhlcvDataDTO("2024-10-07", 44000.0)   // Q4
            );

            DcaResponse result = service.calculate(request, priceData);

            // Should use 4 points (one per quarter)
            assertThat(result.getTotalInvested()).isEqualTo(400.0);
        }

        private OhlcvDataDTO createOhlcvDataDTO(String date, double closePrice) {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            return OhlcvDataDTO.builder()
                    .date(localDate)
                    .open(BigDecimal.valueOf(closePrice))
                    .high(BigDecimal.valueOf(closePrice + 1000))
                    .low(BigDecimal.valueOf(closePrice - 1000))
                    .close(BigDecimal.valueOf(closePrice))
                    .volume(50000000L)
                    .dividend(BigDecimal.ZERO)
                    .build();
        }
    }
}
