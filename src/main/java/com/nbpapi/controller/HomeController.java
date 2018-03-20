package com.nbpapi.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.nbpapi.Indicators;
import com.nbpapi.entity.ExchangeCurrency;
import com.nbpapi.entity.RatesTable;
import com.nbpapi.repository.ExchangeCurrencyRepository;
import com.nbpapi.repository.RatesTableRepository;

import java.time.temporal.ChronoUnit;

@Controller
public class HomeController {

    static final String nbpApiUrlCommonPartForTableA = "http://api.nbp.pl/api/exchangerates/tables/A/";
    static final int maxNumberOfTablesFromNbpAPI = 67;

    @Autowired
    RatesTableRepository ratesTableRepository;
    @Autowired
    ExchangeCurrencyRepository exchangeCurrencyRepository;

    Locale currentLocale = new Locale("en");
    ResourceBundle messages = ResourceBundle.getBundle("messages", currentLocale);
    ResourceBundle labels = ResourceBundle.getBundle("labels", currentLocale);

    @RequestMapping("/")
    public String hello(Model model, @RequestParam(required = false) String lang) {
        if (null != lang && !lang.isEmpty()) {
            languageCheck(lang);
        }
        String message = "";
        String nbpApiUrl = nbpApiUrlCommonPartForTableA + "last/2?format=json";
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        boolean dataInDatabase = checkIfTablesInLocalDatabaseByDates(yesterday, 1);
        if (!dataInDatabase) {
            List<RatesTable> lastTwoTables = getTables(nbpApiUrl);
            if (null != lastTwoTables) {
                message = saveToDatabase(lastTwoTables);
                model.addAttribute("tableOld", lastTwoTables.get(0));
                model.addAttribute("tableNew", lastTwoTables.get(1));
            } else {
                message = messages.getString("noInternetOrNbpAPIDown");
            }
        } else {
            model.addAttribute("tableOld", ratesTableRepository.findByTableDate(yesterday));
            model.addAttribute("tableNew", ratesTableRepository.findByTableDate(today));
            message = messages.getString("locallyAcquired");
        }
        model.addAttribute("message", message);
        model.addAttribute("labels", labels);
        return "index";
    }

    @RequestMapping("/search-date")
    public String searchDate(@RequestParam(required = false) String tableDate, Model model) {
        String message = "";
        if (null != tableDate && !tableDate.isEmpty()) {
            LocalDate dateToCheckInDatabase = LocalDate.parse(tableDate);
            boolean dataInDatabase = checkIfTablesInLocalDatabaseByDates(dateToCheckInDatabase, 0);
            if (!dataInDatabase) {
                String nbpApiUrl = nbpApiUrlCommonPartForTableA + tableDate + "?format=json";
                List<RatesTable> tableFromQuery = getTables(nbpApiUrl);
                if (null != tableFromQuery) {
                    message = saveToDatabase(tableFromQuery);
                    model.addAttribute("tableQueryResult", tableFromQuery.get(0));
                } else {
                    message = messages.getString("noPublicationOnThisDay");
                }
            } else {
                RatesTable tableFromDatabase = ratesTableRepository.findByTableDate(dateToCheckInDatabase);
                message = messages.getString("locallyAcquired");
                model.addAttribute("tableQueryResult", tableFromDatabase);
            }
        } else {
            message = messages.getString("enterDate");
        }
        model.addAttribute("message", message);
        model.addAttribute("labels", labels);
        return "resultQuery";
    }

    @RequestMapping("/date-range")
    public String hello(@RequestParam(required = false) String tableDateFrom,
            @RequestParam(required = false) String tableDateTo, Model model) {
        String message = checkDates(tableDateFrom, tableDateTo);
        if (message.isEmpty()) {
            LocalDate minDate = LocalDate.parse(tableDateFrom);
            LocalDate maxDate = LocalDate.parse(tableDateTo);
            long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);
            boolean dataInDatabase = checkIfTablesInLocalDatabaseByDates(minDate, daysBetween);
            if (!dataInDatabase) {
                String nbpApiUrl = nbpApiUrlCommonPartForTableA + tableDateFrom + "/" + tableDateTo + "?format=json";
                List<RatesTable> listTables = getTables(nbpApiUrl);
                if (null != listTables) {
                    message = saveToDatabase(listTables);
                    Indicators indicators = getIndicators(listTables, minDate, maxDate);
                    addIndicatorsToModel(model, indicators);
                } else {
                    message = messages.getString("invalidDateRangeOrNoPublications");
                }
            } else {
                List<RatesTable> listTables = ratesTableRepository
                        .findByTableDateGreaterThanEqualAndTableDateLessThanEqual(minDate, maxDate);
                Indicators indicators = getIndicators(listTables, minDate, maxDate);
                addIndicatorsToModel(model, indicators);
                message = messages.getString("locallyAcquired");
            }
        }
        model.addAttribute("message", message);
        model.addAttribute("labels", labels);
        return "resultRange";
    }

    public String checkDates(String tableDateFrom, String tableDateTo) {
        String message = "";
        if (null == tableDateFrom || tableDateFrom.isEmpty() || null == tableDateTo || tableDateTo.isEmpty()) {
            message = "Podaj Daty";
        } else {
            LocalDate minDate = LocalDate.parse(tableDateFrom);
            LocalDate maxDate = LocalDate.parse(tableDateTo);
            long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);
            if (daysBetween > maxNumberOfTablesFromNbpAPI) {
                message = messages.getString("dateRangeExceedsMaximum") + daysBetween + " / "
                        + maxNumberOfTablesFromNbpAPI;
            }
            if (daysBetween < 0) {
                message = messages.getString("secondDateBeforeFirst");
            }
        }
        return message;
    }

    public boolean checkIfTablesInLocalDatabaseByDates(LocalDate minDate, long daysBetween) {
        for (int i = 0; i <= daysBetween; i++) {
            if (null == ratesTableRepository.findByTableDate(minDate.plusDays(i))) {
                return false;
            }
        }
        return true;
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

    public String saveToDatabase(List<RatesTable> listTables) {
        long databaseCheck = 0;
        long tableCount = listTables.size();
        String message = messages.getString("dataAcuiredFromNbpAPI");
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
                    message = messages.getString("partialDataInDatabase");
                    databaseCheck++;
                    if (databaseCheck == tableCount) {
                        message = messages.getString("completeDataInDatabase");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
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

    public void addIndicatorsToModel(Model model, Indicators indicators) {
        model.addAttribute("tableNew", ratesTableRepository.findByTableDate(indicators.getMaxDate()));
        model.addAttribute("tableOld", ratesTableRepository.findByTableDate(indicators.getMinDate()));
        model.addAttribute("minRate", indicators.getMapMin());
        model.addAttribute("maxRate", indicators.getMapMax());
    }

    public void languageCheck(String lang) {
        if (lang.equals("en")) {
            currentLocale = new Locale("en");
        }
        if (lang.equals("pl")) {
            currentLocale = new Locale("pl");
        }
        refreshLanguageSpecificResources();
    }

    public void refreshLanguageSpecificResources() {
        messages = ResourceBundle.getBundle("messages", currentLocale);
        labels = ResourceBundle.getBundle("labels", currentLocale);
    }
}