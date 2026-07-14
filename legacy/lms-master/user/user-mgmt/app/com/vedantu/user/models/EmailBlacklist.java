package com.vedantu.user.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.enums.EmailBlacklistAction;
import com.vedantu.user.pojos.EmailBlacklistInfo;

@Entity(value = "emailblacklists", noClassnameStored = true)
@Indexes({ @Index(value = "email", unique = true) })
public class EmailBlacklist extends VedantuBaseMongoModel {

    public String                   email;
    public boolean                  blacklisted;
    public List<EmailBlacklistInfo> infos;

    public EmailBlacklist() {

        this(null);

    }

    public EmailBlacklist(String email) {

        super();
        this.email = email.toLowerCase().trim();
    }

    public EmailBlacklistInfo addBlacklistInfo(String reason, EmailBlacklistAction action) {

        EmailBlacklistInfo info = new EmailBlacklistInfo(reason, action);
        if (this.infos == null) {
            this.infos = new ArrayList<EmailBlacklistInfo>();
        }
        infos.add(info);
        this.blacklisted = info.action == EmailBlacklistAction.BLOCKED;
        return info;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{email:").append(email).append(", blacklisted:").append(blacklisted)
                .append(", infos:").append(infos).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
