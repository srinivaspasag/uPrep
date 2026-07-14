var currentGraphData;
var testIds=[];
var courseName;
var showAvgGraph=false;
var showHighestGraph=false;
var org = $("#myInstitutePage").data("orgObj");
var instAnalytics = new function(){
    var params = {'programId':'','year':(new Date().getFullYear())};
    var memberSearchXHR;
    var graphXHR;
    var tableXHR;
    var hideRankType = ".instCenterRank";
    var showRankType = ".instOverallRank";
    var compareWithGraphData;
    var searchBoxFocused = function(){
        showMemberDropDown();
        $(this).closest(".instTACompare").addClass("focused");
    };
    var searchBoxOutFocus = function(){
        $(this).closest(".instTACompare").removeClass("focused");
        hideMemberDropDown();
    };
    var mouseEnterCompare = function(){
        $(".instTACompare").find(".searchCompareTA").off("focusin",searchBoxFocused).off("focusout",searchBoxOutFocus);
    };
    var mouseOutCompare = function(){
        $(".instTACompare").find(".searchCompareTA").off("focusin",searchBoxFocused).off("focusout",searchBoxOutFocus);
        $(".instTACompare").find(".searchCompareTA").on("focusin",searchBoxFocused).on("focusout",searchBoxOutFocus);
    };
    var compareUserSelected = function(){
        var userName=$(this).text();
        var userId=$(this).data("userId");
        if(!userId){
            $(".instTAComparedUsers").addClass("nonner").data("targetUserId","")
            .find(".instComUser .uName").text(userName);
            hideMemberDropDown();
            instAnalytics.refreshGraph();
            return;
        }
        $(".instTAComparedUsers").removeClass("nonner").data("targetUserId",userId)
        .find(".instComUser .uName").text(userName);
        //added by Shankar
        hideMemberDropDown();
        var compare=true;
        getTAGraph(false, 0, userId, compare);
    };
    var hideMemberDropDown = this.closeCompare = function(){
        $(".instTAUsersHolder").addClass("nonner");
        $(".instTACompare").find(".searchCompareTA").val("");
        mouseOutCompare();
    }
    var showMemberDropDown = function(){
        $(".instTAUsersHolder").removeClass("nonner");
    }
    var fetchMembers = function(freshLoad){
        var selectTag = $(".instTACompare");
        var query = selectTag.find(".searchCompareTA").val();
        var p = cloneObject(params);
        if(query){ p["query"] = query; }else{$(p).removeProp("query");}
        p["size"] = 8;
        p["searchAll"] = true;
        p["start"]=0;
        showMemberDropDown();
        smallLoader($(selectTag.find(".instTAUsersHolder").find(".instTAUsers").get(0)));
        if(memberSearchXHR){ memberSearchXHR.abort();}
        memberSearchXHR = vReq.get("/Institute/searchMembersTA",p,function(data){
            selectTag.find(".instTAUsersHolder").html(data);
        });
    };
    var uiCloneHelper={
        par:"",toClone:"",available:0,
        set:function(par,toClone,available){
            this.par = $(par);this.available = available!=undefined?available:this.available;
            this.toCloneStr = toClone;this.toClone = this.par.find(toClone);
        },
        findByIndex:function(index){
            return this.toClone.get(index);
        },
        create:function(index,setAttrs){
            var div = this.findByIndex(index);
            if(!div){
                div = $(this.toClone.get(this.available)).clone();
                this.par.append(div);this.toClone = this.par.find(this.toCloneStr);
            }if(typeof setAttrs == "object") $(div).attr(setAttrs);
            return div;
        },
        remove:function(index){
            var div = this.findByIndex(index);
            if(this.toClone.length<=1)return false;
            $(div).remove();
            this.toClone = this.par.find(this.toCloneStr);
        },
        removeFrom:function(index){
            if(index<=this.available)index=this.available+1;
            for(index;index<this.toClone.length;index++){
                var div = this.findByIndex(index);
                if(!div)break;
                $(div).remove();
            }
            this.toClone = this.par.find(this.toCloneStr);
        },
        removeByObj:function(obj){
            var index = this.toClone.index(obj);
            this.remove(index);
        }
    };
    var getProgrammeInfo = function(){
        vReq.get("/Institute/getProgrammeInfo",params,function(data){
            var courses = [{'name':"All",'_id':''}];
            try{
                if(data.result.list){
                    courses = courses.concat(data.result.list);
                }
            }catch(err){ courses = [{'name':"All",'_id':''}];}
            uiCloneHelper.set(".instTASubjectSelect .dropDownContainer",".eachDropedElement",0);var index=0;
            for(i in courses){
                var course = courses[i];
                if(course.recordState != "ACTIVE"){index++;continue;}
                var sp = uiCloneHelper.create(index++);
                $(sp).text(course["name"]);
                $(sp).data("value",course["id"]);
                $(sp).data("text",course["name"]);
                $(sp).attr("title",course["name"]);
                $(sp).attr("name","SELECT_SUBJECT");
            }
            uiCloneHelper.removeFrom(index+1);
            $(".instTASubjects").data("value","").find(".textCont").val(i18nJS("TXT_ALL_SUBJECTS"));
        });
    };
    var getMoreTATable = function(){
        getTATable(false,false);
    };
    this.generateTATable = function(){
        getTATable(true,false);
    };
    function cloneObject(inObj){
        var outObj = JSON.stringify(inObj);
        outObj = JSON.parse(outObj);
        return outObj;
    };
        function abort() {
        if (canvases) {
          canvases = null;
          renderProgress();
          dispatchEvent('afterprint');
        }
    };
    function showLoader() {
        $("#topLoader").removeClass("nonner");
    }
    function hideLoader() {
        $("#topLoader").addClass("nonner");
    }
    var getTATable = function(fetchFresh,drawGraphToo){
        showLoader();
        var div = $(".instTATable");
        var tParams = div.data("params");
        if(!tParams || fetchFresh){
            tParams = cloneObject(params);
            tParams["start"]=0;tParams['size']=10;
            bigLoader(div);
            tParams["freshLoad"]="true";
        }else if(!fetchFresh){
            tParams["start"]+=10;
            smallLoader($(".instTATestLoadMore"));
            tParams["freshLoad"]="false";
        }
        tParams["initialDraw"] = drawGraphToo?true:false;
        tParams["targetUserId"] = $("#instTestAnalytics").data("targetUserId");
        tParams["entityType"] = "TEST";
        var query = $(".instSearchTests").find(".customSearchBox").val();
        if(query){tParams["query"]=query;}else{$(tParams).removeProp("query");}
        div.data("params",tParams);
        if(tableXHR){ tableXHR.abort();}
        var url = "/Institute/drawTATestTable";
        var userRole = org.userRole;
        if(org.userRole=="STUDENT"){
            url = "/Institute/drawStudentTATestTable";
        }
        tableXHR = vReq.get(url,tParams,function(data){
            hideLoader();
            org.userRole = userRole;
            $(".instTATestLoadMore").closest("tr").remove();
            if(fetchFresh){
                div.html(data);
            }else{
                div.find(".tableInstTA").append(data);
            }
            if(instTATestData && drawGraphToo){
                currentGraphData = cloneObject(instTATestData); 
                $(".instTAGraphHolder").find(".test-graph").data("params",tParams);
                generateInstTAGraph.init(instTATestData,instAnalytics.updateGraphArrow);
                checkForCheckBoxes();
            }
            $(hideRankType).addClass("nonner");
            $(showRankType).removeClass("nonner");
        });
    };
    var prevGraph = function(e){
        if(!$(this).data("active")) return;
        var targetUserId;
        if(e.data && e.data.targetUserId){
            targetUserId = e.data.targetUserId;
        }
                getTAGraph(false,-1,targetUserId);
    };
    var nextGraph = function(e){
        if(!$(this).data("active")) return;
                //instAnalytics.refreshGraph();
        var targetUserId;
        if(e.data && e.data.targetUserId){
            targetUserId = e.data.targetUserId;
        }
        getTAGraph(false,1,targetUserId);
    };
    this.generateTAGraph = function(targetUserId,callback){
        //getTAGraphAndCheckCompare(true);
                getTAGraph(true,1,targetUserId,undefined,callback);
    };
    this.updateGraphArrow = function(total){
        var tParams = $(".instTAGraphHolder").find(".test-graph").data("params");
        var start = tParams['start'];var size = tParams['size'];
        if(start<=0){
            $(".instTANextPrev").find(".prevTA").removeClass("active").data("active",false);
        }else{
            $(".instTANextPrev").find(".prevTA").addClass("active").data("active",true);
        }
        if((start+size)>=total){
            $(".instTANextPrev").find(".nextTA").removeClass("active").data('active',false);
        }else{
            $(".instTANextPrev").find(".nextTA").addClass("active").data('active',true);
        }
    };
    var getTAGraph = function(fetchFresh,multBy, targetUserId,compare,callback){
        var div = $(".instTAGraphHolder").find(".test-graph");
        var tParams = div.data("params");
        if(!compare)bigLoader(div);
        if(!tParams || fetchFresh){
            tParams = cloneObject(params);
            tParams["start"]=0;tParams['size']=10;
            tParams["freshLoad"]="true";
        }else if(!fetchFresh){
            var start = tParams["start"];
            start+=10*multBy;
            if(start<0){start = 0;}
            tParams["start"]=start;
            tParams["freshLoad"]="false";
        }
        tParams["targetUserId"] = targetUserId?targetUserId:$("#instTestAnalytics").data("targetUserId");
        tParams["entityType"]="TEST";
        if(compare){
            tParams["testIds"]=testIds;
        }else{
            $(tParams).removeProp("testIds");
        }
        var courseId = $(".instTASubjectSelect .nDropDown").data("value");
        /*if(courseId){
            tParams["brdId"]=courseId;
        }else{
            $(tParams).removeProp("brdId");
        }*/
        div.data("params",tParams);
        if(graphXHR){ graphXHR.abort();}
        var url = "/Institute/getTATestGraph";
        if(instAnalytics.userRole=="STUDENT"){
            url = "/Institute/getTATestGraphStudent";
        }
        graphXHR = vReq.get(url,tParams,function(data){
            data = data.result;
                    //ISSUE #212 :$(".showTAHighestMarks,.showTAAvgMarks").prop("checked",false);
            if(compare){
                                if(compareWithGraphEls){
                                    compareWithGraphEls[0].remove();
                                    compareWithGraphEls[1].remove();
                                }
                var graphType = instAnalytics.userRole!="STUDENT"?"AVG":undefined;
                            var dataSet=generateInstTAGraph.createItemsByMarks(0,data.testsInfo.length,data.testsInfo,graphType);
                            compareWithGraphEls=$(".test-graph")
                                .appendToAreaGraph({name:data.user.firstName,items:dataSet.plotItems,
                                circleColor:"#339933",lineColor:"#339933"});                        
            }else{
                currentGraphData = data;
                                generateInstTAGraph.init(data,instAnalytics.updateGraphArrow);
            }
            checkForCheckBoxes();
            try{callback(data);}catch(err){}
        });
    };
    /*var switchRankType = function(){
        showRankType = $(this).data("showType");
        hideRankType = $(this).data("hideType");
        $(hideRankType).addClass("nonner");
        $(showRankType).removeClass("nonner");
        var type = $(this).data("type");
        generateInstTAGraph.createItemsFn = generateInstTAGraph.createItemsFnRankTypeList[type];    
        generateInstTAGraph.createItemsFnTypeList["RANK"] = generateInstTAGraph.createItemsFnRankTypeList[type];
        instAnalytics.refreshGraph();
        $(".selectInstTAGraphType").data("value","RANK").find(".vSelectText").val("Rank");
    };*/
    function instTASubChanged(){
        var $this = $(this);
        courseName = $this.find(".textCont").text();
        generateInstTAGraph.subjectBrdId = $this.data("value");
            instAnalytics.refreshGraph();
        //instAnalytics.generateTAGraph();
    }
    this.refreshGraph = function(){
        //var trgtUserId=$(".instTAComparedUsers").data("targetUserId");
        //generateInstTAGraph.init((compare ? compareWithGraphData :currentGraphData),'real',null);
        generateInstTAGraph.refresh(checkForCheckBoxes);
                //ISSUE #212 : $(".showTAHighestMarks,.showTAAvgMarks").prop("checked",false);
                $(".instTAComparedUsers").addClass("nonner");  
                if(compareWithGraphEls){
                    compareWithGraphEls[0].remove();
                    compareWithGraphEls[1].remove();
                }
    };
    var showPageInternal = function(){
        $(".instTAGraphH").removeClass("nonner");
                $(".instSearchTests").show();
    };
    var hidePageInternal = function(){
        $(".instTAGraphH").addClass("nonner");
                $(".instSearchTests").hide();
    };
    var showPageExternal = function(){
        $(".instTAGraphH").removeClass("nonner");
        $(".instTAGraphNoTest").addClass("nonner");
        $(".instTAGraphContainer").removeClass("nonner");
    };
    var hidePageExternal = function(){
        $(".instTAGraphNoTest").removeClass("nonner");
        $(".instTAGraphContainer").addClass("nonner");
    };
    this.showPage = showPageInternal;
    this.hidePage = hidePageInternal;
    var refreshProgramme = function(progId,centerId,sectionId,pushHistory){
        // if(progId){
            params['programId'] = progId;
            getProgrammeInfo();
            params['centerId'] = centerId?centerId:"";
            params['sectionId'] = sectionId?sectionId:"";
            if(pushHistory){
                urlQueryHelper.push("program",progId);
                urlQueryHelper.push("center",centerId);
                urlQueryHelper.push("section",sectionId);
            }
            getTATable(true,true);
        // }
    };
    this.initExtUse = function(paraDivId,userRole,progId,targetUserId,callBack){
        instAnalytics.userRole = userRole;
        if(userRole == "STUDENT"){
            instAnalytics.extStudent = {
                targetUserRole : userRole,
                targetUserId : targetUserId
            }
        }
        params['programId'] = progId;
        this.showPage = showPageExternal;
        this.hidePage = hidePageExternal;

        getProgrammeInfo();
        $(".instTAGraphHolder").find(".test-graph").data("params",params);
        instAnalytics.generateTAGraph(targetUserId,callBack);
        $(".instTACompare").find(".searchCompareTA").keyup(fetchMembers)
                    .focusin(searchBoxFocused)
                    .focusout(searchBoxOutFocus);
        $(".instTAUsersHolder").on("click",".instTAUsers",compareUserSelected)
                .on("mouseenter",mouseEnterCompare)
                .on("mouseleave",mouseOutCompare);
        //$(".instTAshowRankTab").on("click",".instSwitchRankType",switchRankType);
        $(".instTANextPrev").on("click",".prevTA",{"targetUserId":targetUserId},prevGraph)
                    .on("click",".nextTA",{"targetUserId":targetUserId},nextGraph);
                $(paraDivId)
                    .on("change",".showTAHighestMarks",showTAHighestMarks)
                    .on("change",".showTAAvgMarks",showTAAvgMarks)
                    .on("change",".instTASubjectSelect .nDropDown",instTASubChanged);
        generateInstTAGraph.reset();
    };
    var getDistinctData = function(){
        var progId = $(".instAnalyticsSelMyProg").find(".nDropDown").data("value");
        var centerId = $(".instAnalyticsSelMyCenter").find(".nDropDown").data("value");
        var sectionId = $(".instAnalyticsSelMySection").find(".nDropDown").data("value");
        if(progId){
            return {"progId":progId,"center":centerId,"section":sectionId};
        }
        return {"progId":"","center":"","section":""};
    };
    this.onBack = function(){
    };
    this.init = function(){
        // try{institute.init();}catch(err){}
        var progInfo = getDistinctData();
        progInfo = institute.readUrlForProgInfo(progInfo);
        var progId = progInfo["progId"];
        if(!progId){ return;}
        this.showPage = showPageInternal;
        this.hidePage = hidePageInternal;
        var userRole = org.userRole ;
        if(userRole=="STUDENT"){
            $(".instTATests").addClass("studentTA");
        }else{
            $(".instTATests").addClass("professorTA");
        }
        refreshProgramme(progId,progInfo["center"],progInfo["section"]);
        //params['programId'] = progId;
        $(".instTACompare").find(".searchCompareTA").keyup(fetchMembers)
                    .focusin(searchBoxFocused)
                    .focusout(searchBoxOutFocus);
        $(".instTAUsersHolder").on("click",".instTAUsers",compareUserSelected)
                .on("mouseenter",mouseEnterCompare)
                .on("mouseleave",mouseOutCompare);
        $(".instTATests")
            .on("click",".instTATestLoadMore",getMoreTATable)
            .on("click",".showIntAnalyticsCenters",getTATableCenters);
        //$(".instTAshowRankTab").on("click",".instSwitchRankType",switchRankType);
        $(".instTANextPrev").on("click",".prevTA",prevGraph)
                    .on("click",".nextTA",nextGraph);
        $(".instSearchTests").on("keyup",".customSearchBox",this.generateTATable);
                $("#instTestAnalytics")
                    .on("change",".showTAHighestMarks",showTAHighestMarks)
                    .on("change",".showTAAvgMarks",showTAAvgMarks)
                    .on("change",".instTASubjectSelect .nDropDown",instTASubChanged)
                    .on("change",".instAnalyticsSelMyProg .nDropDown,.instAnalyticsSelMyCenter .nDropDown,.instAnalyticsSelMySection .nDropDown",instTAProgChanged);
        generateInstTAGraph.reset();
    };
    var getTATableCenters = function(){
        var testId = $(this).data("testId");
        var testName = $(this).data("testName");
        var pr = {"testName":testName};
        pr["entity.type"] = "TEST";
        pr["entity.id"] = testId;
        vReq.get("/Institute/getTATestCenters",pr,function(data){
            swal({
                html:data,
                customClass:"getTATestCenters"
            });
        });
    };
    var instTAProgChanged = function(){
        setTimeout(function(){
            var progInfo = getDistinctData();
            refreshProgramme(progInfo["progId"],progInfo["center"],progInfo["section"],true);
        },100);
    };
        //by ajith
        var highestMarksGraphEls,avgMarksGraphEls,compareWithGraphEls;
    var checkForCheckBoxes=function(){
        var parDiv = $(".instTAshowHighestTab");
        setTimeout(function(){
            parDiv.find(".showTAAvgMarks").trigger("change");
                    parDiv.find(".showTAHighestMarks").trigger("change");
        },100);
    }
        var showTAHighestMarks=function(e){
            if($(this).is(":checked")){
                var data=generateInstTAGraph.createItemsByMarks(0,currentGraphData.list.length,currentGraphData.list,"HIGHEST");
                highestMarksGraphEls=$(".test-graph").appendToAreaGraph({name:data.name||FULLNAME,
                    items:data.plotItems,circleColor:"#CC3333",lineColor:"#CC3333"});        

            }else if(highestMarksGraphEls){
                highestMarksGraphEls[0].remove();
                highestMarksGraphEls[1].remove();
            }
        }
        var showTAAvgMarks=function(){
            if($(this).is(":checked")){
                var data=generateInstTAGraph.createItemsByMarks(0,currentGraphData.list.length,currentGraphData.list,"AVG");
                avgMarksGraphEls=$(".test-graph").appendToAreaGraph({name:data.name||FULLNAME,
                    items:data.plotItems,circleColor:"#339933",lineColor:"#339933"});        

            }else if(avgMarksGraphEls){
                avgMarksGraphEls[0].remove();
                avgMarksGraphEls[1].remove();
            }
        }
}

