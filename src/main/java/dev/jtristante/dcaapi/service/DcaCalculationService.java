package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaCalculationResult;
import dev.jtristante.dcaapi.dto.DcaRequest;
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

    private record InvestmentPoint(
            LocalDate date,
            BigDecimal totalInvested,
            BigDecimal totalUnits,
            BigDecimal currentPrice
    ) {
    }

    public List<DcaCalculationResult> calculateDca(DcaRequest request, List<OhlcvDataDTO> ohlcvData, boolean detailed) {
        if (isEmptyPriceData(ohlcvData)) {
            return createEmptyResponse(request.getEndDate());
        }

        CalculationContext context = createCalculationContext(request, ohlcvData);
        List<InvestmentPoint> points = accumulateInvestments(context);

        if (points.isEmpty()) {
            return createEmptyResponse(request.getEndDate());
        }

        if (detailed) {
            return buildDetailedResults(points);
        } else {
            return buildSummaryResult(points, context.currentPrice(), request.getEndDate());
        }
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

    private List<InvestmentPoint> accumulateInvestments(CalculationContext context) {
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits = BigDecimal.ZERO;
        List<InvestmentPoint> points = new ArrayList<>();

        for (OhlcvDataDTO bar : context.filteredData()) {
            if (!isDateInRange(bar, context.startDate(), context.endDate())) {
                continue;
            }

            BigDecimal units = calculateUnitsForPurchase(context.amount(), bar.close().doubleValue());
            if (units.compareTo(BigDecimal.ZERO) > 0) {
                totalInvested = totalInvested.add(context.amount());
                totalUnits = totalUnits.add(units);
                points.add(new InvestmentPoint(bar.date(), totalInvested, totalUnits, bar.close()));
            }
        }

        return points;
    }

    private List<DcaCalculationResult> buildSummaryResult(List<InvestmentPoint> points, BigDecimal currentPrice, LocalDate endDate) {
        InvestmentPoint lastPoint = points.getLast();
        DcaCalculationResult result = new DcaCalculationResult();
        result.setDate(endDate);
        result.setTotalInvested(formatToTwoDecimals(lastPoint.totalInvested()));
        result.setUnits(formatToEightDecimals(lastPoint.totalUnits()));
        result.setWeightedAveragePrice(calculateWeightedAveragePrice(lastPoint.totalInvested(), lastPoint.totalUnits()));
        result.setProfit(calculateProfit(lastPoint.totalUnits(), currentPrice, lastPoint.totalInvested()));
        result.setRoi(calculateRoiPercentage(lastPoint.totalUnits(), currentPrice, lastPoint.totalInvested()));
        return List.of(result);
    }

    private List<DcaCalculationResult> buildDetailedResults(List<InvestmentPoint> points) {
        List<DcaCalculationResult> results = new ArrayList<>();

        for (InvestmentPoint point : points) {
            DcaCalculationResult result = new DcaCalculationResult();
            result.setDate(point.date());
            result.setTotalInvested(formatToTwoDecimals(point.totalInvested()));
            result.setUnits(formatToEightDecimals(point.totalUnits()));
            result.setWeightedAveragePrice(calculateWeightedAveragePrice(point.totalInvested(), point.totalUnits()));
            result.setProfit(calculateProfit(point.totalUnits(), point.currentPrice(), point.totalInvested()));
            result.setRoi(calculateRoiPercentage(point.totalUnits(), point.currentPrice(), point.totalInvested()));
            results.add(result);
        }

        return results;
    }

    private Double calculateWeightedAveragePrice(BigDecimal totalInvested, BigDecimal totalUnits) {
        if (totalUnits.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return formatToTwoDecimals(totalInvested.divide(totalUnits, CALCULATION_SCALE, RoundingMode.HALF_UP));
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

    private List<DcaCalculationResult> createEmptyResponse(LocalDate endDate) {
        DcaCalculationResult result = new DcaCalculationResult();
        result.setDate(endDate);
        result.setTotalInvested(0.0);
        result.setUnits(0.0);
        result.setWeightedAveragePrice(0.0);
        result.setProfit(0.0);
        result.setRoi(0.0);
        return List.of(result);
    }
}
