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
	<b>${message}</b>
	<br>
	<br>
	<table>

		<tr>
			Tabela z dnia: ${tableNew.tableDate} Numer: ${tableNew.tableNumber}
			<td>Nazwa waluty</td>
			<td>Kod waluty</td>
			<td>Kurs ${tableNew.tableDate}</td>
			<td>Kurs ${tableOld.tableDate}</td>
			<c:forEach items="${tableNew.currencies}" var="currency">
				<c:forEach items="${tableOld.currencies}" var="currencyOld">
					<c:if test="${currency.currencyCode eq currencyOld.currencyCode}">
						<tr>
							<td>${currency.currencyName}</td>
							<td>${currency.currencyCode}</td>
							<td><b>${currency.exchangeRate}</b></td>
							<td>${currencyOld.exchangeRate}</td>
							<c:if test="${currency.exchangeRate > currencyOld.exchangeRate}">
								<td><font color="green">WZROST</font></td>
							</c:if>
							<c:if test="${currency.exchangeRate < currencyOld.exchangeRate}">
								<td><font color="red">SPADEK</font></td>
							</c:if>
							<c:if test="${currency.exchangeRate eq currencyOld.exchangeRate}">
								<td>BEZ ZMIAN</td>
							</c:if>
						</tr>
					</c:if>
				</c:forEach>
			</c:forEach>
		</tr>

	</table>
	<br>
	<form action="/search-date" method="post">
		Tabela z dnia: <input type="date" name="tableDate" value=""/> <input
			type="submit" />
	</form>
	<br>
	<form action="/date-range" method="post">
		Zakres tabel:<input type="date" name="tableDateFrom" value=""/>
		<input type="date" name="tableDateTo" value=""/> <input
			type="submit" />
	</form>
</body>
</html>