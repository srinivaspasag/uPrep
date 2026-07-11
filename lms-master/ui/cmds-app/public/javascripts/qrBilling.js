var qrBilling = new function(){
    var parDiv;
    this.init = function(){
        parDiv = $("#qrOrgBillingDashboard");
        parDiv.off("click").off("change")
            .on("click",".submitStudentCount",showStudentCountTable);

    };

var showStudentCountTable = function(){
    
     var dateDivs = parDiv.find(".datevChooseDiv");
     var dateFn = getDateMillisFromvChoose;
     var startDate = dateFn(dateDivs.eq(0));
     var endDate = dateFn(dateDivs.eq(1));
     if (startDate >= endDate || startDate === -1 || endDate === -1) {
               showcmdsError("Please enter a proper start and end dates.", "OK");
                          return;
       }
        var getStudentCountParams= {
            "endDate" : endDate,
            "startDate" : startDate
        };
        vReq.post("/QrBilling/billingDetails",getStudentCountParams,function(data){
             $("#billingTableData").html(data);
        });
    };
};