var qrSaleDetails = new function(){
    var parDiv;
    var CLICK = "click.qrSaleDetails";
    var CHANGE = "change.qrSaleDetails";
    this.init = function(){
        parDiv = $("#qrSaleDetailsPage");
        var paymentitems = $(".paymentItems");
        parDiv.off(CLICK).off(CHANGE)
            .on(CLICK,".paymentItems",loadPayment);

    };

    var loadPayment = function(){
        var $this = $(this);
        var paymentItems = $this.data('paymentitems');
        showTopLoader();
        vReq.get("/qrSaleDetails/paymentItemsPopup", null, function(data){
            var popup = showVPopup(0.7);
            popup.html(data);
        var paymentItemsTable = popup.find(".qrSaleDetailsTable");
        for(var i = 0, size = paymentItems.length; i < size ; i++){
            var paymentItem = paymentItems[i];
            console.log(paymentItem.paymentType);
        var payableDate = new Date(paymentItem.payableDate);
        var payableDateString =  payableDate.getDate() + " / " + (payableDate.getMonth() + 1)+" / "+ payableDate.getFullYear();
        switch(paymentItem.paymentType) {
            case "CASH":
                var paymentItemTr = "<tr class=\"paymentTr\"><td class=\"width100\">"+paymentItem.paymentType+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.amount/100+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.isReceived+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td></tr";
                break;
            case "CHEQUE":
                var paymentItemTr = "<tr class=\"paymentTr\"><td class=\"width100\">"+paymentItem.paymentType+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.amount/100+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.chequeNumber+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.bankName+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+payableDateString+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.isReceived+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td></tr";
                break;
            case "PAYTM":
                var paymentItemTr = "<tr class=\"paymentTr\"><td class=\"width100\">"+paymentItem.paymentType+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.amount/100+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.isReceived+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.reference+" </td></tr";
                break;
            case "ETRANSFER":
                var paymentItemTr = "<tr class=\"paymentTr\"><td class=\"width100\">"+paymentItem.paymentType+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.amount/100+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.isReceived+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.reference+" </td></tr";
                break;
            case "SWIPE":
                var paymentItemTr = "<tr class=\"paymentTr\"><td class=\"width100\">"+paymentItem.paymentType+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.amount/100+" </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\"> NA </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.isReceived+" </td>";
                    paymentItemTr += "<td class=\"width100\">"+paymentItem.reference+" </td></tr";
                break;
        }
        paymentItemsTable.append(paymentItemTr);
        }

        });
    };
};