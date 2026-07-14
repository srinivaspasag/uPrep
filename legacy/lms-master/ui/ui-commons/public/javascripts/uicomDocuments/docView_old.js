var commentWidgetRepliesClick=new Object();
var maxPageWidth=700;
var vblePaneHeight=$(window).height();
var docView=new(function($){
    var pageViewHolder,documentId,totalPagesInDoc;
    var areaWiseHighlight="AREA_WISE",lineWiseHighlight="LINE_WISE",initialPageLoadCount=2,
    isWebappDomain=false;
    this.init=function(params){
        viewPageType="DOC_VIEW";
        $("body").addClass("PL_DOC_VIEW");
        pageViewHolder=$("#pageViewHolder");   
        if(params.domain=="WEB_APP"){
            isWebappDomain=true;
        }
        var pageCount=params.pageCount;
        totalPagesInDoc=pageCount;
        documentId=params.docId;
        var pages=params.pages;
        if(pageCount){
           initLoadImagesAndComms(pageCount,pages);           
        }        
        
        
        var menuOptions=$("#docViewOptions");
        vblePaneHeight=$(window).height()-($("#topBarHolder").height()+menuOptions.height());
        //(menuOptions.height()+menuOptions.offset().top)
        
        
        //setting left sec max height
          var DTDocLeft=$("#DTDocLeft");
          var topBars=$("#topBarHolder").outerHeight(true);
          var leftSecContents=DTDocLeft.children(".DTDocLeftBody").height()-DTDocLeft.find("#leftSecContent").height();
          var leftSecHeight=$(window).height()-(topBars+leftSecContents);
          DTDocLeft.find("#leftSecContent").height(leftSecHeight-10);//10 for margin at the bottom        
    }
    var makeTag=makeHTMLTag;
    
    
    
    //page load utils
    var initLoadImagesAndComms=function(pageCount,pages){
        loadEmptyDivs(pageCount);
        var imgLimit=pageCount;
         if(pageCount>initialPageLoadCount)imgLimit=initialPageLoadCount;         
         for (var l=1;l<=imgLimit;l++){
            var page=pages[l-1];
            onImgDivLoad(l,page._id,page.image,page.hocr,page.highlights,page);
         }

         var page1=pageViewHolder.children("#page1");
         page1.addClass("pageInAction");            
         if(isWebappDomain){
            var comparams={pageId:pages[0]._id,docId:documentId,
                 start:0,size:10,orderBy:"timeCreated"};
             $.get("/doc/docComments",comparams,function(data){
                 var DPLSComments=$("#DPLSComments");
                 DPLSComments.html(data);
                 DPLSComments.find(".LMHandlerDiv").data("urlStr","/doc/docComments").data("size",25)
                 .data("allParams",comparams);
             });             
         }else{
            var contentsDiv=$("#DPLSContents");
            var successFn=function(data){
                contentsDiv.html(data);
            }
            var errorFn=function(){
                contentsDiv.html("<div class=userMessage>There was some error in\n\
                 fetching contents.</div>")
            }
            vReq.get("/qrDocuments/docContents",{docId:documentId},successFn,errorFn,null,false);           
         }          
    }
    var loadEmptyDivs=function(totalPages){
        var htm="";
        for(var m=1;m<=totalPages;m++){
            htm+="<div id='page"+m+"' class='docDivs'></div>";
        }
        pageViewHolder.append(htm);
    }
    
    var onImgDivLoad=function(pageNum,pageId,imageSrc,hocrUrl,highlights,pageData){        
        var page=getPageDiv(pageNum);
        var charQuality=pageData.charactersQuality,hocrQuality=pageData.hocrQuality;
        if(!charQuality)charQuality=0;
        if(!hocrQuality)hocrQuality=0;
        
        var highlightingType="";        
        if(hocrQuality>80)highlightingType=lineWiseHighlight;
        else highlightingType=areaWiseHighlight;
        
        page.data({pageId:pageId,imageSrc:imageSrc,hocrUrl:hocrUrl,hocrQuality:hocrQuality,
        charQuality:charQuality});
        var page$Data=page.data();
        if(isWebappDomain){
            page.html(makeTag("div",{"class":highlightingType+" highlighter"}));
        }        
        page.append(makeTag("img",{"class":"pageImage",src:"/public/images/loading.gif"})); 
        
        var i=new Image();
          i.onload=function(){              
              page.children("img").attr("src",imageSrc);
              var pageActualWidth=i.width;                  
              page$Data.pageActualWidth=pageActualWidth;
              page$Data.pageActualHeight=i.height;                  
              var scaleFactor=maxPageWidth/pageActualWidth;
              page.data("scaleFactor",scaleFactor);                   
              adjustPageHeight(page,i.height,scaleFactor);     
              if(isWebappDomain){
                 appendHighlights(page,highlights,scaleFactor);
              }   
          }
          i.src=imageSrc;          
        
        if(highlightingType==lineWiseHighlight&&isWebappDomain){
            //==>there is hocr      
               $.get("/uicomdocuments/hocrGetter",{url:hocrUrl},function(data){
                   page.append(makeTag("div",{"class":"hocrDivision"}).html(data));                                                        
               });
        }        
    }
    var appendHighlights=function(page,highlights,scaleFactor){
            var highlightsHolder=makeTag("div");
            for(var k=0;k<highlights.length;k++){                
                var highlight=highlights[k],hocrElements=highlight.hocrElements;
                var bBoxDiv=makeTag("div",{"class":"bBoxDiv",id:"bBoxDiv_"+highlight._id});
                for(var p=0;p<hocrElements.length;p++){
                    var hocrCoords=hocrElements[p];
                    var width=(hocrCoords.bottomRight.X-hocrCoords.topLeft.X)*scaleFactor;
                    var height=(hocrCoords.bottomRight.Y-hocrCoords.topLeft.Y)*scaleFactor;
                    var left=hocrCoords.topLeft.X*scaleFactor,top=hocrCoords.topLeft.Y*scaleFactor;
                    var hocrDiv=makeTag("div",{"class":"HOCRDiv",lang:top})
                    .css({width:width,height:height,top:top,left:left});                    
                    bBoxDiv.append(hocrDiv);
                }                                
                if(highlight.color){
                    bBoxDiv.addClass("hasColor").children().css("background-color","#"+highlight.color);
                }else{
                    bBoxDiv.addClass("commentHL");
                }
                highlightsHolder.append(bBoxDiv);
            }
            page.append(highlightsHolder.children()); 
    }
    var adjustPageHeight=function(page,pageActualHeight,scaleFactor){
        var h=pageActualHeight*scaleFactor;
        if(h>800){
            page.height(h);
        }
    }
    var getPageDiv=function(pageNum){
        return pageViewHolder.children("#page"+pageNum);
    }
    
    
    
    //scroll document utils
    onWindowScroll["DOC_VIEW"]=function(scrollType){
        var currentPage=getCurrentPage().pageNum;
        $("#docPageNumInput").val(currentPage);
        getPageDiv(currentPage).addClass("pageInAction")
        .siblings(".docDivs").removeClass("pageInAction");
        var docDetVar=$("#leftSecContent");
        if(currentPage!=docDetVar.attr("rel")){
            docDetVar.attr("rel",currentPage);
            insideOutClick();           
            setTimeout(function(){waitAndLoadComments(currentPage,docDetVar,scrollType)},750);
        }
    }    
    var waitAndLoadComments=function(currentPage,docDetVar,scrollType){
        if(docDetVar.attr("rel")==currentPage){
            //load the current page if not loaded
            if(pageViewHolder.children("#page"+currentPage).children("img").length==0){
                loadThisPage(currentPage);
            }             
            if(isWebappDomain){
               commentLoadAfterPageReq();  
            }
            //load boundary pages
            if(scrollType=="down"&&pageDownScrollFunc(currentPage)!=-1){
               loadThisPage(pageDownScrollFunc(currentPage));
            }else if(scrollType=="up"&&pageUpScrollFunc(currentPage)!=-1){
               loadThisPage(pageUpScrollFunc(currentPage));
            }
       }
    }    
    var loadThisPage=function(pageNum){
        if(!(parseInt(pageNum)<1||parseInt(pageNum)>totalPagesInDoc)){                
               $.get("/uicomdocuments/getPage",{docId:documentId,pageNumber:pageNum},function(data){
                    var result=data.result;
                   onImgDivLoad(pageNum,result.pageId,result.image,result.hocr,result.highlights,result.page);                   
             });
        }
    } 
    var pageDownScrollFunc=function(currentPage){
        var j=1,pageToLoad=-1;
            for(j=1;j<=5;j++){
                var page=pageViewHolder.find("#page"+(currentPage+j));
                if(page.children("img").length==0&&totalPagesInDoc>=(currentPage+j)){
                    pageToLoad=currentPage+j;
                    break;
                }
        }
        return pageToLoad;
    }
    var pageUpScrollFunc=function(currentPage){
        var j=1,pageToLoad=-1;
            for(j=1;j<=3;j++){
                var page=pageViewHolder.find("#page"+(currentPage+j));
                if(page.children("img").length==0&&(currentPage-j)>=1){
                    pageToLoad=currentPage-j;
                    break;
                }
        }
        return pageToLoad;
    }    
    var loadBoundaryPages=function(newPageNo){
         newPageNo=parseInt(newPageNo);         
         if($("#page"+(newPageNo-1)+" img").attr("class")==undefined)loadThisPage((newPageNo-1));
         //if($("#page"+(newPageNo-2)+" img").attr("class")==undefined)loadThisPage((newPageNo-2));
         if($("#page"+(newPageNo+1)+" img").attr("class")==undefined)loadThisPage((newPageNo+1));
         //if($("#page"+(newPageNo+2)+" img").attr("class")==undefined)loadThisPage((newPageNo+2));
    }
    var requestForPage=function(pageNum){
        var topOffset=0;
        topOffset=getPageDiv(pageNum).position().top;        
        if(topOffset>100)$(window).scrollTop(topOffset-50);
        else $(window).scrollTop(topOffset);
    }   
    var commentLoadAfterPageReq=function(){
        var LMHandlerDiv=$("#DPLSComments .LMHandlerDiv");
        var params=LMHandlerDiv.data("allParams");
        params.pageId=pageViewHolder.children(".pageInAction").data("pageId");
        if(!params.pageId){
            return;
        }
        $.get("/Doc/docCommItems",params,function(data){
            LMHandlerDiv.html(data);
        });
    }    
    
    
    
    //interactions like navigation
    this.clickOnToc=function(pageNum,pageWise){
        if(isNaN(parseInt(pageNum))&&pageNum!='')
        alert("please enter a number");
        else if((parseInt(pageNum)<=0||parseInt(pageNum)>parseInt($("#docTotalPages").text()))&&!pageWise)
            alert("The page you are trying to access is either unavailable/invalid");
        else if($("#page"+pageNum+" "+"img").attr("class")!=undefined){
                requestForPage(pageNum);
                loadBoundaryPages(pageNum);
         }
         else{
             //letting the comments be loaded through onqindowscroll itself
             loadThisPage(parseInt(pageNum));
             requestForPage(pageNum);
             loadBoundaryPages(pageNum);
         }
    }    
})(jQuery);

    //for doc viewer exclusively


    $("#docPageNumInput").live('keyup',function(e){
       if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
                  docView.clickOnToc($("#docPageNumInput").val());
          }
    });
    $(".DVPageUp").live('click',function(e){
        var p=getCurrentPage().pageNum;
         docView.clickOnToc(p-1,"yes");
    });
    $(".DVPageTop").live('click',function(e){
         docView.clickOnToc(1,"yes");
    });
    $(".DVPageDown").live('click',function(e){
        var p=getCurrentPage().pageNum;
         docView.clickOnToc(p+1,"yes");
    });
        
        
        
    //for highlighting
  var x1,y1,gx1,gy1,heightH=0,widthH=0,pageOwner=1,xh1,yh1,xh2,yh2;
  //reset values on mouse down to preserve the context
  $(".highlighter").live('click',function(){
      var parentId=$(this).parent().parent().attr("id").substring(15);
      $("#searchDocument_"+parentId).focus();
  });
  $(".highlighter").live('mousedown',function(e) {
      var pageViewHolder=$("#pageViewHolder");
      var page=$(this).parent();
     //if($("#"+pageId+" .notConfirmed").hasClass("hasColor"))$("#"+pageId+" .notConfirmed").attr("id","");
     page.find(".notConfirmed").remove();
        insideOutClick();
        page.children(".highlighter").html('<div class="light" style="opacity:0.5;border: 1px dotted #404040;position:absolute;">');
        gx1 = e.pageX-pageViewHolder.offset().left;
        gy1 = e.pageY-pageViewHolder.offset().top;
        pageOwner=onWhichPage(gy1);
        y1=gy1-page.position().top;
        x1=gx1;
        page.find(".light").css("top",y1+"px").css("left",x1+"px");
  });
  $(".highlighter").live('mousemove',function(e) {
      var pageViewHolder=$("#pageViewHolder");
      var page=$(this).parent();
      page.find(".light").css("width",Math.abs(e.pageX-pageViewHolder.offset().left - x1))
      .css("height",Math.abs(e.pageY-pageViewHolder.offset().top -gy1));
       xh1=x1,yh1=y1;
//          if((startedHL&&$("#page"+pageOwner+" .hocrDivision").html()=="")||$("#page"+pageOwner).attr("rel")==''||$("#page"+pageOwner).attr("rel")==undefined){
//              alert("Please refresh the page or wait for sometime, since the image did not load properly due to some techincal reasons");
//              startedHL=false;
//              return false;
//          }
      if((e.pageX-pageViewHolder.offset().left)<x1){
          page.find(".light").css("left",(e.pageX-pageViewHolder.offset().left)+"px");
          xh1=(e.pageX-pageViewHolder.offset().left);
      }
      if((e.pageY-pageViewHolder.offset().top)<gy1){
          page.find(".light").css("top",(e.pageY-pageViewHolder.offset().top)+"px");
          yh1=(e.pageY-pageViewHolder.offset().top);
      }
       xh2=(xh1+page.find(".light").width()),yh2=(yh1+page.find(".light").height());
  });
  $(".highlighter").live('mouseup',function() {
      var dimens=getElDimens;
      var highlighter=$("#page"+pageOwner+" .highlighter");
      if($(this).hasClass("LINE_WISE")){
          var t=parseFloat($("#page"+pageOwner).data("scaleFactor"));
               $("#page"+pageOwner+" .hocrDivision .ocr_page .ocr_carea").each(function(){
                   var block="#page"+pageOwner+" #"+$(this).attr("id");
                   var cordis=paraCoord($(this).attr("title"),t);
                   if(!(cordis[3]<yh1||(cordis[1]>yh2)))
                    {
                       $(block+" .ocr_line").each(function(){
                           var paris=paraCoord($(this).attr("title"),t);
                            if(!(paris[3]<yh1||(paris[1]>yh2))){
                               var re="<div class='HOCRDiv "+$(this).attr("id")+"' style='position:absolute;top:"+paris[1]+"px;left:"+paris[0]+"px;width:"+(paris[2]-paris[0])+"px;height:"+(paris[3]-paris[1])+"px'></div>";
                               $("#page"+pageOwner+" .highlighter").append(re);
                            }
                       });
                    }
// var re="<div class='HOCRDiv "+$(this).attr("id")+"' style='position:absolute;top:"+paris[1]+"px;left:"+xh1+"px;width:"+(paris[2]-xh1)+"px;height:"+(paris[3]-paris[1])+"px'></div>";
               });
          var hocrDivsEls=highlighter.find(".HOCRDiv");
          var firstEl=hocrDivsEls.first(),firstElDimens=dimens(hocrDivsEls.first());
          firstEl.css({width:firstElDimens.width+firstElDimens.x1-xh1,left:xh1});
          var lastEl=hocrDivsEls.last(),lastElDimens=dimens(hocrDivsEls.last());          
          lastEl.css("width",xh2-lastElDimens.x1);
      }
      else{
          highlighter.append("<div class='HOCRDiv' style='position:absolute;top:"+yh1+"px;left:"+xh1+"px;width:"+(xh2-xh1)+"px;height:"+(yh2-yh1)+"px'></div>");
      }
      var pageId=$(this).parent().attr("id");
      heightH=$("#"+pageId+" .light").height();
      widthH=$("#"+pageId+" .light").width();
      $("#"+pageId+" .light").remove();
      var hocrDivs=highlighter.children();

      highlighter.html("");
      if(heightH>0&&widthH>0){
        $("#page"+pageOwner).append(makeHTMLTag("div",{"class":"notConfirmed"}).html(hocrDivs))
        $("#highlightOptions").css("top",(gy1)).css("left",(x1-60)).attr("rel",(gy1)).attr("lang",(x1-60));
        var highlightOptions=$("#highlightOptions");
        addToggler(highlightOptions,hocrDivs);
        highlightOptions.children().hide();
        highlightOptions.children(".DVOptions").show();        
    }
  });
  $(".colorPicker span").live('click',function(){
        var parentId=$(this).parent().attr("rel");
        var newColor=$(this).attr("rel");
        var bb = hocrCoords();
        $.post("/doc/addhighlight",{pageId:$("#pageViewHolder .pageInAction").data("pageId"),docId:parentId,
            bb:bb,color:$(this).attr("rel"),scope:"PRIVATE"},function(data){
            if(data.errorMessage==""){
               $("#pageViewHolder .pageInAction .notConfirmed").attr("id","bBoxDiv_"+data.result.highlightId);
            }
            $("#pageViewHolder .pageInAction .notConfirmed .HOCRDiv").css("background-color","#"+newColor);
            $("#pageViewHolder .pageInAction .notConfirmed").addClass("hasColor").removeClass("notConfirmed");
        });
        insideOutClick();
   });
   $(".submitCommentHL").live('click',function(){
       var $this=$(this);
       var inputEl=$this.siblings(".docCommentInput");
       var textContent=inputEl.val();
       if (textContent!='') {
            textContent=textContent;
            //var tagStr=commentConvert(textContent).tags;
            var bb;
//            //var scope=$("#commentType_"+parentId).val();
//            if($("#pageViewHolder .pageInAction .notConfirmed").hasClass("hasColor")){
//                var notConfirmHTML=$("#pageViewHolder .pageInAction .notConfirmed").html();
//                $("#pageViewHolder .pageInAction .notConfirmed").removeClass("hasColor").attr("id","");
//                $("#pageViewHolder .pageInAction").append("<div class='notConfirmed' class='hasColor commentHL'>"+notConfirmHTML+"</div>");
//                bb = hocrCoords();
//                postComment(bb,textContent,tagStr,scope,parentId,$(this));
//            }
//            else {
//                bb = hocrCoords();
//                postComment(bb,textContent,tagStr,scope,parentId,$(this));
//            }
                bb = hocrCoords();
                postComment(bb,textContent);
        insideOutClick();
      }else{
          showError("Please enter something and submit.");
      }
      inputEl.val("");
   });
