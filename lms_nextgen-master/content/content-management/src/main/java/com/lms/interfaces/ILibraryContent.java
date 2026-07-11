package com.lms.interfaces;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.models.ContentSearchDetails;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public interface ILibraryContent {

    String INDEX_NAME = "contents";
    String INDEX_TYPE = "content";
    List<EntityType> libraryEntityType = Arrays.asList(EntityType.VIDEO, EntityType.TEST, EntityType.ASSIGNMENT, EntityType.DOCUMENT,
            EntityType.QUESTION, EntityType.FILE, EntityType.MODULE);

    ContentSearchDetails __getContentSearchDetails() throws JSONException;

}


