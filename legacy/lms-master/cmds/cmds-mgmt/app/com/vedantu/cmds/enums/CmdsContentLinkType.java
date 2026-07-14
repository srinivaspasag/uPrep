package com.vedantu.cmds.enums;

public enum CmdsContentLinkType {
	UNKNOWN, ADDED, PUBLISHED, VISIBLED, USED,INVISIBLED,DOWNLOAD_ENABLED, DOWNLOAD_DISABLED;

	public static CmdsContentLinkType valueOfKey(String key) {
		CmdsContentLinkType linkType = UNKNOWN;
		try {
			linkType = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}

		return linkType;
	}
	

    public String getSearchIndexType() {

        return this.name().toLowerCase();
    }

}