function getCurrentPage(){
     var page={pageNum:1,pageId:''};
     $("#pageViewHolder .docDivs").each(function(){
        if($(window).scrollTop()+vblePaneHeight>=$(this).position().top+(vblePaneHeight/2)){
            page.pageNum=parseInt($(this).attr("id").substring(4));
            page.pageId=$(this).data("pageId");
         }
      });
     return page;
}

    function onWhichPage(y1){
              var foundPage=false,page=1;
                $("#pageViewHolder .docDivs").each(function(){
                    if(y1< $(this).position().top){
                       foundPage=true;
                        page=parseInt($(this).attr('id').substring(4))-1;
                        return false;
                    }
                });
                if(foundPage==false)page=$("#pageViewHolder .docDivs").length;
                return page;
    }

    var getElDimens=function(hocrDiv){
    //    var p=hocrDiv.attr("style").split(";");
    //    var stylesMap={};
    //    for(var k=0;k<p.length;k++){
    //        var styleAttr=p[k].split(":");
    //        if(styleAttr.length>1){
    //            styleAttr[1].trim();
    //            var dimenLength=styleAttr[1].length;            
    //            stylesMap[styleAttr[0].trim()]=parseFloat(styleAttr[1].substring(0,dimenLength-2));
    //        }
    //    }    
        return getScaledHocrDivDimens(hocrDiv,1);
    }
    var getScaledHocrDivDimens=function(hocrDiv,scaleFactor){
        return {width:(hocrDiv.width()*scaleFactor),height:(hocrDiv.height()*scaleFactor),
            x1:(parseInt(hocrDiv.css("left"))*scaleFactor),y1:(parseInt(hocrDiv.css("top"))*scaleFactor)};    
    }
    function paraCoord(str,t){
           var d=[];
           var spaceInd=str.indexOf(" ");
           while(spaceInd!=-1&&spaceInd!=str.length){
               var sp=str.indexOf(" ",spaceInd+1);
               if(sp==-1)sp=str.length;
               d.push(parseFloat(str.substring(spaceInd,sp))*t);
               spaceInd=sp;
           }
           return d;
    }
    function hocrCoords(){
        var bbb=[];
        var page=$("#pageViewHolder .pageInAction");
        var dimens=getElDimens;
        var Hfactor=page.data("scaleFactor");
            page.find(".notConfirmed .HOCRDiv").each(function(){
                var coords=dimens($(this));    
                var xdh1=coords.x1/Hfactor;
                var ydh1=coords.y1/Hfactor;
                var xdh2=(coords.width/Hfactor)+xdh1;
                var ydh2=(coords.height/Hfactor)+ydh1;
                bbb.push(ydh1+","+xdh1+","+ydh2+","+xdh2);
            });
            return bbb;
    }    


















   //doc viewer options like higglight comment bookmark
  $(".docOptColorDiv").live('click',function(){
    var notConfirmed=$("#pageViewHolder .notConfirmed");
    if(notConfirmed.length>0&&notConfirmed.children().length>0){
        var target=$("#docViewOptions  .colorPicker");
        addToggler(target,$(this));                
    }
    else{
        showError("please highlight a portion of the document first");
    }
   });
  $(".docOptColorFlyDiv").live('click',function(e){
      e.stopPropagation();
      insideOutClick();
      var notConfirmed=$("#pageViewHolder .notConfirmed");            
      var colorPicker=$(this).closest("#docViewer").children(".colorPicker");
      var dimens=getElDimens($("#highlightOptions"));
      addToggler(colorPicker,notConfirmed.children());
      colorPicker.css({top:dimens.y1,left:dimens.x1});
   });   
  $(".docOptCommDiv").live('click',function(e){
      e.stopPropagation();
    var notConfirmed=$("#pageViewHolder .notConfirmed");
    var highlightOptions=$("#highlightOptions");    
    if(notConfirmed.length>0&&notConfirmed.children().length>0){
      
      insideOutClick();        
      var combox=$("#docViewer").children("#docOptCommBox");
      var dimens=getElDimens($("#highlightOptions"));
      addToggler(combox,notConfirmed.children());
      var notConfirmedHocrs=notConfirmed.children();
      var extraHeight=notConfirmedHocrs.height()*notConfirmedHocrs.length+60;
      combox.css({top:(dimens.y1+extraHeight),left:dimens.x1});          
    }
    else{
        $("#leftSecContent .inputerArea").focus();
    } 
   });
  $(".docOptCommFlyDiv").live('click',function(e){
      e.stopPropagation();
      insideOutClick();
      var notConfirmed=$("#pageViewHolder .notConfirmed");            
      var combox=$(this).closest("#docViewer").children("#docOptCommBox");
      var dimens=getElDimens($("#highlightOptions"));
      addToggler(combox,notConfirmed.children());
      var notConfirmedHocrs=notConfirmed.children();
      var extraHeight=notConfirmedHocrs.height()*notConfirmedHocrs.length+60;
      combox.css({top:(dimens.y1+extraHeight),left:dimens.x1});    
   });   
 $(".docOptFS").live('click',function(){
      var parentId=$(this).attr("id").substring(9);
      if(!$("#DTDoc1002").hasClass("DTDoc1002FS"))
            fullScreenView();
      else  fullScreenExit(parentId);
   });
   $(".docOptZoomClass").live('click',function(){
       var FSActive=false,wid=parseInt($("#pageViewHolder .docDivs .pageImage").width());
       if($("#DTDoc1002").hasClass("DTDoc1002FS"))FSActive=true;
       var newwidth=wid*1.14;
        if(!FSActive){
            if(wid<870){
                newwidth=870;
                $("#DTDocRight").css("width","40px");
            }
            else fullScreenView();
        }
        imgAndHLScaling(newwidth);
        $("#pageViewHolder .notConfirmed").remove();
   });
