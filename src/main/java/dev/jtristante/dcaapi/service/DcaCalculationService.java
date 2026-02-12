package dev.jtristante.dcaapi.service;


import dev.jtristante.dcaapi.dto.DcaDetailedResponse;
import dev.jtristante.dcaapi.dto.DcaInvestmentDetail;
import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.dto.OhlcvDataDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class DcaCalculationService {

    private static final int CALCULATION_SCALE = 10;

    private record CalculationContext(
            List<OhlcvDataDTO> filteredData,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal currentPrice,
            BigDecimal amount
    ) {
    }

    private record CalculationMetrics(
            BigDecimal totalInvested,
            BigDecimal totalUnits,
            List<DcaInvestmentDetail> investmentDetails
    ) {
    }

    public DcaResponse calculate(DcaRequest request, List<OhlcvDataDTO> ohlcvData) {
        if (isEmptyPriceData(ohlcvData)) {
            return createEmptyResponse();
        }

        CalculationContext context = createCalculationContext(request, ohlcvData);
        CalculationMetrics metrics = accumulateInvestments(context, false);

        if (hasNoUnits(metrics.totalUnits())) {
            return createEmptyResponse();
        }

        return buildSummaryResponse(context.currentPrice(), metrics);
    }

    public DcaDetailedResponse calculateDetailed(DcaRequest request, List<OhlcvDataDTO> ohlcvData) {
        if (isEmptyPriceData(ohlcvData)) {
            return createEmptyDetailedResponse();
        }

        CalculationContext context = createCalculationContext(request, ohlcvData);
        CalculationMetrics metrics = accumulateInvestments(context, true);

        if (hasNoUnits(metrics.totalUnits())) {
            return createEmptyDetailedResponse();
        }

        return buildDetailedResponse(context.currentPrice(), metrics);
    }

    private CalculationContext createCalculationContext(DcaRequest request, List<OhlcvDataDTO> ohlcvData) {
        return new CalculationContext(
                filterByFrequency(ohlcvData, request.getFrequency()),
                request.getStartDate(),
                request.getEndDate(),
                getLatestPrice(ohlcvData),
                BigDecimal.valueOf(request.getAmount())
        );
    }

    private CalculationMetrics accumulateInvestments(CalculationContext context, boolean collectDetails) {
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits = BigDecimal.ZERO;
        List<DcaInvestmentDetail> investmentDetails = collectDetails ? new ArrayList<>() : null;

        for (OhlcvDataDTO bar : context.filteredData()) {
            if (!isDateInRange(bar, context.startDate(), context.endDate())) {
                continue;
            }

            BigDecimal units = calculateUnitsForPurchase(context.amount(), bar.close().doubleValue());
            if (units.compareTo(BigDecimal.ZERO) > 0) {
                totalInvested = totalInvested.add(context.amount());
                totalUnits = totalUnits.add(units);

                if (collectDetails) {
                    investmentDetails.add(createInvestmentDetail(bar, context.amount(), units, totalInvested, totalUnits));
                }
            }
        }

        return new CalculationMetrics(totalInvested, totalUnits, investmentDetails);
    }

    private DcaInvestmentDetail createInvestmentDetail(OhlcvDataDTO bar, BigDecimal amount,
                                                       BigDecimal units, BigDecimal cumulativeInvested,
                                                       BigDecimal cumulativeUnits) {
        return new DcaInvestmentDetail()
                .date(bar.date())
                .amount(formatToTwoDecimals(amount))
                .price(formatToTwoDecimals(bar.close()))
                .unitsPurchased(formatToEightDecimals(units))
                .cumulativeUnits(formatToEightDecimals(cumulativeUnits))
                .cumulativeInvested(formatToTwoDecimals(cumulativeInvested))
                .valueAtDate(formatToTwoDecimals(cumulativeUnits.multiply(bar.close())));
    }

    private DcaResponse buildSummaryResponse(BigDecimal currentPrice, CalculationMetrics metrics) {
        return new DcaResponse()
                .totalInvested(formatToTwoDecimals(metrics.totalInvested()))
                .units(formatToEightDecimals(metrics.totalUnits()))
                .weightedAveragePrice(calculateWeightedAveragePrice(metrics.totalInvested(), metrics.totalUnits()))
                .currentValue(calculateCurrentValue(metrics.totalUnits(), currentPrice))
                .profit(calculateProfit(metrics.totalUnits(), currentPrice, metrics.totalInvested()))
                .roi(calculateRoiPercentage(metrics.totalUnits(), currentPrice, metrics.totalInvested()));
    }

    private DcaDetailedResponse buildDetailedResponse(BigDecimal currentPrice, CalculationMetrics metrics) {
        return new DcaDetailedResponse()
                .totalInvested(formatToTwoDecimals(metrics.totalInvested()))
                .units(formatToEightDecimals(metrics.totalUnits()))
                .weightedAveragePrice(calculateWeightedAveragePrice(metrics.totalInvested(), metrics.totalUnits()))
                .currentValue(calculateCurrentValue(metrics.totalUnits(), currentPrice))
                .profit(calculateProfit(metrics.totalUnits(), currentPrice, metrics.totalInvested()))
                .roi(calculateRoiPercentage(metrics.totalUnits(), currentPrice, metrics.totalInvested()))
                .investments(metrics.investmentDetails());
    }

    private Double calculateWeightedAveragePrice(BigDecimal totalInvested, BigDecimal totalUnits) {
        return formatToTwoDecimals(totalInvested.divide(totalUnits, CALCULATION_SCALE, RoundingMode.HALF_UP));
    }

    private Double calculateCurrentValue(BigDecimal totalUnits, BigDecimal currentPrice) {
        return formatToTwoDecimals(totalUnits.multiply(currentPrice));
    }

    private Double calculateProfit(BigDecimal totalUnits, BigDecimal currentPrice, BigDecimal totalInvested) {
        return formatToTwoDecimals(totalUnits.multiply(currentPrice).subtract(totalInvested));
    }

    private Double calculateRoiPercentage(BigDecimal totalUnits, BigDecimal currentPrice, BigDecimal totalInvested) {
        if (totalInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal currentValue = totalUnits.multiply(currentPrice);
        BigDecimal profit = currentValue.subtract(totalInvested);
        return profit.divide(totalInvested, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isEmptyPriceData(List<OhlcvDataDTO> priceData) {
        return priceData == null || priceData.isEmpty();
    }

    private BigDecimal getLatestPrice(List<OhlcvDataDTO> priceData) {
        return priceData.getLast().close();
    }

    private boolean isDateInRange(OhlcvDataDTO bar, LocalDate startDate, LocalDate endDate) {
        LocalDate barDate = bar.date();
        return !barDate.isBefore(startDate) && !barDate.isAfter(endDate);
    }

    private BigDecimal calculateUnitsForPurchase(BigDecimal amount, double price) {
        if (price <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(BigDecimal.valueOf(price), CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    private boolean hasNoUnits(BigDecimal totalUnits) {
        return totalUnits.compareTo(BigDecimal.ZERO) == 0;
    }

    private Double formatToTwoDecimals(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double formatToEightDecimals(BigDecimal value) {
        return value.setScale(8, RoundingMode.HALF_UP).doubleValue();
    }

    private List<OhlcvDataDTO> filterByFrequency(List<OhlcvDataDTO> data, DcaRequest.FrequencyEnum frequency) {
        return switch (frequency) {
            case WEEKLY -> data;
            case MONTHLY -> filterFirstPerMonth(data);
            case QUARTERLY -> filterFirstPerQuarter(data);
        };
    }

    private List<OhlcvDataDTO> sortDataByDate(List<OhlcvDataDTO> data) {
        if (data.isEmpty()) {
            return data;
        }
        List<OhlcvDataDTO> sortedData = new ArrayList<>(data);
        sortedData.sort(Comparator.comparing(OhlcvDataDTO::date));
        return sortedData;
    }

    private List<OhlcvDataDTO> filterFirstPerMonth(List<OhlcvDataDTO> data) {
        List<OhlcvDataDTO> sortedData = sortDataByDate(data);
        if (sortedData.isEmpty()) {
            return sortedData;
        }

        Map<String, OhlcvDataDTO> firstOfMonthMap = new LinkedHashMap<>();

        for (OhlcvDataDTO bar : sortedData) {
            String yearMonth = bar.date().getYear() + "-" + bar.date().getMonthValue();
            firstOfMonthMap.putIfAbsent(yearMonth, bar);
        }

        return new ArrayList<>(firstOfMonthMap.values());
    }

    private List<OhlcvDataDTO> filterFirstPerQuarter(List<OhlcvDataDTO> data) {
        List<OhlcvDataDTO> sortedData = sortDataByDate(data);
        if (sortedData.isEmpty()) {
            return sortedData;
        }

        Map<String, OhlcvDataDTO> firstOfQuarterMap = new LinkedHashMap<>();

        for (OhlcvDataDTO bar : sortedData) {
            String yearQuarter = bar.date().getYear() + "-" + getQuarter(bar.date());
            firstOfQuarterMap.putIfAbsent(yearQuarter, bar);
        }

        return new ArrayList<>(firstOfQuarterMap.values());
    }

    private int getQuarter(LocalDate date) {
        Month month = date.getMonth();
        return switch (month) {
            case JANUARY, FEBRUARY, MARCH -> 1;
            case APRIL, MAY, JUNE -> 2;
            case JULY, AUGUST, SEPTEMBER -> 3;
            case OCTOBER, NOVEMBER, DECEMBER -> 4;
        };
    }

    private DcaResponse createEmptyResponse() {
        return new DcaResponse()
                .totalInvested(0.0)
                .units(0.0)
                .weightedAveragePrice(0.0)
                .currentValue(0.0)
                .profit(0.0)
                .roi(0.0);
    }

    private DcaDetailedResponse createEmptyDetailedResponse() {
        return new DcaDetailedResponse()
                .totalInvested(0.0)
                .units(0.0)
                .weightedAveragePrice(0.0)
                .currentValue(0.0)
                .profit(0.0)
                .roi(0.0)
                .investments(new ArrayList<>());
    }
}
