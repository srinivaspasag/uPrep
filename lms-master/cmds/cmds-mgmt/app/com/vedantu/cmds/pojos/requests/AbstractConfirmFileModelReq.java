package com.vedantu.cmds.pojos.requests;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.pojos.LinkInfo;
import com.vedantu.content.pojos.SrcType.LinkType;

public class AbstractConfirmFileModelReq extends AbstractAuthCheckReq {

    @Required
    public String       orgId;

    public String       folderId;

    public List<String> tags;
    public Difficulty   difficulty;
    public List<String> brdIds;
    public List<String> targetIds;

    @Required
    public String       name;
    public String       thumbnail;
    public String       url;
    public LinkType     type;
    public String       externalURL;
    public String       description;
    public String       originalFileName;

    public String       uploadedFileName;

    public String       uuid;
    public LinkInfo     linkInfo;

    @Override
    public String validate() {

        String value = super.validate();
        if (value != null) {
            return value;
        }
        if( type == null ){
            return "no link type is provided";
        }
        if (type == LinkType.UPLOADED && StringUtils.isEmpty(uploadedFileName)) {
            return "uploaded file name is absent";
        }
        if (type == LinkType.ADDED && linkInfo == null) {
            return "link info is absent";
        }
        return null;

    }

}
