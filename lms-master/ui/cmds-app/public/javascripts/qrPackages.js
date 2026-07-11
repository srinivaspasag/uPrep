var qrPackages=new(function(){
    var clickEvent="click",bodyClickEvent="click.qrPackages",packagePage,currentPkgFolderId,packageId;
    this.contentsChecked=[];
    this.init=function(params){
        packagePage=$("#packagePage");
        packageId=packagePage.data("pkgId");
        resetCmdsPages($("#cmdsPkgs"));
        packagePage.off(clickEvent)
        .on(clickEvent,".exportPackage",exportPackage)
        .on(clickEvent,".getPkgFolderContent",getPkgFolderContent)
        .on(clickEvent,".togglePkgFolderList",togglePkgFolderList)
        .on(clickEvent,".remFrmPkg",remFrmPkg)         
        
        $("body").off(bodyClickEvent)
        .on(bodyClickEvent,".submitExportPackage",submitExportPackage)
        fixContentSec();
        
        getPkgFolderContent(null,params.folderId);
        if(params.exportStatus=="EXPORT_IN_PROGRESS"){
            publishUtil.init("/qrproducts/getPkgExpStatus",params.jobId,packagePage.find(".pkgPageBtnsBody"),
            onPackageExportComplete);
        } 
    }
    var startLoader=showTopLoader;    
    var stopLoader=hideTopLoader;
    var closePopup=closecmdsPopup;
    var postReq=vReq.post;
    var getReq=vReq.get;
    
    var exportPackage=function(){
        fillcmdsPopup("submitExportPackage","exportPkgSample");    
    }    
    var submitExportPackage=function(){               
        startLoader();
        var popup=$(this).closest("#cmdsPopup");
        var successFn=function(data){
            stopLoader();
            var result=data.result;
            var link=result.downloadLink;
            if(link){
                showMessage("<b>Download Link:</b>\n\
                <a href='"+link+"' target=_blank>"+link+"</a>");        
            }else if(result.jobId){
                publishUtil.init("/qrproducts/getPkgExpStatus",result.jobId,null,onPackageExportComplete);               
            }            
        }
        postReq("/qrproducts/exportPackage",{packageId:packageId,
            encLevel:popup.find(".pkgEncLevel").val()},successFn);
    }
    var onPackageExportComplete=function(){
        getReq("/qrproducts/getpackageInfo",{packageId:packageId},function(data){
            var linkList=data.result.packages.exportInfo;
            var link=linkList[linkList.length-1].downloadLink;
            showMessage("<b>Download Link:</b>\n\
            <a href='"+link+"' target=_blank>"+link+"</a>");                          
            packagePage.find(".pkgPageBtnsBody").html('<div class="gRedButton exportPackage" \n\
            data-pkg-id="'+packageId+'">Export</div>');
        });
    }
    
    var getPkgFolderContent=function(e,fdrId){
        packagePage.find(".packagePageTabs,.pkgFolderContent").removeClass("nonner");
        packagePage.find(".packageContentsList").addClass("nonner");
        var targetDiv=packagePage.find(".pkgFolderContent");
        var folderId=fdrId||$(this).data("folderId");
        currentPkgFolderId=folderId
        if(targetDiv.html()==""){
            startLoader();
            var params={folderId:folderId,start:0,size:50};
            var successFn=function(data){
                targetDiv.html(data);
                stopLoader();
                var mcWidget=targetDiv.find(".mcWidget");
                initmcWidgetforCMDS(mcWidget,"/qrproducts/pkgContentTable",params,false,true); 
                fixContentSec();
            }
            getReq("/qrproducts/pkgFolderContent",params,successFn);
        }                
    }
    var togglePkgFolderList=function(){
        packagePage.find(".packagePageTabs,.pkgFolderContent").addClass("nonner");
        packagePage.find(".packageContentsList").removeClass("nonner");
    }
  
    
    var remFrmPkg=function(){
        var contents=remContentFrmPkgCDPUtil().contents;
        var params={contents:contents,packageId:packagePage.data("pkgId"),
            folderId:currentPkgFolderId};
        var successFn=function(data){
            var fn=function(tr){
                tr.remove();
            }
            checkResultList(data.result.contents,packagePage.find(".cmdsTable tbody"),fn);
            resetcmdsCBoxes();
        }
        postReq("/qrproducts/remFrmPkg",params,successFn);    
    }  
    
    this.seteTypesForPkgs=function(mcWidget,vChoose){
        var mcWidgetDataParams=mcWidget.data().params;
        var eType=mcWidgetDataParams.eType;
        var finalEType=mcWidgetDataParams.eType,finalType;
        if(eType=="VIDEO"){
            finalEType="DOCUMENT";
            finalType="VIDEO";
        }else if(eType=="ASSIGNMENT"){
            finalEType="TEST";
            finalType="ASSIGNMENT";
        }
        mcWidgetDataParams.eType=finalEType;
        mcWidgetDataParams.type=finalType;
        manageContent.loadmcContent(mcWidget,vChoose);
    }
})(jQuery);
var seteTypesForPkgs=function(mcWidget,vChoose){
    qrPackages.seteTypesForPkgs(mcWidget,vChoose);
}