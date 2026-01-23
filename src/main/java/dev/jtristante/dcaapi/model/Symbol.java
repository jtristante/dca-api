package dev.jtristante.dcaapi.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "symbols", uniqueConstraints = {
        @UniqueConstraint(name = "uk_symbols_ticker", columnNames = "ticker")
})
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    /**
     * Default constructor for JPA.
     */
    public Symbol() {
        //Default constructor for JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol1 = (Symbol) o;
        return Objects.equals(id, symbol1.id) && Objects.equals(ticker, symbol1.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticker);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
