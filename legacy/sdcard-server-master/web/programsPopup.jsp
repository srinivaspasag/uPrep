<%@page import="com.vedantu.ext.cmds.web.VedantuHttpResponse"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@page import="org.apache.http.HttpStatus"%>
<%@page import="org.json.JSONObject"%>
<%
    VedantuHttpResponse webRes = (VedantuHttpResponse) request
					.getAttribute("webRes");
			JSONObject r = webRes.getResult();
			if (webRes.responseCode != HttpStatus.SC_OK||StringUtils.isNotEmpty(webRes.errorCode)||webRes.getResult() == null) {
%>
<div class="userMessage">Some error occurred. Refresh the page and try again.</div>
<%
    } else {
%>
<div class="cmdsPopupHead">
	Select Program
	<div class="color9 smally margBot20">Select a program to view its details</div>
</div>
<table id="acadStrTable">
	<tbody>
		<tr>
			<td class="ASTSearchTd ASTProgramsTdHead">
				<div class="inputDiv ASTSearchHolder hasSearchImg smallInputDiv">
					<input
						type="text"
						placeholder="Runs Programs"
						data-entity-type='PROGRAM'
						class="ASTSearchPrograms ASTSearchInput" /> <span class="inputSearchImg"></span>
				</div>
			</td>
			<td class="ASTSearchTd editAcadStrTd ASTCentersTdHead">
				<div class="inputDiv ASTSearchHolder hasSearchImg smallInputDiv">
					<input
						type="text"
						placeholder="in Centers"
						data-entity-type='CENTER'
						class="ASTSearchCenters ASTSearchInput" /> <span class="inputSearchImg"></span>
				</div>
			</td>
			<td class="ASTSearchTd editAcadStrTd ASTSectionsTdHead">
				<div class="inputDiv ASTSearchHolder hasSearchImg smallInputDiv">
					<input
						type="text"
						placeholder="Has Sections"
						data-entity-type='SECTION'
						class="ASTSearchSections ASTSearchInput" /> <span class="inputSearchImg"></span>
				</div>
			</td>
		</tr>
		<tr>
			<td
				class="ASTResultsTd ASTProgramsTd"
				data-entity-type="PROGRAM">
					<jsp:include page="includes/acadEntities.jsp" />
				</td>
			<td
				class="ASTResultsTd ASTCentersTd editAcadStrTd"
				data-entity-type="CENTER">
				<div class="acadEntityDefaultText">Select a program to view the centers it is run.</div>
			</td>
			<td
				class="ASTResultsTd ASTSectionsTd editAcadStrTd"
				data-entity-type="SECTION">
				<div class="acadEntityDefaultText">Select a program and center to view the sections.</div>
			</td>
		</tr>

	</tbody>
</table>
<div class="rightText margTop">
	<div class="blueButton submitSelectedSection">Select</div>
</div>
<script type="text/javascript">
	qrAcadStr.init({
		targetTable : "CHANGE_PROGRAM"
	});
</script>

<%
    }
%>


