package com.example.demo;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatesTableRepository extends JpaRepository<RatesTable, Integer> {

    RatesTable findByTableDate(LocalDate tableDate);
    RatesTable findByTableNumber(String tableNumber);
}
