db.orgmembers.find
(
    { $and:
        [
        {"contactNumber" :null,"orgId":"5513a38de4b095b85aa162fd"},
        {$where:function()
            {var currentDate = new Date();
             currentDate.setDate(currentDate.getDate()-1);
             var current_day = currentDate.getDate();
             var current_mon = currentDate.getMonth();
             var current_year = currentDate.getYear();

             var created_date = new Date(this.timeCreated);
             var created_date_day = created_date.getDate();
             var created_date_mon = created_date.getMonth();
             var created_date_year = created_date.getYear();

            return (current_day==created_date_day && current_mon==created_date_mon 
                && current_year==created_date_year);}}
         ]
    },
    {firstName:1,lastName:1,email:1,timeCreated:1}
).sort({timeCreated:1}).
forEach
(
    function(userNew)
    {
        var programName = "NA";
        if(userNew.mappings != null){
            var programId = userNew.mappings[0].programId;
            db.orgprograms.find({'_id': ObjectId(programId)}).forEach(function(orgprogram){
                programName = orgprogram.name;
            })
        }
        print
        (
            userNew.firstName+","+userNew.lastName+","+userNew.email + "," +","+
            new Date(userNew.timeCreated)+","+programName
        )
    }
); 
































