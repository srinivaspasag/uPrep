<%@page import="com.vedantu.ext.cmds.db.models.SDCardGroup"%>
<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%@page import="java.util.List"%>
<%
	String sectionName=request.getParameter("name");
%>
<!DOCTYPE html>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="SD Card Groups" />
</jsp:include>
<body>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<a id="sdcardsProgNameDiv" href='/vedantu-ext-cmds-uploader/imports.jsp'>
			<div class="sdcardsProgNameImg commonSprite"></div>
			<div class="sdcardsProgName"><%=sectionName %></div> </a>
		<%
List<SDCardGroup> sdCardGroups = (List<SDCardGroup>) request.getAttribute("sdCardGroups");
if(!sdCardGroups.isEmpty()){
%>
		<table id="sdcardsTable" cell-spacing="0" cell-padding="0">
			<tr>
				<td id="sdcardGroupsSection">
					<%
					for(SDCardGroup grp:sdCardGroups){		
					    String timeCreated=StringUtils.dateString("dd/MM/yyyy",grp.timeCreated);
				%>
					<div class="sdcardGroup" data-card-grp-id="<%=grp.id%>">
						<div class="sdcardGroupActiveTile"></div>
						<div class="sdcardGroupContent">
							<div class="sdcardGroupTitle"><%=grp.name %></div>
							<div class="sdcardGroupCreationTime">
								Created on
								<%=timeCreated %></div>
						</div>
						<div class="sdcardArrow"></div>
						<div class="sdcardGroupWhiteTile"></div>
					</div> <%
					}
				%>
				</td>
				<td id="sdcardsSection"></td>
				<td id="sdcardFlashingSection">
					<div id="sdcardStatusSection" class="sdcardFlashSection">
						<div class="sdcardState sdcardStateActive sdcardFlashStepInactive">
							<div>1.</div>
							<div>
								<div class="sdcardIcon sdcardIconInactive commonSprite"></div>
							</div>
							<div>SD Card</div>
							<div></div>
						</div>
						<div class="sdcardState sdcardFlashStepChecking">
							<div>1.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>Checking for SD Card...</div>
							<div>
								<div class="sdcardLoadingImg"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepFailed">
							<div>1.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>
								No SD Card Found!
								<div class="smally" style="font-weight: normal; font-size: 12px; margin-top: 2px;">Insert a SD Card to continue..</div>
							</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepSuccess">
							<div>1.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>SD Card Found!</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
					</div>
					<div id="sdcardStorageSection" class="sdcardFlashSection">
						<div class="sdcardState sdcardStateActive sdcardFlashStepInactive">
							<div>2.</div>
							<div>
								<div class="sdcardIcon sdcardIconInactive commonSprite"></div>
							</div>
							<div>Storage</div>
							<div></div>
						</div>
						<div class="sdcardState sdcardFlashStepChecking">
							<div>2.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>Checking for Storage...</div>
							<div>
								<div class="sdcardLoadingImg"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepFailed">
							<div>2.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>
								Not Enough Space!
								<div class="smally" style="font-weight: normal; font-size: 12px; margin-top: 2px;">Format the SD Card to continue..</div>
							</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepSuccess">
							<div>2.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>Storage Available!</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
					</div>
					<div id="sdcardEncryptionSection" class="sdcardFlashSection">
						<div class="sdcardState sdcardStateActive sdcardFlashStepInactive">
							<div>3.</div>
							<div>
								<div class="sdcardIcon sdcardIconInactive commonSprite"></div>
							</div>
							<div>Encryption</div>
							<div></div>
						</div>
						<div class="sdcardState sdcardFlashStepChecking">
							<div>3.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>Checking for Encryption...</div>
							<div>
								<div class="sdcardLoadingImg"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepFailed">
							<div>3.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>
								Encrypted!
								<div class="smally" style="font-weight: normal; font-size: 12px; margin-top: 2px;">Format the SD Card to continue..</div>
							</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
						<div class="sdcardState sdcardFlashStepSuccess">
							<div>3.</div>
							<div>
								<div class="sdcardIcon commonSprite"></div>
							</div>
							<div>Not Encrypted!</div>
							<div>
								<div class="sdcardSuccessImg commonSprite"></div>
							</div>
						</div>
					</div>
					<div class="centerText">
						<div class="greenButton nonner" id="flashNow">Flash</div>
					</div>
				</td>
			</tr>
		</table>

		<% 
}else{
%>
		<div class="userMessage">No SD Groups found.</div>
		<%} %>
	</div>
	<div id="sdcardSample" class="nonner">
		<div class="sdcard">
			<div class="sdcardImageDiv">
				<div class="sdcardFillPercentDiv"></div>
				<div class="sdcardImage"></div>
			</div>
			<div class="sdcardContent">
				<div class="sdcardTitle"></div>
				Estimated Size <span class="color3 boldy margLeft5 sdcardContentSize">0</span><br> Content count <span class="color3 boldy margLeft5 sdcardContentCount">0</span>
			</div>
			<div class="sdcardArrow"></div>
		</div>
	</div>
	<div id="flashProgressSample" class="nonner">
		<div class="cmdsPopupHead">Flashing Status</div>
		<div class="flashProgressDiv">			
			<div class="flashGrayDiv"></div>
			<div class="flashGreenDiv"></div>
		</div>
		<div class="margHalfTop big16">
			Percentage Completed: <span class="flashPercentText">0%</span>
		</div>
	</div>
	<div id="mountPointSample" class="nonner">
		<div class="cmdsPopupHead">Provide the mount point of SD Card</div>
		<table width="100%">
			<tbody>
				<tr>
					<td class="grayHead alignTop" style="padding-top: 10px">Mount Point*</td>
					<td>
						<div class="inputDiv">
							<input type="text" name="name" class="formInput" maxlength="120" />
						</div>
						<div style="font-size: 11px; line-height: 13px; margin-top: 5px;">
							<b>For Example </b><br> Windows= G:\ <span style="color: rgb(136, 136, 136);">where G: is the drive for SD Card</span><br> Mac and Linux= /media/SDCardName
						</div></td>
				</tr>
			</tbody>
		</table>
	</div>	
	<jsp:include page="includes/footer.jsp" />
	<script type="text/javascript" src="public/javascripts/imports.js"></script>
	<script type="text/javascript">
		dtappImports.init();
	</script>
</body>
</html>

