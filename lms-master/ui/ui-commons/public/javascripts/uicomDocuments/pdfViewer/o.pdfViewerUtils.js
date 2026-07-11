var pdfViewerUtils=new(function($){
    var viewer,highlightOptionsDiv;
    this.init=function(){
        viewer=$("#viewer");
        highlightOptionsDiv=$("#highlightOptionsDiv");
        viewer.off()
        .on("mouseup",".textLayer",checkForSelection)

        $(document)
        .on("click",".makeHighlight",makeHighlight)
    };
    
    var selectedNodes,selectedTextLayer;
    var checkForSelection=function(){
        var selectedRange=getUserSelectionRange();
        var startContainer=$(selectedRange.startContainer).closest("div");
        var endContainer=$(selectedRange.endContainer).closest("div");
        var offsetStartContainer=startContainer.offset();
        var startContainerTextLayer=startContainer.closest(".textLayer");
        var endContainerTextLayer=endContainer.closest(".textLayer");
        selectedTextLayer=startContainerTextLayer;
        if(startContainerTextLayer.length>0&&
            endContainerTextLayer.length>0&&
            !selectedRange.collapsed&&
            startContainerTextLayer.is(endContainerTextLayer)){
            selectedNodes=$(selectedRange.cloneContents().childNodes);
            highlightOptionsDiv.show()
                    .css({top:(($(window).scrollTop())+offsetStartContainer.top-30),
                left:offsetStartContainer.left})
        }else{
            highlightOptionsDiv.hide();
        }
    };
    
    var makeHighlight=function(){
        var resp=perpareHighlightedDivs();
        var errorFn=function(){
            resp.highlight.remove();
        };
        var pageId=selectedTextLayer.closest(".page").attr("id");
        var pageNum=pageId.substring(13);
//        vReq.post("/document/addHighlight",{pageNumber:pageNum,bb:resp.hocrCoords},null,errorFn);
//TODO fetch highlights and add to data when a new highlight is added;
        highlightOptionsDiv.hide();
    };
    var perpareHighlightedDivs=function(){
        if(!selectedNodes||!selectedTextLayer)return;
        var nodes=selectedNodes,nodesLength=selectedNodes.length;
        var hocrCoords=[];
        var highlight=makeHTMLTag("div",{'class':"highlight"});
        for(var k=0;k<nodesLength;k++){
            var node=nodes.eq(k);
            var div=node.closest("[dir=ltr]");
            if(div.length===0)continue;
            hocrCoords.push(getHocrCoords(div));
            var highlightPart=div.clone().addClass("highlightPart");
            highlight.append(highlightPart);
        }
        prepareHighlightsHolder(selectedTextLayer).append(highlight);
        return {highlight:highlight,hocrCoords:hocrCoords};
    };
    var prepareHighlightsHolder=function(selectedTextLayer){
        var highlightsHolderName="highlightsHolder";
        var highlightsHolder=selectedTextLayer.siblings("."+highlightsHolderName);
        if(highlightsHolder.length>0)return highlightsHolder;
        else{
           highlightsHolder=makeHTMLTag("div",{'class':highlightsHolderName});
           highlightsHolder.insertBefore(selectedTextLayer);
           return highlightsHolder;
        }
    };
    var getHocrCoords=function(div){
        var currentScale=PDFView.currentScale;
        var x1=parseFloat(div.css("left"))/currentScale;
        var y1=parseFloat(div.css("top"))/currentScale;
        var x2=div.width()/currentScale;
        var y2=div.height()/currentScale;        
        return y1+","+x1+","+y2+","+x2;
    };
    var loadHighlights=function(pageNum){
        var page=$("#pageContainer"+pageNum);
        var highlights=page.data("highlights");
        var highlightsReqSent=page.data("highlightsReqSent");
//        if(!highlights&&!highlightsReqSent){
//            page.data("highlightsReqSent",true);
//            $.get("/docuemnt/getHighlights",{},function(data){
//                //TODO put highlights in data
//                  page.data("highlightsReqSent",false);                
//                  putScaledHighlights(pageNum);
//            });            
//        }else if(highlights){
//            var textLayer=page.children(".textLayer");
//            if(textLayer.siblings(".highlightsHolder").length===0){
//              putScaledHighlights(pageNum);
//            }            
//        }
            var textLayer=page.children(".textLayer");
            if(textLayer.siblings(".highlightsHolder").length===0){
              putScaledHighlights(pageNum);
            } 
    };
    var putScaledHighlights=function(pageNum){
        var page=$("#pageContainer"+pageNum);
        var highlights=page.data("highlights");
//        TODO remove this hardcoding for highlights
         highlights=[[[100,100,200,200]],[[200,200,300,400]]];                
         //To care of situation in zooming if a zoom scrolls to a different page.
        if(!highlights){
            loadHighlights(pageNum);
        }
        var tagger=makeHTMLTag;
        var textLayer=page.children(".textLayer");
        var highlightsDummyHolder=tagger("div");
        var currentScale=PDFView.currentScale;
        for(var k=0;k<highlights.length;k++){
            var singleHighlightParts=highlights[k];
            var highlight=tagger("div",{'class':"highlight"});
            for(var p=0;p<singleHighlightParts.length;p++){
                var coords=singleHighlightParts[p];
                var top=coords[0]*currentScale;
                var left=coords[1]*currentScale;
                var h=(coords[2]*currentScale-top)+"px";
                var w=(coords[3]*currentScale-left)+"px";
                var highlightPart=tagger("div",{'class':"highlightPart","transform-origin":"0px 0px 0px",dir:"ltr"});
                highlightPart.css({top:top,left:left,width:w,height:h});
                highlight.append(highlightPart);
            }
            highlightsDummyHolder.append(highlight);
        }       
        prepareHighlightsHolder(textLayer).html(highlightsDummyHolder.children());        
    };
    this.loadHighlights=loadHighlights;
    this.putScaledHighlights=putScaledHighlights;
})(jQuery);
pdfViewerUtils.init();
