db.orgmembers.find({}).forEach(function(e){
                      if(e.interval == undefined){
                        e.interval = {}
                        e.interval.from = e.timeCreated;
                        e.interval.till = new NumberLong(-1);

                    }
                    printjson (e) ;
                    db.orgmembers.save(e);    
                }
)
