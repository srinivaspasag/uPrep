package com.vedantu.ext.cmds.pojo.responses.local;

import java.io.Serializable;

import com.vedantu.ext.cmds.db.models.ImportedLibrary;

public class GetImportedLibrariesRes implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            id;
    private String            type;
    private String            name;
    private long              size;
    private int               noOfCards;
    private int               contentCount;
    private int               flashCount;
    private long              lastSynced;
    private long              timeCreated;
    private String            state;
    private long              downloadedSize;

    
    public String getState() {
    
        return state;
    }

    
    public void setState(String state) {
    
        this.state = state;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long size) {

        this.size = size;
    }

    public int getNoOfCards() {

        return noOfCards;
    }

    public void setNoOfCards(int noOfCards) {

        this.noOfCards = noOfCards;
    }

    public int getContentCount() {

        return contentCount;
    }

    public void setContentCount(int contentCount) {

        this.contentCount = contentCount;
    }

    public long getLastSynced() {

        return lastSynced;
    }

    public void setLastSynced(long lastSynced) {

        this.lastSynced = lastSynced;
    }

    public long getTimeCreated() {

        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        http://localhost:8080/vedantu-ext-cmds-uploader/getImportedLibraries?id=529cca9944aef63ba3a7e669&type=529cca9944aef63ba3a7e669&name=sectionName
        this.timeCreated = timeCreated;
    }

    
    public int getFlashCount() {
    
        return flashCount;
    }


    
    public void setFlashCount(int flashCount) {
    
        this.flashCount = flashCount;
    }


    
    public long getDownloadedSize() {
    
        return downloadedSize;
    }


    
    public void setDownloadedSize(long downloadedSize) {
    
        this.downloadedSize = downloadedSize;
    }


    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", type:").append(type).append(", name:")
                .append(name).append(", size:").append(size).append(", noOfCards:")
                .append(noOfCards).append(", contentCount:").append(contentCount)
                .append(", lastSynced:").append(lastSynced).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

    public void fromModel(ImportedLibrary syncedLibrary) {

        id = syncedLibrary.id;
        type = syncedLibrary.type;
        name = syncedLibrary.name;
        size = syncedLibrary.size;
        timeCreated = syncedLibrary.timeCreated;
        state = syncedLibrary.state;
        downloadedSize= syncedLibrary.downloadedSize;

    }
}
