<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Spring MVC 表单处理</title>
</head>
<body>

<h2>Student Infomation</h2>
<form:form action="/FormHandling/addStudent" method="POST">
	<table>
		<tr>
			<td><form:label path="name">名字： </form:label></td>
			<td><form:input path="name"/></td>
		</tr>
		<tr>
			<td><form:label path="age">年龄： </form:label></td>
			<td><form:input path="age"/></td>
		</tr>
		<tr>
			<td><form:label path="id">编号： </form:label></td>
			<td><form:input path="id"/></td>
		</tr>
		<tr>
			<td colspan="2">
				<input type="submit" value="提交">
			</td>
		</tr>
	</table>
</form:form>
</body>
</html>