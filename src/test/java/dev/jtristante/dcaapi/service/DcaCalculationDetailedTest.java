package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaDetailedResponse;
import dev.jtristante.dcaapi.dto.DcaInvestmentDetail;
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
    @DisplayName("calculateDetailed basic functionality")
    class CalculateDetailedBasicTests {

        @Test
        @DisplayName("should return detailed response with investments array")
        void calculateDetailed_shouldReturnDetailedResponse_withInvestments() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(300.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getWeightedAveragePrice()).isGreaterThan(0.0);
            assertThat(result.getCurrentValue()).isGreaterThan(0.0);
            assertThat(result.getProfit()).isGreaterThan(0.0);
            assertThat(result.getRoi()).isNotNull();
            assertThat(result.getInvestments()).isNotNull();
            assertThat(result.getInvestments()).hasSize(3);
        }

        @Test
        @DisplayName("should return empty investments list when no price data")
        void calculateDetailed_shouldReturnEmptyInvestmentsList_whenNoPriceData() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.emptyPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getUnits()).isEqualTo(0.0);
            assertThat(result.getWeightedAveragePrice()).isEqualTo(0.0);
            assertThat(result.getCurrentValue()).isEqualTo(0.0);
            assertThat(result.getProfit()).isEqualTo(0.0);
            assertThat(result.getRoi()).isEqualTo(0.0);
            assertThat(result.getInvestments()).isNotNull();
            assertThat(result.getInvestments()).isEmpty();
        }

        @Test
        @DisplayName("should return empty investments list when price data is null")
        void calculateDetailed_shouldReturnEmptyInvestmentsList_whenPriceDataIsNull() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            DcaDetailedResponse result = service.calculateDetailed(request, null);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getRoi()).isEqualTo(0.0);
            assertThat(result.getInvestments()).isNotNull();
            assertThat(result.getInvestments()).isEmpty();
        }

        @Test
        @DisplayName("should return empty investments list when no valid purchases")
        void calculateDetailed_shouldReturnEmptyInvestmentsList_whenNoValidPurchases() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(0.0);
            assertThat(result.getUnits()).isEqualTo(0.0);
            assertThat(result.getInvestments()).isNotNull();
            assertThat(result.getInvestments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateDetailed investment detail fields")
    class CalculateDetailedFieldsTests {

        @Test
        @DisplayName("should include correct date in investment details")
        void calculateDetailed_shouldIncludeCorrectDate_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(2);
            assertThat(result.getInvestments().get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 31));
            assertThat(result.getInvestments().get(1).getDate()).isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @Test
        @DisplayName("should include correct amount in investment details")
        void calculateDetailed_shouldIncludeCorrectAmount_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 150.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            for (DcaInvestmentDetail detail : result.getInvestments()) {
                assertThat(detail.getAmount()).isEqualTo(150.0);
            }
        }

        @Test
        @DisplayName("should include correct price in investment details")
        void calculateDetailed_shouldIncludeCorrectPrice_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            assertThat(result.getInvestments().get(0).getPrice()).isEqualTo(42500.0);
            assertThat(result.getInvestments().get(1).getPrice()).isEqualTo(52500.0);
            assertThat(result.getInvestments().get(2).getPrice()).isEqualTo(61500.0);
        }

        @Test
        @DisplayName("should include correct units purchased in investment details")
        void calculateDetailed_shouldIncludeCorrectUnitsPurchased_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 10000.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            assertThat(result.getInvestments().get(0).getUnitsPurchased())
                    .isCloseTo(0.23529412, offset(0.00000001));
            assertThat(result.getInvestments().get(1).getUnitsPurchased())
                    .isCloseTo(0.19047619, offset(0.00000001));
            assertThat(result.getInvestments().get(2).getUnitsPurchased())
                    .isCloseTo(0.16260163, offset(0.00000001));
        }

        @Test
        @DisplayName("should include correct cumulative units in investment details")
        void calculateDetailed_shouldIncludeCorrectCumulativeUnits_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 10000.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            assertThat(result.getInvestments().get(0).getCumulativeUnits())
                    .isCloseTo(0.23529412, offset(0.00000001));
            assertThat(result.getInvestments().get(1).getCumulativeUnits())
                    .isCloseTo(0.42577031, offset(0.00000001));
            assertThat(result.getInvestments().get(2).getCumulativeUnits())
                    .isCloseTo(0.58837194, offset(0.00000001));
        }

        @Test
        @DisplayName("should include correct cumulative invested in investment details")
        void calculateDetailed_shouldIncludeCorrectCumulativeInvested_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            assertThat(result.getInvestments().get(0).getCumulativeInvested()).isEqualTo(100.0);
            assertThat(result.getInvestments().get(1).getCumulativeInvested()).isEqualTo(200.0);
            assertThat(result.getInvestments().get(2).getCumulativeInvested()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("should include correct value at date in investment details")
        void calculateDetailed_shouldIncludeCorrectValueAtDate_investmentDetails() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getInvestments()).hasSize(3);
            assertThat(result.getInvestments().get(0).getValueAtDate())
                    .isCloseTo(100.00, offset(0.5));
            assertThat(result.getInvestments().get(1).getValueAtDate())
                    .isCloseTo(223.53, offset(0.5));
            assertThat(result.getInvestments().get(2).getValueAtDate())
                    .isCloseTo(361.85, offset(0.5));
        }
    }

    @Nested
    @DisplayName("calculateDetailed edge cases")
    class CalculateDetailedEdgeCaseTests {

        @Test
        @DisplayName("should handle single purchase")
        void calculateDetailed_shouldHandleSinglePurchase() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.singlePurchasePricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(100.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getInvestments()).isNotNull();
            assertThat(result.getInvestments()).hasSize(1);
            assertThat(result.getInvestments().getFirst().getCumulativeUnits())
                    .isEqualTo(result.getInvestments().getFirst().getUnitsPurchased());
            assertThat(result.getInvestments().getFirst().getCumulativeInvested())
                    .isEqualTo(100.0);
        }

        @Test
        @DisplayName("should skip dates outside investment period")
        void calculateDetailed_shouldSkipDatesOutsideInvestmentPeriod() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.mixedPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getUnits()).isGreaterThan(0.0);
            assertThat(result.getInvestments()).hasSize(2);
            assertThat(result.getInvestments().get(0).getDate()).isAfterOrEqualTo(LocalDate.of(2024, 3, 1));
            assertThat(result.getInvestments().get(1).getDate()).isBeforeOrEqualTo(LocalDate.of(2024, 4, 30));
        }

        @Test
        @DisplayName("should calculate positive ROI when price appreciates")
        void calculateDetailed_shouldCalculatePositiveRoi_whenPriceAppreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.appreciationPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isGreaterThan(0.0);
            assertThat(result.getRoi()).isNotNull();
            assertThat(result.getInvestments()).hasSize(2);
            assertThat(result.getInvestments().get(1).getValueAtDate())
                    .isGreaterThan(result.getInvestments().get(1).getCumulativeInvested());
        }

        @Test
        @DisplayName("should calculate negative ROI when price depreciates")
        void calculateDetailed_shouldCalculateNegativeRoi_whenPriceDepreciates() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));

            List<OhlcvDataDTO> priceData = MockPriceData.fallingPricesOhlcv();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getProfit()).isLessThan(0.0);
            assertThat(result.getRoi()).isLessThan(0.0);
            assertThat(result.getInvestments()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("calculateDetailed with frequency filtering")
    class CalculateDetailedFrequencyTests {

        @Test
        @DisplayName("WEEKLY frequency should track all data points")
        void calculateDetailed_withWeeklyFrequency_shouldTrackAllPoints() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(800.0);
            assertThat(result.getInvestments()).hasSize(8);
        }

        @Test
        @DisplayName("MONTHLY frequency should track first per month")
        void calculateDetailed_withMonthlyFrequency_shouldTrackFirstPerMonth() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(300.0);
            assertThat(result.getInvestments()).hasSize(3);
        }

        @Test
        @DisplayName("QUARTERLY frequency should track first per quarter")
        void calculateDetailed_withQuarterlyFrequency_shouldTrackFirstPerQuarter() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.QUARTERLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerQuarter();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            assertThat(result.getTotalInvested()).isEqualTo(200.0);
            assertThat(result.getInvestments()).hasSize(2);
        }

        @Test
        @DisplayName("cumulative values should be increasing")
        void calculateDetailed_cumulativeValuesShouldBeIncreasing() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.WEEKLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.multipleWeeklyPointsPerMonth();

            DcaDetailedResponse result = service.calculateDetailed(request, priceData);

            List<DcaInvestmentDetail> investments = result.getInvestments();
            for (int i = 1; i < investments.size(); i++) {
                assertThat(investments.get(i).getCumulativeInvested())
                        .isGreaterThan(investments.get(i - 1).getCumulativeInvested());
                assertThat(investments.get(i).getCumulativeUnits())
                        .isGreaterThan(investments.get(i - 1).getCumulativeUnits());
            }
        }
    }

    @Nested
    @DisplayName("calculateDetailed matches calculate summary fields")
    class CalculateDetailedMatchesCalculateTests {

        @Test
        @DisplayName("should match calculate summary fields")
        void calculateDetailed_shouldMatchCalculate_summaryFields() {
            DcaRequest request = new DcaRequest("BTC-EUR", 100.0, DcaRequest.FrequencyEnum.MONTHLY,
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            List<OhlcvDataDTO> priceData = MockPriceData.risingPricesOhlcv();

            dev.jtristante.dcaapi.dto.DcaResponse simpleResult = service.calculate(request, priceData);
            DcaDetailedResponse detailedResult = service.calculateDetailed(request, priceData);

            assertThat(detailedResult.getTotalInvested())
                    .isEqualTo(simpleResult.getTotalInvested());
            assertThat(detailedResult.getUnits())
                    .isEqualTo(simpleResult.getUnits());
            assertThat(detailedResult.getWeightedAveragePrice())
                    .isEqualTo(simpleResult.getWeightedAveragePrice());
            assertThat(detailedResult.getCurrentValue())
                    .isEqualTo(simpleResult.getCurrentValue());
            assertThat(detailedResult.getProfit())
                    .isEqualTo(simpleResult.getProfit());
            assertThat(detailedResult.getRoi())
                    .isEqualTo(simpleResult.getRoi());
        }
    }
}
