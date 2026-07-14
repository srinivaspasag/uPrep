
<%@page import="com.vedantu.ext.cmds.db.models.Organization"%>
<%@page import="com.vedantu.ext.cmds.db.datamanagers.OrgDataManager"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Configure The APP" />
</jsp:include>
<body>
	<%
	    Organization orgVar = OrgDataManager.INSTANCE.getOrganization();
	    if (orgVar != null) {
	        response.sendRedirect("login.jsp");
	    } else {
	%>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<jsp:include page="includes/welcome.jsp" />
		<form id="firstPagesForm" class="centerText" action="addOrg" method="post">
			<div class="firstPagesFormHead">Enter organization SLUG</div>
			<input type="text" class="firstPagesFormInput" placeholder="" required name="slug" />

			<div class="errorMessageDiv margTop10"></div>

			<input type="submit" class="firstPagesFormSubmit greenButton" value="SUBMIT" />
		</form>
	</div>

	<%
	    }
	%>
</body>
</html>