package dev.jtristante.dcaapi.service;

import dev.jtristante.dcaapi.dto.DcaRequest;
import dev.jtristante.dcaapi.dto.DcaResponse;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.api.YahooFinanceApi;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.GetStocksHistoryResponseDTO;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.IntervalType;
import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.StockHistoryDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DcaCalculationService {

    private static final int CALCULATION_SCALE = 10;

    private final YahooFinanceApi yahooFinanceApi;

    public DcaCalculationService(YahooFinanceApi yahooFinanceApi) {
        this.yahooFinanceApi = yahooFinanceApi;
    }

    public DcaResponse calculate(DcaRequest request) {
        IntervalType interval = mapFrequencyToInterval(request.getFrequency());
        List<StockHistoryDTO> priceData = fetchHistoricalPrices(request.getSymbol(), interval);

        if (isEmptyPriceData(priceData)) {
            return createEmptyResponse();
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        BigDecimal currentPrice = getLatestPrice(priceData);
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits = BigDecimal.ZERO;

        for (StockHistoryDTO bar : priceData) {
            if (!isDateInRange(bar, startDate, endDate)) {
                continue;
            }

            BigDecimal units = calculateUnitsForPurchase(amount, bar.close());
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
        double roiPct = calculateRoiPercentage(profit, totalInvested);

        return buildResponse(totalInvested, totalUnits, weightedAveragePrice, currentValue, profit, roiPct);
    }

    private List<StockHistoryDTO> fetchHistoricalPrices(String symbol, IntervalType interval) {
        GetStocksHistoryResponseDTO response = yahooFinanceApi.getStocksHistory(symbol, interval, null, null);
        return response.body();
    }

    private boolean isEmptyPriceData(List<StockHistoryDTO> priceData) {
        return priceData == null || priceData.isEmpty();
    }

    private BigDecimal getLatestPrice(List<StockHistoryDTO> priceData) {
        return BigDecimal.valueOf(priceData.getLast().close());
    }

    private LocalDate barToLocalDate(StockHistoryDTO bar) {
        return LocalDate.ofEpochDay(bar.timestampUnix() / 86400);
    }

    private boolean isDateInRange(StockHistoryDTO bar, LocalDate startDate, LocalDate endDate) {
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

    private double calculateRoiPercentage(BigDecimal profit, BigDecimal totalInvested) {
        if (totalInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return profit.divide(totalInvested, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private DcaResponse buildResponse(BigDecimal totalInvested, BigDecimal totalUnits,
                                      BigDecimal weightedAveragePrice, BigDecimal currentValue,
                                      BigDecimal profit, double roiPct) {
        return new DcaResponse()
                .totalInvested(formatToTwoDecimals(totalInvested))
                .units(formatToEightDecimals(totalUnits))
                .weightedAveragePrice(formatToTwoDecimals(weightedAveragePrice))
                .currentValue(formatToTwoDecimals(currentValue))
                .profit(formatToTwoDecimals(profit))
                .roiPct(roiPct);
    }

    private Double formatToTwoDecimals(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double formatToEightDecimals(BigDecimal value) {
        return value.setScale(8, RoundingMode.HALF_UP).doubleValue();
    }

    private IntervalType mapFrequencyToInterval(DcaRequest.FrequencyEnum frequency) {
        return switch (frequency) {
            case WEEKLY -> IntervalType.W1;
            case MONTHLY -> IntervalType.MO1;
            case QUARTERLY -> IntervalType.Q1;
        };
    }

    private DcaResponse createEmptyResponse() {
        return new DcaResponse()
                .totalInvested(0.0)
                .units(0.0)
                .weightedAveragePrice(0.0)
                .currentValue(0.0)
                .profit(0.0)
                .roiPct(0.0);
    }
}
