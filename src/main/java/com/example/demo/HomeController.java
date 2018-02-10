package com.example.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import java.time.temporal.ChronoUnit;

@Controller
public class HomeController {

    @Autowired
    RatesTableRepository ratesTableRepository;
    @Autowired
    ExchangeCurrencyRepository exchangeCurrencyRepository;

    @RequestMapping("/")
    public String hello(Model model) {
        String message = "";
        String nbpApiUrl = "http://api.nbp.pl/api/exchangerates/tables/A/last/2?format=json";
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        try {
            if (null == ratesTableRepository.findByTableDate(today)
                    || null == ratesTableRepository.findByTableDate(yesterday)) {
                List<RatesTable> twoTables = getTables(nbpApiUrl);
                if (null != twoTables) {
                    message = "Dane pobrane.";
                    for (RatesTable table : twoTables) {
                        if (null == ratesTableRepository.findByTableNumber(table.getTableNumber())) {
                            List<ExchangeCurrency> tableRates = table.getCurrencies();
                            ratesTableRepository.save(table);
                            RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(table.getTableDate());
                            for (ExchangeCurrency rate : tableRates) {
                                rate.setTable(tableFromDatabase);
                                exchangeCurrencyRepository.save(rate);
                            }
                        } else {
                            message = "Część danych już w bazie ";
                            if (message.equals("Część danych już w bazie ")) {
                                message = "Dane już w bazie";
                            }
                        }
                    }
                    model.addAttribute("tableOld", twoTables.get(0));
                    model.addAttribute("tableNew", twoTables.get(1));
                } else {
                    message = "Błąd, smuteczek";
                }
            } else {
                message = "Dwa ostatnie notowania są już w bazie";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("message", message);
        return "index";
    }

    @RequestMapping("/search-date")
    public String searchDate(@RequestParam String tableDate, Model model) {
        String message = "";
        if ("" != tableDate) {
            String nbpApiUrl = "http://api.nbp.pl/api/exchangerates/tables/A/" + tableDate + "?format=json";
            LocalDate dateToCheckInDatabase = LocalDate.parse(tableDate);
            try {
                if (null == ratesTableRepository.findByTableDate(dateToCheckInDatabase)) {
                    List<RatesTable> tableFromQuery = getTables(nbpApiUrl);
                    RatesTable singleResultTable = tableFromQuery.get(0);
                    List<ExchangeCurrency> tableRates = singleResultTable.getCurrencies();
                    ratesTableRepository.save(singleResultTable);
                    RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(dateToCheckInDatabase);
                    for (ExchangeCurrency rate : tableRates) {
                        rate.setTable(tableFromDatabase);
                        exchangeCurrencyRepository.save(rate);
                    }
                    model.addAttribute("tableQueryResult", tableFromDatabase);
                    message = "Dane pobrane.";
                } else {
                    message = "Dane są już w bazie.";
                    RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(dateToCheckInDatabase);
                    model.addAttribute("tableQueryResult", tableFromDatabase);
                }
            } catch (Exception e) {
                message = "Tego dnia nie było publikacji tabeli kursowej.";
            }
        } else {
            message = "Podaj Datę";
        }
        model.addAttribute("message", message);
        return "resultQuery";
    }

    @RequestMapping("/date-range")
    public String hello(@RequestParam String tableDateFrom, @RequestParam String tableDateTo, Model model) {
        String message = "";
        if ("" != tableDateFrom && "" != tableDateTo) {
            LocalDate minDate = LocalDate.parse(tableDateFrom);
            LocalDate maxDate = LocalDate.parse(tableDateTo);
            long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);
            long databaseCheck = 0;
            if (daysBetween <= 67) {
                if (daysBetween > 0) {
                    String nbpApiUrl = "http://api.nbp.pl/api/exchangerates/tables/A/" + tableDateFrom + "/"
                            + tableDateTo + "?format=json" + "?format=json";
                    try {
                        List<RatesTable> listTables = getTables(nbpApiUrl);
                        if (null != listTables) {
                            Map<String, Double> mapMin = new HashMap<>();
                            Map<String, Double> mapMax = new HashMap<>();
                            for (RatesTable table : listTables) {
                                if (table.getTableDate().isBefore(maxDate)) {
                                    maxDate = table.getTableDate();
                                }
                                if (table.getTableDate().isAfter(minDate)) {
                                    minDate = table.getTableDate();
                                }
                                List<ExchangeCurrency> tableRates = table.getCurrencies();
                                for (ExchangeCurrency rateToCheck : tableRates) {
                                    if (!mapMin.containsKey(rateToCheck.getCurrencyCode())) {
                                        mapMin.put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                                        mapMax.put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                                    } else {
                                        if (mapMin.get(rateToCheck.getCurrencyCode()) > rateToCheck.getExchangeRate()) {
                                            mapMin.put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                                        }
                                        if (mapMax.get(rateToCheck.getCurrencyCode()) < rateToCheck.getExchangeRate()) {
                                            mapMax.put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                                        }
                                    }
                                }
                                if (null == ratesTableRepository.findByTableNumber(table.getTableNumber())) {
                                    ratesTableRepository.save(table);
                                    RatesTable tableFromDatabase = ratesTableRepository
                                            .findByTableDate(table.getTableDate());
                                    for (ExchangeCurrency rate : tableRates) {
                                        rate.setTable(tableFromDatabase);
                                        exchangeCurrencyRepository.save(rate);
                                    }
                                    message = "Dane pobrane.";
                                } else {
                                    message = "Częściowe dane już w bazie ";
                                    databaseCheck++;
                                    if (databaseCheck == (daysBetween + 1)) {
                                        message = "Pełne dane były już w bazie ";
                                    }
                                }
                            }
                            model.addAttribute("tableOld", ratesTableRepository.findByTableDate(maxDate));
                            model.addAttribute("tableNew", ratesTableRepository.findByTableDate(minDate));
                            model.addAttribute("minRate", mapMin);
                            model.addAttribute("maxRate", mapMax);

                        } else {
                            message = "W wybranym okresie nie było publikacji tabel kursów";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    message = "Druga data musi być większa niż startowa";
                }
            } else {
                message = "Zakres dat (" + daysBetween + "dni) przekracza dopuszczalne maksimum 67 dni";
            }
        } else {
            message = "Podaj Daty";
        }
        model.addAttribute("message", message);
        return "resultRange";
    }

    public List<RatesTable> getTables(String nbpApiUrl) {
        List<RatesTable> tablesToReturn = new ArrayList<RatesTable>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(nbpApiUrl, String.class);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(response);

            JSONArray jsonObject = (JSONArray) obj;
            for (Object tableArray : jsonObject.toArray()) {
                JSONObject jsonSubObject = (JSONObject) tableArray;

                String no = (String) jsonSubObject.get("no");
                String effectiveDate = (String) jsonSubObject.get("effectiveDate");
                RatesTable ratesTable = new RatesTable(LocalDate.parse(effectiveDate), no);

                JSONArray rates = (JSONArray) jsonSubObject.get("rates");
                for (Object rate : rates.toArray()) {
                    JSONObject jsonSecondSubObject = (JSONObject) rate;
                    String currency = (String) jsonSecondSubObject.get("currency");
                    String code = (String) jsonSecondSubObject.get("code");
                    Double mid = (Double) jsonSecondSubObject.get("mid");
                    ExchangeCurrency exchangeCurrency = new ExchangeCurrency(currency, code, mid);
                    List<ExchangeCurrency> exchangeCurrencies = ratesTable.getCurrencies();
                    exchangeCurrencies.add(exchangeCurrency);
                    ratesTable.setCurrencies(exchangeCurrencies);
                }
                tablesToReturn.add(ratesTable);

            }
            return tablesToReturn;
        } catch (Exception e) {
            return null;
        }
    }
}
