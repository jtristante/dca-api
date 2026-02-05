package dev.jtristante.dcaapi.repository;

import dev.jtristante.dcaapi.model.OhlcvData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OhlcvDataRepository extends JpaRepository<OhlcvData, Long> {

    @Query("SELECT o FROM OhlcvData o WHERE o.symbol.id = :symbolId AND o.id.priceDate BETWEEN :startDate AND :endDate ORDER BY o.id.priceDate ASC")
    List<OhlcvData> findBySymbolIdAndDateRange(
            @Param("symbolId") Long symbolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsBySymbolIdAndIdPriceDateBetween(Long symbolId, LocalDate startDate, LocalDate endDate);
}
