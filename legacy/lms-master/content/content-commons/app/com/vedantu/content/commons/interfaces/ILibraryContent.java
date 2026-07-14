package com.vedantu.content.commons.interfaces;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.models.ContentSearchDetails;

public interface ILibraryContent {

    public static final String           INDEX_NAME        = "contents";
    public static final String           INDEX_TYPE        = "content";
    public static final List<EntityType> libraryEntityType = Arrays.asList(new EntityType[] {
            EntityType.VIDEO, EntityType.TEST, EntityType.ASSIGNMENT, EntityType.DOCUMENT,
            EntityType.QUESTION, EntityType.FILE, EntityType.MODULE });

    public ContentSearchDetails __getContentSearchDetails() throws JSONException;

}
