package com.lms.common.vedantu.enums;

import com.lms.common.vedantu.entity.storage.MediaType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum EntityType {
    UNKNOWN(null, null, null, null),

    USER("usr", "u", "users", "users"),
    MODULE("mod", "m", "module", "modules"),
    DOCUMENT("doc", "d", "document", "documents"),
    VIDEO("vid", "v", "video", "videos"),
    QUESTION("qus", "q", "question", "questions"),
    CHALLENGE("chall", "chall", "challenge", "challenges"),
    SOLUTION("sol", "sol", null, null),
    PLAYLIST(null, null, null, null),
    TEST("test", "t", "test", "tests"),
    ASSIGNMENT("assignment", "asnm", "assignment", "assignments"),
    DISCUSSION("diss", "diss", "discussion", "discussions"),
    FILE("fl", "file", "file", "files"),

    COMMENT("cmnt", "cmnt", null, null),
    FOLDER(null, null, null, null),
    REMARK("rmk", "rmk", null, null),
    DIAGRAM("dia", "dia", null, null),
    MESSAGE("msg", "msg", null, null),

    INVOICE("iv", "iv", null, null),
    PLAN("plan", "plan", null, null),

    CONTENTGROUP("cg", "cg", null, null),

    // this is only needed for internal cmds purpose
    CMDSRESOURCE("cmdres", "cr", "resource", "cmdsresources"),
    CMDSMODULE("mod", "cmdsmod", "cmdsmodule", "cmdsmodules"),
    CMDSDOCUMENT("doc", "cmdsdoc", "cmdsdocument","cmdsdocuments"),
    CMDSFILE("fl", "file", "cmdsfile", "cmdsfiles"),
    CMDSQUESTION("qus","cmdsqs", "cmdsquestion", "cmdsquestions"),
    CMDSASSIGNMENT("cmdsa", "cmdsasnm", "cmdsassignment", "cmdsassignments"),
    CMDSTEST("cmdst", "cmdstes", "cmdstest", "cmdstests"),
    CMDSQUESTIONSET("cmdsqs", "cmdsqs", "cmdsquestionset", "cmdsquestionsets"),
    CMDSVIDEO("vid", "cmdsvid", "cmdsvideo", "cmdsvideos"),
    STATUSFEED("sf","sf", null, null),
    CHANNEL,
    ORGANIZATION("org", "org", null, null),
    DEPARTMENT("dept", "dept", null, null),
    PROGRAM("prg", "prog", null, null),
    SECTION("sect", "sect", null, null),
    CENTER("cntr", "cntr", null, null),

    EXPORTRECORD("exports", "export", null, null),
    COMPOUNDMEDIA,
    SDCARD("sdcards", "sdcard", null, null),
    SDCARDGROUP("sdgroups", "sdgroup", null, null);
    // @formatter:on

    private static Set<EntityType> cmdsLibrarySupportedType = new HashSet<EntityType>();
    private static Set<EntityType>         orgEntityTypes           = new HashSet<EntityType>();
    private static Set<EntityType>         supportedPublishedType   = new HashSet<EntityType>();
    private static Set<EntityType>         attemptableEntities      = new HashSet<EntityType>();
    static {
        supportedPublishedType.add(DOCUMENT);
        supportedPublishedType.add(VIDEO);
        supportedPublishedType.add(QUESTION);
        supportedPublishedType.add(PLAYLIST);
        supportedPublishedType.add(TEST);
        supportedPublishedType.add(ASSIGNMENT);
        supportedPublishedType.add(STATUSFEED);
        supportedPublishedType.add(REMARK);
        supportedPublishedType.add(DISCUSSION);
        supportedPublishedType.add(CHALLENGE);
        supportedPublishedType.add(FILE);
        supportedPublishedType.add(MODULE);

        cmdsLibrarySupportedType.add(CMDSTEST);
        cmdsLibrarySupportedType.add(CMDSASSIGNMENT);
        cmdsLibrarySupportedType.add(CMDSQUESTION);
        cmdsLibrarySupportedType.add(CMDSVIDEO);
        cmdsLibrarySupportedType.add(CMDSDOCUMENT);
        cmdsLibrarySupportedType.add(CMDSFILE);
        cmdsLibrarySupportedType.add(CMDSMODULE);

        orgEntityTypes.add(CENTER);
        orgEntityTypes.add(PROGRAM);
        orgEntityTypes.add(ORGANIZATION);
        orgEntityTypes.add(SECTION);

        attemptableEntities.add(QUESTION);
        attemptableEntities.add(CHALLENGE);
        attemptableEntities.add(TEST);
        attemptableEntities.add(ASSIGNMENT);
    }

    private String                         acronym;
    private String                         qualifierChar;
    private String                         indexType;
    private String                         indexName;

    // private Class<?> classAbstractBoardSearchEntityTagDetails;

    private static Map<String, EntityType> mapAcronym               = null;

    private EntityType() {

        this(null, null, null, null);
    }

    private EntityType(final String acronym, final String qualifierChar, String indexType,
                       final String indexName
                       // , final Class<?> classAbstractBoardSearchEntityTagDetails
    ) {

        this.acronym = acronym;
        this.qualifierChar = qualifierChar;
        this.indexType = indexType;
        this.indexName = indexName;
        // if (null != classAbstractBoardSearchEntityTagDetails
        // && !AbstractBoardSearchEntityTagDetails.class
        // .isAssignableFrom(classAbstractBoardSearchEntityTagDetails)) {
        // throw new RuntimeException("illegal class specification: "
        // + classAbstractBoardSearchEntityTagDetails);
        // }
        // this.classAbstractBoardSearchEntityTagDetails =
        // classAbstractBoardSearchEntityTagDetails;
    }

    public String getAcronym() {

        return acronym;
    }

    public String getQualifierChar() {

        return qualifierChar;
    }

    public String getIndexType() {

        return indexType;
    }

    public String getIndexName() {

        return indexName;
    }

    public static boolean isValidEntityType(String entityTypeValue) {

        EntityType entityType = null;
        try {
            entityType = EntityType.valueOf(entityTypeValue);
        } catch (Exception e) {
            // Swallow
        }
        return null != entityType;
    }

    public static EntityType valueOfKey(String value) {

        EntityType entityType = UNKNOWN;
        try {
            entityType = EntityType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return entityType;
    }

    public static boolean isValidQualifierChar(String q) {

        boolean result = false;
        for (EntityType e : EntityType.values()) {
            if (e.qualifierChar.equalsIgnoreCase(q)) {

                result = true;
                break;
            }
        }
        return result;
    }

    public static EntityType getByAcronym(String requestedAcronym) {

        if (null == mapAcronym) {
            synchronized (MediaType.class) {
                if (null == mapAcronym) {
                    mapAcronym = new HashMap<String, EntityType>();
                    for (EntityType entityType : EntityType.values()) {
                        mapAcronym.put(entityType.acronym, entityType);
                    }
                }
            }
        }
        return null != requestedAcronym && null != mapAcronym ? mapAcronym.get(requestedAcronym)
                : null;
    }

    public static boolean isValidOrgEntity(EntityType orgEntityType) {

        return orgEntityTypes.contains(orgEntityType);
    }

    public static boolean isSupportedCMDSLibraryEntityType(EntityType contentTYpe) {

        return cmdsLibrarySupportedType.contains(contentTYpe);
    }

    public static boolean isSupportedContentType(EntityType contentType) {

        return supportedPublishedType.contains(contentType);
    }

    public static boolean isAttemptableEntity(EntityType entityType) {

        return attemptableEntities.contains(entityType);
    }

    public String _getStorageId() {

        switch (this) {
            case CMDSQUESTION:
                return QUESTION.name().toLowerCase();
            case CMDSVIDEO:
                return VIDEO.name().toLowerCase();
            case CMDSDOCUMENT:
                return DOCUMENT.name().toLowerCase();
            case CMDSFILE:
                return FILE.name().toLowerCase();
            case EXPORTRECORD:
                return "exports";
            default:
                break;
        }

        return this.name().toLowerCase();
    }

    public EntityType _getPublishedType() {

        switch (this) {
            case CMDSQUESTION:
                return QUESTION;
            case CMDSVIDEO:
                return VIDEO;
            case CMDSDOCUMENT:
                return DOCUMENT;
            case CMDSFILE:
                return FILE;
            case CMDSMODULE:
                return MODULE;
            case CMDSTEST:
                return TEST;
            case CMDSASSIGNMENT:
                return ASSIGNMENT;
            default:
                return null;
        }
    }
}