$(".docOptBoomClass").live('click',function(){
         var FSActive=false,parentId=$(this).attr("id").substring(11);
         var wid=parseInt($("#pageViewHolder .docDivs .pageImage").width());
       if($("#DTDoc1002").hasClass("DTDoc1002FS"))FSActive=true;
        var newwidth=wid/1.14;
        if(FSActive){
            if(wid<750){
                fullScreenExit(parentId);
                restoreDocSecs();
                newwidth=maxPageWidth;
            }
            else if(wid>=760&&wid<900)newwidth=maxPageWidth;
        }
        else {
            newwidth=maxPageWidth;
            restoreDocSecs();
        }
        $("#pageViewHolder .pageImage").width(newwidth);
        var scaler=getScaledHocrDivDimens;
        $("#pageViewHolder .docDivs").each(function(){
            var newHFactor=newwidth/parseFloat($(this).children("img").attr("lang"));
            $(this).attr("rel",newHFactor);
            $(this).children(".bBoxDiv").each(function(){
                $(this).children(".HOCRDiv").each(function(){
                    var FS=1/1.14;
                    if(newwidth==maxPageWidth)FS=newHFactor*parseFloat($(this).attr("lang"))/parseFloat($(this).css("top").substring(0,$(this).css("top").length-2))
                    var z=scaler($(this),FS);
                    $(this).css({left:z.x1,top:z.y1,width:z.width,height:z.height});
                });
            });
        });
        $("#docViewer").css("width",newwidth+"px");
        $("#DTDocMiddle").css("width",newwidth+"px");
        $("#pageViewHolder .notConfirmed").remove();
   });
  $(".docOptBM").live('click',function(){
      var bm=$(this).children("#BMDiv");
      addToggler(bm,$(this));     
   });
   $(".submitBM").live('click',function(e){
        e.stopPropagation();
        var $this=$(this);
        var bmDiv=$(this).closest("#BMDiv");
        var docId=$(this).closest("#docViewPage").data("docId");
        var inputEl=bmDiv.find(".BMInput");
        var title=inputEl.val();
        $this.val("Saving..");
        if(title!=""){                        
            $.post("/doc/addBookmark",{docId:docId,pageId:$("#docViewer .pageInAction").data("pageId")
                ,title:title},function(data){
                inputEl.val("");
                $this.val("Save");
                insideOutClick();
            });
        }else{
            showError("Please enter something to save.");
        }
   });
    $(".docContentsSpan").live('click',function(){
       var docId=$("#docViewPage").data("docId");
       $("#docDetails").children(".bodi").html("<div class='smallLoader centry'><img src='/public/images/loading.gif' alt='loading..' /></div>");
        docDetMainMenu("#docDetails",".docContentsSpan");
        $.get("/doc/docContents",{docId:docId},function(data){
                $("#docDetails").children(".bodi").html(data);
                $("#docContents .bodi").css("height",parseFloat($(window).height()-223)+"px");
        });
   });
