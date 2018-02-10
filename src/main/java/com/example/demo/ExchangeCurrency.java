package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ExchangeCurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String currencyName;
    private String currencyCode;
    private double exchangeRate;
    @ManyToOne
    private RatesTable table;

    public ExchangeCurrency() {
    }

    public ExchangeCurrency(String currencyName, String currencyCode, double exchangeRate) {
        super();
        this.currencyName = currencyName;
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public RatesTable getTable() {
        return table;
    }

    public void setTable(RatesTable table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "exchangeCurrency " + id + " [currencyCode=" + currencyCode + ", exchangeRate=" + exchangeRate + "]";
    }

}
