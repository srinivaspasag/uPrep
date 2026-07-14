package com.vedantu.content.models;

import java.util.Date;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "librarycontentlinks", noClassnameStored = true)
@Indexes({
    @Index(value = "target.id, target.type, linkType, scope"),
    @Index(value = "target.type, target.id, source.type, source.id, linkType, recordState"),
    @Index(value = "source.id, target.id, recordState")
})
public class LibraryContentLink extends VedantuBaseMongoModel {

    @Transient
    public static final String POSITION = "position";

    public String              userId;
    public SrcEntity           source;
    public SrcEntity           target;
    public UserActionType      linkType;
    private Scope              scope;
    private ScheduleInfo       schedule;
    private boolean            downloadable;
    private List<SrcEntity>    downloadableEntities;
    private EncryptionLevel    encLevel;
    public long                position;

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

    public boolean isDownloadable() {

        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {

        this.downloadable = downloadable;
    }

    public void setDownloadableEntities(List<SrcEntity> downloadableEntities) {

        this.downloadableEntities = downloadableEntities;
    }

    public List<SrcEntity> getDownloadableEntities() {

        return this.downloadableEntities;
    }

    public void setSchedule(ScheduleInfo schedule) {

        this.schedule = schedule;
    }

    public Scope getScope() {

        return scope;
    }

    public void setScope(Scope scope) {

        this.scope = scope;
    }

    public void setEncLevel(EncryptionLevel encLevel) {

        this.encLevel = encLevel;
    }

    public EncryptionLevel getEncLevel() {

        return encLevel;
    }


    public long getPosition() {

        return position;
    }


    public void setPosition(long position) {

        this.position = position;
    }

}
