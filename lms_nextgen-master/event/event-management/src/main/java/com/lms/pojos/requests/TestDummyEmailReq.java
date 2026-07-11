package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.io.File;

@Setter
@Getter
public class TestDummyEmailReq extends AbstractAuthCheckReq {

    private static final Logger logger = LoggerFactory.getLogger(TestDummyEmailReq.class);
    public String emails;
    @NotBlank
    public File htmlFile;

    public String htmlFileName;

    public String subject;

    public String orgId;


    @Override
    public String validate() {

        if (StringUtils.isEmpty(emails)) {
            return "no recepients provided ";
        }

        return null;
    }
}
