
db.getCollection('users').find({$where:function(){
var currentDate = new Date();
currentDate.setDate(currentDate.getDate()-1);
var current_day = currentDate.getDate();
var current_mon = currentDate.getMonth();
var current_year = currentDate.getYear();

var created_date = new Date(this.timeCreated);
var created_date_day = created_date.getDate();
var created_date_mon = created_date.getMonth();
var created_date_year = created_date.getYear();

return (current_day==created_date_day && current_mon==created_date_mon && current_year==created_date_year);
}},{firstName:1,lastName:1,email:1,username:1,isEmailVerified:1}).forEach(function(userNew){
print(userNew.firstName+","+userNew.lastName+","+userNew.email+","+userNew.username+","+userNew.isEmailVerified)});
