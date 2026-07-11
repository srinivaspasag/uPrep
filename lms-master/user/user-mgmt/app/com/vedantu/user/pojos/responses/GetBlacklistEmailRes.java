package com.vedantu.user.pojos.responses;

import java.util.List;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.user.pojos.EmailBlacklistInfo;

public class GetBlacklistEmailRes implements IListResponseObj {

    public String                   email;

    public boolean                  blacklisted;
    public List<EmailBlacklistInfo> infos;

}
