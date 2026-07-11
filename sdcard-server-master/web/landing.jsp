<%@page import="com.vedantu.ext.cmds.utils.config.Config"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@page import="com.vedantu.ext.cmds.db.models.Organization"%>
<%@page import="com.vedantu.ext.cmds.managers.DataManagerUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Desktop Client" />
</jsp:include>
<body>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<jsp:include page="includes/welcome.jsp" />
		<%
		    //if the user has not provided security credentials, we will ask her to provide it
					Organization orgVar = DataManagerUtils.getOrganization();
					if (StringUtils.isEmpty(orgVar.authToken)
							|| StringUtils.isEmpty(orgVar.secretKey)) {
		%>

		<form id="firstPagesForm" class="centerText" action="validateAppCredentials" method="post">
			<div class="firstPagesFormHead">Authenticate</div>
			<input type="text" class="firstPagesFormInput" placeholder="Secret Key" required name="secretKey" autocomplete="off" /> <input type="text" class="firstPagesFormInput" autocomplete="off" placeholder="Authentication ID" required name="authToken" /> <input type="hidden" name="orgId" value="<%=orgVar.id%>"> <input type="hidden" name="appId" value="<%=Config.APP_ID%>">
			<div class="errorMessageDiv margTop10"></div>

			<input type="submit" class="firstPagesFormSubmit greenButton" value="SUBMIT" />
		</form>
		<%
		    } else {
		%>
		<div class="landingPageHead">What do you want to do?</div>
		<div class="landingPageOpts">
			<div id="goToUploadPage" class="landingPageOpt">
				<div class="landingPageImgHolder">
					<div class="landingPageUploadImg commonSprite"></div>
				</div>
				<div class="landingPageOptText">UPLOAD</div>
				<div class="landingPageOptCaption">Upload content to Library</div>
			</div>
			<a id="goToImportsPage" class="landingPageOpt" href="imports.jsp">
				<div class="landingPageImgHolder">
					<div class="landingPageUploadImg commonSprite"></div>
				</div>
				<div class="landingPageOptText">VIEW/IMPORT LIBRARIES</div>
				<div class="landingPageOptCaption">
					Download program libraries<br> and flash SD cards
				</div> </a>
		</div>
		<%
		    }
		%>
	</div>
	<script type="text/javascript">
		var uploadBtn = document.getElementById('goToUploadPage');
		uploadBtn.addEventListener("click", function() {
			alert("This feature is currently under development.")
		});
	</script>
</body>
</html>