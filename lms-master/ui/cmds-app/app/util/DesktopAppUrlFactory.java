package util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import uicom.util.ClientUtil;

public class DesktopAppUrlFactory {

    private static final Map<String, String> serviceUrlMap = new HashMap<String, String>();
    public static final DesktopAppUrlFactory INSTANCE      = new DesktopAppUrlFactory();

    private DesktopAppUrlFactory() {

        super();
        serviceUrlMap.put(StringUtils.lowerCase("validateOrgAppCredentials"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/verifyAppCredentials");

        serviceUrlMap.put(StringUtils.lowerCase("createFolder"), ClientUtil.CMDS_SERVICE_URL
                + "/cmdsResources/createFolder");
        serviceUrlMap.put(StringUtils.lowerCase("getFolders"), ClientUtil.CMDS_SERVICE_URL
                + "/cmdsResources/getFolders");
        serviceUrlMap.put(StringUtils.lowerCase("getSDCardGroups"), ClientUtil.CMDS_SERVICE_URL
                + "/sdGroups/gets");
        serviceUrlMap.put(StringUtils.lowerCase("getSDCardGroupInfo"), ClientUtil.CMDS_SERVICE_URL
                + "/sdGroups/get");

        serviceUrlMap.put(StringUtils.lowerCase("getContentLinks"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getContentLinks");
        serviceUrlMap.put(StringUtils.lowerCase("getRemovedContentLinks"),
                ClientUtil.CONTENT_SERVICE_URL + "/contents/getRemovedContentLinks");

        serviceUrlMap.put(StringUtils.lowerCase("getFileInfos"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getFileInfos");
        serviceUrlMap.put(StringUtils.lowerCase("getSecureURL"), ClientUtil.CONTENT_SERVICE_URL
               + "/contents/getSecureLink");
        serviceUrlMap.put(StringUtils.lowerCase("getOrgInfo"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizationBySlug");
        serviceUrlMap.put(StringUtils.lowerCase("getPrograms"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getPrograms");
        serviceUrlMap.put(StringUtils.lowerCase("getCentersOfProgram"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getProgramCenters");
        serviceUrlMap.put(StringUtils.lowerCase("getSectionsOfCenter"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSections");
        serviceUrlMap.put(StringUtils.lowerCase("getLibrary"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSection");
        serviceUrlMap.put(StringUtils.lowerCase("getContents"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getContents");

    }

    public String getServiceUrl(String funtionName) {

        String serviceUrl = serviceUrlMap.get(StringUtils.lowerCase(funtionName));
        Logger.log4j.info("service url for functionName[" + funtionName + "] : " + serviceUrl);
        return serviceUrl;
    }

}
