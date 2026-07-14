package com.vedantu.content.pojos.responses;

import java.util.List;

import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class GetContentLinkRes implements IListResponseObj {

    public String           id;
    public long             timeCreated;
    public long             lastUpdated;
    public SrcEntity        target;
    public boolean          downloadable;
    public IListResponseObj content;             // {@link ContentSearchDetails}
    public String           passphrase;
    public EncryptionLevel  encLevel;
    public List<SrcEntity>  downloadableEntities;
    public int              position;
    public long             startTime;
    public long             endTime;
    public long             closeTime;

    public GetContentLinkRes() {

    }

    public GetContentLinkRes(String id, long timeCreated, long lastUpdated, SrcEntity target,
            boolean downloadable, List<SrcEntity> downloadableEntities, int position) {

        this.id = id;
        this.timeCreated = timeCreated;
        this.lastUpdated = lastUpdated;
        this.target = target;
        this.downloadable = downloadable;
        this.downloadableEntities = downloadableEntities;
        this.position = position;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", target:").append(target)
                .append(", downloadable:").append(downloadable).append(", content:")
                .append(content).append(", passphrase:").append(passphrase).append(", encLevel:")
                .append(encLevel).append(", downloadableEntities:").append(downloadableEntities)
                .append(", position:").append(position).append("}");
        return builder.toString();
    }

}
