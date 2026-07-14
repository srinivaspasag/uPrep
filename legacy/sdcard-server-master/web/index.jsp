<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@page import="com.vedantu.ext.cmds.utils.config.Config"%>
<%@page import="com.vedantu.ext.cmds.db.models.Organization"%>
<%@page import="com.vedantu.ext.cmds.db.datamanagers.OrgDataManager"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.ConstantGlobal"%>
<!DOCTYPE>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Configure The APP" />
</jsp:include>
<body>
	<%
	    String validateEntry = "";
	    String errorMessage = "";
	    String formAction = "";

	    Organization orgVar = OrgDataManager.INSTANCE.getOrganization();
	    if (orgVar == null) {
	        validateEntry = "NO_ORG";
	        formAction = "addOrg";
	    } else if (session == null || session.getAttribute(ConstantGlobal.USER_ID) == null) {
	        validateEntry = "NOT_LOGGED_IN";
	        formAction = "logins";
	    } else if (StringUtils.isEmpty(orgVar.authToken)
	            || StringUtils.isEmpty(orgVar.secretKey)) {
	        validateEntry = "NO_SECRET_KEYS_FOUND";
	        formAction = "validateAppCredentials";
	    } else {
	        response.sendRedirect("landing.jsp");
	    }

	    Cookie[] cookies = request.getCookies();
	    if (cookies != null) {
	        for (int i = 0; i < cookies.length; i++) {
	            Cookie cookie = cookies[i];
	            if (cookie.getName().equals("validateEntry")) {
	                validateEntry = cookie.getValue();
	                cookie.setMaxAge(0);
	                response.addCookie(cookie);
	            } else if (cookie.getName().equals("errorMessage")) {
	                errorMessage = cookie.getValue();
	                cookie.setMaxAge(0);
	                response.addCookie(cookie);
	            }
	        }
	    }
	%>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<jsp:include page="includes/welcome.jsp" />
		<form id="firstPagesForm" class="centerText" action="<%=formAction%>" method="post">
			<%
			    if (validateEntry == "NO_ORG") {
			%>
			<div class="firstPagesFormHead">Enter organization SLUG</div>
			<input type="text" class="firstPagesFormInput" placeholder="http://cmds.vedantu.com/org/vedantu" required name="url" />
			<%
			    } else if (validateEntry == "NOT_LOGGED_IN") {
			%>
			<div class="firstPagesFormHead">Enter Admin Credentials</div>
			<input type="text" class="firstPagesFormInput" placeholder="Login ID" required name="username" /> <input type="password" class="firstPagesFormInput" placeholder="Password" required name="password" /> <input type="hidden" name="next" value="<%=request.getParameter("next") == null ? "" : request.getParameter("next")%>">
			<%
			    } else if (validateEntry == "NO_SECRET_KEYS_FOUND") {
			%>
			<div class="firstPagesFormHead">Authenticate</div>
			<input type="text" class="firstPagesFormInput" placeholder="Secret Key" required name="secretKey" autocomplete="off" /> <input type="text" class="firstPagesFormInput" autocomplete="off" placeholder="Authentication ID" required name="authToken" /> <input type="hidden" name="orgId" value="<%=orgVar.id%>"> <input type="hidden" name="appId" value="<%=Config.APP_ID%>">

			<%
			    }
			    if (StringUtils.isNotEmpty(errorMessage)) {
			%>

			<div class="errorMessageDiv margTop10"><%=errorMessage%></div>
			<%
			    }
			%>
			<input type="submit" class="firstPagesFormSubmit greenButton" value="SUBMIT" />
		</form>
	</div>

	<script type="text/javascript">
	$(function(){
		 var settingsHolder=$("#settingsHolder");
			<%
		    if (validateEntry == "NO_ORG") {
		%>
		settingsHolder.remove();
		<%
		    } else if (validateEntry == "NOT_LOGGED_IN") {
		%>
		settingsHolder.find(".settingsOpt").eq(1).remove();
		<%
		    }
		%>			
	});	 
	</script>

</body>
</html>