# CurrencyExchangeRatesUsingNBPWebAPI

## Description

This Spring Boot project gets average exchange rates of foreign currencies in zlotys (Polish złoty - PLN) from NPB's (central bank of the Republic of Poland) Web API.
It stores the values received from NBP's Web API in local database, so that they are available for future queries.
Each query starts by checking the database for availability of required data, and if it is missing - a request to NBP's Web API is sent. 

## Installation notes

Project requires a MySQL database (database name - default *nbp*, username and password must be specified in application.properties).

## Usage

Upon start, application checks for availability of current and previous average exchange rates. If not available in local database, it will send a query to NBP's Web API. The two are compared and results are shown in a table.
There are two additional queries possible using the forms below the table:
* First one returns currency exchange rates for a specific date (first checked if available locally, if not - recovered from NBP's Web API)
* Second gives You the opportunity to see the changes in average exchange rates during a specified period. It provides information on exchange rates on the first and last day and the maximum and minimum exchange rates during specified time period (as in previous examples, the data is first checked for availability locally).
