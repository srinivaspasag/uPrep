var qrAddContent=new(function($){
    var cmdsUploader,addContentPage,addContentPageMain,clickEvent="click",
    bodyClickEvent="click.addContent",addContentBtns;
    this.init=function(params){      
       //variables
       resetCmdsPages($("#cmdsAddContent"));
       addContentPage=$("#addContentPage");
       addContentBtns=addContentPage.children(".addContentBtns");
       addContentPageMain=addContentPage.children("#addContentPageMain");
       
       //events
       addContentPage.off(clickEvent)
       .on(clickEvent,".loadACPQuesnsUploadArea",loadACPQuesnsUploadArea)
       .on(clickEvent,".addcmdsQuestion",addcmdsQuestion)
       .on(clickEvent,".addMultipleCmdsQuestions",addMultipleCmdsQuestions)
       .on(clickEvent,".uploadcmdsDoc",uploadcmdsDoc)
       .on(clickEvent,".addcmdsVideo",addcmdsVideo)
       
       //add or upload video
       .on(clickEvent,".submitAddcmdsVideo",submitAddcmdsVideo)
       .on(clickEvent,".cancelAddcmdsVideo",cancelAddcmdsVideo)
       
       //target selection
       .on(clickEvent,".chooseContentTarget",chooseContentTarget)
       
       //upload              
       var addType=params.addType||"QUESTION_SET";       
       chooseAddContentType[addType]();
       
       $("body").off(bodyClickEvent)
       .on(bodyClickEvent,".savecmdsUploadedQuesns",savecmdsUploadedQuesns)
       .on(bodyClickEvent,".cancelcmdsUploadedQuesns",cancelcmdsUploadedQuesns)   
       .on(bodyClickEvent,".submitAddContentTarget",submitAddContentTarget)   
       
       var folderId;
       if(params.folderId){
           folderId=params.folderId;           
           addContentPage.data("folderId",folderId);
       }else{
           vReq.get("/qrresources/fetchRootFolderId",{},function(data){
               folderId=data.result;
               addContentPage.data("folderId",folderId);
           });
       }       
       
       fetchScripts([{fname:"uicomWidgets/tagging.js"},{fname:"uicomWidgets/fileuploader.js"}]);
    };
    var startLoader=showTopLoader;    
    var stopLoader=hideTopLoader;
    var closePopup=closecmdsPopup;
    var getReq=vReq.get;
    var postReq=vReq.post;       
    
    
    
    //target selection
    var chooseContentTarget=function(){
        preparemoveItemList("Add content to","submitAddContentTarget");
    };
    var submitAddContentTarget=function(){
        var popup=$(this).closest("#cmdsPopup");
        var moveWidgetdata=popup.find(".moveWidget").data();
        var targetFolderId=moveWidgetdata.finalValue;
        if(targetFolderId){
            addContentPage.data("folderId",targetFolderId);
            addContentPage.find(".uploadTargetDiv").html
            (getFolderTag(targetFolderId,moveWidgetdata.finalName)); 
            addContentPage.find(".chooseContentTarget").text("Choose Another Folder");
        }
        closePopup();           
    };
    
    //tabs toggle
    var chooseAddContentType={
        DOCUMENT:function(){
            uploadcmdsDoc(null,addContentBtns.children(".uploadcmdsDoc"));
        },
        VIDEO:function(){
            addcmdsVideo(null,addContentBtns.children(".addcmdsVideo"));
        },
        QUESTION_SET:function(){
            loadACPQuesnsUploadArea(null,addContentBtns.children(".loadACPQuesnsUploadArea"));
        },
        QUESTION:function(){
            addcmdsQuestion(null,addContentBtns.children(".addcmdsQuestion"));
        }                
    };    
    var loadACPQuesnsUploadArea=function(e,addQuestionSetBtn){
        $(".targetFolderDiv").css("visibility","visible");
        var $this=addQuestionSetBtn||$(this);
        changeActiveClass($this,"gButtonActive");
        setUploadAreaForQuestions();            
    };
    var addcmdsQuestion=function(e,addQuestionBtn){
        var $this=addQuestionBtn||$(this);
        changeActiveClass($this,"gButtonActive");
        startLoader();
        var successFn=function(data){
            addContentPageMain.html(data);
            stopLoader();
        };
        getReq("/qrAddContent/addQuestionPage",{},successFn);   
    };

    var addMultipleCmdsQuestions = function(e,addQuestionBtn){
        var $this=addQuestionBtn||$(this);
        changeActiveClass($this,"gButtonActive");
        startLoader();
        var successFn=function(data){
            addContentPageMain.html(data);
            $(".targetFolderDiv").css("visibility","hidden");
            stopLoader();
        };
        getReq("/qrAddContent/addMultipleQuestionsPage",{},successFn);
    }
    ;
    this.loadQuesAddArea=function(refresh){
        if((refresh!="null"|| refresh!="undefined") && refresh === 'multiple'){
            addMultipleCmdsQuestions();
        }
        else{
            addcmdsQuestion();
        }
    };
    
    //uploading question set
    var createcmdsUploader=function(){
        var uploader=$("#cmdsUploader");
        var u= new qq.FileUploader({
            element: uploader.get(0),
            action: '/qraddcontent/uploadQuestionset',
            params:{uploadFileParamName:"questionSetFile"},
            debug: true,
            allowedExtensions : ["docx"]
        });     
        u.onUploadDone=onUploadDone;
        u.onUploadProgress=onUploadProgress; 
        u.hasHTMLResp=true;
        uploader.find(".qq-upload-button").addClass("biggerRedButton");
        uploader.find(".qq-button-title").html("Choose File");
        return u;
    };    
    var onUploadDone=function(id,fileName,result){
        var xhr={responseText:result};
        try{
            var resp=$.parseJSON(xhr.responseText);
            setUploadAreaForQuestions();
            var err=resp.errorCode;
            if(err){
                if(resp.errorMessage){
                    err=resp.errorMessage;
                }
                showcmdsError(err);
            }else{
                showcmdsError("There is some problem in uploading.We are looking into it.\n\
                 Please refresh the page and try to upload again.");
            }            
        }catch(err){
            addContentPageMain.html(result);
            var questionSetId=addContentPageMain.find(".formInput[name=questionSetId]").val();
            initmcWidgetforCMDS(addContentPageMain.find(".mcWidget"),
            "/qraddcontent/getUploadedQSetQuesns",{questionSetId:questionSetId},false,true); 
            loadMJEqns(addContentPageMain.get(0));
	    setTimeout(function(){
	    	addContentPageMain.find("#fixedSec").css("height","initial");
	    },100);
        }
    };
    var onUploadProgress=function(percentDecimal,progressText){
        addContentPageMain.find(".uploadProgressDiv")
        .html("<span class='color8 smally'>Percentage Uploaded:</span>"+Math.round(percentDecimal*100)+" %");
    };
    
    
    
    //accepting and cancel quesns
    var savecmdsUploadedQuesns=function(){
        var fn=function(){
            setUploadAreaForQuestions("Questions saved Successfully");
        };
        saveCancelcmdsUploadedQuesns({shouldConfirm:true},fn,$(this));
    };    
    var cancelcmdsUploadedQuesns=function(){        
        var fn=function(){
          setUploadAreaForQuestions("Your previous upload is cancelled");  
        };        
        saveCancelcmdsUploadedQuesns({shouldConfirm:false},fn,$(this));
    };
    var saveCancelcmdsUploadedQuesns=function(extraParams,cbFn,$this){
	if($this.hasClass("btnDisabled")){ return;}
        var params=getFormValues(addContentPageMain);
        params.folderId=addContentPage.data("folderId");
        if(extraParams)$.extend(params,extraParams);
        startLoader();
	$this.addClass('btnDisabled').siblings().addClass('btnDisabled');
        postReq("/qraddcontent/submitUploadedQuesns",params,function(){
            stopLoader();
	    $this.removeClass('btnDisabled').siblings().removeClass('btnDisabled');
            if(extraParams.shouldConfirm){
                trackEventForGA("CMDSQUESTIONSET","ADD_CONTENT",params.questionsSetName);
            }
            cbFn();
        },function(){
            stopLoader();
	    $this.removeClass('btnDisabled').siblings().removeClass('btnDisabled');
	});         
    };
    var setUploadAreaForQuestions=function(stateText){
        stateText=stateText||'';
        stopLoader();
        addContentPageMain.html("<div class='grayHead margBot10'>"+stateText+"</div>\n\
        <div class='uploadContentDiv'><div id='cmdsUploader'></div><div class='uploadProgressDiv'>\n\
        </div></div>");
        addContentPageMain.find(".uploadProgressDiv").html("");
	addContentPageMain.find(".uploadContentDiv").before($("#addQuesSetSample").removeClass("nonner"));
        var fn=function(){cmdsUploader=createcmdsUploader();}
        fetchScripts([{fname:"uicomWidgets/fileuploader.js",cb:fn}]);        
    };
    
    
    
    //uploading docs
    var uploadcmdsDoc=function(e,addDocBtn){
        startLoader();
        var $this=addDocBtn||$(this);
        changeActiveClass($this,"gButtonActive");
        var successFn=function(data){
            addContentPageMain.html(data);
            stopLoader();
        };
        getReq("/uicomdocuments/uploadstart",{start:0,size:50,uploadContentType:"DOC"},successFn);                       
    };    
    
    
    //adding videos
    var addcmdsVideo=function(e,addVideoBtn){
        startLoader();
        var $this=addVideoBtn||$(this);
        changeActiveClass($this,"gButtonActive");
        var successFn=function(data){
            addContentPageMain.html(data);
            stopLoader();
        };
        getReq("/uicomdocuments/addVideo",{start:0,size:50,uploadContentType:"VIDEO"},successFn);                          
    };
    var submitAddcmdsVideo=function(){
        var $this=$(this),vVideoHolder=$this.closest(".vVideoHolder");
        var params=getFormValues(vVideoHolder);
        params.sourceId=addContentPage.data("folderId");
        var desc=vVideoHolder.find(".vVideoDesc").val().trim();
        if(desc!=""){
            params.description=desc;
        }        
        var domain=getUrlDomain(params.videoUrl);
        if(domain)params.srcLink=domain;
        startLoader();
        var successFn=function(){
            stopLoader();
            clearAddcmdsVideo($this);
        };
        postReq("/qraddcontent/createAddVideo",params,successFn);        
    };
    var cancelAddcmdsVideo=function(){
        clearAddcmdsVideo($(this));
    };
    var clearAddcmdsVideo=function($this){
        var vVideoHolder=$this.closest(".vVideoHolder");
        vVideoHolder.find(".cmdsAddVideoBtns").addClass("nonner");
        vVideoHolder.find(".vVideoUrlInput").val("");
        vVideoHolder.find(".vVideoInfoBody").html("");        
    };
})(jQuery);

var cmdsVideoInfoAdded=function(vVideoHolder){
    var btns=vVideoHolder.find(".cmdsAddVideoBtns");
    if(vVideoHolder.find(".vVideoContent").length>0){
        btns.removeClass("nonner");
    }else{
        btns.addClass("nonner");
    }
};
