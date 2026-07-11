
var orgId = "561f82e7e4b04529cf8dcdb2";
var dateOfOrder = "";
db.orders.find({"items.seller.id":orgId,"items.seller.type":"ORGANIZATION",recordState:"ACTIVE"}).forEach(function(order){
    db.orgmembers.find({userId:order.customer.id}).forEach(function(member){
        dateOfOrder = new Date(order.orderTime);
        print(order.orderId+ "," +
            "\""+order.items[0].category+"\"" + "-" +
            "\""+order.items[0].name+"\""+ ","
            +member.firstName +" "+ member.lastName + "," + member.email+","+member.contactNumber+","+order.deviceType+","+dateOfOrder+","+order.orderState+","+order.billingEmail);
    });
});