package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaCalculationResult;
import dev.jtristante.dcaapi.dto.DcaRequest;
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
    @DisplayName("calculateDca summary (detailed=false)")
    class CalculateSummaryTests {

        @Test
        @DisplayName("should return list with single element")
        void calculateDca_summary_shouldReturnListWithSingleElement() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return correct DCA calculation with multiple purchases")
        void calculateDca_summary_shouldReturnCorrectResult_withMultiplePurchases() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(300.0);
            assertThat(summary.getUnits()).isGreaterThan(0.0);
            assertThat(summary.getWeightedAveragePrice()).isGreaterThan(0.0);
            assertThat(summary.getProfit()).isGreaterThan(0.0);
            assertThat(summary.getRoi()).isNotNull();
            assertThat(summary.getDate()).isEqualTo(request.getEndDate());
        }

        @Test
        @DisplayName("should return empty response when no price data")
        void calculateDca_summary_shouldReturnEmptyResponse_whenNoPriceData() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.emptyPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(0.0);
            assertThat(summary.getUnits()).isEqualTo(0.0);
            assertThat(summary.getWeightedAveragePrice()).isEqualTo(0.0);
            assertThat(summary.getProfit()).isEqualTo(0.0);
            assertThat(summary.getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return empty response when price data is null")
        void calculateDca_summary_shouldReturnEmptyResponse_whenPriceDataIsNull() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<DcaCalculationResult> result = service.calculateDca(request, null, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(0.0);
            assertThat(summary.getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should calculate positive ROI when current price is higher than average")
        void calculateDca_summary_shouldReturnPositiveRoi_whenPriceAppreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.appreciationPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(200.0);
            assertThat(summary.getProfit()).isGreaterThan(0.0);
            assertThat(summary.getRoi()).isNotNull();
        }

        @Test
        @DisplayName("should calculate negative ROI when current price is lower than average")
        void calculateDca_summary_shouldReturnNegativeRoi_whenPriceDepreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(200.0);
            assertThat(summary.getProfit()).isLessThan(0.0);
            assertThat(summary.getRoi()).isLessThan(0.0);
        }

        @Test
        @DisplayName("should handle single purchase within period")
        void calculateDca_summary_shouldHandleSinglePurchase() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.singlePurchasePricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(100.0);
            assertThat(summary.getUnits()).isGreaterThan(0.0);
            assertThat(summary.getWeightedAveragePrice()).isEqualTo(45000.0);
            assertThat(summary.getProfit()).isCloseTo(11.11, offset(0.01));
            assertThat(summary.getRoi()).isCloseTo(0.111, offset(0.001));
        }
    }

    @Nested
    @DisplayName("calculateDca detailed (detailed=true)")
    class CalculateDetailedTests {

        @Test
        @DisplayName("should return list with multiple elements (one per purchase)")
        void calculateDca_detailed_shouldReturnMultipleElements() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("each result should have cumulative values")
        void calculateDca_detailed_shouldHaveCumulativeValues() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            // First result should have 100 invested
            assertThat(result.get(0).getTotalInvested()).isEqualTo(100.0);
            // Second result should have 200 invested
            assertThat(result.get(1).getTotalInvested()).isEqualTo(200.0);
            // Third result should have 300 invested
            assertThat(result.get(2).getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("each result should have the date of the purchase")
        void calculateDca_detailed_shouldHavePurchaseDates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result.get(0).getDate()).isNotNull();
            assertThat(result.get(1).getDate()).isNotNull();
            assertThat(result.get(2).getDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("frequency filtering tests")
    class FrequencyFilteringTests {

        @Test
        @DisplayName("WEEKLY frequency should use all data points")
        void calculateDca_withWeeklyFrequency_shouldUseAllDataPoints() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 8 weekly data points in the list

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            // All 8 points should be used (100 * 8 = 800)
            assertThat(summary.getTotalInvested()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("MONTHLY frequency should select only first point per month")
        void calculateDca_withMonthlyFrequency_shouldSelectFirstPointPerMonth() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 8 weekly points across 3 months (Jan, Feb, Mar)
            // Should use only first point of each month: Jan 7, Feb 4, Mar 3

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            // Only 3 purchases (one per month)
            assertThat(summary.getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("QUARTERLY frequency should select only first point per quarter")
        void calculateDca_withQuarterlyFrequency_shouldSelectFirstPointPerQuarter() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();
            // 6 weekly points across 2 quarters (Q1, Q2)
            // Should use only first point of each quarter: Jan 7, Apr 7

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            // Only 2 purchases (one per quarter)
            assertThat(summary.getTotalInvested()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("should handle unsorted data correctly")
        void calculateDca_withUnsortedData_shouldSortBeforeFiltering() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.unsortedWeeklyPoints();
            // Unsorted data but still 3 months present

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            // Should still pick 3 points (one per month) after sorting
            assertThat(summary.getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("MONTHLY frequency with single month should use only first point")
        void calculateDca_withMonthlyFrequencyAndSingleMonth_shouldUseFirstPoint() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();
            // 3 points in January, but should only use the first one (Jan 7)

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("QUARTERLY frequency with single quarter should use only first point")
        void calculateDca_withQuarterlyFrequencyAndSingleQuarter_shouldUseFirstPoint() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();
            // 3 points in Q1, but should only use the first one (Jan 7)

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            assertThat(summary.getTotalInvested()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("empty data should return empty response for all frequencies")
        void calculateDca_withEmptyData_shouldReturnEmptyResponse() {
            DcaRequest weeklyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
            DcaRequest monthlyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
            DcaRequest quarterlyRequest = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<OhlcvDataDTO> emptyData = MockPriceData.emptyPricesOhlcv();

            DcaCalculationResult weeklyResult = service.calculateDca(weeklyRequest, emptyData, false).getFirst();
            DcaCalculationResult monthlyResult = service.calculateDca(monthlyRequest, emptyData, false).getFirst();
            DcaCalculationResult quarterlyResult = service.calculateDca(quarterlyRequest, emptyData, false).getFirst();

            assertThat(weeklyResult.getTotalInvested()).isEqualTo(0.0);
            assertThat(monthlyResult.getTotalInvested()).isEqualTo(0.0);
            assertThat(quarterlyResult.getTotalInvested()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should handle data spanning multiple quarters correctly")
        void calculateDca_withMultipleQuarters_shouldFilterCorrectly() {
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

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, false);
            DcaCalculationResult summary = result.getFirst();

            // Should use 4 points (one per quarter)
            assertThat(summary.getTotalInvested()).isEqualTo(400.0);
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
