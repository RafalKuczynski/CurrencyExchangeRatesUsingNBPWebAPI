package com.nbpapi.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class RatesTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDate tableDate;
    private String tableNumber;
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    private List<ExchangeCurrency> currencies = new ArrayList<>();

    public RatesTable() {
    }

    public RatesTable(LocalDate tableDate, String tableNumber) {
        this.tableDate = tableDate;
        this.tableNumber = tableNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getTableDate() {
        return tableDate;
    }

    public void setTableDate(LocalDate tableDate) {
        this.tableDate = tableDate;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public List<ExchangeCurrency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<ExchangeCurrency> currencies) {
        this.currencies = currencies;
    }

    @Override
    public String toString() {
        return "ratesTable [tableDate=" + tableDate + ", tableNumber=" + tableNumber + ", currencies=" + currencies
                + "]";
    }

}
