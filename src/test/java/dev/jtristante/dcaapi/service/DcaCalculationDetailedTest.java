package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaCalculationResult;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.OhlcvDataDTO;
import dev.jtristante.dcaapi.testdata.MockPriceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DcaCalculationDetailedTest {

    private DcaCalculationService service;

    @BeforeEach
    void setUp() {
        service = new DcaCalculationService();
    }

    @Nested
    @DisplayName("calculateDca detailed basic functionality")
    class CalculateDetailedBasicTests {

        @Test
        @DisplayName("should return list with multiple results (one per purchase)")
        void calculateDca_detailed_shouldReturnMultipleResults() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(100.0);
            assertThat(result.getFirst().getUnits()).isGreaterThan(0.0);
            assertThat(result.getFirst().getWeightedAveragePrice()).isGreaterThan(0.0);
            assertThat(result.getFirst().getProfit()).isNotNull();
            assertThat(result.getFirst().getRoi()).isNotNull();
        }

        @Test
        @DisplayName("should return single empty result when no price data")
        void calculateDca_detailed_shouldReturnEmptyResult_whenNoPriceData() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.emptyPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getFirst().getUnits()).isEqualTo(0.0);
            assertThat(result.getFirst().getWeightedAveragePrice()).isEqualTo(0.0);
            assertThat(result.getFirst().getProfit()).isEqualTo(0.0);
            assertThat(result.getFirst().getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return single empty result when price data is null")
        void calculateDca_detailed_shouldReturnEmptyResult_whenPriceDataIsNull() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<DcaCalculationResult> result = service.calculateDca(request, null, true);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getFirst().getRoi()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return single empty result when no valid purchases")
        void calculateDca_detailed_shouldReturnEmptyResult_whenNoValidPurchases() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getFirst().getUnits()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("calculateDca detailed result fields")
    class CalculateDetailedFieldsTests {

        @Test
        @DisplayName("should include correct date for each result")
        void calculateDca_detailed_shouldIncludeCorrectDate() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getDate()).isEqualTo(LocalDate.of(2024, 1, 31));
            assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @Test
        @DisplayName("should have increasing total invested in each result")
        void calculateDca_detailed_shouldHaveIncreasingTotalInvested() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(100.0);
            assertThat(result.get(1).getTotalInvested()).isEqualTo(200.0);
            assertThat(result.get(2).getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("should have increasing units in each result")
        void calculateDca_detailed_shouldHaveIncreasingUnits() {
            DcaRequest request = new DcaRequest("BTC-EUR", 10000.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            assertThat(result.getFirst().getUnits())
                    .isCloseTo(0.23529412, offset(0.00000001));
            assertThat(result.get(1).getUnits())
                    .isCloseTo(0.42577031, offset(0.00000001));
            assertThat(result.get(2).getUnits())
                    .isCloseTo(0.58837194, offset(0.00000001));
            assertThat(result.get(1).getUnits()).isGreaterThan(result.getFirst().getUnits());
            assertThat(result.get(2).getUnits()).isGreaterThan(result.get(1).getUnits());
        }

        @Test
        @DisplayName("should have correct weighted average price in each result")
        void calculateDca_detailed_shouldHaveCorrectWeightedAveragePrice() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            // First purchase: weighted average = purchase price
            assertThat(result.getFirst().getWeightedAveragePrice()).isEqualTo(42500.0);
            // Weighted average should be recalculated with each purchase
            assertThat(result.get(1).getWeightedAveragePrice()).isGreaterThan(0.0);
            assertThat(result.get(2).getWeightedAveragePrice()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should have correct profit and ROI in each result")
        void calculateDca_detailed_shouldHaveCorrectProfitAndRoi() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            for (DcaCalculationResult r : result) {
                assertThat(r.getProfit()).isNotNull();
                assertThat(r.getRoi()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("calculateDca detailed edge cases")
    class CalculateDetailedEdgeCaseTests {

        @Test
        @DisplayName("should handle single purchase")
        void calculateDca_detailed_shouldHandleSinglePurchase() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.singlePurchasePricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(100.0);
            assertThat(result.getFirst().getUnits()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should skip dates outside investment period")
        void calculateDca_detailed_shouldSkipDatesOutsideInvestmentPeriod() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.mixedPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getDate()).isAfterOrEqualTo(LocalDate.of(2024, 3, 1));
            assertThat(result.get(1).getDate()).isBeforeOrEqualTo(LocalDate.of(2024, 4, 30));
        }

        @Test
        @DisplayName("should calculate positive ROI when price appreciates")
        void calculateDca_detailed_shouldCalculatePositiveRoi_whenPriceAppreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.appreciationPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getTotalInvested()).isEqualTo(100.0);
            assertThat(result.get(1).getTotalInvested()).isEqualTo(200.0);
            assertThat(result.get(1).getProfit()).isGreaterThan(0.0);
            assertThat(result.get(1).getRoi()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("should calculate negative ROI when price depreciates")
        void calculateDca_detailed_shouldCalculateNegativeRoi_whenPriceDepreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(2);
            assertThat(result.get(1).getTotalInvested()).isEqualTo(200.0);
            assertThat(result.get(1).getProfit()).isLessThan(0.0);
            assertThat(result.get(1).getRoi()).isLessThan(0.0);
        }
    }

    @Nested
    @DisplayName("calculateDca with frequency filtering")
    class CalculateDetailedFrequencyTests {

        @Test
        @DisplayName("WEEKLY frequency should track all data points")
        void calculateDca_detailed_withWeeklyFrequency_shouldTrackAllPoints() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(8);
            assertThat(result.get(7).getTotalInvested()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("MONTHLY frequency should track first per month")
        void calculateDca_detailed_withMonthlyFrequency_shouldTrackFirstPerMonth() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(3);
            assertThat(result.get(2).getTotalInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("QUARTERLY frequency should track first per quarter")
        void calculateDca_detailed_withQuarterlyFrequency_shouldTrackFirstPerQuarter() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            assertThat(result).hasSize(2);
            assertThat(result.get(1).getTotalInvested()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("cumulative values should be increasing")
        void calculateDca_detailed_cumulativeValuesShouldBeIncreasing() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            List<DcaCalculationResult> result = service.calculateDca(request, priceData, true);

            for (int i = 1; i < result.size(); i++) {
                assertThat(result.get(i).getTotalInvested())
                        .isGreaterThan(result.get(i - 1).getTotalInvested());
                assertThat(result.get(i).getUnits())
                        .isGreaterThan(result.get(i - 1).getUnits());
            }
        }
    }

    @Nested
    @DisplayName("calculateDca detailed matches summary")
    class CalculateDetailedMatchesSummaryTests {

        @Test
        @DisplayName("last detailed result should match summary result")
        void calculateDca_detailed_lastResultShouldMatchSummary() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            List<DcaCalculationResult> detailedResult = service.calculateDca(request, priceData, true);
            List<DcaCalculationResult> summaryResult = service.calculateDca(request, priceData, false);

            DcaCalculationResult lastDetailed = detailedResult.getLast();
            DcaCalculationResult summary = summaryResult.getFirst();

            assertThat(lastDetailed.getTotalInvested()).isEqualTo(summary.getTotalInvested());
            assertThat(lastDetailed.getUnits()).isEqualTo(summary.getUnits());
            assertThat(lastDetailed.getWeightedAveragePrice()).isEqualTo(summary.getWeightedAveragePrice());
            assertThat(lastDetailed.getProfit()).isEqualTo(summary.getProfit());
            assertThat(lastDetailed.getRoi()).isEqualTo(summary.getRoi());
        }
    }
}
