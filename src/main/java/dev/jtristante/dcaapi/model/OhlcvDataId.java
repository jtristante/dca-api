package dev.jtristante.dcaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class OhlcvDataId implements Serializable {

    @Column(name = "symbol_id")
    private Long symbolId;

    @Column(name = "price_date")
    private LocalDate priceDate;

    /**
     * Default constructor for JPA.
     */
    protected OhlcvDataId() {
    }

    public OhlcvDataId(Long symbolId, LocalDate priceDate) {
        this.symbolId = symbolId;
        this.priceDate = priceDate;
    }

    public Long getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(Long symbolId) {
        this.symbolId = symbolId;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OhlcvDataId that = (OhlcvDataId) o;
        return Objects.equals(symbolId, that.symbolId) && Objects.equals(priceDate, that.priceDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolId, priceDate);
    }

    @Override
    public String toString() {
        return "OhlcvDataId{" +
                "symbolId=" + symbolId +
                ", priceDate=" + priceDate +
                '}';
    }
}
