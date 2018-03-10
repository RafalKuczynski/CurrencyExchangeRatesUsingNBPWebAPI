package com.nbpapi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeCurrencyRepository extends JpaRepository<ExchangeCurrency, Integer> {
    ExchangeCurrency findByTable(RatesTable ratesTable);
}
