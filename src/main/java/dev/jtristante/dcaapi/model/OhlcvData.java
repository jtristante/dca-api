package dev.jtristante.dcaapi.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "ohlcv_data", indexes = {
        @Index(name = "idx_ohlcv_symbol_date", columnList = "symbol_id, price_date"),
        @Index(name = "idx_ohlcv_date", columnList = "price_date")
})
public class OhlcvData {

    @EmbeddedId
    private OhlcvDataId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("symbolId")
    @JoinColumn(name = "symbol_id", foreignKey = @ForeignKey(name = "fk_ohlcv_symbol"))
    private Symbol symbol;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal close;

    @Column
    private Long volume;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal dividend = BigDecimal.ZERO;

    /**
     * Default constructor for JPA.
     */
    public OhlcvData() {
    }

    public OhlcvDataId getId() {
        return id;
    }

    public void setId(OhlcvDataId id) {
        this.id = id;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getDividend() {
        return dividend;
    }

    public void setDividend(BigDecimal dividend) {
        this.dividend = dividend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OhlcvData ohlcvData = (OhlcvData) o;
        return Objects.equals(id, ohlcvData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OhlcvData{" +
                "id=" + id +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", dividend=" + dividend +
                '}';
    }
}
