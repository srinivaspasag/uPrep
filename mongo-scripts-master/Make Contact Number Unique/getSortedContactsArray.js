function assignContactNumber(contactNumbersMap, contactNumber) {
  typeof contactNumbersMap[contactNumber] === 'undefined' ? contactNumbersMap[contactNumber] = 1 : contactNumbersMap[contactNumber]++;
}
var contactNumbersMap = {};
db.orgmembers.find({'contactNumber' :{$ne : null}}).forEach(function(orgmember){
	if (orgmember.contactNumber.length != 0){
	    contactNumber = orgmember.contactNumber;
	    assignContactNumber(contactNumbersMap,contactNumber);
	}
})

var sortedArray = [];
for (contactNumberValue in contactNumbersMap){
	sortedArray.push([contactNumberValue, contactNumbersMap[contactNumberValue]])
}
sortedArray.sort(function(a,b){
	return b[1] - a[1]
})

// var times;
// var totalTimes = 0;
// Object.keys(contactNumbersMap).forEach(function(contact){
// 	times = contactNumbersMap[contact];
// 	if(times > 1){
// 		totalTimes += times; 
// 		print("Contact Number : " + contact + " ,Repeated : " + times);
// 	}
// })
// print(totalTimes);
