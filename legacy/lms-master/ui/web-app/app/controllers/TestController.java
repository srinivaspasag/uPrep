/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import play.Logger;

/**
 *
 * @author anirban
 */
public class TestController extends AbstractUIController{
    public static void ccavenuePaymentNotification() {

        Map<String, Object> reqParams = getReqParams();
        Logger.log4j.info("reqParams : " + reqParams);
        renderJSON(reqParams);
    }
}
