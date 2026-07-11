function getContactNumber(userId) {
    var contactNumber = null;
    db.orgmembers.find({'userId': userId}).forEach(function(orgmember){
        contactNumber = orgmember.contactNumber;
    })
    return contactNumber;
}
function getFirstName(userId) {
    var firstName = null;
    db.orgmembers.find({'userId': userId}).forEach(function(orgmember){
        firstName = orgmember.firstName;
    })
    return firstName;
}
db.campaigncodes.find({"expired":true}).forEach(function(doc){
    var userId = doc.consumerUserIds[0];
    print(getFirstName(userId)+","+getContactNumber(userId));
})