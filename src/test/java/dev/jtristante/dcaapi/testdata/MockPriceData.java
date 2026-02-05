package dev.jtristante.dcaapi.testdata;

import dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto.StockHistoryDTO;

import java.time.LocalDate;
import java.util.List;

public final class MockPriceData {

    private MockPriceData() {
    }

    public static List<StockHistoryDTO> risingPrices() {
        return List.of(
                createBar("2024-01-31", 42500.0),
                createBar("2024-02-29", 52500.0),
                createBar("2024-03-31", 61500.0)
        );
    }

    public static List<StockHistoryDTO> fallingPrices() {
        return List.of(
                createBar("2024-01-31", 60000.0),
                createBar("2024-02-29", 40000.0)
        );
    }

    public static List<StockHistoryDTO> appreciationPrices() {
        return List.of(
                createBar("2024-03-31", 40000.0),
                createBar("2024-06-30", 60000.0)
        );
    }

    public static List<StockHistoryDTO> mixedPrices() {
        return List.of(
                createBar("2024-01-15", 40000.0),
                createBar("2024-02-15", 42000.0),
                createBar("2024-03-15", 45000.0),
                createBar("2024-04-15", 50000.0),
                createBar("2024-05-15", 55000.0)
        );
    }

    public static List<StockHistoryDTO> singlePurchasePrices() {
        return List.of(
                createBar("2024-01-15", 40000.0),
                createBar("2024-03-15", 45000.0),
                createBar("2024-04-15", 50000.0)
        );
    }

    public static StockHistoryDTO createBar(String date, double closePrice) {
        LocalDate localDate = LocalDate.parse(date);
        return StockHistoryDTO.builder()
                .timestamp(date)
                .timestampUnix(localDate.toEpochDay() * 86400)
                .open(closePrice)
                .high(closePrice + 1000)
                .low(closePrice - 1000)
                .close(closePrice)
                .volume(50000000L)
                .build();
    }
}
