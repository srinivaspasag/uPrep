package controllers;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;


@With(Security.class)
public class QrProducts extends AbstractQRUIController {

    public static void main(){
        request.params.put("start","0");
        request.params.put("size","50");
        request.params.put("productType","TEST");
        request.params.put("orderBy","lastUpdated");  
        request.params.put("type","ONLINE");
        Map<String, Object> allParams=getReqParams();
        JSONObject subjects=UIComBoards._getOrgBoards(allParams);
        JSONObject products=_getProducts(allParams);        
        render(subjects,products);
    }
    public static void productsTable() throws JSONException{      
        JSONObject products=_getProducts(null);   
        render(products);
    }  
    public static void getProducts(){
        JSONObject products=_getProducts(null);
        renderJSON(products.toString());
    }      
    public static void addedToInfo(){  
        Scope.Params.current().put("target","PRODUCT");
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsTests/getQrSourcesForProduct",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject info = ResponseUtil.checkResponse(getJSON(promise));
        render("Widgets/addedToInfo.html",info);
    }            
    public static void addTestsToTS(){          
        JSONObject testSeries = _getTestSeries(null);
        render(testSeries);
    }  
    public static void addTestsToTSList(){          
        JSONObject testSeries = _getTestSeries(null);
        render(testSeries);
    }      
    public static void addTestsToTSSubmit(){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsTests/addTestToTestSeries",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }      
        
  
    
    
    
    //study package
    public static void createPackage(){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/createPackage",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void allPackages(){          
        JSONObject packages = _getAllPackages(null);
        render(packages);
    }    
    public static void allPackagesList(){          
        JSONObject packages = _getAllPackages(null);
        render(packages);
    }        
    public static void packagePage(){          
        JSONObject pkgInfo = _getPackageInfo(null);
        render(pkgInfo);
    }  
    public static void getPackageInfo(){          
        JSONObject pkgInfo = _getPackageInfo(null);
        renderJSON(pkgInfo.toString());
    }      
    public static void pkgFolderContent(){      
        request.params.put("libraryType","PACKAGE_LIBRARY");
        JSONObject contents= _getContentInLibrary(null);
        render("Widgets/pkgCDPCommons/contentmcWidget.html",contents);
    }              
    public static void pkgContentTable(){          
        request.params.put("libraryType","PACKAGE_LIBRARY");
        JSONObject contents= _getContentInLibrary(null);
        render("Widgets/pkgCDPCommons/contentTable.html",contents);
    }                      
    public static void addToPackage(){          
        JSONObject packages = _getAllPackages(null);
        render(packages);
    }      
    public static void addToPackageList(){          
        JSONObject packages = _getAllPackages(null);
        render(packages);
    }          
    public static void addToPackageSubmit(){   
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/addContentToFolder",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }        
    public static void remFrmPkg(){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/removeContentFromFolder",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }            
    public static void exportPackage(){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/export",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    } 
    public static void getPkgExpStatus(){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/getStatus",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }     
    //return funtions
    protected static JSONObject _getProducts(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsProducts/getqrproducts",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }     
    protected static JSONObject _getAllPackages(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/getAllPackages",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    } 
    protected static JSONObject _getPackageInfo(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/getPackageInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }         
    protected static JSONObject _getLibraryInPackage(Map<String, Object> allParams){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/getLibraryInPackage",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }     
    protected static JSONObject _getContentInLibrary(Map<String, Object> allParams){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsPackages/getContentInLibrary",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _getTestSeries(Map<String, Object> allParams){          
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsTests/getTestSeries",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    //for direct access
    public static void productsDirect(){
        request.params.put("start","0");
        request.params.put("size","50");
        request.params.put("productType","TEST");
        request.params.put("type","ONLINE");
        request.params.put("orderBy","lastUpdated");             
        JSONObject products=_getProducts(null);         
        String includeName="QrProducts/main.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/mapper.html",includeName,products);               
    }    
    public static void packagesDirect(){
        request.params.put("start","0");
        request.params.put("size","50");
        request.params.put("orderBy","lastUpdated");     
        JSONObject packages = _getAllPackages(null);
        String includeName="QrProducts/allPackages.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/mapper.html",includeName,packages);               
    }        
    public static void packageDirect(String packageId){
        JSONObject pkgInfo = _getPackageInfo(null);
        String includeName="QrProducts/packagePage.html";
        flash.put("ENTRY", "DIRECT");        
        render("Application/mapper.html",includeName,pkgInfo); 
    }         
}