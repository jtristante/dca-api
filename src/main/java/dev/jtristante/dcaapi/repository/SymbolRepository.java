package dev.jtristante.dcaapi.repository;

import dev.jtristante.dcaapi.model.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {

    List<Symbol> findByTickerStartingWithIgnoreCase(String ticker);

    List<Symbol> findByNameStartingWithIgnoreCase(String name);

    List<Symbol> findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(String name, String ticker);

    Optional<Symbol> findByTickerIgnoreCase(String ticker);
}
