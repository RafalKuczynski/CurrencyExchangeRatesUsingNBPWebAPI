package com.nbpapi;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatesTableRepository extends JpaRepository<RatesTable, Integer> {

    RatesTable findByTableDate(LocalDate tableDate);
    RatesTable findByTableNumber(String tableNumber);
    List<RatesTable> findByTableDateGreaterThanEqualAndTableDateLessThanEqual(LocalDate start, LocalDate end);
}