var toCamelCase=function(c){
    if(!c){return"";}
    var b=c.charAt(0).toUpperCase();
    var a=c.substr(1).toLowerCase();
    a=a?a.toLowerCase():"";
    b=b+a;
    return b;
}

var generateInstTAGraph = {
    //count: 1,
    createItemsFnTypeList:{"RANK":"createItemsByRank","MARK":"createItemsByMarks"},
    /*createItemsFnRankTypeList:{"OVERALL":"createItemsByOverallRank","CENTER":"createItemsByCenterRank"},*/
    createItemsFn:"createItemsByMarks",
    maxTests:10,
    yAxisText:'% Marks',
    currentData:null,
    graphType:undefined,
    subjectBrdId:undefined,
    reset: function(){
        this.yAxisText = '% Marks';
        this.currentData = this.graphType = this.subjectBrdId = undefined;
        this.createItemsFn = "createItemsByMarks";
    },
        init: function(data,callBack){
            var takenNo = data.totalHits;
                var testInfo = data.list;
                var d = {};
                var testNo = testInfo.length;
                if(testNo<generateInstTAGraph.maxTests){var tLength = testNo}else tLength = generateInstTAGraph.maxTests;
                generateInstTAGraph.storeTestIds(testInfo);
                var xAxisAndDataSet;
        var graphType = generateInstTAGraph.graphType = instAnalytics.userRole!="STUDENT"?"AVG":undefined;
                if(data.totalHits>0){
                    xAxisAndDataSet=generateInstTAGraph[generateInstTAGraph.createItemsFn](0, tLength, testInfo,graphType);
                }else{
                    xAxisAndDataSet=generateInstTAGraph[generateInstTAGraph.createItemsFn](0, 0, testInfo,graphType);
                }
                //xAxisAndDataSet["name"]=data.user.firstName;
                generateInstTAGraph.generateData(takenNo,xAxisAndDataSet);
            try{callBack(takenNo,d);}catch(err){}
        generateInstTAGraph.currentData = data;
        generateInstTAGraph.currentData.testLength = tLength;
        vSelectFns.init();
        },
    refresh: function(callBack){
        var data = generateInstTAGraph.currentData;
                var takenNo = data.totalHits;
                var testInfo = data.list;
                var d = {};
                var tLength = data.testLength;
                var xAxisAndDataSet;
        var graphType = generateInstTAGraph.graphType;
                if(takenNo>0){
                    xAxisAndDataSet=generateInstTAGraph[generateInstTAGraph.createItemsFn](0, tLength, testInfo,graphType);
                }else{
                    xAxisAndDataSet=generateInstTAGraph[generateInstTAGraph.createItemsFn](0, 0, testInfo,graphType);
                }
                //xAxisAndDataSet["name"]=data.user.firstName;
                generateInstTAGraph.generateData(takenNo,xAxisAndDataSet);
            try{callBack(takenNo,d);}catch(err){}
    },
        validate: function(testTaken, data, type){
            return testTaken>0;
            //depricated code
            if(testTaken>0){
                return true;//generateInstTAGraph.generateData(data, type);
            }
            else{
                $('.viewDummyWrapper').html('<a class="viewDummyAn" href="#dialog" name="modal">View Demo Analytics</a>');
                var $mesHtml = '<div class="wrapperForNoGraph"><p class="messageForQA">Looks like you have not <b>attempted</b> any tests</p><span style="color:#666;">Do so to view your analytics here</span><br><a class="attemptAdd goToExploreContent" href="/explorecontent">Attempt Tests</a></div>';
                $('#allAnalyticsWrapper .tests-stats').html($($mesHtml));
            }
        },
        generateData: function(takenNo,xAxisAndDataSet){
            var $selector;
                $('#tests-taken').html(takenNo);
                $selector= $(".instTAGraphHolder").find(".test-graph");
                generateInstTAGraph.createGraph($selector,xAxisAndDataSet);
        },
    testLinkToolTip : function(testData){
        var html = "<div><a class='openInstTest blueTextColor' data-test-id='"+testData.id+"'>"+i18nJS("VIEW_TEST")+"<a></div>";
        return html;
    },
    userAnalyticsLinkToolTip : function(testData){
        if(!instAnalytics.extStudent) return "";
        var targetUserId = instAnalytics.extStudent.targetUserId;
        var targetUserRole = instAnalytics.extStudent.targetUserRole;
        var html = "<div><a class='openInstTest blueTextColor' data-test-id='"+testData.id+"' data-target-user-id='"+targetUserId+"' data-target-user-role='"+targetUserRole+"'>"+i18nJS("VIEW_ANALYTICS")+"<a></div>";
        return html;
    },
    getBrdSpecificInfo : function(brdId,testData){
        var boards = testData.entity.boards;
        for(var i=0;i<boards.length;i++){
            if(boards[i].id == brdId){
                var newData = cloneObject(testData);
                newData.entity.qusCount = boards[i].qusCount;   
                newData.entity.totalMarks = boards[i].totalMarks;   
                newData.entity.measures = boards[i].measures;
                newData.boards = [{id:brdId,name:boards[i].name}];
                newData.entity.id = testData.entity.id;
                newData.entity.name = testData.entity.name;
                return newData;
            }
        }
        return;
    },
    getStudentBrdSpecificInfo : function(brdId,testData){
        var boards = testData.info.boards;
        for(var i=0;i<boards.length;i++){
            if(boards[i].entity.id == brdId){
                var newData = cloneObject(testData);
                var board = newData.info.boards[i];
                newData.qusCount = board.qusCount;
                newData.totalMarks = board.totalMarks;
                newData.info = board;
                newData.info.startTime = testData.info.startTime;
                newData.info.entity.name = testData.info.entity.name;
                newData.info.entity.totalAttempts = testData.info.entity.totalAttempts;
                newData.info.entity.mode = testData.info.entity.mode;
                newData.info.entity.type = testData.info.entity.type;
                newData.info.entity.id = testData.info.entity.id;
                return newData;
            }
        }
        return;
    },
        createItemsByMarks: function(start, end, testsInfo, globalGraphType){
            var items=[],xAxisItems=[],plotItems=[],commonTests=false;
            for(var i=end-1; i>=start;i--){
            var testData=testsInfo[i],testInfo,marksVal,rankHTML="",marksHTML="",avgMarks=0;
            var testEntity = {};
            var val = 0,maxScore,date,totalMarks,maxScorePercent=0;
            if(org.userRole == "STUDENT"){
                if(generateInstTAGraph.subjectBorrdId){
                    var ret = generateInstTAGraph.getStudentBrdSpecificInfo(generateInstTAGraph.subjectBrdId,testData);
                    if(!ret){
                        val = undefined;
                    }else{
                        testData = ret;
                    }
                }
                testInfo=testData.info;
                if(!testInfo.entity.totalAttempts){
                    val = undefined;
                    avgMarks = marksVal = 0;
                }else{
                    marksVal = testInfo.measures.score;
                    avgMarks = testInfo.entity.measures.score / testInfo.entity.totalAttempts;
                    var totalMarks = testInfo.totalMarks?testInfo.totalMarks:1;
                            val = val == 0?(marksVal*100)/totalMarks:undefined;
                    maxScore = testInfo.entity.measures.maxScore;
                    maxScorePercent = (maxScore*100)/totalMarks;
                }
                date = testInfo.startTime;
                totalMarks = testInfo.totalMarks;
            }else{
                if(generateInstTAGraph.subjectBrdId){
                    var ret = generateInstTAGraph.getBrdSpecificInfo(generateInstTAGraph.subjectBrdId,testData);
                    if(!ret){
                        val = undefined;
                    }else{
                        testData = ret;
                    }
                }
                testInfo=testData;
                if(!testInfo.entity.totalAttempts){
                    val = undefined;
                    maxScore = marksVal = avgMarks = 0;
                }else{
                    marksVal = avgMarks = testInfo.entity.measures.score / testInfo.entity.totalAttempts;
                    var totalMarks = testInfo.entity.totalMarks?testInfo.entity.totalMarks:1;
                    val = val == 0?(marksVal*100)/totalMarks:undefined;
                    maxScore = testInfo.entity.measures.maxScore;
                    maxScorePercent = (maxScore*100)/totalMarks;
                }
                date = testInfo.schedule.startTime;
                totalMarks = testInfo.entity.totalMarks?testInfo.entity.totalMarks:1;
            }
            var toolTipHtml = "<div class='blueTextColor'>"+testInfo.entity.name+"</div>";
            var testMode = testInfo.entity.mode;
            testMode = testMode ? toCamelCase(testMode):"";
            var testType = toCamelCase(testInfo.entity.type);
            toolTipHtml += "<div class='greyTextColor'>"+testMode+" "+testType+"</div>"; 
                    if(testData.rank && instAnalytics.userRole == "STUDENT"){
                toolTipHtml += "<div>Rank: "+testData.rank+"/"+testInfo.entity.totalAttempts+"</div>";
                var marksPercent = (marksVal*100)/totalMarks;
                marksPercent = Math.round(marksPercent);
                        toolTipHtml += "<div>Your Score: "+marksPercent+"% ("+marksVal+"/"+totalMarks+")</div>";
            }
                    if(globalGraphType=="AVG"){
                        marksVal = avgMarks;
                var avgPercent = (avgMarks*100)/totalMarks;
                val = val == undefined?undefined:avgPercent;
                        avgPercent = Math.round(avgPercent);
                        marksVal = Math.round(marksVal);
                        toolTipHtml += "<div class='big14'>"+i18nJS("TXT_AVERAGE")+"</div><div class='big16'>"+avgPercent+"%</div><div>("+marksVal+"/"+totalMarks+" "+i18nJS("TXT_MARKS")+")</div>";
                    }
                    else if(globalGraphType=="HIGHEST"){
                        marksVal = maxScore;
                val = val == undefined?undefined:maxScorePercent;
                maxScorePercent = Math.round(maxScorePercent);
                        toolTipHtml += "<div class='big14'>"+i18nJS("TXT_MARKS")+"</div><div class='big16'>"+maxScorePercent+"%</div><div>("+marksVal+"/"+totalMarks+" "+i18nJS("TXT_MARKS")+")</div>";
                    }
                xAxisItems.push($(date).formatDate('dd-mm-yyyy'));
                    toolTipHtml += generateInstTAGraph.testLinkToolTip(testInfo.entity);
                    toolTipHtml += generateInstTAGraph.userAnalyticsLinkToolTip(testInfo.entity);
            if(val>100){val = 100;}
                    plotItems.push({val:val,infoHTML:toolTipHtml,callback:"default"});
                    if(marksVal)commonTests=true;
            }
            if(!commonTests){
                //showError("No Tests found");
                $(".instTAComparedUsers").addClass("nonner");  
            $(".instTACompare").removeClass("focused");
            instAnalytics.closeCompare();
            }
            return {xAxisItems:xAxisItems,plotItems:plotItems};
        },
    getRankPrecentile:function(rank,worst){
        //var p = (worst - rank)/worst * 100;
        if(worst<=1) return 1;
        var p = 1 + (rank - 1) * (100 - 1) / (worst - 1);
        return p;
    },
        createItemsByRank: function(start, end, testsInfo){
            var items=[],xAxisItems=[],plotItems=[],yAxisMax,yAxisMin;
            generateInstTAGraph.yAxisText=i18nJS("TXT_RANK");
            for(var i=end-1; i>=start; i--){
                    var testData = testsInfo[i],
            testInfo = testData.info,
            rankHTML="",rank=testData.rank;
                    if(testData.overallRank)rankHTML="<div>"+i18nJS("TXT_RANK")+" "+rank+"/"+testInfo.entity.totalAttempts+"</div>";
                    xAxisItems.push(testInfo.entity.name);
                    rank=(testData.overallRank)?generateInstTAGraph.getRankPrecentile(testInfo.rank,testInfo.entity.lastRank):undefined;
                    if(rank>yAxisMax)yAxisMax=rank;
                    if(rank<yAxisMin)yAxisMin=rank;
                    plotItems.push({val:rank,infoHTML:rankHTML,callback:"default"});
                }
            return {xAxisItems:xAxisItems,plotItems:plotItems,yAxisMax:yAxisMax,yAxisMin:yAxisMin,plotReverseY:true};
        },
        /*createItemsByCenterRank: function(start, end, testsInfo){
            var items=[];
            generateInstTAGraph.yAxisText="Rank";
            for(var i=start; i<end;i++){
                    var testObj = new Object;
                    var testData = testsInfo[i];
            var testInfo = testData.info;
                    var und= (testData == undefined || testInfo.entity.name == undefined || testData.rank==undefined || testInfo.measures.score==undefined);
                    testObj.itemName= und ?  undefined : testInfo.entity.name;
                    testObj.itemValue= und ?  undefined :generateInstTAGraph.getRankPrecentile(testInfo.rank,testInfo.entity.lastRank);
                    testObj.listData= und ?  undefined : ["Rank:"+testData.rank+"/"+testData.entity.totalAttempts,"Marks:"+testInfo.measures.score+"/"+testInfo.totalMarks];
                    items.push(testObj);
                }
        items.plotReverseY = true;
            return items;
        },*/
        createGraph: function(selector, xAxisAndDataSet){
            var yAxisMax=xAxisAndDataSet.yAxisMax||null;
            var yAxisMin=xAxisAndDataSet.yAxisMin||null;
            var  dataObj={
                xAxis:{title:"Tests",items:xAxisAndDataSet.xAxisItems,width:500,rotateTextBy:-90,textWidth:65,titlePosition:"SIDE",plotReverse:xAxisAndDataSet.plotReverseX?true:false},
                yAxis:{title:generateInstTAGraph.yAxisText,width:25,height:275,yAxisMax:yAxisMax,yAxisMin:yAxisMin,plotReverse:xAxisAndDataSet.plotReverseY?true:false},
                plotData:[{name:xAxisAndDataSet.name,items:xAxisAndDataSet.plotItems}]
            };
            // selector.createAreaGraph(dataObj);
        },
        storeTestIds: function(testInfos){
                testIds=[];
                for(var i=0;i<testInfos.length;i++){
                    testIds.push(testInfos[i].id);
                }
        }
};
function instTAGraphChanged(value,target,targetValue){
    generateInstTAGraph.yAxisText = $(target).data("yaxisText");
    var type = targetValue;
    generateInstTAGraph.createItemsFn = generateInstTAGraph.createItemsFnTypeList[type];
        var pageHolderM=$(target).closest(".pageHolderM")
        if(type=="RANK"){
            pageHolderM.find(".instTAshowRankTab").show();
            pageHolderM.find(".instTAshowHighestTab").hide();
        }else{
            pageHolderM.find(".instTAshowRankTab").hide();
            pageHolderM.find(".instTAshowHighestTab").show();
        }
    instAnalytics.refreshGraph();
}
