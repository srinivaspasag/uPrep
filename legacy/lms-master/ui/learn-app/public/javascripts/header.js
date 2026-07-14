$(document).ready(function(){
    instHeader.init();
    institute.init();
})

var institute = new function($){
    var parDivId = "#instituteHome";
    var params = {'tabType':"MY_INSTITUTE","year":new Date().getFullYear()};
    function notify(message, type){
        $.growl({
            message: message
        },{
            type: type,
            allow_dismiss: false,
            label: 'Cancel',
            className: 'btn-xs btn-inverse',
            placement: {
                from: 'bottom',
                align: 'left'
            },
            delay: 2500,
            animate: {
                enter: 'animated fadeInUp',
                exit: 'animated fadeOutDown'
            },
            offset: {
                x: 30,
                y: 30
            }
        });
    };
    function stopWelcomeMessage(){
        $.get("/Application/stopWelcomeMessage");
    }
    this.init = function(homePgVal){
        setTimeout(function () {
            if ($("#showWelcomeMessage").val()=="true") {
                notify('Welcome back '+ $('.sp-info').text(), 'inverse');
                $("#showWelcomeMessage").val("false");
                stopWelcomeMessage();
            }
        }, 1000);
        params['contentSrc.type']="ORGANIZATION";
        params['contentSrc.id'] = $("#myInstitutePage").data('orgId');
        if($(parDivId).data("inited")) return false;
        $(".instNavTab,.examsDropdown .exams").on("click",function(){
            $('body').removeClass('sidebar-toggled');
            $('.ma-backdrop').remove();
            $('.sidebar, .ma-trigger').removeClass('toggled');
        });

        $(".instPageNavigator").on("click",".instNavTab",this.headerTabClick);
        $(".instituteHeader")
                 .on("click",".instProfilePage",this.instProfilePage)
                 .on("click",".instReferHolder",this.getRecentPage)
                 .on("click",".instOpenAnalytics",this.openResultAnalytics)
                 .on("click",".instOpenLibraryPage",this.openLibrary)
                 .on("click",".instDoubtsForum",this.doubts.open)
                 .on("click",".instRATab",this.activities.open)
                 .on("click",".openInstAvailPrograms",this.openInstAvailPrograms)
                 .on("click",".openInstSettings",this.openPopup)
                 .on("click",".backToOldUi",backToOldUi)
                 .on("click",".openInstiTestPage",instTestClicked)
                 .on("click",".exams",examsPage)
                 .on("click",".openMyAccessCodePopup",this.openMyAccessCodePopup);
        $(parDivId).on("click",".instAttachLinkPrev",attachThumbPrev)
                   .on("click",".instAttachLinkNext",attachThumbNext);
        };

    this.goToUrl = function(url,append,params){
        openInstSubPage(url,append,params);
    }
    var backToOldUi = function(){
        var url = $(this).data("url");
        window.location = url;
    }

    var examsPage = function(){
        url = $(this).data('examName');
        openInstSubPage("/Institute/"+url,""+url,params,refreshSelectPicker);
        $(".instPageNavigator").find(".active").removeClass("active");
        $(".instPageNavigator").find(".instExamTab").closest("li").addClass("active");
        // $(".instPageNavigator").removeClass("toggled");
    }

    this.aiims = function(){
        openInstSubPage("/Institute/aiims","aiims",params,refreshSelectPicker);
    }

    this.jeemain = function(){
        openInstSubPage("/Institute/jeemain","jeemain",params,refreshSelectPicker);
    }

    this.jeeadvanced = function(){
        openInstSubPage("/Institute/jeeadvanced","jeeadvanced",params,refreshSelectPicker);
    }

    this.neet = function(){
        openInstSubPage("/Institute/neet","neet",params,refreshSelectPicker);
    }

    this.bitsat = function(){
        openInstSubPage("/Institute/bitsat","bitsat",params,refreshSelectPicker);
    }

    function instTestClicked(e){
        var testId = $(this).data("testId");
        var testGroupId = $(this).data("testGroup");
        var targetUserId = $(this).data("targetUserId");
        var otherParams;
        if(targetUserId){
            otherParams = {"targetUserId":targetUserId};
        }
        if(testGroupId){
                institute.openTestPage(testGroupId,otherParams);
        }else if(testId){
            institute.openTestPage(testId,otherParams);
        }else{
            swal({
                    title:"Cannot open Test",
                    type:"warning"
                });
        }
        if(e){
        e.preventDefault();
        }
    };
    var urlQueryHelper = new function(){
    var url;
    var readAll = this.readAll = function(){
        var params = getAllUrlParams();
        return params;
    };
    function getURLParameter(name){
    var param="";
    try{
    var url = location.search;
    if(!url){ return param;}
    url = url.replace("?","");
    name += "=";
    var nameIndex = url.indexOf(name);
    if(nameIndex>=0){
        param = url.substr(nameIndex+name.length);
        if((eIndex = param.indexOf("&"))>0){
            param = param.substring(0,eIndex);
        }
        param = decodeURIComponent(param);
    }
    }
     catch(err){ param="";}
     return param;
}
    this.getQueryString = function(){
        var string = location.href;
        string = string.split("?")[1];
        string = string ? "?"+string : "";
        return string;
    };
    this.read = function(name){
        return getURLParameter(name);
    };
    this.push = function(name,val){
        writeUrl(name,val);
    };
    this.replace = function(name,val){
        writeUrl(name,val,true);
    };
    var writeUrl = function(name,val,isReplace){
        val = val?encodeURIComponent(val):val;
        var loc = location.pathname + location.search;
        var newLoc = loc;
        if(loc.contains(name)){
            newLoc = loc.substring(0,loc.indexOf("?"));
            var params = readAll();
            if(val){
                params[name] = val;
            }else{
                $(params).removeProp(name);
            }
            newLoc += "?";
            for(p in params){
                newLoc += p+"="+params[p]+"&";
            }
            newLoc = newLoc.substr(0,newLoc.length-1);
        }else if(val){
            var joinChar = newLoc.contains("?") ? "&" : "?";
            newLoc += joinChar + name + "=" + val;
        }
        if(isReplace){
            pushHistory(null,null,newLoc,true);
        }else{
            pushHistory(null,null,newLoc);
        }
    };
};
    this.readUrlForProgInfo = function(progInfo){
        var data = urlQueryHelper.readAll();
        progInfo = $.extend({"progId":"","center":"","section":""},progInfo);
        if(!data.program){
            nDropDown.reset($(".instSelMyProgram"));
        }else{
           var obj = nDropDown.searchAndUpdate($(".instSelMyProgram"),data.program,true);
           if(obj){
            progInfo["progId"] = obj.value;
           }
        }
        if(!data.center){
            nDropDown.reset($(".instSelMyCenter"));
        }else{
           var obj = nDropDown.searchAndUpdate($(".instSelMyCenter"),data.center,true);
           if(obj){
            progInfo["center"] = obj.value;
           }
        }
        if(!data.section){
            nDropDown.reset($(".instSelMySection"));
        }else{
           var obj = nDropDown.searchAndUpdate($(".instSelMySection"),data.section,true);
           if(obj){
            progInfo["section"] = obj.value;
           }
        }
        return progInfo;
    };
    var nDropDown = new function(){
    var parDivId = document;
    this.init = function(){
        $(parDivId).on("click",".nDropDown .dropDownHead",openOrClose);
        $(parDivId).on("click",".nDropDown .eachDropedElement",selected);
    };
    var closeAll = function(){
        $(parDivId).off("click",closeAll);
        $(parDivId).find(".nDropDown").each(function(){
            $(this).find(".dropDownContainer").addClass("nonner");
        });
    };
    var show = function($this){
        $this.find(".dropDownContainer").removeClass("nonner");
        $(parDivId).on("click",closeAll);
    };
    var hide = function($this){
        $(parDivId).off("click",closeAll);
        $this.find(".dropDownContainer").addClass("nonner");
    };
    var openOrClose = function(){
        var head = $(this);
        var $this = $(this).closest(".nDropDown");
        if(!$this.find(".dropDownContainer").hasClass("nonner")){
            hide($this);
        }else{
            show($this);
        }
    };
    var selected = function(){
        var $this= $(this);
        var par = $this.closest(".nDropDown");
        var value = $this.data("value");
        value = value ? value : "";
        par.data("value",value);
        par.data("itemValue",{});
        par.data("itemValue",$this.data("itemValue"));
        var text = $.trim($this.data("text"));
        text = text?text:$this.text().trim();
        var appendText = $this.closest(".eachDroperElemHolder").data("appendText");
        appendText = appendText?$.trim(appendText)+" ":"";
        text = appendText+text;
        par.find(".textCont").text(text);
        hide(par);
        par.trigger("change");
    };
    var newElement = function(container,text,value,item){
        var el = document.createElement("div");
        $(el).addClass("eachDropedElement")
            .data("value",value)
            .data("itemValue",item)
            .text(text);
        $(container).append(el);
        return $(el);
    };
    var clearAll = function(container){
        container.find(".dropElemPackage,.eachDropedElement").remove();
    }
    var reset = this.reset = function(holders){
        var drop;
        $(holders).each(function(index,holder){
            drop = $(holder).find(".nDropDown");
            var container = drop.find(".dropDownContainer");
            var item = container.find(".eachDropedElement:first");
            var val = item.data("value") ? item.data("value") : "";
            drop.data("value",val)
                .data("itemValue",item.data("itemValue"));
            var text = item.data("text");
            text = text?text:item.text();
            drop.find(".textCont").text(text);
        });
        return drop;
    };
    this.redraw = function(holder,allowSelectAll,list,listType,append){
        var drop = $(holder).find(".nDropDown");
        var container = drop.find(".dropDownContainer");
        if(!append){
            clearAll(container);
        }
        if(allowSelectAll){
            newElement(container,"All").data("text","All "+listType);   
        }
        if(list && list.length>0){
            $(list).each(function(index,item){
                newElement(container,item.name,item.id,item);
            });
        }
        reset(holder);
        return true;
    };
    var searchByVal = function(holder,val){
        var drop = $(holder).find(".nDropDown");
        var container = drop.find(".dropDownContainer");
        var retObj;
        container.find(".eachDropedElement").each(function(index){
            var $this = $(this);
            var v = $this.data("value");
            if(v == val){
                retObj = {index:index,text:$this.text(),value:v,data:cloneObject($this.data())};
            }
        });
        return retObj;
    };
    this.searchAndUpdate = function(holder,val,doTrigger){
        var obj = searchByVal(holder,val);
        if(obj){
            var drop = $(holder).find(".nDropDown");
            for(d in obj.data){
                drop.data(d,obj.data[d]);
            }
            drop.data("value",obj.value);
            drop.find(".dropDownHead .textCont").text(obj.text);
            if(doTrigger){
                drop.trigger("change");
            }
        }
        return obj;
    };
};
    var attachThumbPrev = function(){
        moveThumbNextPrev($(this),-1);
    };
    var animate = this.animate = function(msg,thisOnly){
        $(msg).animate({"left":"0px"},200,"linear",function(){
            if(!thisOnly){
                animate($(this).next());
            }
        });
    };
    var toJSON = function(data){
        if(typeof data=="string"){
            return JSON.parse(data);
        }else{
            data = cloneObject(data);
        }
        return data;
    };
    function cloneObject(inObj){
        var outObj = JSON.stringify(inObj);
        outObj = JSON.parse(outObj);
        return outObj;
    };
    var homeImgLoaded = function(){
        $(this).fadeTo(120,1);
        $(this).closest(".instProfilePicContainer").addClass("imgLoaded");
    };
    var homeImgError = function(){
        this.error = true;
    };
    var attachThumbNext = function(){
        moveThumbNextPrev($(this),1);
    };
    var showHomeImgs =  function(picId){
        showImgsFwk(picId,homeImgLoaded,homeImgError);
    };
    var showImgsFwk = function(picId,successFn,errorFn){
        var pics = $(parDivId).find(picId)
            .load(successFn)
            .error(errorFn);
        setTimeout(function(){
            pics.each(function(){
                if(this.complete && !this.error){
                    $(this).load();
                }
            });
        },1000);
    };
    var moveThumbNextPrev = function($this,addBy){
        if(!$this.hasClass("active")) return;
        var holder = $this.closest(".instThumbNextPrevHold");
        var par = $this.closest(".instLinkPreview");
        var index = holder.data("count")+addBy;
        var total = holder.data("total");
        if(index < total-1){
            holder.find(".instAttachLinkNext").addClass("active");      
        }else{
            holder.find(".instAttachLinkNext").removeClass("active");   
        }
        if(index>0){
            holder.find(".instAttachLinkPrev").addClass("active");      
        }else{
            holder.find(".instAttachLinkPrev").removeClass("active");       
        }
        var width = par.find(".instAttachLinkImgContainer").width()+3;
        var left = index *(-1)*width;
        par.find(".instAttachLinkAllImgs").animate({"left":left+"px"});
        holder.data("count",index);
        holder.find(".instAttachThumbCount").find("#curCount").text(index+1);
        holder.closest(".thumbnailCell").data("index",index);
    };

    this.openPopup = function(extParams){
        var orgId = $("#myInstitutePage").data("orgId");
        var pr = {"orgId":orgId,"closePopupClass":"crossVPopup"};
        $.get("/UserSettings/openSettings",pr,function(data){
            swal({
                html: data,
                showConfirmButton:false,
                customClass:"settingsPopup"
            }).catch(swal.noop);
            userSettings.init(orgId);
        });
    };

    var deleteItem = function(parentDiv,url,$this,cbFn){
            swal({
              title: 'Are you sure?',
              text: "Deleted post will not be shown again",
              type: 'warning',
              showCancelButton: true,
              confirmButtonColor: '#3085d6',
              cancelButtonColor: '#d33',
              confirmButtonText: 'Yes, delete it!',
              customClass:"activityPopup"
            }).then(function () {
                var pr = {id:$this.data("id")};
                var parentFeed = $this.closest(parentDiv);
                vReq.get(url,pr,function(data){
                    var deleted = false;
                    if(data && data.errorCode=="" && data.result.deleted == true){
                        parentFeed.remove();
                        deleted = true;
                        swal({
                            title:"Successfully deleted",
                            type:"success",
                        });
                        $(".feedPostSuccessMsg").html("");
                    }else{
                        swal({
                            title:"Something went wrong",
                            type:"warning"
                        });
                    }
                    try{
                        if(cbFn){
                            cbFn(deleted,data);
                        }
                    }catch(err){console.error(err);}
                        });
            });
        };

    function showLoader() {
        $("#topLoader").removeClass("nonner");
    }
    function hideLoader() {
        $("#topLoader").addClass("nonner");
    }

    var openInstSubPage = function(url,history,params,callBack,childHolderId){
        showLoader();
        // beforePageOpen();
        params["newPageOpen"] = pushInHistory(history);
        // params["newPageOpen"] = "/library";
        $.get(url,params,function(data){
            hideLoader();
            if(childHolderId){
                $(parDivId).find(childHolderId).html(data);
            }else{
                $(parDivId).html(data);
            }
            if(callBack){
                try{
                    callBack(data);
                }catch(err){
                    putConsoleError(err);
                }
            }
            instHeader.getCount();
        });
        window.scrollTo(0,0);
    };

    function getInstPushUrl(orgId,append){
        // console.log("In getInstPushUrl");
    append = append?"/"+append:"";
    var pushUrl = append;
    pushUrl = encodeURI(pushUrl);
    return pushUrl;
    }

    var followUnfollowDoubt = function(){
        var $this = $(this);
        var doubt = $this.closest(".instEachDbt");
        var countDiv = doubt.find(".followersCount .count");
        if($this.hasClass("followEntity")){
            increaseCount(countDiv);
        }
        else{
            decreaseCount(countDiv);
        }
    };

    var followersPopup = function(){
        $this = $(this);
        var defaultSize = 10;
        var entityCount = parseInt($this.data("entityCount"),10);
        if(entityCount<=0){ return;}
        var ent = $this.closest(".instUpvoteHolder");
        var entityType = $this.data("entityType");
        entityType = entityType?entityType:ent.data("entityType");
        var entityId = $this.data("entityId");
        entityId = entityId?entityId:ent.data("entityId");
        if(!entityType || !entityId){ return;}
        var pr = {"start":0,"size":defaultSize};
        pr.orgId = $("#myInstitutePage").data("orgId");
        pr.userRole = $("#myInstitutePage").data("orgObj").userRole;
        pr["entity.id"] = entityId;
        pr["entity.type"] = entityType;
        vReq.get("/widgets/popupEntityFollowers",pr,function(html){
            swal({
                html:html,
                showConfirmButton:false,
                customClass:"followersPopup",
                timer:3000
            });
        $(".followersPopup").find(".swal2-content").prepend("<h2 class='popupHeading'>Followers</h2>");
        });
    }

    function followDoubt(e){
       var $this=$(this),d=$this.data();
       $this.removeClass("followEntity").addClass("unfollowEntity");
       $this.text("Unfollow").addClass("btn btn-danger waves-effect");
       if(followEntity[d.callback])followEntity[d.callback]($(this));
       params["entity.type"] = d.entityType;
       params["entity.id"] = d.entityId;
       vReq.post("/Widgets/followEntity",params,function(data,s,xhr){
       })
    };
    var followEntity={},unfollowEntity={};
    function unfollowDoubt(e){
       var $this=$(this),d=$this.data();
       $this.removeClass("unfollowEntity").addClass("followEntity");
       $this.text("follow").removeClass("btn btn-danger").addClass("btn btn-success");
       if(unfollowEntity[d.callback])unfollowEntity[d.callback]($(this));
       params["entity.type"] = d.entityType;
       params["entity.id"] = d.entityId;
       vReq.post("/Widgets/unfollowEntity",params,function(data,s,xhr){
       })
    };

    pushInHistory = function(append){
        // console.log("In pushInHistory");
        var orgId = $("#myInstitutePage").data("orgId");
        return pushInstHistory(orgId,append);
    };

    function pushInstHistory(orgId,append){
        // console.log("In pushInstHistory");
        var pushUrl = getInstPushUrl(orgId,append);
        pushHistory(null , null,pushUrl);
        // trackPageView();
        return pushUrl;
    }

    function pushHistory(state, title, pathWithSearhParams, doReplace) {
        // console.log("In pushHistory");
        var returnLocation = history.location || document.location;
        var currPath = returnLocation.pathname + returnLocation.search;
        state = state || {};
        state["prevUrl"] = currPath;
        if (pathWithSearhParams !== currPath) {
            if (doReplace) {
                history.replaceState(state, title, pathWithSearhParams);
            } else {
                history.pushState(state, title, pathWithSearhParams);
            }
        }
        history.lastPathName = returnLocation.pathname;
    }

    function refreshSelectPicker(){
        $(".selectpicker").selectpicker({});
        return;
    };

    this.openLibrary = function(){
        openInstSubPage("/Institute/library","library",params,refreshSelectPicker);
    };
    this.openLibraryWithRefresh = function(){
        openInstSubPage("/Institute/library","library",params,refreshSelectPicker);
        window.location.reload();
    };
    this.doubts = new function(){
        var dParams;
        var dbtParId;
        var dbtFacetId;
        var subBrdId;
        this.facetsData;
        this.open = function(e,cbFn,extParams){
            var pr = {};
            $.extend(pr,params,extParams);
            openInstSubPage("/Institute/doubts","discussions",pr,cbFn);
        }
        var resetBrdParams = function(paramsOnly){
            subBrdId = undefined;
            topicBrdId = undefined;
            dbtParId.find(".instDoubtSearchedTopics").html("").removeClass("txtFilled");
            $(dParams).removeProp("brdIds");
            if(!paramsOnly){
               dbtParId.find(".instDoubtSearchBox .searchDivBox")
                .html("<span class='placeHolder'>Search</span>").removeClass("txtFilled");
            }
        };

        var getQueryText = function(){
            var searchDiv = $(parDivId).find(".instDoubtSearchBox");
            var searchBox = searchDiv.find(".searchDivBox");
            var queryTxt = "";
            if(searchBox.hasClass("txtFilled") && !searchBox.find(".placeHolder").get(0)){
                queryTxt = searchBox.text();
            }
            return queryTxt;
        };
        var browseXHR;
        var putFacets = function(facetDiv,facetHolder){
            var rightSec = $(parDivId).find(".instDoubtRightSec");
            rightSec.find(".dbtFacetSubjTopicsHolder").html("").removeClass("anim").height("inherit");
            rightSec.find(".dbtFacetSubject .instImgs").addClass("nonner");
            var facets = $(facetDiv).html();
            facets = facets ? facets : "";
            if(facetHolder){
                facetHolder = rightSec.find(facetHolder);
                facetHolder.html(facets).addClass("anim");
                // facetHolder.height(facetHolder.children().height());//Anim purpose
                facetHolder.closest(".dbtFacetSubject").find(".instImgs").removeClass("nonner");
                rightSec.find(".instDbtPopularTags").remove();
            }else{
                rightSec.html(facets);
            }
            $(facetDiv).remove();
        };
        this.extLoad = function(brdId,brdName,query){
            var brdIds;
            if(brdId){
                putBoardInSearchBox(brdId,brdName);
                brdIds = [brdId];
                topicBrdId = brdId;
            }
            getDoubts(brdIds,query);
            // browse(true,null,query);
        };
        var browse = function(facet,subFacetHolder,query,start){
            showLoader();
            dParams["facet"] = facet;
            dParams["subFacetHolder"] = subFacetHolder?subFacetHolder:"";
            dParams["query"] = query?query:getQueryText();
            if(dParams["query"] == ""){
                if($(".doubtSearchBox").val()!=""){
                    dParams["query"] = $(".doubtSearchBox").val();
                }
            }
            dParams["orgId"] = $("#myInstitutePage").data("orgObj").orgId;
            dParams["userRole"] = $("#myInstitutePage").data("orgObj").userRole;
            var divId = $(parDivId).find(".instDoubtsLists");
            var lastAvailDbt;
            if(start){
                dParams["start"]=start;
                lastAvailDbt = divId.find(".instEachDbt:last");
            }else{
                dParams["start"]=0;
                bigLoader(divId);
            }
            if(browseXHR){ browseXHR.abort(); }
            browseXHR = vReq.get("/Institute/getDoubts",dParams,function(data){
                hideLoader();
                divId.find(".loadMoreDoubts").remove();
                if(dParams["start"]>1){
                    divId.append(data);
                }else{
                    divId.html(data);
                }
                if(!lastAvailDbt){
                    lastAvailDbt = divId.find(".instEachDbt:first");
                }else{
                    lastAvailDbt = lastAvailDbt.next();
                }
                animate(lastAvailDbt,false);
                if(facet){
                    putFacets(divId.find(".instIncomingDbtFacets"),subFacetHolder);
                }
                if(dParams["brdIds"] && dParams["brdIds"][0]){
                    var inBrdId = dParams["brdIds"][0];
                    $(".doubtBrd-"+inBrdId).addClass("instEachTrTopicSelect");
                }
                // showImgs();
                try{
                    if(MathJax && MathJax.Hub){
                        MathJax.Hub.Queue(["Typeset",MathJax.Hub],divId.get(0));
                    }
                }catch(err){}
            });
        };
        var manageTabs = function(){
            var $this = $(this);
            $this.addClass("simpleBlackActiveTab")
                .siblings().removeClass("simpleBlackActiveTab");
            dParams["orderBy"] = $(this).data("orderBy");
            dParams["resultType"] = $(this).data("resultType");
            urlQueryHelper.push("tabIndex",$this.index());
            resetBrdParams();
            browse(true);
        };

        var askDoubt =function(){
            $(".instAddDoubtPopup").removeClass("nonner").addClass("card p-20");
            $(".doubtsFeed").addClass("nonner");
        };
        var getRTEContent = function(rteHolder) {
            var rteArea = rteHolder.clone(true);
            rteArea.find(".MathJax_Preview,.MathJax_Display").remove();
            rteArea.find("script").each(function() {
                var $this = $(this);
                if ($this.attr("id").indexOf("MathJax-Element-") != -1) {
                    var latex = "\\[" + $this.text() + "\\]";
                    if ($this.parent().hasClass("RTELatex")) {
                        $this.parent().html(latex);
                    } else {
                        $("<div class='RTELatex' contenteditable=false>" + latex + "</div>").insertAfter($this);
                        $this.remove();
                    }
                }
            });
            return rteArea.html().trim();
        };
        var postDoubt = function(){
            var quesParams = cloneObject(params);
            var QADiv=$(this).closest(".instAddDoubtPopup");
            quesParams.name = QADiv.find(".instAddDoubtTitle input").val();
            if(quesParams.name.length==0){
                swal({
                    title:"Doubt needs a title",
                    type:'warning'
                });
                return;
            }
            var rteHolder = $(".note-editable");
            quesParams.content = getRTEContent(rteHolder);
            if(quesParams.content.length==0){
                swal({
                    title:"Please explain your doubt further",
                    type:'warning'
                });
                return;
            }
            var tagsJson=returnAllTagsAdded(QADiv.find(".askDoubtHolder"));
            if(tagsJson.brdIds.length<2){
                swal({
                    title:"Please enter atleast one subject and topic",
                    type:"warning"
                });
                return;
            }
            quesParams.brdIds=tagsJson.brdIds;
            quesParams.scope="PUBLIC";
            quesParams.orgId = $("#myInstitutePage").data("orgId");
            QADiv.find("#instPostQues").addClass("nonner");
            showLoader();
            vReq.post("/Institute/addNewDoubt",quesParams,function(data){
                hideLoader();
                var dHolder = $(".instDoubtsLists");
                var fDbt = dHolder.find(".instEachDbt").get(0);
                if(fDbt){
                        $(fDbt).before(data);
                }else{
                    dHolder.html(data);
                }
                var newInstDiss = $(dHolder.find(".instEachDbt").get(0));
                animate(newInstDiss,true);
                $(".doubtsFeed").removeClass("nonner");
                $(".instAddDoubtPopup").addClass("nonner");
                $(".note-editable").html("");
                $(".instAddDoubtTitle input").val("");
                $('#subjectPicker').val('').selectpicker('refresh');
                $('#topicPicker').val('').selectpicker('refresh');
            });
        }
        var cancelPostingDoubt = function(){
            $(".note-editable").html("");
            $(".instAddDoubtPopup").addClass("nonner");
            $(".doubtsFeed").removeClass("nonner");
        }
        var searchDivFocus = function(e){
            switch(e.type){
                case "focusin":
                    if($(this).find(".placeHolder").get(0)){
                        $(this).html("");
                    }
                    // vtooltip.show(e,i18nJS("TYPE_AND_ENTER_TO_SEARCH"));
                    $(this).on("mouseleave",function(){
                        // vtooltip.hide();
                        $(this).off("mouseleave");
                    });
                    break;
                case "focusout":
                    if($(this).text().trim().length == 0){
                        $(this).removeClass("txtFilled");
                        $(this).html("<span class='placeHolder'>"+i18nJS("TXT_SEARCH")+"</span>");
                    }else if(!$(this).find(".placeHolder").get(0)){
                        $(this).addClass("txtFilled");
                    }
                    // vtooltip.hide();
                    break;
            }
        };
        var putTopicBrdIdInParam = function(){
            if(topicBrdId){
                dParams["brdIds"] = [topicBrdId];
            }else{
                $(dParams).removeProp("brdIds");
            }
        };
        var searchTimeoutObj;
        var searchDivKeys = function(e){
            var $this = $(this);
            switch(e.which){
                case 13:
                    var cln = $this.clone();
                    $(cln).find("span").remove();
                    var val = $(cln).text().trim();
                    if(val.length>0){
                        putTopicBrdIdInParam();
                        browse(true,null,val);
                    }else if($this.text().trim().length==0){
                        $(parDivId).find(".instEachTrTopicSelect")
                            .removeClass("instEachTrTopicSelect");
                        resetBrdParams(true);
                        browse(true);
                    }else{
                        putTopicBrdIdInParam();
                        browse(false);
                    }
                    e.preventDefault();
                    break;
                case 8:
                    break;
            }
            if(searchTimeoutObj){ clearTimeout(searchTimeoutObj);}
            searchTimeoutObj = setTimeout(function(){
               var cln = $this.clone();
               $(cln).find("span").remove();
               var val = $(cln).text().trim();
               if(!val && dParams.query){
                resetBrdParams();
                browse(true,null,"");
               }
            },100);
        };

        var loadMore = function(){
            smallLoader($(this));
            browse(false,null,null,dParams["start"]+dParams["size"]);   
        }
        var crossSelTopic = function(){
            var $this = $(this).closest(".instDbtTopicSeld");
            var brdId = $this.data("brdId");
            if(dParams.brdIds && dParams.brdIds.length > 0){
                dParams.brdIds.splice(dParams.brdIds.indexOf(brdId),1);
            }
            if(subBrdId){
                dParams.brdIds.push(subBrdId);
            }
            $(parDivId).find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
            $this.remove();
            browse(false);
        };
        var deleteDoubt = function(){
            deleteItem(".instEachDbt","/Institute/deleteDoubt",$(this));
        };
        var subChanged = function(){
            resetBrdParams(true);
            var $this = $(this);
            if($this.is(":checked")){
                $this.closest(".dbtFacetSub").addClass("selected")
                .closest(".dbtFacetSubject").siblings().find(".dbtFacetSub").removeClass("selected")
                .find(".dbtFacetSubChk").removeAttr("checked");
                var brdId = $this.val();
                var facetHolder;
                if(brdId){
                    dParams["brdIds"] = [brdId];
                    subBrdId = brdId;
                    facetHolder = ".dbtFacetSubId_"+brdId+" .dbtFacetSubjTopicsHolder";
                }else{
                    $(dParams).removeProp("brdIds");
                }
                browse(true,facetHolder);
            }else{
                $this.closest(".dbtFacetSub").removeClass("selected");
                dbtFacetId.find(".dbtFacetAllSub").addClass("selected");
                $(dParams).removeProp("brdIds");
                browse(true);
            }
        };

        var putBoardInSearchBox = function(brdId,brdName){
            var html = "<li class='instDbtTopicSeld m-l-10' data-brd-id='"+brdId+"' contenteditable=false>"+brdName+"&nbsp;&nbsp; <span class='closeDbtTopic'>x</span></li>";
            dbtParId.find(".instDoubtSearchedTopics")
                                .html(html)
                .addClass("txtFilled");
        };

        var topicChanged = function(){
            var $this = $(this);
            if($this.hasClass("instEachTrTopicSelect")) return;

            $(parDivId).find(".instEachTrTopicSelect").removeClass("instEachTrTopicSelect");
            $this.addClass("instEachTrTopicSelect");
            var brdId = $this.data("brdId");
            dParams["brdIds"] = [brdId];
            topicBrdId = brdId;
            putBoardInSearchBox(brdId,$this.text());
            browse(false);
        };

        var toggleSearchDiv = function(){
            var $this = $(this);
            var searchDiv = $(parDivId).find(".instDoubtSearchBox");
            var searchBox = searchDiv.find(".searchDivBox");
            if($this.data("opened")){
                searchDiv.addClass("nonner");
                if(searchBox.hasClass("txtFilled")){
                    searchBox.html("<span class='placeHolder'>"+i18nJS("TXT_SEARCH")+"</span>").removeClass("txtFilled");
                    browse(true);
                }
                $this.data("opened",false);
            }else{
                searchDiv.removeClass("nonner");
                $(".instDoubtSearchBox").addClass("card").css("padding","20px");
                $this.data("opened",true);
                searchBox.focus();
            }
        };
        this.onBack = function(){
            var tabIndex = urlQueryHelper.read("tabIndex");
            if(tabIndex){
                initDParams();
                $(dbtParId.find(".mangeDoubtsTab").get(tabIndex)).trigger("click");
            }else{
                getDoubts();
            }
            return true;
        };
        var getDoubts = function(brdIds,query){
            $(".instFilterDoubts").find(".simpleBlackActiveTab").removeClass("simpleBlackActiveTab");
            $($(".instFilterDoubts").find(".simpleBlackTab").get(0)).addClass("simpleBlackActiveTab");
            initDParams(brdIds,query);
            browse(true);
        };
        var initDParams = function(brdIds,query){
            dParams["start"]=0;
            dParams["size"]=10;
            dParams["sortOrder"] = "DESC";
            dParams["facet"] = true;
            dParams["orderBy"] = "timeCreated";
            dParams["resultType"] = "ALL";
            dParams["query"] = query?query:"";
            if(brdIds){
                dParams["brdIds"] = brdIds;
            }else{
                resetBrdParams(true);
                $(dParams).removeProp("brdIds");
            }
        };
        this.init = function(isExtLoad){
            // institute.init();
            params['contentSrc.type']="ORGANIZATION";
            params['contentSrc.id'] = $("#myInstitutePage").data('orgId');
            dbtParId = $(".instDoubtsHolder");
            dbtFacetId = $(".instDoubtRightSec");
            if(dbtParId.data("inited")) return false;
            $(".instHomeLinksHold").find(".instDbtTab").addClass("active").siblings().removeClass("active");
            dParams = cloneObject(params);
            dbtParId.on("click",".mangeDoubtsTab",manageTabs)
            //     .on("mouseenter mouseleave",".instEachDbt .openInstDoubt",doubtHover)
                .on("click",".askDoubtBtn",askDoubt)
                .on("click","#instCancelDoubt",cancelPostingDoubt)
                .on("focusin blur",".instDoubtSearchBox .searchDivBox",searchDivFocus)
                .on("keydown",".instDoubtSearchBox .searchDivBox",searchDivKeys)
                .on("click",".loadMoreDoubts",loadMore)
                .on("click",".followEntity,.unfollowEntity",followUnfollowDoubt)
                .on("click",".followersInstFeedCount",followersPopup)
                .on("click",".followEntity",followDoubt)
                .on("click",".unfollowEntity",unfollowDoubt)
                // .on("click",".openInstDoubtAns",institute.oneDbt.open)
                .on("click",".closeDbtTopic",crossSelTopic)
                .on("click",".searchDbtsBtn",toggleSearchDiv)
                .on("click",".delInstDbt",deleteDoubt)
                // .on("click",".openInstDoubt",institute.oneDbt.open)
                .on("click",".instTrTopicText",topicChanged);
            $("#instPostDoubt").on("click",postDoubt);
            dbtFacetId.on("change",".dbtFacetSubChk",subChanged)
                .on("click",".instTrTopicText",topicChanged);
            // assignRTEs($(".instRTEHolder"));
            if(!isExtLoad){
                this.onBack();
            }
            dbtParId.data("inited",true);
            // institute.initWidgets("DOUBTS");
        };
    };
    this.oneDbt = new function(){
        var dParams;
        var dbtParId;
        var getRightSec = function(){
            if(dbtParId.find(".instDoubtRightSec").data("fetched"))return;  
            // dParams["entity"] = {"id":dbtParId.find('.instEachDbt').data("dissId"),"type":"DISCUSSION"};
            dParams["entity.id"] = dbtParId.find('.instEachDbt').data("dissId");
            dParams["entity.type"] = "DISCUSSION";
            showTopLoader();
            $.get("/Institute/getSimilarDoubts",dParams,function(data,stats){
                hideTopLoader();
                var instRightSec = dbtParId.find(".instDoubtRightSec");
                instRightSec.html(data);
                $(".instSimilarQues").find(".eachSimQues").each(function(){
                    $this = $(this);
                    if($this.height()>60){
                        $this.addClass("eachSimQuesMaxHeight");
                    }
                });
            });
            dbtParId.find(".instDoubtRightSec").data("fetched",true);   
        };
        this.open = function(e){
            dbtParId = $(".instDoubtOpen");
            dParams = cloneObject(params);
            var dissId = $(this).data("dissId");
            userRole = $("#myInstitutePage").data("orgObj").userRole;
            dParams["id"] = dissId;
            dParams["userRole"] = userRole;
            openInstSubPage("/Institute/openDoubt","discussion/"+dissId,{'id':dissId,'userRole':userRole});
            e.preventDefault();
        };
        var searchByTopic = function(e){
            var $this = $(this);
            var brdId = $this.data("brdId");
            var brdName = $this.text().trim();
            institute.doubts.open(e,function(){
                setTimeout(function(){
                    institute.doubts.extLoad(brdId,brdName);
                },10);
            },{isExtLoad:true});
            e.preventDefault();
        };
        this.init = function(){
            // institute.init();
            dbtParId = $(".instDoubtOpen");
            dParams = cloneObject(params);
            var dissId = dbtParId.find('.instEachDbt').data("dissId");  
            dParams["id"] = dissId;
            dbtParId.on("click",".instTrTopicText",searchByTopic)
                .on("click",".delInstDbt",deleteOneDoubt)
                .on("click",".followEntity,.unfollowEntity",followUnfollowDoubt)
                .on("click",".followersInstFeedCount",followersPopup)
                .on("click",".followEntity",followDoubt)
                .on("click",".unfollowEntity",unfollowDoubt)
                .on("change",".instCommentFilterDiv .nDropDown",sortDoubtOnComment)
                .on("click",".openInstDoubt",institute.oneDbt.open);
                // .on("click",".openInstDoubtAns",institute.oneDbt.open);
            dbtParId.find(".instEachDbt").addClass("fadeAnim");
            dbtParId.find(".instDbtProfilePic").load(homeImgLoaded);
            // dbtParId.on("click",".RTEImageDiv",rteImageOpen);
            getRightSec();
            initComments();
            // institute.initWidgets("DOUBT");
            showHomeImgs(dbtParId.find(".instDbtProfilePic"));
        };
        var deleteOneDoubt = function(e){
            deleteItem(".instEachDbt","/Institute/deleteDoubt",$(this),function(deleted){
                if(deleted){
                    institute.doubts.open(e,function(){
                        setTimeout(function(){
                            institute.doubts.extLoad();
                        },10);
                    },{isExtLoad:true});
                }
            });
        };
        var sortDoubtOnComment = function(){
            var $this = $(this);
            var val = $this.data("value");
            var sortOrder = $this.data("itemValue").order;
            dbtParId.find('.instCommentFilterDivHolder').html($this.closest(".instCommentFilterDiv"));
            initComments(val,sortOrder);
        };
        var makeHTMLTag = function(tag, attrs) {
            var el = document.createElement(tag);
            if (attrs) {
                for (var k in attrs)
                    el.setAttribute(k, attrs[k]);
            }
            return $(el);
        };
        var uiSamplesDiv = $("#uiSamplesDiv");
        var createCommonUIEl = function(source) {
            var targetVar = makeHTMLTag('div');
            targetVar.html(source.html());
            source.remove();
            // if (img20Sample.children(".img20Wrapper").length > 0) {
            //     targetVar.find(".img20Holder").html(img20Sample.children().clone(true));
            // }
            // if (img30Sample.children(".img30Wrapper").length > 0) {
            //     targetVar.find(".img30Holder").html(img30Sample.children().clone(true));
            // }
            return targetVar;
        };
        commWidgetSample=createCommonUIEl(uiSamplesDiv.children("#commWidgetSample"));
        function initCommWidget(allParams,LMDataParams,targetDiv,callBack){
            var commWidget=commWidgetSample.children().clone(true);
            targetDiv.html(commWidget);
            // assignRTEs(commWidget.find(".inputerRTEDiv"),allParams.placeHolder);
            delete allParams.placeHolder;
            commWidget.data("allParams",allParams);
            var LMHandlerDiv=commWidget.children(".LMHandlerDiv");
            LMHandlerDiv.data("urlStr","/widgets/commItems")
            .data("size",25).data("allParams",LMDataParams);
            showTopLoader();
            vReq.get("/widgets/commItems",LMDataParams,function(data,s,xhr){
                hideTopLoader();
                LMHandlerDiv.html(data);
                loadMJEqns(LMHandlerDiv.get(0));
            if(callBack){
                    callBack(LMHandlerDiv,data,LMDataParams);
                    LMHandlerDiv.data("cbFn",callBack);
            }
        });
    }
        var initComments = function(orderBy,sortOrder){
            var discuss = dbtParId.find('.instEachDbt');
            var commHolder=$(parDivId).find(".instCommentWidgetContainer");
            commHolder.removeClass('nonner');
            var discId=discuss.data("dissId");
            commHolder=commHolder.find(".instCommentWidgetHolder");
            commHolder.html("");
            orderBy = orderBy ? orderBy : "timeCreated";
            sortOrder = sortOrder ? sortOrder : "ASC";
                var LMData={urlStr:"/widgets/commItems",size:10,start:0,
                    orderBy:orderBy,sortOrder:sortOrder,
                    target:'MY_INSTITUTE'
            };
            LMData["parent.type"] = "DISCUSSION";
            LMData["parent.id"] = discId;
            LMData["root.id"] = discId;
            LMData["root.type"] = "DISCUSSION";
            var allParams={};
            allParams.root = {"id":discId,"type":"DISCUSSION"};
            allParams.base = {"id":discId,"type":"DISCUSSION"};
            allParams.parent = {"id":discId,"type":"DISCUSSION"};
            allParams.scope = "ORG";
            allParams.callBack="INST_DISCUSSION";
            allParams.targetPage="MY_INSTITUTE";
            allParams.placeHolder=i18nJS("ADD_ANSWER");
            bigLoader(commHolder);
                initCommWidget(allParams,LMData,commHolder,function(holder,data,params){
                var firstIndex = params ? params.start ? params.start : 0 : 0;
                var firstItem = $(holder).find(".commItem").get(firstIndex);
                if(firstItem){
                    animate($(firstItem),false);
                }
            });
            var LMHandlerDiv = commHolder.find(".LMHandlerDiv");
            var inputBox = commHolder.find(".inputerRTE");
            inputBox.insertAfter(LMHandlerDiv);
        };
    }

    this.notifications = new function(){
        var parDiv;
        var noNotisFoundDiv = "<div class='text-center c-gray noMoreNotis'>No Recent Notifications.</div>";
        this.init = function(){
            // institute.init();
            parDiv = $("#userAllNotifcations");
            parDiv.on("click",".instGetOlderNotis",getMore);
            get(0);
        };
        var getMore = function(){
            get($(this).data("nextStart"),"OLD");
        };
        var get = function(start,feedType){
            var holder = parDiv.find(".notificationsHolder");
            feedType = feedType?feedType:"NEW";
            start = start ? start : 0;
            var size = 10;
            var clustered = true;
            var params = {"feedType":feedType,size:size,start:start,"needClustered":clustered};
            if(feedType == "OLD"){
                params["beforeNotificationId"] = holder.find(".notiFeed:last").data("feedId");
            }
            holder.find(".instGetOlderNotisHolder").remove();
                $.get("/Institute/getNotifications",params,function(data){
                 try{
                var htmlData = "";
                    if(data && data.errorMessage=='' && data.result.list.length>0){
                           $.each(data.result.list,function(i,feed){
                    var aType = feed.info && feed.info.actionType ? feed.info.actionType : feed.eType;
                            if(newsEntityProcessor.isSupported(aType)){
                                    //htmlData += notiFeedFns[aType].init(feed);
                                    htmlData += newsEntityProcessor.process("NOTIFICATIONS",feed,aType,clustered);
                            }
                           });
                       if(data.result.list.length>=size){
                    htmlData += "<div class='instGetOlderNotisHolder'> \n\
                        <a class='instGetOlderNotis big16' data-next-start='"+(start+size)+"'>"+i18nJS("SHOW_MORE")+"</a> \n\
                        </div>";
                   }
                   holder.append(htmlData);
                   MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
                }else{
                   holder.find(".noMoreNotis").remove();
                   holder.append(noNotisFoundDiv);
                }
                parDiv.find(".notisCurCount").text((start+data.result.list.length));
                 }catch(err){
                   holder.find(".noMoreNotis").remove();
                   holder.append(noNotisFoundDiv);
                 }
            });
        };
    };
    this.activities = new function(){
        var actParams;
        var feedParams;
        var activityDiv =".instShareFeedsHold";
        //var videoEmbed = "<embed width='420' height='345' src='%url%' type='application/x-shockwave-flash'></embed>";
        var videoEmbed = "<iframe width='510' height='287' src='%playUrl%' frameborder='0' mozallowfullscreen webkitallowfullscreen allowfullscreen style='background:#000;'></iframe>";
        this.open = function(){
            // institute.activities.init();
            openInstSubPage("/Institute/activities","activities",params,refreshSelectPicker);
        };
        var showImgs = function(holder){
            showHomeImgs($(holder).find(".instFeedProfilePic"));
            showImgsFwk($(holder).find(".instActFeedImg"),function(){
                $(this).fadeTo(120,1);
                $(this).closest(".instActFeedImgHolder").addClass("imgLoaded");
               },
               function(){
                this.error = true;
               }
            );
        }
        this.showImgs = showImgs;
        this.showComImgs = function(divId){
            showHomeImgs($(divId).find(".instFeedCommentPic"));
        };
        this.init = function(){
            actParams = cloneObject(params);
            feedParams = cloneObject(actParams);
            activityDiv = $(parDivId).find(".instShareFeedsHold");
            if($(activityDiv).data("activityInited")) return false;
            showHomeImgs($(activityDiv).find(".instCreateShare").find(".instUserPic"));
            $(activityDiv)
                .on("change",".instActivitySelMyBatch",batchChanged)
                .on("click",".instFeedPostContainer .instImgPreviewCont,.instFeedsContainer .instImgPreviewCont",uploadedImageOpen)
                .on("click",".instFeedPostContainer .playInstVideo,.instFeedsContainer .playInstVideo",previewVideo)
                .on("click",".instFeedsContainer .delInstFeed",deleteFeed)
                .on("click",".crossVidPreview",closeVidPreview)
                .on("click",".loadMoreOrgFeeds",loadMore);
            getFeeds(0);
            // institute.initWidgets("ACTIVITY");
            $(activityDiv).data("activityInited",true);
        };
        var loadMore = function(){
            smallLoader($(this));
            getFeeds(feedParams["start"]+feedParams["size"]);
        };
        var closeVidPreview = function(){
            var $this=$(this);
            var url = $this.attr("rel");
            $this.closest(".instVideoPreview").find("table").removeClass("nonner");
            $this.closest(".videoPlayPreview").addClass("nonner").find(".vidPreviewEmbedHolder").html("");
        };
        var previewVideo = function(){
            var $this=$(this);
            var url = $this.attr("rel");
            var playUrl = $this.data("playUrl");
            var previewDiv = $this.closest(".instVideoPreview").find(".videoPlayPreview");
            var embed = videoEmbed.replace("%playUrl%",playUrl);
            previewDiv.find(".vidPreviewEmbedHolder").html(embed);
            previewDiv.removeClass("nonner");
            $this.closest("table").addClass("nonner");
        };
        var deleteFeed = function(){
            deleteItem(".eachInstFeedHolder","/Institute/deleteActivityFeed",$(this));
        };
        var uploadedImageOpen = function(e){
            showImagePreview($(this).html());
            if(e) e.preventDefault();
            return false;
        };
        var batchChanged = function(){
            getFeeds(0);
        }
        var getFeeds = function(start){
            showLoader();
            var holder = $(activityDiv).find(".instFeedsContainer");
            var select = $(".instActivitySelMyBatch").find(".nDropDown").data("itemValue");
            $(activityDiv).find(".instFeedPostContainer").html("");
            if(select){
                select = toJSON(select);
                feedParams["eType"] = select.type;
                feedParams["eId"] = select.id;
            }else{
                feedParams["eType"] = "ORGANIZATION";
                feedParams["eId"] = $("#myInstitutePage").data("orgId");
            }
            /*feedParams["filter"] = [
                {
                    type:"eventType",
                    value:"SHARE_ENTITY"
                }
            ];*/
            feedParams["size"] = 10;
            feedParams["needClustered"] = true;
            var lastAvail;
            var url = "/Institute/getActivityFeeds";
            if(start){
                feedParams["start"]=start;
                lastAvail = holder.find(".eachInstFeedHolder:last");
                url = "/Institute/getMoreActivityFeeds";
                //feedParams["beforeNewsActivityId"]=lastAvail.data("newsFeedId");
                feedParams["beforeNewsActivityId"]=lastAvail.data("lastNewsFeedId");
            }else{
                feedParams["start"]=0;
                bigLoader(holder);
                $(feedParams).removeProp("beforeNewsActivityId");
            }
            vReq.get(url,feedParams,function(data){
                hideLoader();
                holder.find(".loadMoreOrgFeeds").remove();
                if(feedParams["start"]>1){
                    holder.append(data);
                }else{
                    holder.html(data);
                }
                if(!lastAvail){
                    lastAvail = holder.find(".eachInstFeedHolder:first");
                }else{
                    lastAvail = lastAvail.next();
                }
                animate(lastAvail,false);
                showImgs(holder);
            });
        };
        this.addNewFeed =  function(htmlData){
            var ret = true;
            var holder = $(activityDiv).find(".instFeedPostContainer").html(htmlData).addClass("nonner");
            var feed;
            try{
                ret = institute.activities.postSuccess;
                feed = institute.activities.postData;
                feed = toJSON(feed);
            }catch(err){
                ret=false;
                holder.html("");
            }
            var select = $(".instActivitySelMyBatch .nDropDown").data("itemValue");
            var setTimedFn = function(){};
            function putOutside(){
                holder.removeClass("nonner");
                setTimedFn = function(){
                holder.fadeTo(0,250,function(){
                        $(this).html("");
                });
            };
        }
            function putInside(){
                holder.removeClass("nonner").find(".eachInstFeedHolder").remove();
                holder = $(activityDiv).find(".instFeedsContainer");
                var firstOne = holder.find(".eachInstFeedHolder:first");
                if(firstOne.length>0){
                    holder.find(".eachInstFeedHolder:first").before(htmlData);
                }else{
                    holder.html(htmlData);
                }
                holder.find(".eachInstFeedHolder:first").addClass("feedJustAdded");
                                holder.find(".feedPostSuccessMsg").remove();
                setTimedFn = function(){
                                    holder.find(".feedJustAdded").removeClass("feedJustAdded");
                    $(activityDiv).find(".instFeedPostContainer").html("");
                };
            }
            if(ret && !select){
                putInside();
            }
            else if(ret && feed){
                var shrdWith = feed.sharedWith;
                if(shrdWith.length>0 && shrdWith[0].id && shrdWith[0].type=="PROGRAMME"){
                    var id = shrdWith[0].id;
                    var selectArr = select.split("_");
                    if(id == selectArr[1]){
                        putInside();
                    }else{
                        putOutside();
                    }
                }else{
                    putOutside();
                }
            }else{
                putOutside();
            }
            animate(holder.find(".eachInstFeedHolder:first"),true);
            // showImgs(holder);
            setTimeout(setTimedFn,10000);
            return ret;
        };
    }
    this.openInstAvailPrograms = function(){
        openInstSubPage("/Institute/programs","programs",params);
    };

    this.openMyAccessCodePopup = function(){
        var params = {
            orderBy : "timeCreated",
            sortOrder : "DESC"
        };
        $.get("/Profile/getMyAccessCodes",params,function(data){
            swal({
                html: data,
                customClass:"accessCodePopup"
            });
        });
    };

    this.openResultAnalytics = function(){
        openInstSubPage("/Institute/analytics","analytics",params);
    };

    this.instProfilePage = function(){
        openInstSubPage("/Institute/profile","profile",params);
    }
     this.openAllNotifications = function(){
        $(".instPageNavigator").find(".active").removeClass("active");
        $("#instNotiHolder").removeClass("open");
        openInstSubPage("/Institute/openAllNotifications","notifications",params);
    };

    this.headerTabClick = function(){
        $(".instPageNavigator").find(".active").removeClass("active");
        $(this).closest("li").addClass("active");
        $(".examsDropdown").removeClass("toggled");
        $(".examsDropdown").find("ul").css("display","none");
    };

    this.getRecentPage = function(){
        var params = {"campaignType":"REFERRAL"};
        openInstSubPage("/Institute/getReferralPage","referral",params);
    };

    this.openProgramPage = function(programId){
        var params = {
            "programId":programId
        };
        openInstSubPage("/Institute/library","library/program/"+programId,params,refreshSelectPicker);
        window.location.reload();
    };

    this.openSubjectPage = function(programId,parentId){
        var params = {
            "programId":programId,
            "parentId" : parentId
        };
        openInstSubPage("/MyContents/subject","library/program/"+programId+"/subject/"+parentId,
            params,refreshSelectPicker);
    };

    this.openChapterPage = function(programId,parentId,chapterId){
        params = {
            "programId" : programId,
            "brdIds" : [chapterId],
            "parentId": parentId,
            "chapterId": chapterId
        };
        openInstSubPage("/MyContents/chapter","library/program/"+programId+"/subject/"+parentId+"/chapter/"+chapterId,
            params,refreshSelectPicker);
    };

    this.openPretestPage = function(testId){
        params = {
            "id" : testId
        };
        openInstSubPage("/MyContents/preTestPage","pretest/"+testId,params,refreshSelectPicker);
    };

    this.openTestPage = function(testId){
        params = {
            "id" : testId
        };
        openInstSubPage("/MyContents/testPage","test/"+testId,params,refreshSelectPicker);
    };

    this.openDiscussionPage = function(dissId){
        params = {
            "id" : dissId
        };
        openInstSubPage("/Institute/openDoubt","discussion/"+dissId,{'id':dissId});
        window.location.reload();
    };

    this.openDocumentPage = function(documentId){
        params = {
            "id" : documentId
        };
        openInstSubPage("/MyContents/documentPage","document/"+documentId,params);
    };

    this.openVideoPage = function(videoId){
        params = {
            "id" : videoId
        };
        openInstSubPage("/MyContents/videoPage","video/"+videoId,params);
    };

    this.openVideoPageWithModule = function(videoId,moduleId){
        params = {
            "id" : videoId,
            "moduleId" : moduleId
        };
        openInstSubPage("/MyContents/videoPage","video/"+videoId+"?moduleId="+moduleId,params);
    };

}(jQuery);

