package com.lms.user.vedantu.user.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.user.vedantu.user.pojo.EmailBlacklistInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetBlacklistEmailRes implements IListResponseObj {
    public String                   email;

    public boolean                  blacklisted;
    public List<EmailBlacklistInfo> infos;
}
