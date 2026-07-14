db.accesscodes.find({}).forEach(function(e)
{
  if(e.buyerContactDetails!=null)
  {
        if(e.buyerContactDetails.billingAddress!=null && e.buyerContactDetails.billingAddress.location==null) 
        {
                  e.buyerContactDetails.billingAddress.location = {};
                  if(e.buyerContactDetails.billingAddress.city != null)
                  {
                            e.buyerContactDetails.billingAddress.location.city = e.buyerContactDetails.billingAddress.city;
                  }

                 if(e.buyerContactDetails.billingAddress.country != null)
                 {
                          e.buyerContactDetails.billingAddress.location.country = e.buyerContactDetails.billingAddress.country;
                }
       
                if( e.buyerContactDetails.billingAddress.state!=null)
                {
                            e.buyerContactDetails.billingAddress.location.state = e.buyerContactDetails.billingAddress.state;
                }
        }

    
         if(e.buyerContactDetails.shipmentAddress!=null && e.buyerContactDetails.shipmentAddress.location==null) 
         {        e.buyerContactDetails.shipmentAddress.location = {};
                
              if(e.buyerContactDetails.shipmentAddress.city != null)
              {
                 e.buyerContactDetails.shipmentAddress.location.city = e.buyerContactDetails.shipmentAddress.city;
              }

              if( e.buyerContactDetails.shipmentAddress.country!=null)
              {
                 e.buyerContactDetails.shipmentAddress.location.country = e.buyerContactDetails.shipmentAddress.country;
              }

              if(e.buyerContactDetails.shipmentAddress.state!=null)
              {
                 e.buyerContactDetails.shipmentAddress.location.state = e.buyerContactDetails.shipmentAddress.state;
             }
        }
        
      
        printjson (e) ;
        db.accesscodes.save(e)
    }
}
)
