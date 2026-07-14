<!DOCTYPE html>
<html>
<jsp:include page="includes/headTag.jsp">
	<jsp:param name="pageTitle" value="Imports" />
</jsp:include>
<body>
	<jsp:include page="includes/header.jsp" />
	<div id="dtappContentDiv">
		<div id="importDiv">
			<div id="importsFetchingDiv" class="nonner">
				<span class="onlyImportButtonImage commonSprite"></span>
				<div class="color9 big24 margBot20 centerText">Fetching the current state...</div>
			</div>
			<div id="onlyImportButtonDiv">
				<span class="onlyImportButtonImage commonSprite"></span>
				<div class="greenButton onlyImportLibraryBtn">Import Library</div>
				<div class="importLibrarySuggText">
					Choose a program library to import <br> (Requires internet connection)
				</div>
			</div>
			<div id="importProgressDiv" class="nonner">
				<div class="importProgressDivHead">
					<div class="greenButton importLibraryBtn floatRight">Import Library</div>
					<div class="programsPageHead">IMPORTING</div>
				</div>
				<div id="importingItems"></div>
			</div>
		</div>
		<div id="importedLibrariesSection">
			<div class="programsPageHead importedLibrariesHead">IMPORTED LIBRARIES</div>
			<div id="importedLibrariesFetchingDiv">
				<div class="color9 big24 margBot20 centerText">Fetching the imported libraries...</div>
			</div>
			<div id="importedLibraries" class="nonner"></div>
			<div class="cleaner_with_divider">&nbsp;</div>
		</div>
	</div>
	<jsp:include page="includes/footer.jsp" />
	<div id="importedLibrarySample" class="nonner">
		<a class="importedLibrary">
			<div class="importedLibraryTitle">Untitled</div>
			<div class="importedLibraryDetails">
				<span class="boldy color3 importedLibrarySDCardGrpCount">0</span> SD CARD Groups <label class="pipe">|</label> <span class="boldy color3 importedLibraryContentCount">0</span> Content Pieces <label class="pipe">|</label> Flashed <span class="boldy color3 importedLibraryFlashCount">0</span> Times
			</div>
			<div class="importedLibrarySyncDiv">
				<span class="color6">Last Sync</span> <span class="color9 importedLibraryLastSyncTime inlineBlock margTop">0 days ago</span>
				<div class="syncLibraryButton commonSprite"></div>
			</div> </a>
	</div>
	<div id="importingItemSample" class="nonner">
		<div class="importingItem">
			<span class="importingItemImage commonSprite"></span>
			<div class="importingItemContent">
				<div class="importingItemTitle">Untitled</div>
				<div class="importingItemDownloadStartedDiv nonner">
					<div class="importingItemProgressDiv">
						<div class="importingItemGrayDiv"></div>
						<div class="importingItemGreenDiv"></div>
					</div>
					<span class="importingItemPauseResumeButton importingItemPauseButton commonSprite" style="display: none;"></span> <span class="importingItemCancelButton commonSprite nonner"></span>
					<div class="importingItemStatusText nonner">
						<span class="importingItemImportPercent">0%</span> <span class="importingItemImportedSize color6">(0 KB </span> <span class="importingItemImportSize color9">of 0 MB) </span>
						<div class="importingItemTimeDiv nonner">
							<span class="importingItemSpeedText">0kb/s .</span> <span class="italica importingItemTimeLeft"> about 0 hrs </span>
						</div>
					</div>
					<div class="importingItemPreparingText">
						<span class="importingItemImportSize color9">Preparing for download...</span>
					</div>
				</div>
				<div class="importingItemDownloadNotStartedDiv margTop nonner">
					<span class="color9">Initialising download...</span> <label class="preparedDownloadPercent italica color6">0% complete</label>
				</div>
				<div class="startImportingButtonsDiv">
					<div class="smallGreenButton margTop startImportingLibrary">Start Download</div>
					<div class="smallGrayButton margTop removeImportingLibrary">Remove</div>
				</div>

			</div>
		</div>
	</div>
	<script type="text/javascript" src="public/javascripts/imports.js"></script>
	<script type="text/javascript" src="public/javascripts/qrAcadStr.js"></script>
	<script type="text/javascript">
		dtappImports.init({
			targetPage : "IMPORTS"
		});
	</script>
</body>
</html>

