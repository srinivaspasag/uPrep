package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.Difficulty;
import com.lms.enums.SrcType;
import com.lms.pojos.LinkInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class AbstractConfirmFileModelReq extends AbstractAuthCheckReq {

    @NotBlank
    public String orgId;

    public String folderId;

    public List<String> tags;
    public Difficulty difficulty;
    public List<String> brdIds;
    public List<String> targetIds;

    @NotBlank
    public String name;
    public String thumbnail;
    public String url;
    public SrcType.LinkType type;
    public String externalURL;
    public String description;
    public String originalFileName;

    public String uploadedFileName;

    public String uuid;
    public LinkInfo linkInfo;

    @Override
    public String validate() {

        String value = super.validate();
        if (value != null) {
            return value;
        }
        if (type == null) {
            return "no link type is provided";
        }
        if (type == SrcType.LinkType.UPLOADED && StringUtils.isEmpty(uploadedFileName)) {
            return "uploaded file name is absent";
        }
        if (type == SrcType.LinkType.ADDED && linkInfo == null) {
            return "link info is absent";
        }
        return null;

    }

}
