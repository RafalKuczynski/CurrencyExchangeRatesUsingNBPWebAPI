package com.example.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    static final String nbpApiUrlCommonPartForTableA = "http://api.nbp.pl/api/exchangerates/tables/A/";
    static final int maxNumberOfTablesFromNbpAPI = 67;

    @Autowired
    RatesTableRepository ratesTableRepository;
    @Autowired
    ExchangeCurrencyRepository exchangeCurrencyRepository;

    @RequestMapping("/")
    public String hello(Model model) {
        String message = "";
        String nbpApiUrl = nbpApiUrlCommonPartForTableA + "last/2?format=json";
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        try {
            if (null == ratesTableRepository.findByTableDate(today)
                    || null == ratesTableRepository.findByTableDate(yesterday)) {
                List<RatesTable> twoTables = getTables(nbpApiUrl);
                if (null != twoTables) {
                    message = saveToDatabase(twoTables);
                    model.addAttribute("tableOld", twoTables.get(0));
                    model.addAttribute("tableNew", twoTables.get(1));
                } else {
                    message = "Błąd, smuteczek (brak internetu bądź API NPB nie działa?)";
                }
            } else {
                model.addAttribute("tableOld", ratesTableRepository.findByTableDate(yesterday));
                model.addAttribute("tableNew", ratesTableRepository.findByTableDate(today));
                message = "Dane pobrane z bazy lokalnej.";
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
            LocalDate dateToCheckInDatabase = LocalDate.parse(tableDate);
            try {
                if (null == ratesTableRepository.findByTableDate(dateToCheckInDatabase)) {
                    String nbpApiUrl = nbpApiUrlCommonPartForTableA + tableDate + "?format=json";
                    List<RatesTable> tableFromQuery = getTables(nbpApiUrl);
                    if (null != tableFromQuery) {
                        message = saveToDatabase(tableFromQuery);
                        model.addAttribute("tableQueryResult", tableFromQuery.get(0));
                    } else {
                        message = "Tego dnia nie było publikacji tabeli kursowej.";
                    }
                } else {
                    RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(dateToCheckInDatabase);
                    message = "Dane pobrane z bazy lokalnej.";
                    model.addAttribute("tableQueryResult", tableFromDatabase);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            if (daysBetween <= maxNumberOfTablesFromNbpAPI) {
                if (daysBetween > 0) {
                    try {
                        boolean dataInDatabase = true;
                        for (int i = 0; i <= daysBetween; i++) {
                            if (null == ratesTableRepository.findByTableDate(minDate.plusDays(i))) {
                                dataInDatabase = false;
                                break;
                            }
                        }
                        if (!dataInDatabase) {
                            String nbpApiUrl = nbpApiUrlCommonPartForTableA + tableDateFrom + "/" + tableDateTo
                                    + "?format=json";
                            List<RatesTable> listTables = getTables(nbpApiUrl);
                            if (null != listTables) {
                                message = saveToDatabase(listTables);
                                Indicators indicators = getIndicators(listTables, minDate, maxDate);
                                model.addAttribute("tableNew",
                                        ratesTableRepository.findByTableDate(indicators.getMaxDate()));
                                model.addAttribute("tableOld",
                                        ratesTableRepository.findByTableDate(indicators.getMinDate()));
                                model.addAttribute("minRate", indicators.getMapMin());
                                model.addAttribute("maxRate", indicators.getMapMax());
                            } else {
                                message = "Błędny zakres dat bądź w wybranym okresie nie było publikacji tabel kursów";
                            }
                        } else {
                            List<RatesTable> listTables = ratesTableRepository
                                    .findByTableDateGreaterThanEqualAndTableDateLessThanEqual(minDate, maxDate);
                            if (null != listTables) {
                                Indicators indicators = getIndicators(listTables, minDate, maxDate);
                                model.addAttribute("tableNew",
                                        ratesTableRepository.findByTableDate(indicators.getMaxDate()));
                                model.addAttribute("tableOld",
                                        ratesTableRepository.findByTableDate(indicators.getMinDate()));
                                model.addAttribute("minRate", indicators.getMapMin());
                                model.addAttribute("maxRate", indicators.getMapMax());
                            } else {
                                message = "Błąd przy pobieraniu z bazy lokalnej";
                            }
                            message = "Dane pobrane z bazy lokalnej.";
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

    public Indicators getIndicators(List<RatesTable> listTables, LocalDate minDate, LocalDate maxDate) {
        Indicators indicators = new Indicators();
        indicators.setMapMin(new HashMap<>());
        indicators.setMapMax(new HashMap<>());
        indicators.setMaxDate(minDate);
        indicators.setMinDate(maxDate);
        for (RatesTable table : listTables) {
            if (table.getTableDate().isBefore(indicators.getMinDate())) {
                indicators.setMinDate(table.getTableDate());
            }
            if (table.getTableDate().isAfter(indicators.getMaxDate())) {
                indicators.setMaxDate(table.getTableDate());
            }
            List<ExchangeCurrency> tableRates = table.getCurrencies();
            for (ExchangeCurrency rateToCheck : tableRates) {
                if (!indicators.getMapMin().containsKey(rateToCheck.getCurrencyCode())) {
                    indicators.getMapMin().put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                    indicators.getMapMax().put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                } else {
                    if (indicators.getMapMin().get(rateToCheck.getCurrencyCode()) > rateToCheck.getExchangeRate()) {
                        indicators.getMapMin().put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                    }
                    if (indicators.getMapMax().get(rateToCheck.getCurrencyCode()) < rateToCheck.getExchangeRate()) {
                        indicators.getMapMax().put(rateToCheck.getCurrencyCode(), rateToCheck.getExchangeRate());
                    }
                }
            }
        }
        return indicators;
    }

    public String saveToDatabase(List<RatesTable> listTables) {
        long databaseCheck = 0;
        long tableCount = listTables.size();
        String message = "Dane pobrane z API NBP.";
        try {
            for (RatesTable table : listTables) {
                List<ExchangeCurrency> tableRates = table.getCurrencies();
                if (null == ratesTableRepository.findByTableNumber(table.getTableNumber())) {
                    ratesTableRepository.save(table);
                    RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(table.getTableDate());
                    for (ExchangeCurrency rate : tableRates) {
                        rate.setTable(tableFromDatabase);
                        exchangeCurrencyRepository.save(rate);
                    }
                } else {
                    message = "Częściowe dane już w bazie ";
                    databaseCheck++;
                    if (databaseCheck == tableCount) {
                        message = "Pełne dane były już w bazie ";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}