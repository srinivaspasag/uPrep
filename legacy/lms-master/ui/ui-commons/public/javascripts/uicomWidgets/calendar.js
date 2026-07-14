var vCalendar=new function($){
    var m=makeHTMLTag;
    var days=["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"];
    var months=["January","Febrauary","March","April","May","June","July","August","September","October","November","December"];
    
    
    //utilities
    var getCalenHTML=function(dateObj,daysList,monthsList){                
        var finalDateObj=dateObj||new Date();        
        if(daysList&&daysList.length==7)days=daysList;
        if(monthsList&&monthsList.length==12)months=monthsList;
        
        
        
        var head=m("span",{"class":"vCalenHead"});               
        var calenHead=m("div",{"class":"vCalenHeadDiv"});
        calenHead.html(m("span",{"class":"vCalenPrevMonth"})).append(head)
        .append(m("span",{"class":"vCalenNextMonth"}));


        
        var calenTable=m("table",{"class":"vCalenTable"});    
        var daysStr="";
        for(var i=0;i<=6;i++){
            daysStr+="<th>"+days[i]+"</th>";
        }
        calenTable.html("<tbody><tr>"+daysStr+"</tr></tbody");
        
        var dayNum=getDaysOffset(finalDateObj);
        var dayCount=0,maxMonthDays=getMonthMaxDays(finalDateObj),k=1;
        for(var r=0;r<5;r++){
            var tr=m("tr");
            for(var c=0;c<7;c++){  
                 var dayVal="";
                 if(dayCount>=dayNum&&k<=maxMonthDays){
                    dayVal=k;
                    k++;
                 }
                var td=getSetDateCell(m("td"),dayVal);
                tr.append(td);
                dayCount++;
            }
            calenTable.append(tr);
        }
        
        
        var vCalen=m("div",{"class":"vCalen"});
        vCalen.html(calenHead).append(calenTable);
        setVCalenDataAndHead(vCalen,finalDateObj,dayNum);
        vCalendar.init(vCalen);
        return vCalen;
    }           
    var doDateNumbering=function(vCalen,dateObj){
        var dayCount=1,dayVal="",offset=getDaysOffset(dateObj);
        var maxMonthDays=getMonthMaxDays(dateObj);
        var tds=vCalen.find("td");        
        for(var k=0;k<tds.length;k++){
             if(k>=offset&&dayCount<=maxMonthDays){
                dayVal=dayCount;
                dayCount++;
             }else dayVal="";            
             getSetDateCell(tds.eq(k),dayVal);
        }
        setVCalenDataAndHead(vCalen,dateObj,offset);
    }
    var getSetDateCell=function(td,dayVal){
        td.html("<div class='vCalenDate'>"+dayVal+"</div><div class='vCalenDateBody'></div>")
        .data("date",dayVal);
        return td;
    }
    var getDaysOffset=function(dateObj){
        var dayNum=new Date(dateObj.getFullYear(),dateObj.getMonth(),1).getDay();
        if(dayNum==0)dayNum=6;
        else dayNum-=1;        
        return dayNum;
    }
    var setVCalenDataAndHead=function(vCalen,dateObj,offset){
        var m=dateObj.getMonth(),y=dateObj.getFullYear();
        vCalen.data({monthIndex:m,year:y,offsetIndex:offset});        
        vCalen.find(".vCalenHead").text(months[m]+" "+y);
    }
    var prevNextMonthClick= function(e){
        var vCalen=e.data,$data=vCalen.data(),y=$data.year;
        var monthIndex=$data.monthIndex;
        if($(this).hasClass("vCalenNextMonth")){
            monthIndex++;
        }else monthIndex--;
        
        if(monthIndex>11){
            monthIndex=0;
            y++;
        }else if(monthIndex<0){
            monthIndex=11;
            y--;
        }
        vCalendar.highlightVCalenDate(vCalen,new Date(y,monthIndex,1))
    }    
    
    //externally accessible
    $.fn.putCalendar = function(dateObj,daysList,monthsList) {
        //dateObj=new Date(2012,5,23)==>april 23,2012
        $(this).html(getCalenHTML(dateObj,daysList,monthsList));        
    };
    this.getVCalenDateCell=function(vCalen,dateObj){
        var $data=vCalen.data();        
        var eqNum=$data.offsetIndex+(dateObj.getDate()-1);
        return vCalen.find("td").eq(eqNum);                
    }
    this.highlightVCalenDate=function(vCalen,dateObj,highlightClassName){
        var $data=vCalen.data();        
        dateObj=dateObj||new Date();
        if($data.monthIndex!=dateObj.getMonth()||$data.year!=dateObj.getFullYear()){
              doDateNumbering(vCalen,dateObj);
        }
        var eqNum=$data.offsetIndex+(dateObj.getDate()-1);
        var tds=vCalen.find("td").removeClass(c);
        highlightClassName=highlightClassName||'';
        var c="activeVCalenDate "+highlightClassName;        
        tds.eq(eqNum).addClass(c);
    }   
    this.init=function(vCalen){
        vCalen.on("click",".vCalenPrevMonth,.vCalenNextMonth",vCalen,prevNextMonthClick)
    }   
}(jQuery);

//
//
//    var makeDateObj=function(dateObj){
//        var nowDateObj=new Date();
//        if(dateObj){
//            var monthNo=dateObj.monthNo,monthIndex;
//            if(monthNo&&monthNo<13&&monthNo>0)monthIndex=monthNo;
//            else monthIndex=nowDateObj.getMonth();
//            var year=dateObj.year||nowDateObj.getFullYear();
//            
//            var maxDays=getMonthMaxDays(dateObj);
//            var objDate=dateObj.date,date;
//            if(objDate&&objDate>0&&objDate<=maxDays)date=dateObj;
//            else date=nowDateObj.getDate();
//            
//            return new Date(year,monthIndex,date);
//        } else return nowDateObj;
//    }    
//  
  
