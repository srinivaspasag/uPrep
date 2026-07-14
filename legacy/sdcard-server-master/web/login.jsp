<%@page import="org.apache.log4j.Logger"%>
<%@page import="com.vedantu.ext.cmds.db.datamanagers.OrgDataManager"%>
<%@page import="com.vedantu.ext.cmds.db.models.Organization"%>
<%@page import="com.vedantu.ext.cmds.managers.DataManagerUtils"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Login" />
</jsp:include>
<body>
	<%
	    Organization orgVar = OrgDataManager.INSTANCE.getOrganization();
	    if (orgVar == null) {
	        response.sendRedirect("");
	        return;
	    }

	    if (session.getAttribute("userId") == null) {
	%>

	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<jsp:include page="includes/welcome.jsp" />
		<form id="firstPagesForm" class="centerText" action="logins" method="post">
			<div class="firstPagesFormHead">Enter Admin Credentials</div>
			<input type="text" class="firstPagesFormInput" placeholder="Login ID" required name="username" /> <input type="password" class="firstPagesFormInput" placeholder="Password" required name="password" /> <input type="hidden" name="next" value="<%=request.getParameter("next") == null ? "" : request.getParameter("next")%>">
			<div class="errorMessageDiv margTop10"></div>
			<input type="submit" class="firstPagesFormSubmit greenButton" value="SUBMIT" />

		</form>
	</div>


	<%
	    } else {
	        response.sendRedirect("landing.jsp");
	    }
	%>
</body>
</html>