function restoreDocSecs(){
        $("#DTDocRight").css("width","245px");
}
function fullScreenView(){
        restoreDocSecs();
        $("#topBarHolder,#searchContentHeader,#DTDocRight").css("display",'none');
        $("#docOptFS").addClass("activeDocOptFS");
        $("#pageViewHolder .notConfirmed").remove();
        $("#DTDoc1002").addClass("DTDoc1002FS");
}
function fullScreenExit(parentId){
        $("#topBarHolder,#searchContentHeader,#DTDocRight").css("display",'block');
        $("#docOptFS").removeClass("activeDocOptFS");
        imgAndHLScaling(maxPageWidth);
        $("#pageViewHolder .notConfirmed").remove();
        $("#DTDoc1002").removeClass("DTDoc1002FS");
        loadCompleteComment(parentId);
}
function imgAndHLScaling(newwidth){
        $("#pageViewHolder .pageImage").width(newwidth);
        var scaler=getScaledHocrDivDimens;
        $("#pageViewHolder .docDivs").each(function(){
            var newHFactor=newwidth/parseFloat($(this).children("img").attr("lang"));
            $(this).attr("rel",newHFactor);
//                $(this).children(".notConfirmed").children(".HOCRDiv").each(function(){
//                    var FS=1.14;
//                    if(newwidth==maxPageWidth)FS=newHFactor*parseFloat($(this).attr("lang"))/parseFloat($(this).css("top").substring(0,$(this).css("top").length-2));
//                     var z=calcCoord(this, FS);
//                       $(this).css("top",z[0].y1+"px").css("left",z[0].x1+"px").css("width",z[0].width+"px").css("height",z[0].height+"px");
//                });
            $(this).children(".bBoxDiv").each(function(){
                $(this).children(".HOCRDiv").each(function(){
                var FS=1.14;
                if(newwidth==maxPageWidth)FS=newHFactor*parseFloat($(this).attr("lang"))/parseFloat($(this).css("top").substring(0,$(this).css("top").length-2));
                    var z=scaler($(this),FS);
                    $(this).css({left:z.x1,top:z.y1,width:z.width,height:z.height});
                });
            });
        });
        $("#docViewer").css("width",newwidth+"px");
        $("#DTDocMiddle").css("width",newwidth+"px");
}



















