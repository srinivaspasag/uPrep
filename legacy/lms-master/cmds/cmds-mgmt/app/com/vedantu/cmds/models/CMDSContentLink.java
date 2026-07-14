package com.vedantu.cmds.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.pojos.content.question.CMDSContentLinkInfo;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "cmdscontentlinks", noClassnameStored = true)
@Indexes({ @Index(value = "source.id, source.type,linkType", unique = false),
        @Index(value = "target.id, target.type,linkType", unique = false) })
public class CMDSContentLink extends VedantuBaseMongoModel {

    @Transient
    public static final String POSITION       = "position";
    @Transient
    public static final String GLOBAL_LINK_ID = "globalLinkId";

    public String              userId;
    public SrcEntity           source;
    public SrcEntity           target;
    public CmdsContentLinkType linkType;
    private Scope              scope;
    private ScheduleInfo       schedule;
    private boolean            downloadable;
    public String              globalLinkId;
    private List<SrcEntity>    downloadableEntities = new ArrayList<SrcEntity>();
    public long                position;

    public CMDSContentLink() {

        scope = Scope.PRIVATE;
        downloadable = false;

    }

    public CMDSContentLink(SrcEntity target, SrcEntity content) {

        this();
        this.source = content;
        this.target = target;

    }

    public CMDSContentLink(String userId, SrcEntity source, SrcEntity target,
            CmdsContentLinkType linkType) {

        this(target, source);
        this.userId = userId;
        this.linkType = linkType;
    }

    @Override
    public String toString() {

        StringBuilder createdLinkBetween = new StringBuilder();
        createdLinkBetween.append(" Source :").append(source).append(" , target :").append(target)
                .append(" linkType:").append(linkType);
        return super.toString();
    }

    /**
     * @return the schedule
     */
    public ScheduleInfo getSchedule() {
        return schedule;
    }

    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(ScheduleInfo schedule) {
        this.schedule = schedule;
    }

    public Scope getScope() {

        return scope;
    }

    public void setScope(Scope scope) {

        this.scope = scope;
    }

    public void setDownloadableEntities(List<SrcEntity> downloadableEntities) {

        this.downloadableEntities = downloadableEntities;
    }

    public boolean isDownloadable() {

        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {

        this.downloadable = downloadable;
    }

    public List<SrcEntity> getDownloadableEntities() {

        return this.downloadableEntities;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        CMDSContentLinkInfo info = new CMDSContentLinkInfo(_getStringId(), recordState, this.scope);

        info.linkType = this.linkType;
        info.downloadable = this.isDownloadable();
        info.scope = this.scope;
        info.position = this.position;
        return info;
    }

}
