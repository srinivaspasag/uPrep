<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true"%>
<!DOCTYPE html>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Error Occurred" />
</jsp:include>
<body>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<a href="/vedantu-ext-cmds-uploader" style="margin-top: 16px; display: inline-block; font-size: 16px; font-weight: bold; text-decoration: underline;">Back to Home</a>
		<div style="margin-top: 100px; text-align: center;">
			<div class="" style="color: rgb(102, 102, 102); font-size: 45px; line-height: 45px;">OOPS!!</div>
			<span class="" style="line-height: 20px; color: rgb(153, 153, 153); font-size: 20px;">Something went wrong..</span>
		</div>
		<%
		    if (exception.getMessage() != null) {
		%>
		<div style="text-align: center; margin-top: 3px; font-weight: bold; font-size: 15px;">
			<span style="font-weight: normal;">Error: </span><%=exception.getMessage()%>
		</div>
		<%
		    }
		%>
	</div>
</body>
</html>