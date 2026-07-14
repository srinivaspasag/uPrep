
db.cmdsvideos.find({}).forEach(function(e){
                var url = e.url;                                       
                if(url)                    
                {     
                      if(url.indexOf('https://')==0  )
                      {
//                         url = url.substring((url.indexOf("://")+1)); 
                         url = url.replace('https://','http://'); 
                         e.url = url;   
                      }                                 
			else if( url.indexOf("//") == 0){
                         url = url.replace('//','https://');
			 e.url = url;
			}
			 db.cmdsvideos.save(e);

                }})

db.videos.find({}).forEach(function(e){
                var url = e.url;                                       
                if(url)                    
                {     
                      if(url.indexOf('https://')==0  ){
			  return; 
		      }
                      if(url.indexOf('http://')==0 )
                      {
                         url = url.replace('http://','https://'); 
                         e.url = url;   
                      }                                 
			else if( url.indexOf("//") == 0){
                         url = url.replace('//','https://'); 
			 e.url = url;
		}
                         db.videos.save(e);
                }})
