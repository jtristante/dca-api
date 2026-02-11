package dev.jtristante.dcaapi.service;

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

    public DcaResponse calculate(DcaRequest request, List<OhlcvDataDTO> ohlcvData) {
        if (isEmptyPriceData(ohlcvData)) {
            return createEmptyResponse();
        }

        // Filter data by frequency before processing
        List<OhlcvDataDTO> filteredData = filterByFrequency(ohlcvData, request.getFrequency());

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        BigDecimal currentPrice = getLatestPrice(ohlcvData);
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits = BigDecimal.ZERO;

        for (OhlcvDataDTO bar : filteredData) {
            if (!isDateInRange(bar, startDate, endDate)) {
                continue;
            }

            BigDecimal units = calculateUnitsForPurchase(amount, bar.close().doubleValue());
            if (units.compareTo(BigDecimal.ZERO) > 0) {
                totalInvested = totalInvested.add(amount);
                totalUnits = totalUnits.add(units);
            }
        }

        if (hasNoUnits(totalUnits)) {
            return createEmptyResponse();
        }

        BigDecimal weightedAveragePrice = calculateWeightedAveragePrice(totalInvested, totalUnits);
        BigDecimal currentValue = calculateCurrentValue(totalUnits, currentPrice);
        BigDecimal profit = calculateProfit(currentValue, totalInvested);
        Double roiPct = calculateRoiPercentage(profit, totalInvested);

        return buildResponse(totalInvested, totalUnits, weightedAveragePrice, currentValue, profit, roiPct);
    }

    private boolean isEmptyPriceData(List<OhlcvDataDTO> priceData) {
        return priceData == null || priceData.isEmpty();
    }

    private BigDecimal getLatestPrice(List<OhlcvDataDTO> priceData) {
        return priceData.getLast().close();
    }

    private LocalDate barToLocalDate(OhlcvDataDTO bar) {
        return bar.date();
    }

    private boolean isDateInRange(OhlcvDataDTO bar, LocalDate startDate, LocalDate endDate) {
        LocalDate barDate = barToLocalDate(bar);
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

    private BigDecimal calculateWeightedAveragePrice(BigDecimal totalInvested, BigDecimal totalUnits) {
        return totalInvested.divide(totalUnits, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentValue(BigDecimal totalUnits, BigDecimal currentPrice) {
        return totalUnits.multiply(currentPrice);
    }

    private BigDecimal calculateProfit(BigDecimal currentValue, BigDecimal totalInvested) {
        return currentValue.subtract(totalInvested);
    }

    private Double calculateRoiPercentage(BigDecimal profit, BigDecimal totalInvested) {
        if (totalInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return profit.divide(totalInvested, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private DcaResponse buildResponse(BigDecimal totalInvested, BigDecimal totalUnits,
                                      BigDecimal weightedAveragePrice, BigDecimal currentValue,
                                      BigDecimal profit, Double roiPct) {
        return new DcaResponse()
                .totalInvested(formatToTwoDecimals(totalInvested))
                .units(formatToEightDecimals(totalUnits))
                .weightedAveragePrice(formatToTwoDecimals(weightedAveragePrice))
                .currentValue(formatToTwoDecimals(currentValue))
                .profit(formatToTwoDecimals(profit))
                .roi(roiPct);
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
            firstOfMonthMap.computeIfAbsent(yearMonth, k -> bar);
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
            firstOfQuarterMap.computeIfAbsent(yearQuarter, k -> bar);
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
}
