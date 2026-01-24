package dev.jtristante.dcaapi.repository;

import dev.jtristante.dcaapi.model.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {

    List<Symbol> findByNameContainingIgnoreCase(String name);

    List<Symbol> findByNameContainingIgnoreCaseAndTickerStartingWithIgnoreCase(String name, String ticker);
}
