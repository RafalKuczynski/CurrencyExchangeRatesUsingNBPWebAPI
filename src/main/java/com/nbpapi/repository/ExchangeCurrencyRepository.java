package com.nbpapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nbpapi.entity.ExchangeCurrency;
import com.nbpapi.entity.RatesTable;

public interface ExchangeCurrencyRepository extends JpaRepository<ExchangeCurrency, Integer> {
    ExchangeCurrency findByTable(RatesTable ratesTable);
}
