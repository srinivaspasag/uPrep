/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import uicom.util.ClientUtil;

/**
 *
 * @author anirban
 */
public class UIComHelpCenter extends AbstractUIController {
    private static boolean checkLogin(){
        return true;
//        if(session.contains("userId")){
//            return true;
//        }
//        return false;
    }
    public static void home(){
        if(checkLogin()){
            recordActivity(ClientUtil.ActivityPages.HELP_CENTER,ClientUtil.ActivityAction.OPEN);
            render("UIComHelpCenter/home.html");
        }else{
            redirect("/login");
        }
    }
    public static void homePage(){
         if(checkLogin()){
            recordActivity(ClientUtil.ActivityPages.HELP_CENTER,ClientUtil.ActivityAction.OPEN);
            render("UIComHelpCenter/homePage.html");
        }else{
            redirect("/login");
        }
    }
    public static void ping(){
        renderJSON(true);
    }
    public static void topics(){
        if(checkLogin()){
            render("UIComHelpCenter/topics.html");;
        }else{
            redirect("/login");
        }
    }
    public static void topicsDirect(){
        String includeFile = "UIComHelpCenter/topics.html";
        if(checkLogin()){
            recordActivity(ClientUtil.ActivityPages.HELP_CENTER,ClientUtil.ActivityAction.OPEN);
            render("UIComHelpCenter/myPage.html",includeFile);
        }else{
            redirect("/login");
        }
    }
}
