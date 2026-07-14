
db.organizations.find({}).forEach(function(e){
					  if(e.planId != undefined){
						e.subscription = {}
						e.subscription.validity={};
						e.subscription.validity.from=e.timeCreated;

						plan=db.plans.find( { _id : ObjectId( e.planId) });
						var date=new Date(e.timeCreated);
						print( date.toString() )
					        date.setFullYear( date.getFullYear() + 1)
						print( date.toString() )
						if( plan != undefined){
						if( plan.peruser !=  true ){
						e.subscription.validity.till=new NumberLong(date.getTime())   ;
						}else {
						 e.subscription.validity.till=new NumberLong(-1);

						}
						}
						e.subscription.planId=e.planId;

						e.planId=null;
					}
					printjson (e) ;
					db.organizations.save(e);	
				}
)
