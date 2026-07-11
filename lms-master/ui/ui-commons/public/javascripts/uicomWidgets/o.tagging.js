var ATTopicToSTMap={};
var vTagging=new(function($){
    this.init=function(params){
       var masterDiv=$("#addTagsMasterDiv");  
        params=params||{};
        if(params.addTagsId&&params.addTagsId!="")masterDiv=$("#addTagsMasterDiv."+params.addTagsId);
        var parentId=params.parentIdForCourses;
        var targetPage = params.targetPage;
        populateSelectOpts("SUBJECT","",targetPage);
        if(parentId){            
            populateSelectOpts("TOPIC",parentId);
        }
        $(".instSelMySubject").off("change")
                              .on("change","#subjectPicker",selectInstSubject);
    };
})(jQuery);
    $(".ATSelectBoxHead").live('click',function(){
        var tagsType=$(this).closest(".ATHolder").data("tagsType");
        var selectOptionsDiv=$(this).siblings(".ATSelectOptionsDiv");
        addToggler(selectOptionsDiv,$(this));
        var tagTreeDiv=$(this).siblings(".ATTagTreeDiv");
        tagTreeDiv.addClass("nonner");  
        removeATTickers(selectOptionsDiv);
        ATTopicToSTMap={};
        tagTreeDiv.children(".ATTTItem").each(function(){
            var input=selectOptionsDiv.find("#ATCRBox_"+$(this).data("brdId"));
            getATTicker(input);
            if(tagsType=="SUBJECT")selectOptionsDiv.find("input").addClass("hider");
            else if(tagsType=="TOPIC"){
               ATTopicToSTMap["topic_"+$(this).data("brdId")]=$(this).find(".ATTTSubTopicsDiv").children();
            }
        });       
    });
    $(document).on('click',".confirmAT",function(){
        var ATHolder=$(this).closest(".ATHolder"),tagsType=ATHolder.data("tagsType");
        var tagsList=ATHolder.find(".ATSelectOptList"),tagTree=ATHolder.find(".ATTagTreeDiv");
        var tags=makeHTMLTag('div'),tagIds=[];
        tagsList.find(".ATCRBox:checked,.ATCRBoxTick").each(function(){
            var $this=$(this),brdId=$this.data("brdId");
            var tag=window["ATTTSample"+tagsType].clone(true);
            assignBrdIdAndName($this,tag);
            tagIds.push(brdId);
            if(tagsType=="TOPIC"&&ATTopicToSTMap["topic_"+brdId]){
                tag.find(".ATTTSubTopicsDiv").html(ATTopicToSTMap["topic_"+brdId]);
            }
            tags.append(tag.children());
        });
        tagTree.html(tags.children()).removeClass("nonner");
        insideOutClick();
        if(tagsType==="SUBJECT"){
            var ATTHolder=ATHolder.siblings(".ATTHolder");
            var subId=ATTHolder.data("subId");
            if(!subId||tagIds.length==0||subId!=tagIds[0]){
                ATTHolder.find(".ATTagTreeDiv").html("");
                ATTHolder.find(".ATSelectOptList").html("");
            }
            populateSelectOpts("TOPIC",tagIds[0]);
        }        
    });

    function selectInstSubject(){
      $(".ATTagTreeDiv").html("");
      $(".ATTTSubject").remove();
      $.each($("#subjectPicker"),function(){
        $("#subjectPicker .ATCRBox").removeClass("selectSubActive");
      });
      $("#subjectPicker option:selected").addClass("selectSubActive");
      brdId = $("#subjectPicker .selectSubActive").data("brdId");
      var subName = $("#subjectPicker .selectSubActive").data("name");
      if(brdId!= undefined){
        $(".addTagsWrapper").prepend("<div class='ATTTSubject nonner ATTTItem doCStream' data-brd-id='"+brdId+"' data-name='"+subName+"'><span class='ATTTItemName ATTTSubjectName'>"+subName+"</span></div>");
        populateSelectOpts("TOPIC",brdId);
      }
    }

  function populateSelectOpts(reqTagsType,parentId,targetPage){
    var params={};
    var targetAT;
    params.start=0;params.size=25;
    var type="radio";
    if(reqTagsType==="TOPIC"){
        targetAT=$("#addTagsMasterDiv").find(".ATTHolder");            
        type="checkbox";  
        params.type="TOPIC";
        params.parentId=parentId;
    }else{
        targetAT=$("#addTagsMasterDiv").find(".ASTHolder");
        params.type="COURSE";
    }
    var optListDiv=targetAT.find(".ATSelectOptionsDiv");
    $.get("/uicomboards/getOrgBoards",params,function(data){
      var tags=data.result.list;
          if((targetPage !="ADD_MULT_QUESTION" && targetPage != "EDIT_QUESTION") || reqTagsType==="TOPIC"){
            var crBoxHTML=makeHTMLTag('div',{"class":"ATSelectOptList"});
            for(var k=0;k<tags.length;k++){
                var brdId=tags[k].id,name=tags[k].name;
                crBoxHTML.append("<div class='ATSelectOpt'><input type='"+type+"' \n\
                name='ATCRBox' class='ATCRBox doCStream' id='ATCRBox_"+brdId+"'\n\
                 data-brd-id='"+brdId+"' data-name='"+name+"'/>\n\
                <span class='ATCRBoxName'>"+name+"</span></div>");           
           }
           optListDiv.html(crBoxHTML);
           if(optListDiv.children(".addTagsBtns").length===0){
               optListDiv.append(getAddTagsBtns("confirmAT",undefined,reqTagsType))
           }
          }
          else{
            var tagIds = [];
            $(".instSelMySubject").html("<div class='selectSubjTopic'><span class='ATSelectBoxHeadIcon' style='top:10px;'></span><select class='ATSelectSubjectOpt selectpicker' id='subjectPicker' title='Nothing selected'>");
              $(".ATSelectSubjectOpt").html("<option class='ATCRBox' value='selected' disabled selected hidden>Subjects</option>");
              for(var k=0;k<tags.length;k++){
              var brdId=tags[k].id,name=tags[k].name;
              tagIds.push(brdId);
              $(".ATSelectSubjectOpt").append("<option name='ATCRBox' class='ATCRBox doCStream' id='ATCRBox_"+brdId+"' data-brd-id='"+brdId+"' data-name='"+name+"'>"+name+"</option>"+"</select></div>");
            }
            if(targetPage === "EDIT_QUESTION"){
                var brdId = $(".ASTHolder").find(".ATTTSubject").data("brdId");
                $("#ATCRBox_"+brdId).attr("selected","selected");
                var qDataType = $(".QAQTypeSelect").data("value");
                if(qDataType === "PARA"){
                  $(".ATSelectSubjectOpt").attr("disabled","disabled");
                  $(".ATTHolder").css("pointer-events","none");
                }
            }
          }
    });
  }
  
 
  
  
    //selecting subtopics
    $(".ATTTAddST").live('click',function(){
        var topicDiv=$(this).closest(".ATTTTopic");
        var popup=topicDiv.find(".ATSTPopup");
        addToggler(popup,$(this));
        if(popup.html()==""){
            popup.html("Loading...");
            var t=topicToSubTopic[topicDiv.data("brdId")];
            if(t){
                populateATSubTopics(topicDiv,t);
            }
            else{
                $.get("/uicomboards/getOrgBoards",{parentId:topicDiv.data("brdId"),
                    start:0,size:25,type:"SUBTOPIC"},function(data){
                   var sts=data.result.list;
                   topicToSubTopic[topicDiv.data("brdId")]=sts;
                   populateATSubTopics(topicDiv,sts);
                });                
            }
            preCheckSTs(topicDiv);
        }
        else{
            preCheckSTs(topicDiv)
        }
    });
    function populateATSubTopics(topicDiv,sts){
        var popup=topicDiv.find(".ATSTPopup");
       var stList=makeHTMLTag('div',{"class":"ATSTPopupList"});
       for(var k=0;k<sts.length;k++){
           var st=sts[k];
           var stPa=ATSTPSubTopicDivSample.clone(true);
           stPa.find(".ATSTPSTCBox").data("brdId",st.id)
           .data("name",st.name).attr("id","ATSTPSTCBox_"+st.id);
           stPa.find(".ATSTPSTName").text(st.name);
           if(st.hasOwnProperty("children")){
               var ssts=st.children,sstsHTML="";
               for(var l=0;l<ssts.length;l++){
                   var sst=ssts[l];
                   sstsHTML+="<div class='ATSTPSubSubTopic'><input type='checkbox' \n\
                            class='ATSTPSSTCBox' data-brd-id='"+sst.id+"'\n\
                         data-name='"+sst.name+"'/>\n\
                      <span class='ATSTPSSTName'>"+sst.name+"</span></div>";
               }
               stPa.find(".ATSTPSSTsDiv").html(sstsHTML);                       
           }
           stList.append(stPa.children());
       }

       var addTagsBtns="<div class='userMessage'>No Subtopics Found</div>";
        if(sts.length>0){
            addTagsBtns=getAddTagsBtns("confirmATSubTopics",undefined,"SUBTOPICS");
            popup.html(addTagsBtns).append(stList);
            $("<div class='boldy margBot10 ATSTPopupTopicName'>\n\
            "+topicDiv.data("name")+"</div>").insertBefore(stList);
        }
        else{
            popup.html(addTagsBtns);
        }        
    }
    function preCheckSTs(topicDiv){
        var optList=topicDiv.find(".ATSTPopupList");
        topicDiv.find(".ATTTSubTopic").each(function(){
            optList.find("#ATSTPSTCBox_"+$(this).data("brdId")).find("input").prop("checked",true);
        });        
    }    
    $(".confirmATSubTopics").live('click',function(){
        var topicDiv=$(this).closest(".ATTTTopic");
        var stList=$(this).closest(".ATSTPopup");
        var sts=makeHTMLTag('div');
        stList.find(".ATSTPSTCBox:checked").each(function(){
            var stPa=ATTTSubTopicSample.clone(true);
            assignBrdIdAndName($(this),stPa);
            $(this).find(".ATSTPSubSubTopic:checked").each(function(){                
                var sstPa=ATTTSubSubTopicSample.clone(true);
                assignBrdIdAndName($(this),sstPa);
                stPa.find(".ATTTSubSubTopicsDiv").append(sstPa);
            });
            sts.append(stPa.children());
        });
        topicDiv.find(".ATTTSubTopicsDiv").html(sts.children());
        insideOutClick();
    });



  //remove tags
    $(".ATTTRemoveSubject").live('click',function(){
        var ATTHolder=$(this).closest(".ATHolder").siblings(".ATTHolder");
        ATTHolder.find(".ATTagTreeDiv").html("");
        ATTHolder.find(".ATSelectOptionsDiv").html("<div class='ATSelectOptList'></div>");        
    });
    $(".ATTTRemoveTag").live('click',function(){
        $(this).closest(".ATTTItem").remove();
    }); 
    
    
    //checkers
    $(".ATSTPSTCBox,.ATSTPSTName,.ATSTPSSTName,.ATCRBoxName").live('click',function(){
        var cBox=$(this).siblings("input[type=checkbox]");
        if(cBox.prop("checked")){
            cBox.parent().find("input").prop("checked",false);
        }
        else cBox.parent().find("input").prop("checked",true);
    });    
    $(".ATSTPSSTCBox").live('click',function(){
        $(this).closest(".ATSTPSubTopicDiv")
        .find(".ATSTPSTCBox").prop("checked",true);
    });            
    function getAddTagsBtns(confirmClass,cancelClass,tagType){
        var btns=makeHTMLTag("div")
        .html("<div class='addTagsBtns'><a class='liner margRight5 confirmClass doCStream "+confirmClass+"'\n\
	data-tags-type='"+tagType+"'>Done</a>\n\
        <a class='ASTRedColor liner cancelClass doCStream'>Cancel</a></div>"+divi);
        if(cancelClass)btns.find(".cancelClass").addClass(cancelClass);
        else btns.find(".cancelClass").attr("onClick","insideOutClick()");
        return btns.children();
    }
  function getATTicker(input){
        var ticker=makeHTMLTag('span',{"class":'ATCRBoxTick'});
        ticker.data("brdId",input.data("brdId")).data("name",input.data("name"));
        ticker.insertBefore(input);
        input.addClass("nonner ATCRBoxTicked");
  }
  function removeATTickers(optsDiv){
        optsDiv.find(".ATCRBox").prop("checked",false);
        optsDiv.find(".ATCRBoxTick").remove();
        optsDiv.find(".ATCRBox").removeClass("nonner ATCRBoxTicked hider");      
  }
  function assignBrdIdAndName($this,target){
        target.find(".ATTTItemName").text($this.data("name"));
        target.find(".ATTTItem").data("brdId",$this.data("brdId"))
                .data("name",$this.data("name"));      
  }
    function returnAllTagsAdded(addTagsMasterDiv){
        var targetIds=[],brdIds=[],subjectIds=[],normalTags=[],topicIds=[],
        subjects=[],topics=[],exams=[],subTopics=[],subTopicIds=[];
        addTagsMasterDiv.find(".ATTTSubject,.ATTTTopic,.ATTTSubTopic,.ATTTSubSubTopic").each(function(){
           brdIds.push($(this).data("brdId"));
           if($(this).hasClass("ATTTSubject")){
               subjectIds.push($(this).data("brdId"));
               subjects.push($(this).find(".ATTTItemName").text());               
           }
           else if($(this).hasClass("ATTTTopic")){
               topicIds.push($(this).data("brdId"));
               topics.push($(this).find(".ATTTTopicName").text());
           }
           else if($(this).hasClass("ATTTSubTopic")){
                subTopicIds.push($(this).data("brdId"));
                subTopics.push($(this).find(".ATTTSubTopicName").text());
           }
        });
        addTagsMasterDiv.find(".ATTTExam").each(function(){
           targetIds.push($(this).data("brdId"));
           exams.push($(this).find(".ATTTExamName").text());
        });
        normalTags=getNormalTags(addTagsMasterDiv);
        return {targetIds:targetIds,brdIds:brdIds,subjectIds:subjectIds,
            topicIds:topicIds,normalTags:normalTags,subjects:subjects,
            topics:topics,exams:exams,subTopics:subTopics,subTopicIds:subTopicIds};         
    }
    openTogglerCallback["showTagTrees"]=function(selectHead){
        var tagsHolder=selectHead.closest(".addTagsWrapper");
        tagsHolder.find(".ATTagTreeDiv").removeClass("nonner");
    };   
