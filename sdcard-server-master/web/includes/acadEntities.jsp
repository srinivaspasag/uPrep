<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@page import="org.json.JSONArray"%>
<%@page import="com.vedantu.ext.cmds.web.VedantuHttpResponse"%>
<%@page import="org.apache.http.HttpStatus"%>
<%@page import="org.json.JSONObject"%>
<%
    VedantuHttpResponse webRes = (VedantuHttpResponse) request.getAttribute("webRes");
    String entityType = (String) request.getAttribute("targetEntityType");
    if (webRes.responseCode != HttpStatus.SC_OK || StringUtils.isNotEmpty(webRes.errorCode)
            || webRes.getResult() == null) {
%>
<div class="userMessage">Some error occured.Refresh the page and try again.</div>
<%
    } else {
        JSONObject result = webRes.getResult();
        String entityTypeLowerCase = "";
        String astClass = "";
        String notFoundMessage = "";
        if (entityType == "PROGRAM") {
            astClass = "getCentersOfProgram";
            notFoundMessage = "No Programs found";
        } else if (entityType == "CENTER") {
            astClass = "getSectionsOfCenter";
            notFoundMessage = "No Centers found";
        } else if (entityType == "SECTION") {
            astClass = "selectSectionForProgChange";
            notFoundMessage = "No Sections found";
        }
        JSONArray entities = result.getJSONArray("list");
%>

<div class="ASTResultsHolder">
	<div class="ASTResultsDiv">
		<%
		    for (int k = 0; k < entities.length(); k++) {
		            JSONObject entity = entities.getJSONObject(k);
		            String title = entity.getString("name");
		            String entityId = entity.getString("id");
		            String code = entity.getString("code");
		            String recordState = entity.getString("recordState");
		            String astClasses = "";
		            if ("ACTIVE".equals(recordState)) {
		                astClasses = "activeASTItem";
		            } else if ("DELETED".equals(recordState)) {
		                astClasses = "inactiveASTItem";
		            }
		            boolean paidSection = false;
		            if (entity.has("revenueModel") && entity.getString("revenueModel") != null
		                    && entity.getString("revenueModel") == "PAID") {
		                paidSection = true;
		            }
		            long sectionSize=0;
		            if("SECTION".equals(entityType)){
		                sectionSize=entity.getLong("size");
		            }
		%>
		<div class="ASTItem ASTItem_<%=entityId%> <%=astClass%> <%=astClasses%>" data-<%=entityType.toLowerCase()%>-id="<%=entityId%>" data-<%=entityType.toLowerCase()%>-name="<%=title%>" data-entity-type="<%=entityType%>" title="<%=title%>" data-<%=entityType.toLowerCase()%>-code="<%=code%>" data-entity-id="<%=entityId%>" data-paid=<%=paidSection%> data-size=<%=sectionSize %>>
			<div class="ASTItemName gCBoxText"><%=title%></div>
			<div class="ASTItemState">(deleted)</div>
			<div class="ASTItemImg"></div>
		</div>
		<%
		    }
		%>
		<%
		    if (entities.length() == 0) {
		%>
		<div class="acadEntityDefaultText"><%=notFoundMessage%></div>
		<%
		    }
		%>
	</div>
</div>

<%
    }
%>