//right section
//right section top tabs

  $(".docCommentsSpan").live('click',function(){
      var params={pageId:$("#docViewer .pageInAction").data("pageId"),
            start:0,size:10,orderBy:"timeCreated"};
        reorderDOC_VIEW_Tabs($(this),$("#DPLSComments"),$("#DPLSComments"),"/Doc/docComments",params);
  });
  $(".docReviewsSpan").live('click',function(){
      var params={rootId:$(this).closest(".DTDocLeftBody").data("docId"),rootType:"DOCUMENT",type:"REVIEW",
            start:0,size:10,orderBy:"timeCreated"};
      reorderDOC_VIEW_Tabs($(this),$("#DPLSReviews"),$("#DPLSReviews .LMHandlerDiv"),"/Widgets/reviewItems",params);
   });
  $(".docInfoSpan").live('click',function(){
        var params={entityType:"DOCUMENT",entityId:$(this).closest(".DTPLLeftBody").data("plId"),start:0,size:5};
        reorderDOC_VIEW_Tabs($(this),$("#DPLSInfo"),$("#DPLSInfo .DPLSPeoplesHolder"),"/doc/getDocPeople",params);
   });
  $(".showcmdsDocContents").live('click',function(){       
        reorderDOC_VIEW_Tabs($(this),$("#DPLSContents"),null,null);
   });   
   
   function reorderDOC_VIEW_Tabs($this,nonnerDiv,targetDiv,urlStr,params){
        var a="leftSecActiveTab",DTPLLeftBody=$this.closest(".DTDocLeftBody");
        $this.addClass(a).siblings().removeClass(a);
        nonnerDiv.removeClass("nonner").siblings(".docContentDiv").addClass("nonner");
        var allParams=params||{};
        allParams.docId=DTPLLeftBody.data("docId");
        if(targetDiv&&targetDiv.html()==""&&urlStr){
            $.get(urlStr,allParams,function(data){
                targetDiv.html(data);
                var LMHandlerDiv=targetDiv.find(".LMHandlerDiv");
                if(LMHandlerDiv.length>0){
                    LMHandlerDiv.data("urlStr",urlStr).data("size",15).data("allParams",params);
                }
            });
        }
   }




   //for comments section
   $(".submitDocPageComment").live('click',function(){
          var inputer=$(this).closest(".inputer");
          var baseType="DOCUMENT",baseId=$(this).closest("#docViewPage").data("docId");
          var rootType="PAGE",rootId,inputEl=inputer.find("textarea"),textContent=inputEl.val().trim();
          if(textContent!=''){
            rootId=$("#pageViewHolder .pageInAction").data("pageId");
            $.post("/Widgets/postComment",{rootId:rootId,baseId:baseId,baseType:baseType,
                rootType:rootType,textContent:textContent},function(data){
                appendDocPageComment(data,textContent,rootId);                
            });
            inputer.removeClass("activeInputer");
            inputEl.val("");
          }
   });
   var appendDocPageComment=function(data,textContent,rootId){       
        var commItem=docPageCommItemSample.children().clone(true);
        commItem.find(".docPageCommItemText").text(textContent);
        commItem.data("rootId",rootId);
        var LMHandlerDiv=$("#DPLSComments").children(".LMHandlerDiv");
        if(LMHandlerDiv.children(".userMessage").length>0){
            LMHandlerDiv.html(commItem);
        }else{
            LMHandlerDiv.prepend(commItem);                
        }        
        commItem.find(".commReviewVoteDiv").data("commentId",data.result.commentId);
        commItem.find(".upVoteItem").data("entityId",data.result.commentId);       
   }
   $(".docPageCommItem .postShortContent").live('mouseover',function(){
       var hlId=$(this).closest(".docPageCommItem").data("rootId");
        $("#bBoxDiv_"+hlId).css("display","block");
   });
   $(".docPageCommItem .postShortContent").live('mouseout',function(){
        $("#pageViewHolder .docDivs div.commentHL").css("display","none");
   });
    function myCommentsStr(data,parentId){
        var str="";
        var comments=data.result.comments;
        var commentNo=data.result.totalHits;
           if(data.errorMessage==''&&comments.length>0){
                    $.each(comments,function(i,com){
                        str+=returnComment(com,parentId);
                    });
            }
            else str+="<div class='userMessage'>There are no comments to be shown</div>";
        return {str:str,count:comments.length};
    }
    function commentConvert(str){
             var tags=new Array();
                      var pos =str.indexOf("#"),space,coma,line,tag="";
                        while ( pos!= -1 ) {
                         space=str.indexOf(" ",pos+1);
                         coma=str.indexOf(",",pos+1);
                         line=str.indexOf("\n",pos+1);
                         if(line!=-1&&(coma!=-1&&coma<line)){
                             tag=str.substring(pos,coma);
                         }
                         else if(line!=-1&&(space!=-1&&space<line)){
                             tag=str.substring(pos,space);
                         }
                         else if(line!=-1)tag=str.substring(pos,line);
                         else if(coma!=-1)tag=str.substring(pos,coma);
                         else if(space!=-1)tag=str.substring(pos,space);
                         else tag=str.substring(pos);
                          tags.push(tag);
                          str=str.replace(tag,"<a class='clickableTag'>&tag"+tag.substring(1,tag.length)+"</a>");
                          pos = str.indexOf("#",pos+1);
                        }
                        str=str.replace(/\>\&tag/gim,">#");
                        return  {comment:str,tags:tags};
        }
    function commentConvertAfter(parentNameId){
        $("#"+parentNameId+" .docViewComment").each(function(){
            var CWCommText=$(this).find(".CWCommText");
            CWCommText.html(parseRespComment(CWCommText.html()));
        });
    }
    function parseRespComment(comment){
        return (morify(urlify(commentConvert(comment))));
    }
    function postComment(bb,textContent){
        //tagstr is deprecated for now,scope is public by default.For highlighiting it is private by default
        $.post("/doc/addhighlight",{pageId:$("#pageViewHolder .pageInAction").data("pageId"),
            docId:$("#docViewPage").data("docId"),"bb":bb,textContent:textContent,
            tags:[],scope:"PUBLIC"},function(data){
            if(data.errorMessage==""){
                $("#pageViewHolder .pageInAction .notConfirmed").removeClass("notConfirmed")
                .addClass("commentHL").attr("id","bBoxDiv_"+data.result.highlightId);
            }
            appendDocPageComment(data,textContent,data.result.highlightId);
         });
     }
    function loadDocPageComments(parentId,CWMyComments,start,size,replace,urlStr){
        var query,orderBy;
               if($("#commentSearchDiv .searchedHashTag div").html()!='')
                   query=$("#commentSearchDiv .searchedHashTag div").html();
               if($("#sortComments").val()!="-1"&& $("#sortComments").val()!="media") orderBy=$("#sortComments").val();
               if(replace)smallLoader(CWMyComments);
               else CWMyComments.append("<div class='smallLoader'></div>");
                $.get(urlStr,{pageId:$("#docViewer .pageInAction").data("pageId"),docId:parentId,start:start,query:query,orderBy:orderBy,size:size},function(data){
                        if(replace)$("#docCommMCX_"+parentId).html(myCommentsStr(data,parentId).str);
                        else {
                            CWMyComments.find(".smallLoader").remove();
                            CWMyComments.append(myCommentsStr(data,parentId).str);
                        }
                });
    }
    function loadMyDocComments(parentId,start,size,replace){
                if(replace)loadingSmallImg("#docConMC_"+parentId);
               else $("#docConMC_"+parentId).append("<div class='loadMore'><img src='/public/images/loading.gif' alt='loading..'></div>");
            $.get("/doc/docMyComments",{pageId:$("#docViewer .pageInAction").data("pageId"),docId:parentId,start:start,size:size,orderBy:"time"},function(data){
                    if(replace)$("#docConMC_"+parentId).html(myCommentsStr(data,parentId).str);
                    else{
                            if(data.result.length>0)$("#docConMC_"+parentId+" .loadMore").remove();
                            else $("#docConMC_"+parentId+" .loadMore img").remove()
                            $("#docConMC_"+parentId).append(myCommentsStr(data,parentId).str);
                    }
            });
    }


