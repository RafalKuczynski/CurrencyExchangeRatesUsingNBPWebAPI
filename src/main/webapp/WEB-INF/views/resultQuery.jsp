<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Spring Boot</title>
</head>
<body>
	<a href="/">Strona Startowa</a>
	<br>
	<br>
	<b>${message}</b>
	<br>
	<br>
	<table>
		<c:if test="${not empty tableQueryResult}">
			<tr>
				Tabela z dnia: ${tableQueryResult.tableDate} Numer:
				${tableQueryResult.tableNumber}
				<td>Nazwa waluty</td>
				<td>Kod waluty</td>
				<td>Kurs ${tableQueryResult.tableDate}</td>
				<c:forEach items="${tableQueryResult.currencies}" var="currency">
					<tr>
						<td>${currency.currencyName}</td>
						<td>${currency.currencyCode}</td>
						<td><b>${currency.exchangeRate}</b></td>
					</tr>
				</c:forEach>
			</tr>
		</c:if>
	</table>
	<br>
</body>
</html>