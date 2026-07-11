var mySchedule=new function($){
    var programmeParams=[];
    var setCalendar=function(vCalen,params){
       $.get("/institute/getSchedule",params,function(data){ 
	  try{
            if(data.errorCode==""){
                var centerData=data.result.centerCalendarEntries[0];
                var centerName=centerData.name,sectionName=centerData.sections[0];
                var calMap=centerData.entries;
                $.each(calMap,function(datems,lectureList){
                    var dateObj=new Date(parseInt(datems));                
                    var cell=vCalendar.getVCalenDateCell(vCalen,dateObj);     
                    var cellBody=cell.find(".vCalenDateBody");
                    for(var k=0;k<lectureList.length;k++){
                        var lect=lectureList[k];
                        var msItem=makeHTMLTag("div",{"class":"MSCalenItem","rel":lect.course.courseId,"lang":sectionName+"-"+centerName})
                        .html("<div class='MSCItem'>"+lect.course.name+"("+getDayHrsMins(new Date(lect.startTime))+"-"+
                        getDayHrsMins(new Date(lect.endTime))+")</div><div class='MSCIDetails'></div>")
                        .data("lecture",lect);
                        cellBody.append(msItem);
                    }
                }); 
                var leftSec=vCalen.closest("#myScheduleContent").children("#myScheduleLeftSec");
                leftSec.find(".MSCBoxDiv").each(function(){                    
                   var cBox=$(this).find("input"),mscalenItems=findMSCalenItem(cBox,vCalen);
                   if(!cBox.prop("checked")){
                       mscalenItems.addClass("nonner");
                   }
                   mscalenItems.children(".MSCItem").css("color",$(this).find(".MSCBoxName").css("color"));
                });               
            }
	  }catch(err){ putConsoleError(err);} 
	  if(institute.schedules){
		var leftSec = $("#myScheduleContent").find("#myScheduleLeftSec");
		$("#instHomeWidgetsContainer").find(".instSchedulePgWidget")
			.append(leftSec.clone(true));
		institute.showWidgets(".instSchedulePgWidget",true);
		leftSec.remove();
	  }
       });          
    }
        
    //event hanlders
    var msCBoxInputChange=function(e){
        var vCalen=e.data.find(".vCalen");
        findMSCalenItem($(this),vCalen).toggleClass("nonner");
    }
    var findMSCalenItem=function(cBox,vCalen){
        var rel=cBox.attr("rel")||'';
        return vCalen.find(".MSCalenItem[rel='"+rel+"'],.MSCalenItem[lang='"+rel+"']");        
    }
    var jumpToKeyup=function(e){
        e.stopPropagation();
        var scheduleDiv=e.data;
        if((e.which&&e.which==13)||(e.keyCode&&e.keyCode==13)){
            var jumpToDiv=$(this).parent();
            var ms=validateAndGetMS(jumpToDiv.find(".MSJumpToYear").val(),
            jumpToDiv.find(".MSJumpToMonth").val(),jumpToDiv.find(".MSJumpToDate").val());        
            if(ms==0){
                showError("Please enter a valid date");
            }
            else{
                var d=new Date(ms),vCalen=scheduleDiv.find(".vCalen"),$data=vCalen.data();
                var oldMonth=$data.monthIndex,oldYear=$data.year;
                vCalendar.highlightVCalenDate(vCalen,d,"activeMSDate");
                if(oldMonth!=d.getMonth()||oldYear!=d.getFullYear()){                
                    changeCalender(vCalen,d);
                }
            }
        }        
    }
    var prevNextScheduleClick=function(e){
        var vCalen=$(this).closest(".vCalen"),$data=vCalen.data();
        var dateObj=new Date($data.year,$data.monthIndex,1)
        changeCalender(vCalen,dateObj);  
    }
    var MSCItemClick=function(e){
        var $data=$(this).parent().data("lecture"),popup=$(this).siblings(".MSCIDetails");
        addToggler(popup,$(this));
        if(popup.html()==""){
            popup.html(e.data.find(".MSCIDetStore").children().clone(true));
            popup.find(".STIDItem[rel=name] .STIDValue").text($data.course.name);
            popup.find(".STIDItem[rel=time] .STIDValue").text(getDayHrsMins(new Date($data.startTime))+"-"+
                getDayHrsMins(new Date($data.endTime)));            
            var noteStr="No Note found";
            for(var k=0;k<$data.note.length;k++){
                noteStr+="<li class='margHalfTop'>"+$data.note[k]+"</li>";
            }
            if($data.note.length>0)
            popup.find(".STIDItem[rel=note] .STIDValue").html("<ol>"+noteStr+"</ol>");
            $.get("/institute/getProfInfo",{profId:$data.assignee[0]},function(data){
                var info=data.result;
                popup.find(".STIDItem[rel=teacher] .STIDValue").text(info.firstName+" "+info.lastName);            
            });
        }        
    } 
    
    
    //utilities
    var getMonthParams=function(dateObj){
        dateObj=dateObj||new Date();
        var y=dateObj.getFullYear(),m=dateObj.getMonth();
        var params={fromTime:(new Date(y,m,1).getTime()),
           tillTime:new Date(y,m,getMonthMaxDays(dateObj)).getTime()}  
       return params;
    }
    var changeCalender=function(vCalen,dateObj){        
        for(var k=0;k<programmeParams.length;k++){            
            setCalendar(vCalen,$.extend(getMonthParams(dateObj),programmeParams[k])); 
        }        
    }


    this.init=function(params){
           var scheduleDiv=$("#myScheduleContent");
           scheduleDiv.find("#myScheduleTable").putCalendar();
           programmeParams=[];
           try{
               var programmes=params.programmes,program,center,reqParams={};
               if(params.userRole=="MEMBER"){
                   program=programmes[0]
                   center=program.centers[0];
                   reqParams={programmeId:program._id,centers:[{name:center.name,sections:[center.sections[0].name]}]};
                   programmeParams.push(reqParams);                   
               }else{
                   for(var k=0;k<programmes.length;k++){
                       program=programmes[k],reqParams.programmeId=program._id,reqParams.centers=[];
                       for(var c=0;c<program.centers.length;c++){
                           center=program.centers[c];
                           for(var s=0;s<center.sections.length;s++){                               
                               reqParams.centers.push({name:center.name,sections:[center.sections[s].name]});
                           }
                       }
                       programmeParams.push($.extend({},reqParams));                       
                   }              
               }       
               changeCalender(scheduleDiv.find(".vCalen"));               
           }catch(err){}
           //event handlers
           scheduleDiv.on("keyup",".MSJumpToDiv input",scheduleDiv,jumpToKeyup)
           .on("change",".MSCBoxDiv input",scheduleDiv,msCBoxInputChange)
           .on("click",".vCalenPrevMonth,.vCalenNextMonth",scheduleDiv,prevNextScheduleClick)
           .on("click",".MSCItem",scheduleDiv,MSCItemClick);

	  $("#instHomeWidgetsContainer").find(".instSchedulePgWidget")
	   .html("")
	   .on("keyup",".MSJumpToDiv input",scheduleDiv,jumpToKeyup)
           .on("change",".MSCBoxDiv input",scheduleDiv,msCBoxInputChange)
           .on("click",".MSCItem",scheduleDiv,MSCItemClick);
    } 
}(jQuery);

