var qrBoards=new(function($){
    var coursesPage,coursesSection,coursesPageTopics,instituteTopicTree,publicTopicTree,
    bodyClickEvent="click.qrBoards",instParentCourseId,clickEvent="click";
    this.init=function(params){
        coursesPage=$("#coursesPage");
        coursesSection=coursesPage.children("#coursesPageCourses");
        coursesPageTopics=coursesPage.children("#coursesPageTopics");
        coursesPage.off(clickEvent)
        .on(clickEvent,".editCourse",editCourse)
        .on(clickEvent,".backToCourses",backToCourses)
        .on(clickEvent,".publicTopicgCBox",publicTopicgCBox)
        .on(clickEvent,".openSubTopics",openSubTopics)
        .on(clickEvent,".addSelectedBrds",addSelectedBrds)
        .on(clickEvent,".addTopicToInstTree",addTopicToInstTree)
        .on(clickEvent,".topicTreeSubmit",topicTreeSubmit)
        
        $("body").off(bodyClickEvent)
        .on(bodyClickEvent,".renameBrdItemSubmit",renameBrdItemSubmit)
        .on(bodyClickEvent,".addSubTopicSubmit",addSubTopicSubmit)
        .on(bodyClickEvent,".addTopicToInstTreeSubmit",addTopicToInstTreeSubmit)
        
        initESHolder();
    };
    var postReq=vReq.post;
    var getReq=vReq.get;    
    var courseVar="COURSE";
    var topicVar="TOPIC";
    var subTopicVar="SUBTOPIC";
    
    var editCourse=function(){
        var $this=$(this);
        coursesSection.addClass("nonner");
        coursesPageTopics.removeClass("nonner");
        startLoader();
        var successFn=function(data){
            coursesPageTopics.html(data);
            instituteTopicTree=$("#instituteTopicTree");
            publicTopicTree=$("#publicTopicTree");          
            instParentCourseId=$courseData.id;
            setStateOfBoards(instituteTopicTree,publicTopicTree);
            stopLoader();
        };
        var $courseData=$this.closest(".courseItem").data();        
        var params={
            courseName:$courseData.name,
            instParentId:$courseData.id,
            publicParentId:$courseData.publicBrdId,
            type: topicVar
        };
        getReq("/qrboards/topicTree",params,successFn);
    };
    var backToCourses=function(){
        coursesSection.removeClass("nonner");
        coursesPageTopics.addClass("nonner");
    };    
    
    
    var publicTopicgCBox=function(){
        var topicTreeItem=$(this).closest(".topicTreeTopicItem");        
        var publicTargetForSubTopics=topicTreeItem.siblings(".topicTreeTopicSubTopics");
        var isChecked=!($(this).hasClass("gCBoxChecked"));        
        var subTopicCboxes=publicTargetForSubTopics.find(".gCBox");
        if(publicTargetForSubTopics.html()!=""){
            if(isChecked){
                subTopicCboxes.addClass("gCBoxChecked");
            }else{
                subTopicCboxes.removeClass("gCBoxChecked");
            }
        }else{
            var checkSubTopics=function(){                
                publicTargetForSubTopics.find(".gCBox").addClass("gCBoxChecked");
            };
            var publicBrdId=topicTreeItem.data("brdId");
            loadPublicSubTopics(publicTargetForSubTopics,publicBrdId,checkSubTopics);            
        }
    };    
    var openSubTopics=function(){
        var topicTreeItem=$(this).closest(".topicTreeTopic").find(".topicTreeTopicItem");
        var $topicTreeItemData=topicTreeItem.data();
        var publicBrdId=$topicTreeItemData.publicBrdId;
        var subTopicTargetClass=".topicTreeTopicSubTopics";
        var publicTarget,instTarget;
        var instBrdId;
        if(publicBrdId){//req made for subtopics from institute tree
            instTarget=topicTreeItem.siblings(subTopicTargetClass);
            publicTarget=publicTopicTree.find(".publicBrd_"+publicBrdId)
                    .siblings(subTopicTargetClass);
            instBrdId=$topicTreeItemData.id;
        }else{
            publicBrdId=$topicTreeItemData.id; 
            var instTopicTreeItem=instituteTopicTree.find(".brdFrom_"+publicBrdId);
            instTarget=instTopicTreeItem.siblings(subTopicTargetClass);                                
            instBrdId=instTopicTreeItem.data("brdId");
            publicTarget=topicTreeItem.siblings(subTopicTargetClass);
        }   
        
        //first request made for inst subtopics and then to public subtopics
        if(instTarget.length>0&&instTarget.html()==""){
            inlineLoader(instTarget);
            inlineLoader(publicTarget);            
            var params={
                parentId:instBrdId,
                type: subTopicVar,
                start:0,
                size: 500,
                treeType:"INSTITUTE"
            };            
            getReq("/qrboards/subTopics",params,function(data){
                instTarget.html(data);
                var graySubTopics=function(){
                    setStateOfBoards(instTarget,publicTarget);
                };
                loadPublicSubTopics(publicTarget,publicBrdId,graySubTopics);                
            });        
        }else if(publicTarget.length>0&&publicTarget.html()==""){
            inlineLoader(publicTarget);
            loadPublicSubTopics(publicTarget,publicBrdId);
        }        
    };
         
    var addSelectedBrds=function(){
        var grayedTopicTrees=publicTopicTree.
                find(".topicTreeGrayState");
        
        for(var p=0;p<grayedTopicTrees.length>0;p++){
            publicTopicTreeTopic=grayedTopicTrees.eq(p);
            var usableClonedTree=publicTopicTreeTopic.clone(true)
            var publicBrdId=usableClonedTree.find(".topicTreeTopicItem").data("brdId");
            setInstSubTopicClasses(usableClonedTree.find(".topicTreeSubTopicItem"));
            var targetSubTopicListDiv=instituteTopicTree.find(".brdFrom_"+publicBrdId)
                    .siblings(".topicTreeTopicSubTopics");
            targetSubTopicListDiv.append(usableClonedTree.find(".topicTreeSubTopicItem"));
            setStateOfBoards(targetSubTopicListDiv,publicTopicTreeTopic
                    .find(".topicTreeTopicSubTopics"));
        }
        
        
        
        var selectedTopiccCBoxes=publicTopicTree.find(".topicTreeTopicItem .gCBoxChecked");
        var publicTopicTreeTopic;
        for(var k=0;k<selectedTopiccCBoxes.length;k++){
            publicTopicTreeTopic=selectedTopiccCBoxes.eq(k).closest(".topicTreeTopic");
            var topicTree=publicTopicTreeTopic.clone(true);
            setInstTopicClasses(topicTree.find(".topicTreeTopicItem"));            
            setInstSubTopicClasses(topicTree.find(".topicTreeSubTopicItem"));
            instituteTopicTree.children(".topicTreeList").append(topicTree);
            setStateOfBoards(topicTree,publicTopicTreeTopic);
        }        
    };
    var setInstTopicClasses=function(topics){        
        setInstBrdItemsClasses(topics,"Topic");
    };
    var setInstSubTopicClasses=function(subTopics){
        setInstBrdItemsClasses(subTopics,"SubTopic");     
    };    
    var setInstBrdItemsClasses=function(brdItems,brdType){        
        for(var k=0;k<brdItems.length;k++){
            var brdItem=brdItems.eq(k);
            var brdClass="topicTree"+brdType+"Item";
            if(brdItem.find(".gCBoxChecked").length>0){
                var publicBrdId=brdItem.data("brdId");
                brdItem.removeClass().addClass("instBrdItem \n\
                unsavedBrdItem brdFrom_"+publicBrdId+" "+brdClass);                
                var publicBrdId=brdItem.data("brdId");
                brdItem.data({publicBrdId:publicBrdId,brdId:""});
                brdItem.find(".gCBox").remove();
                brdItem.append(editBrdItemvChooseSample.children().clone(true));
                if(brdType==="SubTopic"){
                    brdItem.find(".vchooseOptAddSubTopic").remove();
                }
            }else{
                brdItem.remove();
            }   
        }        
    }; 
    
    //utils
    var loadPublicSubTopics=function(publicTarget,publicBrdId,cbfn){
        var params={
            parentId:publicBrdId,
            type: subTopicVar,
            start:0,
            size: 500,
            treeType:"PUBLIC"
        }; 
        getReq("/qrboards/subTopics",params,function(newData){
            publicTarget.html(newData);
            if(cbfn){
                cbfn();
            }
        });            
    };                
    var setStateOfBoards=function(sourceDiv,targetDiv){
        var instBrds=sourceDiv.find(".instBrdItem");
        for(var k=0;k<instBrds.length;k++){
            var publicBrdId=instBrds.eq(k).data("publicBrdId");
            var publicBrdItem=targetDiv.find(".publicBrd_"+publicBrdId); 
            if(publicBrdItem.length>0){
                publicBrdItem.find(".gCBox").removeClass().addClass("topicTreeCBox");
                publicBrdItem.addClass("grayState");
                publicBrdItem.closest(".topicTreeTopic").addClass("topicTreeGrayState");
            }
        }
    };
    
    
    
    var addBrdsReqPojo=[];
    var topicTreeSubmit=function(){
        addBrdsReqPojo=[];        
        var topicTrees=instituteTopicTree.find(".topicTreeTopic");
        for(var k=0;k<topicTrees.length;k++){
            var topicTree=topicTrees.eq(k);
            if(topicTree.hasClass("savedTopicTree")&&
                topicTree.find(".unsavedBrdItem").length==0){
                    continue;
                }
            var topicItem=topicTree.find(".topicTreeTopicItem");
            var updateChilds=false;
            if(topicItem.data("brdId")){
                updateChilds=true;
            }
            var topicName=topicItem.find(".brdItemName").text().trim();
            var subTopicItems=topicTree.find(".topicTreeSubTopicItem.unsavedBrdItem");
            var childNames=[],childPublicIds=[],extraChilds=[];
            for(var l=0;l<subTopicItems.length;l++){
                var subTopic=subTopicItems.eq(l);
                var publicBrdId=subTopic.data("publicBrdId");
                var subTopicName=subTopic.find(".brdItemName").text().trim();
                if(publicBrdId){
                    childNames.push(subTopicName);                
                    childPublicIds.push(publicBrdId);                    
                }else{
                    extraChilds.push(subTopicName);
                }
            }
            var reqPojo={
                type:topicVar,
                parentId:instParentCourseId,
                name:topicName,
                code:topicName,
                publicBrdId:topicItem.data("publicBrdId"),
                childType:subTopicVar,
                childNames:childNames,
                childPublicIds:childPublicIds,
                updateChilds:updateChilds,
                extraChilds:extraChilds
            };
            addBrdsReqPojo.push(reqPojo);
        }    
        addBoardFn();
    };
    var addBoardFn=function(){
        var params={};
        if(addBrdsReqPojo.length>0){
            startLoader();
            params=addBrdsReqPojo.pop();
            postReq("/qrboards/addBoard",params,addBoardFn);            
        }else{
            stopLoader();
            refreshPage();
        }
    };      
    
    
    
    
    
    
    //for course type and search
    var initESHolder=function(){
        var ESHolder=coursesSection.find(".entrySuggHolder");
        var esParams={
          urlStr:"/qrboards/getPublicBoards",
          query:"query",
          placeHolder:ESHolder.find(".entrySuggInput").attr("placeHolder"),
          params:{"type":"COURSE","start":0,"size":25},
          target:"CMDS_COURSES",
          onQueryResult:onQueryResult,
          onSuggSubmit:onSuggSubmit
        };
        ESHolder.data(esParams);        
    };
    var onQueryResult=function(data){
        var courses=data.result.boards;
        var holder=makeHTMLTag("div");
        for(var k=0;k<courses.length;k++){
            var espTile=makeHTMLTag("div",{"class":"ESPTile"});
            var course=courses[k];
            var name=course.name,brdId=course.id;
            espTile.text(name).data({entityId:brdId,publicBrdId:brdId});
            holder.append(espTile);
        }
        if(courses.length==0){
            holder.html("<div class='userMessage'>No courses found.</div>");
        }
        return holder.children();
    };
    var onSuggSubmit=function(selectedTile,ESHolder){
        var name=selectedTile.text();
        var publicBrdId=selectedTile.data("publicBrdId");
        startLoader();
        var successFn=function(data){
            stopLoader();
            var brdId=data.result.id;
            var targetDiv=coursesSection.find(".addedCoursesList");
            var courseItem=makeHTMLTag("div",{"class":"courseItem"});
            courseItem.html("<span class='courseItemName'>"+name+"</span>\n\
                <span class='editCourse'>View/Edit Topic Tree</span>").
                    data({brdId:brdId,name:name,publicBrdId:publicBrdId});
            if(targetDiv.find(".courseItem").length>0){
                targetDiv.prepend(courseItem);
            }else{
                targetDiv.html(courseItem);
            }
        };
        var params={
            brdId:publicBrdId,
            name:name,
            code:name,
            type:courseVar
        };
        postReq("/qrboards/addBoard",params,successFn);
    };
    
    var brdItemForEditing;
    this.editBrdItem={
        RENAME:function(){
            var popup=fillcmdsPopup("renameBrdItemSubmit","renameBrdItemSample");
            var name=brdItemForEditing.find(".brdItemName").text();
            popup.find(".formInput").val(name);
        },
        REMOVE:function(){
            if(brdItemForEditing.hasClass("topicTreeTopicItem")){
                unsetTopicTree();
            }else{
                unsetSubTopicTree();
            }
        },
        ADD_SUBTOPIC:function(){
            //using same sample as for renaming
            var popup=fillcmdsPopup("addSubTopicSubmit","renameBrdItemSample");
            var topicName=brdItemForEditing.find(".brdItemName").text();
            popup.find(".cmdsPopupHead").text("Add a Sub Topic to "+topicName);
        },  
        setBrdItem:function(vChoose){
            brdItemForEditing=vChoose.closest(".topicTreeTopicItem,.topicTreeSubTopicItem");
        }
    };
    var unsetTopicTree=function(){
        var publicBrdId=brdItemForEditing.data("publicBrdId");
        brdItemForEditing.closest(".topicTreeTopic").remove();
        var topicItem=publicTopicTree.find(".publicBrd_"+publicBrdId);
        unsetBrdItem(topicItem,"publicTopicgCBox");
        topicItem.closest(".topicTreeTopic").removeClass("topicTreeGrayState");
        var subTopicItems=topicItem.siblings(".topicTreeTopicSubTopics")
                .children(".topicTreeSubTopicItem");
        for(var k=0;k<subTopicItems.length;k++){
            unsetBrdItem(subTopicItems.eq(k),"");
        }        
    };
    var unsetSubTopicTree=function(){
        var publicBrdId=brdItemForEditing.data("publicBrdId");
        brdItemForEditing.remove();   
        var subTopicItem=publicTopicTree.find(".publicBrd_"+publicBrdId);
        unsetBrdItem(subTopicItem,"");
    };
    var unsetBrdItem=function(brdItem,extraClasses){
        brdItem.find(".topicTreeCBox").removeClass().addClass("gCBox "+extraClasses);
        brdItem.removeClass("grayState");        
    };
    var renameBrdItemSubmit=function(){        
        var params=getFormValues(getPopupDiv($(this)));
        if(params.hasError){
            return;
        }
        brdItemForEditing.find(".brdItemName").text(params.name);
        if(brdItemForEditing.hasClass("savedBrdItem")){
            params.id=brdItemForEditing.data("brdId");
            startLoader();
            var successFn=function(data){
                stopLoader();
                closePopup();
            }
            postReq("/qrboards/editBoard",params,successFn);
        }else{
            closePopup();
        }
    };
    var addSubTopicSubmit=function(){
        addTopicSubTopicUtil($(this),"topicTreeSubTopicSample",
        brdItemForEditing.siblings(".topicTreeTopicSubTopics"));
    };
    var addTopicToInstTree=function(){
        //using renameBrdItemSample sample for creating topic
        var popup=fillcmdsPopup("addTopicToInstTreeSubmit","renameBrdItemSample");
        var courseName=$(this).data(".courseName");
        popup.find(".cmdsPopupHead").text("Add a Topic to "+courseName);        
    };
    var addTopicToInstTreeSubmit=function(){
        addTopicSubTopicUtil($(this),"topicTreeTopicSample",
        instituteTopicTree.find(".topicTreeList"));
    };
    var addTopicSubTopicUtil=function($this,cloneVar,target){
        var params=getFormValues(getPopupDiv($this));
        if(params.hasError){
            return;
        }
        var brdItem=window[cloneVar].children().clone(true);
        brdItem.find(".brdItemName").text(params.name);
        target.append(brdItem);
        closePopup();        
    };
})(jQuery);
var editBrdItem=function(vChoose,value){
    vChooseVar.reset(vChoose,"-1","<div class='brdItemEdit'></div>");
    qrBoards.editBrdItem.setBrdItem(vChoose);
    qrBoards.editBrdItem[value](vChoose);
};