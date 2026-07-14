/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import play.data.validation.Required;

/**
 *
 * @author anirban
 */
public class UiComInstitute extends AbstractUIController {
    @Deprecated
    public static String getInstLogo(@Required String thumbnailImg){
       /* if(thumbnailImg==null || thumbnailImg.isEmpty()){
            thumbnailImg = ClientUtil.INST_DEFAULT_IMG_PATH;
        }
        return thumbnailImg;
     * 
     */
        return "";
    }
}