//for toc and bm
      $(".showContents").live('click',function(){
          var docId=$(this).closest("#docViewPage").data("docId");
          var conBody=$("#docContentsBody");
          addToggler(conBody,$(this));
          if(conBody.html()==""){
            conBody.html("<div class='smallLoader'></div>");
            $.get("/uicomdocuments/docContents",{docId:docId},function(data){
                    conBody.html(data);
                    var docConTOC=conBody.find("#docConTOC");
                    docConTOC.height($(window).height()-150);
            });
          }else if(!conBody.find(".docConBM").hasClass("nonner")){
            var params={docId:$("#docViewPage").data("docId")};
            reorderDocContentsTabs($(".docConBMSpan"),"#docConBM","/doc/docBM",params);              
          }
       });
  $(".docConTOCSpan").live('click',function(){
        reorderDocContentsTabs($(this),"#docConTOC");
   });
  $(".docConBMSpan").live('click',function(){
      var params={docId:$(this).closest("#docViewPage").data("docId")};
      reorderDocContentsTabs($(this),"#docConBM","/doc/docBM",params);
   });
   function reorderDocContentsTabs($this,targetDivId,urlStr,params){
        $this.addClass("activeTab").siblings().removeClass("activeTab");
        var docContents=$this.closest("#docContentsBody");
        var targetDiv=docContents.find(targetDivId);
        targetDiv.removeClass("nonner").siblings().addClass("nonner");        
        if(urlStr){
            targetDiv.html("<div class='smallLoader'></div>");
            $.get(urlStr,params,function(data){
               targetDiv.html(data);
            });
        }
   }
  $(".tocTextClickable").live('click',function(){
     var pageNum= parseInt($(this).children(".conTocNum").text());
     docView.clickOnToc(pageNum);
     insideOutClick();
  });
  $(".clickBM").live('click',function(){
        var pageNum=parseInt($(this).parent().children("td").last().html());
        docView.clickOnToc(pageNum);
        insideOutClick();
   });
  $(".deleteBM").live('click',function(e){
        e.stopPropagation()
        $.post("/doc/removeBookmark",{pageId:$(this).parent().data("pageId"),
            bookmarkId:$(this).parent().data("bmId")},function(data){            
        });
        $(this).closest("tr").remove();
   });




   //for reviews section

    function alignReplyBox(comm){
            var u=comm.offset();
            var t=($(window).height()-(u.top-$(window).scrollTop()));
            var contTop=u.top,contLeft=u.left+comm.width()+20;
            if($("#repliesBoxContainer").height()+40>t){
                contTop=u.top-($("#repliesBoxContainer").height()+40-t);
            }
            $("#repliesBoxContainer").css({top:contTop,left:contLeft});
            var ptrTop=u.top+13-contTop;
            $("#repliesBoxPtr").css({top:ptrTop});
    }

	

