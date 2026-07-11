var qrDevice = new (function($) {
    var parDiv, clickEvent = 'click', bodyClickEvent = "click.qrDevice";
    var getReq = vReq.get;
    var postReq = vReq.post;
    var pageUrlParams = {profile: "STUDENT", programId: "", centerId: "", sectionId: "",
        query: "", start: 0, size: 50};
    this.init = function() {
        parDiv = $("#deviceManagement");
        parDiv.off(clickEvent)
                .on(clickEvent, ".showUserMappings", showUserMappings)
                .on(clickEvent, ".viewUserDeviceDetails", viewUserDeviceDetails)
                .on(clickEvent, ".openStudentPage", openStudentPage)
                .on(clickEvent, ".openMemberPage", openMemberPage);
        $("body").off(bodyClickEvent)
                .on(bodyClickEvent, ".userDeviceDetailsHold .loadMoreItems", loadMoreDevices);

        var urlParams = fetchUrlParams();
        var mcParams = {profile: "STUDENT", start: 0, size: 50};
        $.extend(mcParams, urlParams);
        var filterWidget = parDiv.find(".filterProgCenterSecDiv");
        filterAcadEntWidget.loadWidget(filterWidget, mcParams);
        var mcWidget = parDiv.addClass("mcWidget");
        initmcWidgetforCMDS(mcWidget, "/qrDevices/deviceTable", mcParams, true, true);
        mcWidget.data("pageUrlParams", pageUrlParams);
        mcWidget.data("changeUrlAfterLoad", aftermcWidgetContentLoaded);
        updatemcWidgetParamHolders(mcWidget, urlParams);
        if (urlParams.profile) {
            setvChooseValue(mcWidget.find(".vChooseUserProfile"), urlParams.profile);
        }
    };
    var loadMoreDevices = function() {
        var $this = $(this);
        var userId = $this.data("userId");
        var start = $this.data("nextStart");
        smallLoader($this);
        var successFn = function(data) {
            var table = $this.closest("table");
            $this.closest(".loadMoreDivTR").remove();
            table.append(data);
            table.updateClientTime();
        };
        var pr = {targetUserId: userId, start: start, size: 10};
        getReq("/qrdevices/userDeviceTable", pr, successFn);
    };
    var viewUserDeviceDetails = function() {
        startLoader();
        var $this = $(this);
        var userId = $this.data("userId");
        var userName = $this.data("userFullName");
        var successFn = function(data) {
            stopLoader();
            var popup = getcmdsPopupBody().html(data);
            popup.children(".cmdsPopupHead").text("Device Details Info for '" + userName + "'");
            popup.updateClientTime();
        };
        var pr = {targetUserId: userId, start: 0, size: 10};
        getReq("/qrdevices/userDeviceDetails", pr, successFn);
    };
    var showUserMappings = function() {
        startLoader();
        var successFn = function(data) {
            stopLoader();
            var popup = getcmdsPopupBody().html(data);
            popup.children(".cmdsPopupHead").text("Institute Info");
        };
        getReq("/qrpeople/showUserMappings", {targetUserId: $(this).data("userId")}, successFn);
    };
})(jQuery);
function onUserDeviceChangeInPopup(selected, value) {
    $(selected).closest(".cmdsPopupBody").find(".userDeviceDetailsHold")
            .removeClass("showAllDevices")
            .removeClass("showOnlyWebDevices")
            .removeClass("showOnlyMobileDevices")
            .addClass(value);
}
var putStatusAndDeviceTypeParams = function(mcWidget, vChoose) {
    var value = vChoose.data("value");
    var mcWidgetParams = mcWidget.data("params");
    if (value == "-1") {
        delete mcWidgetParams.status;
        delete mcWidgetParams.deviceType;
    } else {
        var values = value.split(";");
        mcWidgetParams.status = values[0];
        mcWidgetParams.deviceType = values[1];
    }
    manageContent.loadmcContent(mcWidget, vChoose);
}
