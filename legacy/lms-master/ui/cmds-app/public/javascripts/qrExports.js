var qrExports = new (function($) {
    var parDiv, clickEvent = 'click', bodyClickEvent = "click.qrExports";
    var getReq = vReq.get;
    var postReq = vReq.post;
    var jobIdList = [];
    var fetchJobTimer;
    var pageUrlParams = {state: "", programId: "", centerId: "", sectionId: "",
        start: 0, size: 50};
    var TIME_INTERVAL = 10000;
    this.init = function() {
        parDiv = $("#libraryExports");
        parDiv.off(clickEvent)
                .on(clickEvent, '#createExportBtn', createExportBtn)
                .on(clickEvent, '.showExportDetails', showExportDetails)
                .on(clickEvent, '.hideExportDetails', hideExportDetails)
                .on(clickEvent, '.loadMoreItems', loadMoreDetails)
                .on(clickEvent, '.cancelExport,.deleteExport', cancelExport)
        $("body").off(bodyClickEvent)
                .on(bodyClickEvent, ".submitExportSection", submitExportSection)
                .on(bodyClickEvent, ".confirmExportBtn", confirmExportBtn)

        //var iPr = {'orgEntity':{"type":"ORGANIZATION","id":cmdsOrgId}};

        var urlParams = fetchUrlParams();
        var mcParams = {start: 0, size: 50};
        $.extend(mcParams, urlParams);
        var filterWidget = parDiv.find(".filterProgCenterSecDiv");
        filterAcadEntWidget.loadWidget(filterWidget, mcParams);
        var mcWidget = parDiv.addClass("mcWidget");
        initmcWidgetforCMDS(mcWidget, "/qrExports/exportList", mcParams, true, true, afterLoad);
        mcWidget.data("pageUrlParams", pageUrlParams);
        mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
        if(urlParams.state){
            setvChooseValue(mcWidget.find(".vChooseExportState"), urlParams.state);
        }
    };
    var afterLoad = function() {
        parDiv.find(".progressBarHolder").startProgressBar();
        parDiv.find(".LMHandlerDiv").updateClientTime();
        jobIdList = [];
        getAllJobs();
    };
    var createExportBtn = function() {
        startLoader();
        var successFn = function(data) {
            var popup = getcmdsPopupBody(700);
            popup.html(data);
            popup.find(".submitChangedProgram").text("Start Export")
                    .removeClass("submitChangedProgram")
                    .addClass("submitExportSection");
            popup.find(".cmdsPopupHead").text("Select Section library to export");
            fetchScripts([{fname: "qrAcadStr.js",
                    cb: setAcadStrTable}]);
            stopLoader();
        };
        getReq("/qrprograms/changeProgPageProgram", {}, successFn);
    };
    var setAcadStrTable = function() {
        qrAcadStr.deactivateAcadEntites("ASTProgramsTd", "PROGRAM", "getCentersOfProgram");
    };
    var submitExportSection = function() {
        var tableParams = qrAcadStr.getAcadStrTableParams();
        var programId = tableParams.programId;
        var centerId = tableParams.centerId;
        var sectionId = tableParams.sectionId;
        if (!sectionId) {
            showError("Please choose a section to export library!");
            return;
        }
        closePopup();
        var expName = tableParams.programName + ", " + tableParams.centerName + ", " + tableParams.sectionName;
        var dt = (new Date()).getTime();
        expName += "_" + $(dt).formatDate("dd/mm/yyyy @ hrs:min:sec");
        var params = {orgEntity: {"type": "SECTION", "id": sectionId}, name: expName};
        showConfirmPopup(params, tableParams, expName);
    };
    var showConfirmPopup = function(params, tableParams, name) {
        var popup = getcmdsPopupBody(600);
        popup.html(parDiv.find(".postExportPopup").clone());
        var h = popup.find(".postExportPopup");
        h.data('params', params);
        h.find(".createExportName").val(name);
        h.find(".expProgName").text(tableParams.programName);
        h.find(".expCenterName").text(tableParams.centerName);
        h.find(".expSectionName").text(tableParams.sectionName);
    };
    var confirmExportBtn = function() {
        var par = $(this).closest(".postExportPopup");
        var params = par.data('params');
        params.name = par.find(".createExportName").val().trim();
        var holder = parDiv.find(".LMHandlerDiv");
        closePopup();
        postReq("/QrExports/submitExportReq", params, function(data) {
            var firstItem = holder.find(".exportItem:first");
            if (firstItem.get(0)) {
                firstItem.before(data);
            } else {
                holder.html(data);
            }
            holder.updateClientTime();
            firstItem = holder.find(".exportItem:first");
            firstItem.find(".progressBarHolder").startProgressBar();
            getAllJobs();
        });
    };
    var getAllJobs = function() {
        var holder = parDiv.find(".LMHandlerDiv");
        holder.find(".exportInProgress").each(function() {
            var $this = $(this);
            var jobId = $this.data("jobId");
            appendJobId(jobId);
        });
        startJobs();
    };
    var appendJobId = function(jobId) {
        if (jobIdList.indexOf(jobId) < 0) {
            jobIdList.push(jobId);
        }
    };
    var removeJobId = function(jobId) {
        var ind = jobIdList.indexOf(jobId);
        jobIdList.splice(ind, 1);
    };
    var startJobs = function() {
        if (fetchJobTimer) {
            clearInterval(fetchJobTimer);
        }
        fetchJobTimer = setInterval(function() {
            fetchJobs();
        },TIME_INTERVAL);
        fetchJobs();
    };
    var fetchJobs = function() {
        if (jobIdList.length <= 0) {
            return;
        }
        var params = {jobIds: jobIdList};
        var holder = parDiv.find(".LMHandlerDiv");
        $.get("/qrExports/getPublishStatus", params, function(data) {
            if (data && data.result && data.result.list) {
                var list = data.result.list;
                $(list).each(function(value, index) {
                    var item = holder.find(".exportJobId-" + this.jobId);
                    var progressBar = item.find(".totalProgressTD");
                    if (this.errorCode != "") {
                        var errMsg = this.errorMessage ? this.errorMessage : this.errorCode;
                        progressBar.html("<div class='big16 redTextColor'>" + errMsg + "</div>");
                    } else if (this.numCompletedSteps >= this.numOfSteps) {
                        progressBar.html("<div class='big16 greenTextColor'>Done, ready to download</div>");
                        var params = {"exportId": item.data("id"), "fetchContent": false, start: 0, size: 1};
                        $.get("/QrExports/getExportDetails", params, function(data) {
                            if (data && data.errorCode == "" && data.result.url) {
                                item.find(".cancelExportTD").addClass("nonner");
                                item.find(".doneExportTD,.deleteExportTD").removeClass("nonner");
                                item.find(".downloadExport").attr("href", data.result.url);
                                removeJobId(this.jobId);
                            }
                        });
                    } else {
                        var percent = this.numCompletedSteps / this.numOfSteps * 100;
                        percent = Math.round(percent);
                        item.find(".progressBarHolder").updateProgressBar(percent);
                    }
                });
            }
        });
    };
    var showExportDetails = function() {
        var $this = $(this);
        var item = $this.closest(".exportItem");
        var detailsDiv = item.find(".exportDetails").removeClass("nonner");
        detailsDiv.find(".exportProgCenterSec").html(item.find(".exportBody").find(".exportProgCenterSec").html());
        $this.addClass("nonner").siblings().removeClass("nonner");
        var params = {"exportId": item.data("id"), "fetchContent": true, start: 0, size: 10};
        getReq("/QrExports/viewExportDetails", params, function(data) {
            detailsDiv.find(".exportContentList table").append(data);
        });
    };
    var hideExportDetails = function() {
        var $this = $(this);
        var detailsDiv = $this.closest(".exportItem").find(".exportDetails").addClass("nonner");
        $this.addClass("nonner").siblings().removeClass("nonner");
        detailsDiv.find(".exportContentList table").find("tr:not(:first)").remove();
    };
    var loadMoreDetails = function() {
        var $this = $(this);
        var start = $this.data("nextStart");
        smallLoader($this);
        var item = $this.closest(".exportItem");
        var successFn = function(data) {
            var table = $this.closest("table");
            $this.closest(".loadMoreDivTR").remove();
            table.append(data);
        };
        var pr = {"exportId": item.data("id"), start: start, size: 10, "fetchContent": true};
        getReq("/QrExports/viewExportDetails", pr, successFn);
    };
    var cancelExport = function() {
        var $this = $(this);
        var item = $this.closest(".exportItem");
        var ret = confirm("Are you sure , to " + $this.text());
        if (!ret)
            return;
        getReq("/QrExports/deleteExport", {"exportId": item.data("id")}, function(data) {
            if (data && data.errorCode == "" && data.result.deleted) {
                item.remove();
                removeJobId(item.data("jobId"));
            }
        })
    };
})(jQuery);
