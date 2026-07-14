package com.lms.models;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.CmdsContentLinkType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@Document(value = "cmdscontentlinks")
@CompoundIndexes({@CompoundIndex(name = "source.id, source.type,linkType", unique = false),
        @CompoundIndex(name = "target.id, target.type,linkType", unique = false)})
public class CMDSContentLink extends VedantuBaseMongoModel {

    @Transient
    public static final String POSITION = "position";
    @Transient
    public static final String GLOBAL_LINK_ID = "globalLinkId";

    public String userId;
    public SrcEntity source;
    public SrcEntity target;
    public CmdsContentLinkType linkType;
    public String globalLinkId;
    public long position;
    private Scope scope;
    private ScheduleInfo schedule;
    private boolean downloadable;
    private List<SrcEntity> downloadableEntities = new ArrayList<SrcEntity>();

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