openTogglerCallback["removeactcompost"]=function(){
    $(".actComPost").removeClass("actComPost");
}

//comment widget of docviewer
function docViewRepliesClick(comment,fullCommentHTML,replyHTML,$this){
    $("#repliesBox").html("<div class='activeComment'>"+comment.html()+"</div>"+fullCommentHTML);
    //disabling voteup and reply count in replies box;    
    $("#repliesBox .commReviewVoteDiv,#repliesBox .docCommentNo").remove();
    
    $("#repliesBox .CWRepliesDiv").html(replyHTML);
    addToggler($("#repliesBoxContainer"),$this);
    if($("#repliesBox").height()>($(window).height()-100)){
        $("#repliesBox").css("height",parseInt($(window).height()-100)+"px");
    }
    alignReplyBox(comment);
}
$(".docCommentNo").live('click',function(){
    var comment=$(this).closest(".docPageCommItem"),$this=$(this);
    var fullCommentHTML="<div class='CWRepliesDiv'><img class='removeLoader SDL' src='/public/images/loading.gif' alt='loading..'>\n\
   </div><textarea class='CWReplyInput margHalfTop' placeHolder='type and submit to reply'></textarea>\n\
    <input type='submit' class='CWPostReply blueSubmitButton margLeft5 margBot10 margTop' value='Submit'/>\n\
    <a onclick=insideOutClick()>Close Replies</a>";
   var replyHTML='';
   $.get("/doc/getReplies",{parentId:comment.data("commentId"),orderBy:"time",sortOrder:"ASC",start:0,size:25},function(data){
            var comments=data.result.comments;
            if(data.errorMessage==''&&comments.length>0){
                $.each(comments,function(i,reply){
                    var user=reply.user;
                    var fullname=user.firstName+" "+user.lastName;
                    replyHTML=replyHTML+getReplyContent(reply.textContent,user._id,fullname,
                    user.profilePic,"/user/"+user._id,"openUserProfile");
                });
            }
            else replyHTML="<div class='userMessage'>There are no replies on this comment</div>";
            comment.addClass("actComPost").siblings().removeClass("actComPost");
            docViewRepliesClick(comment,fullCommentHTML,replyHTML,$this);
        });
    });
 $(".CWPostReply").live('click',function(){
     var onVideo=true,comment=$("#DPLSComments").find(".actComPost"),commData=comment.data();
//     if(CW.data("CWType")!="VIDEO_COMMENTS"){
//             onVideo=false;
//     }
    onVideo=false;
    var repliesBox=$("#repliesBox");
     var replyInput=repliesBox.find(".CWReplyInput"),replyText=replyInput.val(),
     CWRepliesDiv=repliesBox.find(".CWRepliesDiv");
     if(replyText!=""){
        $.post("/Widgets/postReply",{rootType:commData.rootType,rootId:commData.rootId,
            parentId:commData.commentId,onVideo:onVideo,textContent:replyText,depth:"1",
            scope:"PUBLIC",reply:true,docId:$("#docViewPage").data("docId"),type:"REPLY",
        baseId:$("#docViewPage").data("docId"),baseType:"DOCUMENT"},function(data){
            if(data.errorMessage==''){
               var rep=getReplyContent(replyText);
               if(CWRepliesDiv.children(".CWReply").length>0)
               CWRepliesDiv.append(rep);
               else CWRepliesDiv.html(rep);
               increaseCount(comment.find(".docCommentNo label"));
             }
        });
     }
     replyInput.val("");
 });
 function getReplyContent(replyText,userId,fullname,pic,href,hrefClass){
    if(!userId)userId=USERID;
    if(!fullname)fullname=FULLNAME;
    if(!pic)pic=PROFILEPIC; 
    if(!href)href="/myprofile";
    if(!hrefClass)hrefClass="openMyProfile";
    return '<div class="CWReply">\n\
 <a data-cs-name="USER_IMAGE" href="'+href+'" data-user-id="'+userId+'"\n\
 class="img30Wrapper img30Round floatLeft '+hrefClass+' doCStream">\n\
            <div class="img30DummyWrapper img30DummyRound">\n\
               <img alt="'+fullname+'" title="'+fullname+'"\n\
 src="'+pic+'" class="img30 img30Round">\n\
            </div>\n\
        </a>\n\
        <div class="CWCommReplyContent">\n\
            <a href="'+href+'" data-user-id="'+userId+'" class="'+hrefClass+'">\n\
                             <b>'+fullname+'</b> -\n\
            </a>\n\
            <span class="CWReplyText">'+replyText+'</span>\n\
        </div>'+divi+'\n\
</div>';    
 }
 

//cmds specific interactions
$(document).on("click",".editcmdsDocTocs",function(){
    var docId=$(this).data("docId");
    showTopLoader();
    $.get("/qrdocuments/editDocTocs",{docId:docId},function(data){
       cSecHolder.html(data); 
       hideTopLoader();
    });
});
