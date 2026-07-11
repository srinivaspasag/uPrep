/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.Validation;


/**
 *
 * @author anirban
 */
@With(Security.class)
public class Remark extends AbstractUIController {
    public static void addRemark(){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.PROFILE,ClientUtil.ActivityAction.ADD,ClientUtil.Entity.REMARK,"");
        }catch(Exception ex){}
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/remarks/addRemark", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        render("tags/remark/post.html",data);
    }
    public static void remarks(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/remarks/getRemarksForUser", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject remarks = Validation.verifyResponse(getJSON(promise));
        render(remarks);
    }
}
