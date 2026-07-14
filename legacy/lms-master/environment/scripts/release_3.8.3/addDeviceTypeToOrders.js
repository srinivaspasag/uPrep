db.transactions.find().forEach(
	function(e){ 
		var order=db.orders.findOne({orderId: e.orderId}); 
		if(order) { 
			db.orders.update({orderId: order.orderId},{"$set" : {"deviceType" : e.deviceType}});
		} 
	})
