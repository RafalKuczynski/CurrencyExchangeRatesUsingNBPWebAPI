<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
<title>Spring Boot</title>
</head>
<body>
	<a href="/">Strona Startowa</a>
	<br>
	<br>
	<b>${message}</b>
	<br>
	<br>
	<c:if test="${not empty tableNew}">
	Tabela z dnia: ${tableNew.tableDate} Numer: ${tableNew.tableNumber}
		<table class="table table-hover table-sm table-bordered">
			<thead class="thead-dark">
				<tr>
					<th scope="col">Nazwa waluty</th>
					<th scope="col">Kod waluty</th>
					<th scope="col">Kurs ${tableNew.tableDate}</th>
					<th scope="col">Kurs ${tableOld.tableDate}</th>
					<th scope="col"></th>
					<th scope="col">Kurs minimalny</th>
					<th scope="col">Kurs maksymalny</th>
				</tr>
			</thead>
			<c:forEach items="${tableNew.currencies}" var="currency">
				<c:forEach items="${tableOld.currencies}" var="currencyOld">
					<c:if test="${currency.currencyCode eq currencyOld.currencyCode}">
						<tr>
							<td>${currency.currencyName}</td>
							<td>${currency.currencyCode}</td>
							<td><b>${currency.exchangeRate}</b></td>
							<td><b>${currencyOld.exchangeRate}</b></td>
							<c:if test="${currency.exchangeRate > currencyOld.exchangeRate}">
								<td><font color="green">WZROST</font></td>
							</c:if>
							<c:if test="${currency.exchangeRate < currencyOld.exchangeRate}">
								<td><font color="red">SPADEK</font></td>
							</c:if>
							<c:if test="${currency.exchangeRate eq currencyOld.exchangeRate}">
								<td>BEZ ZMIAN</td>
							</c:if>
							<td>${minRate[currency.currencyCode]}</td>
							<td>${maxRate[currency.currencyCode]}</td>
						</tr>
					</c:if>
				</c:forEach>
			</c:forEach>
		</table>
	</c:if>
</body>
</html>