package com.vedantu.ext.cmds.db.models;

import com.vedantu.ext.cmds.utils.commons.StringUtils;

public class Folder extends AbstractDBModel {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;

    public static final String FILED_PARENT     = "parent";

    public String              id;
    public String              name;
    public String              cName;
    public String              parent;
    public String              userId;

    public Folder() {

        super();
    }

    public Folder(int orgKeyId, String id, String name, String parent, String userId) {

        super(orgKeyId);
        this.id = id;
        this._setName(name);
        this.parent = parent;
        this.userId = userId;
    }

    public void _setName(String name) {

        this.name = name.trim();
        this.cName = StringUtils.toCanonicalName(name.trim());
    }
}
