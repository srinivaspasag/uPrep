package com.lms.models;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;


@Document(value = "librarycontentlinks")
@CompoundIndexes({
        @CompoundIndex(name = "target.id, target.type, linkType, scope"),
        @CompoundIndex(name = "target.type, target.id, source.type, source.id, linkType, recordState"),
        @CompoundIndex(name = "source.id, target.id, recordState")
})
@Setter
@Getter
public class LibraryContentLink extends VedantuBaseMongoModel {

    @Transient
    public static final String POSITION = "position";

    public String userId;
    public SrcEntity source;
    public SrcEntity target;
    public UserActionType linkType;
    public long position;
    private Scope scope;
    private ScheduleInfo schedule;
    private boolean downloadable;
    private List<SrcEntity> downloadableEntities;
    private EncryptionLevel encLevel;

    public LibraryContentLink() {

        scope = Scope.PRIVATE;
        downloadable = false;
        schedule = new ScheduleInfo(new Date(), null);
        encLevel = EncryptionLevel.NA;
    }

    public LibraryContentLink(SrcEntity target, SrcEntity content) {

        this();
        this.source = content;
        this.target = target;
    }

    public LibraryContentLink(String userId, SrcEntity source, SrcEntity target,
                              UserActionType linkType, ScheduleInfo schedule) {

        super();
        this.userId = userId;
        this.source = source;
        this.target = target;
        this.linkType = linkType;
        this.schedule = schedule;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:");
        builder.append(userId);
        builder.append(", source:");
        builder.append(source);
        builder.append(", target:");
        builder.append(target);
        builder.append(", linkType:");
        builder.append(linkType);
        builder.append(", scope:");
        builder.append(scope);
        builder.append(", schedule:");
        builder.append(schedule);
        builder.append("}");
        return builder.toString();
    }

    public ScheduleInfo getSchedule() {

        return schedule;
    }

    public void setSchedule(ScheduleInfo schedule) {

        this.schedule = schedule;
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

    public void setDownloadableEntities(List<SrcEntity> downloadableEntities) {

        this.downloadableEntities = downloadableEntities;
    }

    public Scope getScope() {

        return scope;
    }

    public void setScope(Scope scope) {

        this.scope = scope;
    }

    public EncryptionLevel getEncLevel() {

        return encLevel;
    }

    public void setEncLevel(EncryptionLevel encLevel) {

        this.encLevel = encLevel;
    }

    public long getPosition() {

        return position;
    }


    public void setPosition(long position) {

        this.position = position;
    }

}
