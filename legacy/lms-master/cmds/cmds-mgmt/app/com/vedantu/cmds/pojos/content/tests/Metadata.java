package com.vedantu.cmds.pojos.content.tests;

import java.util.List;

import org.elasticsearch.common.Required;

import com.vedantu.cmds.enums.PublishedStatus;

public class Metadata {

    @Required
    public String           id;
    public String           name;
    public int              qusCount;
    public PublishedStatus  published;
    public List<Details>    details;
    public List<Metadata>   children;
}
