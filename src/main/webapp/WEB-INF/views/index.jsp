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
<title>NBP API</title>
</head>
<body>
	<b>${message}</b>
	<br><a href="/?lang=en">English language</a> 
	<br><a href="/?lang=pl">Jezyk polski</a> 
	<br> ${labels.tableDay}: ${tableNew.tableDate} ${labels.number}:
	${tableNew.tableNumber}
	<table class="table table-hover table-sm table-bordered">
		<thead class="thead-dark">
			<tr>
				<th scope="col">${labels.currencyName}</th>
				<th scope="col">${labels.currencyCode}</th>
				<th scope="col">${labels.exchangeRate} ${tableNew.tableDate}</th>
				<th scope="col">${labels.exchangeRate} ${tableOld.tableDate}</th>
				<th scope="col"></th>
			</tr>
		</thead>
		<c:forEach items="${tableNew.currencies}" var="currency">
			<c:forEach items="${tableOld.currencies}" var="currencyOld">
				<c:if test="${currency.currencyCode eq currencyOld.currencyCode}">
					<tr>
						<td>${currency.currencyName}</td>
						<td>${currency.currencyCode}</td>
						<td><b>${currency.exchangeRate}</b></td>
						<td>${currencyOld.exchangeRate}</td>
						<c:if test="${currency.exchangeRate > currencyOld.exchangeRate}">
							<td><font color="green">${labels.increase}</font></td>
						</c:if>
						<c:if test="${currency.exchangeRate < currencyOld.exchangeRate}">
							<td><font color="red">${labels.decrease}</font></td>
						</c:if>
						<c:if test="${currency.exchangeRate eq currencyOld.exchangeRate}">
							<td>${labels.noChange}</td>
						</c:if>
					</tr>
				</c:if>
			</c:forEach>
		</c:forEach>
	</table>
	<br>
	<form action="/search-date" method="post">
		${labels.tableDay}: <input type="date" name="tableDate" value="" /> <input
			type="submit" />
	</form>
	<br>
	<form action="/date-range" method="post">
		${labels.tableRange}:<input type="date" name="tableDateFrom" value="" />
		<input type="date" name="tableDateTo" value="" /> <input
			type="submit" />
	</form>
</body>
</html>