var instHeader = new function(){
    this.init = function(){
        this.messages.init();
        this.refer.init();
        this.noti.init();
        this.getCount();
    };
    var liveShareUrl;
    var picUrl = "http://cdn.apk-cloud.com/detail/image/com.learnpedia.android-w250.png";
    var customOutMessage = "Learnpedia is an edu-tech company that promises to make learning a fun and fascinating experience for students preparing to write various competitive exams. Its flagship product ScoreJEE is already redefining preparation for IIT-JEE.";
    var header;
    var caption = "Enjoy Learning";
    this.messages = new function(){
        var parDivId = "#instMsgNotiHolder";
        var hideNow = function(e){
            $(document).off("click",instHeader.hideAll);
            instHeader.messages.hideMe(e);
        };
        this.hideMe =function(e,chkInnerElem){
            return hideMe(e,parDivId,"instMsgNotiDiv",chkInnerElem);
        };
        var fetchConversations = function(){
            var holder = $(parDivId).find(".msgsNotifications");
            // smallLoader(holder);
            $.get("/UserMessages/currentMessages",{start:0,size:5},function(data){
                holder.html(data);
            });
        };
        this.openConversation = function(e){
            var $this = $(this);
            var src = e.srcElement||e.target;
            if(e.originalEvent.returnNow || (src.nodeName=="A" && !$(src).hasClass("openMsgConversation"))){
                return;
            }
            setTimeout(function(){
                var convId = $this.data("conversationId");
                var userConvId = $this.data("userConversationId");
                var params = {"userConversationId":userConvId,"conversationId":convId,"tabType":"MESSAGES"};
                var holder = $("#msgsHeader").find("#msgsHome");
                if(holder.get(0)){
                    showTopLoader();
                    $.get("/UserMessages/conversation",params,function(data){
                        hideTopLoader();
                        holder.html(data);
                        instHeader.getCount();
                    });
                }else{
                    openMyPage("/UserMessages/openConversation",params,function(){
                        instHeader.getCount();
                    },null,null,$this);
                }
                instHeader.messages.hideMe(e);
                pushInstHistory($("#myInstitutePage").data('orgId'),"messages/conversation/"+userConvId);
            },100);
            if(e){
                e.preventDefault();
            }
        };
        this.updateCount = function(data){
                if(data && data.unreadConversationCount>0){
                    $("#instMsgNotiHolder").addClass("notify")
                .find("#instMsgCount")
                .text(data.unreadConversationCount);
                }else{
                    $("#instMsgNotiHolder").removeClass("notify")
                .find("#instMsgCount").text("");
            }
        };
        this.init = function(){
            $("#instMsgNotiHolder").on("click",".instMsgNotiHead",fetchConversations);
            $(document).on("click",".openMsgConversation",this.openConversation);
        }
    };
    this.refer = new function(){
        var parDivId = ".instReferHolder";
        this.hideMe =function(e,chkInnerElem){
            return hideMe(e,parDivId,"instNotiDiv",chkInnerElem);
        };
        var getRecent = function(){
            var params = {"campaignType":"REFERRAL"};
            var credits;
            var referralcode;
            $.get("/Institute/getReferralData",params,function(data){
                credits = data.result.existingRewardPoints;
                referralcode = data.result.referralCode;
                liveShareUrl = data.baseUrl+"signup?referralcode="+referralcode;
                if (data.result.message != null) {
                    var titleMsg = "<div class='text-center f-20'>INVITE TO LEARNPEDIA</div>";
                    var currentMsg ="<div class='text-center c-gray f-16'>You get Rs."+data.result.referrerRewards+" and your friend gets Rs."+data.result.friendRewards+" too</div><br><div class='text-center f-18 c-black'><span class='pull-left'>Credits: Rs."+credits+"</span><span class='pull-right'>Code: "+referralcode+"</span></div>";
                    var creditMsg = "<span class='big16'>Learnpedia Credits: Rs."+credits+"</span>";
                    header = "Signup with my Referral link, You get Rs."+data.result.friendRewards;
                    var button ='<input id="fbShareButton" type="image" src="/public/images/facebook-share-button.png" style="border-radius: 9px; width: 146px; height: 36px">';
                    var terms = "<div class='terms f-10'><a href='/referralTerms' target='_blank'>Terms & Conditions</a></div>";
                }else{
                    var currentMsg = "<h2 class='text-center'>Our Introductory Refer & Earn Is Now Over.</h2><h4 class='centerText greyTextColor'>You can still invite your friends and also use your credits</h4><h3 class='centerText'>Referralcode: "+referralcode+"</h3>";
                    var creditMsg = "<span class='big16'>Learnpedia Credits: Rs."+credits+"</span>";
                    header = "Welcome To Learnpedia";
                }
                swal({
                    title:titleMsg+"<br>",
                    html:currentMsg+"<br><br>"+button + terms,
                    showConfirmButton:false
                });
                $(".swal2-modal").css("width","500px");
                $("#fbShareButton").on("click",function(){
                    var app_id = 1656748771289533;
                    var href = "https://www.facebook.com/dialog/feed?app_id="+app_id+"&caption="+caption+"&name="+header+"&link="+liveShareUrl+"&picture="+picUrl+"&description="+customOutMessage;
                    FB.AppEvents.logEvent('share');
                    window.open(href, "facebook", "left=600,top=200,height=200,width=200");
                });
            });
        };
        var hideNotis = function(e){
            return hideMe(e,parDivId,"instNotiDiv",false);
        };
        this.init = function(){
            $(parDivId).on("click",".popup",getRecent);
        }
    };

    this.getCount = function(){
           $.get("/Institute/getNotificationsSummary",{},function(data){
        instHeader.noti.updateCount(data.others);
       });
    };

    this.noti = new function(){
        var parDivId = "#instNotiHolder";
        var noNotisFoundDiv = "<div class='text-center'>No Recent Notifications</div>";
        this.hideMe =function(e,chkInnerElem){
            return hideMe(e,parDivId,"instNotiDiv",chkInnerElem);
        };
        this.updateCount = function(data){
            if(data && data.totalHits>0){
                $("#instNotiHolder").addClass("notify").find("#instTickerCount").text(data.totalHits).css("display","inline-block");
            }
        };
        var getRecent = function(){
            var instNotiHolder = $("#instNotiHolder");
            var holder = instNotiHolder.find(".instNotificationsH");
                    instNotiHolder.removeClass("notify");
            // smallLoader(holder);
            var feedType = "NEW";
            var clustered = true;
            var params = {"feedType":feedType,size:5,"needClustered":clustered};
                $.get("/Institute/getNotifications",params,function(data){
                 try{
                var htmlData = "";
                    if(data && data.errorMessage=='' && data.result.list.length>0){
                           $.each(data.result.list,function(i,feed){
                            var aType = feed.info && feed.info.actionType ? feed.info.actionType : feed.eType;
                            if(newsEntityProcessor.isSupported(aType)){
                                    htmlData += newsEntityProcessor.process("NOTIFICATIONS",feed,aType,clustered);
                            }
                           });
                   holder.html(htmlData);
                   MathJax.Hub.Queue(["Typeset",MathJax.Hub,holder.get(0)]);
                }else{
                   holder.html(noNotisFoundDiv);
                }
                 }catch(err){
                   holder.html(noNotisFoundDiv);
                 }
            });
        };
        this.init = function(){
            $(parDivId).on("click",".instNotiHead",".instNotiDiv",getRecent)
                        .on("click",".instNotiSeeAll",institute.openAllNotifications);
            // $(parDivId).on("click",".instNotiHead",{
            //     "parDivId":parDivId,
            //     "holder":".instNotiDiv",
            //     "cbFn":getRecent
            // },open)
            // .on("click",".notiFeed,.instNotiSeeAll",hideNotis);
        }
    };
};
