<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Spring Boot</title>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
</head>
<body>
	<a href="/"> ${labels.homepage}</a>
	<br>
	<br>
	<b>${message}</b>
	<br>
	<br>
	<c:if test="${not empty tableQueryResult}">
	${labels.tableDay}: ${tableQueryResult.tableDate} ${labels.number}:	${tableQueryResult.tableNumber}
		<table class="table table-hover table-sm table-bordered">
			<thead class="thead-dark">
				<tr>
					<th scope="col">${labels.currencyName}</th>
					<th scope="col">${labels.currencyCode}</th>
					<th scope="col">${labels.exchangeRate} ${tableQueryResult.tableDate}</th>
				</tr>
			</thead>
			<c:forEach items="${tableQueryResult.currencies}" var="currency">
				<tr>
					<td>${currency.currencyName}</td>
					<td>${currency.currencyCode}</td>
					<td><b>${currency.exchangeRate}</b></td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
	<br>
</body>
</html>