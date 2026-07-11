package com.vedantu.organizations.auth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.Response;

import com.vedantu.billing.managers.OrderManager;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.ei.requests.AbstractVedantuRequest;
import com.vedantu.ei.requests.AuthRequest;
import com.vedantu.ei.requests.EnrollRequest;
import com.vedantu.ei.requests.RegRequest;
import com.vedantu.ei.responses.EnrollResponse;
import com.vedantu.ei.responses.RegResponse;
import com.vedantu.ei.responses.pojos.ClassInfo;
import com.vedantu.ei.results.AuthResult;
import com.vedantu.ei.results.EnrollResult;
import com.vedantu.ei.results.RegResult;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.ei.ExtOrgRequestModelDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.ei.ExtOrgRequestModel;
import com.vedantu.organization.pojos.ExternalOrganizationEndpoints;
import com.vedantu.organization.pojos.OrgMemberExtraInfo;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberMappingReq;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberReq;
import com.vedantu.organization.pojos.responses.members.AddOrgMemberMappingRes;
import com.vedantu.organization.pojos.responses.members.AddOrgMemberRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.enums.Gender;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.responses.UserAuthRes;

public class ExtAuthHandler extends AuthHandler {

    private static final String  CONTENT_TYPE_JSON = "application/json";
    private static final long    DEFAULT_TIMT_OUT  = 60L;
    private static final ALogger LOGGER            = Logger.of(ExtAuthHandler.class);

    public ExtAuthHandler(Organization org) {

        super(org, AuthType.EXT_AUTH_ORG);
    }

    @Override
    public UserAuthRes authenticate(String username, String password) throws VedantuException {

        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);

        ExternalOrganizationEndpoints endPoint = organization.endPoint;
        if (endPoint == null || StringUtils.isEmpty(endPoint.getAuthEndpoint())) {
            throw new VedantuException(VedantuErrorCode.ORG_END_POINT_NOT_REACHABLE,
                    "no endPoint is defined for authentication ");
        }

        JSONObject jsonRes = getExtEndPointResponse(request, endPoint.getAuthEndpoint());

        LOGGER.debug("server response : " + jsonRes);
        String errorCode = JSONUtils.getString(jsonRes, ConstantsGlobal.ERROR_CODE);

        if (jsonRes == null || StringUtils.isNotEmpty(errorCode)) {

            // if failed response from Institute server then check in Vedantu server for SUPER_ADMIN
            // authentication

            User user = UserDAO.INSTANCE.authenticateUser(username, password);
            if (StringUtils.equals(user._getStringId(), organization.adminUserId)) {
                // this is org admin
                return UserManager.getAuthResFromUser(user);
            }

            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED, JSONUtils.getString(
                    jsonRes, "errorMessage"));
        }

        AuthResult authResult = new AuthResult();
        try {
            authResult.fromJSON(JSONUtils.getJSONObject(jsonRes, ConstantsGlobal.RESULT));
        } catch (JSONException e) {
            throw new VedantuException(VedantuErrorCode.AUTHENTICATION_FAILED, e.getMessage());
        }

        LOGGER.debug("auth response " + authResult.getFirstName() + " " + authResult.getGender()
                + ", classes: " + authResult.getClasses());

        String firstName = authResult.getFirstName();
        String lastName = authResult.getLastName();
        Gender gender = Gender.valueOfKey(authResult.getGender());
        String memberId = StringUtils.isNotEmpty(authResult.getMemberId()) ? authResult
                .getMemberId() : authResult.getUserId();

        User user = null;

        try {

            OrgMember orgMember = super.checkOrgMember(memberId, organization._getStringId());
            addMemberMappings(authResult, orgMember);

            user = UserDAO.INSTANCE.getById(orgMember.userId);
            // update any change in firstName, lastName etc
            user.firstName = firstName;
            user.lastName = lastName;
            UserDAO.INSTANCE.save(user);

        } catch (VedantuException ex) {
            if (ex.errorCode != VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND) {
                throw ex;
            }
            // addOrgMember
            AddOrgMemberReq memberAddRequest = new AddOrgMemberReq();
            memberAddRequest.firstName = firstName;
            memberAddRequest.lastName = lastName;
            memberAddRequest.gender = gender;
            memberAddRequest.orgId = organization._getStringId();
            memberAddRequest.dob = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
            memberAddRequest.profile = OrgMemberProfile.valueOfKey(authResult.getRole());
            memberAddRequest.extUserId = authResult.getUserId();
            memberAddRequest.setTargetMemberId(memberId);
            AddOrgMemberRes addOrgMemberRes = addOrgMember(memberAddRequest, false);
            addMemberMappings(authResult, checkOrgMember(memberId, addOrgMemberRes.orgId));
            user = UserDAO.INSTANCE.getById(addOrgMemberRes.userId);
        }
        return UserManager.getAuthResFromUser(user);
    }

    @Override
    public AddOrgMemberRes addMember(AddOrgMemberReq addMemberReq, boolean isMemberIdSysGenerated)
            throws VedantuException {

        if (organization.endPoint == null
                || StringUtils.isEmpty(organization.endPoint.getRegisterEndpoint())) {
            throw new VedantuException(VedantuErrorCode.ORG_SIGNUP_NOT_SUPPORTED,
                    "no signup endPoint is defined for org:" + addMemberReq.orgId);
        }

        if (StringUtils.isEmpty(addMemberReq.password)) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                    "password can not be null");
        }

        RegRequest regRequest = new RegRequest();
        regRequest.setEmail(addMemberReq.getEmail());
        regRequest.setFirstName(addMemberReq.firstName);
        regRequest.setGender(addMemberReq.gender == null ? Gender.UNKNOWN.name()
                : addMemberReq.gender.name());
        regRequest.setLastName(addMemberReq.lastName);
        regRequest.setPassword(addMemberReq.password);
        regRequest.setRole(addMemberReq.profile.name());
        regRequest.setUsername(addMemberReq.getEmail());

        // For now we are providing this field from our end in additionl params
        regRequest.addAdditionalInfo("Date of Birth", addMemberReq.dob);
        // TODO: update *Date of Birth* on RegRequest

        verifyExtraInputFields(addMemberReq);

        if (addMemberReq.extraInfo != null) {
            for (OrgMemberExtraInfo iFieldInfo : addMemberReq.extraInfo) {
                regRequest.addAdditionalInfo(iFieldInfo.name.trim(),
                        iFieldInfo.value == null ? StringUtils.EMPTY : iFieldInfo.value.trim());
            }
        }

        JSONObject jsonRes = getExtEndPointResponse(regRequest,
                organization.endPoint.getRegisterEndpoint());

        LOGGER.debug("server response : " + jsonRes);
        RegResponse response = new RegResponse(null, null, new RegResult());
        try {
            response.fromJSON(jsonRes);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        String errorCode = response.getErrorCode();
        RegResult result = (RegResult) response.getResult();

        if (StringUtils.isNotEmpty(errorCode)) {
            VedantuErrorCode eCode = errorCode
                    .equalsIgnoreCase(VedantuErrorCode.USER_ALREADY_EXISTS.name()) ? VedantuErrorCode.USER_ALREADY_EXISTS
                    : VedantuErrorCode.MISSING_PARAMETERS;
            String msg = response.getErrorMessage();

            if (eCode.equals(VedantuErrorCode.MISSING_PARAMETERS)) {
                @SuppressWarnings("unchecked")
                List<String> missingParams = result.getMissingParameters();
                msg = StringUtils.join(missingParams, ",");
                msg = "missing fields " + msg;
            }
            throw new VedantuException(eCode, msg);
        }
        String extUserId = result.getUserId();

        if (StringUtils.isEmpty(extUserId)) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR,
                    "org endpoint does not returned userId : " + extUserId);
        }
        String memberId = result.getMemberId();
        memberId = StringUtils.isEmpty(memberId) ? extUserId : memberId;
        addMemberReq.setTargetMemberId(memberId);
        addMemberReq.extUserId = extUserId;
        AddOrgMemberRes addOrgMemberRes = addOrgMember(addMemberReq, isMemberIdSysGenerated);
        return addOrgMemberRes;
    }

    @Override
    public AddOrgMemberMappingRes addMemberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq,
            boolean noExceptionOnExistingMapping) throws VedantuException {

        if (organization.endPoint == null
                || StringUtils.isEmpty(organization.endPoint.getEnrollmentEndPoint())) {
            throw new VedantuException(VedantuErrorCode.ORG_ENROLLMENT_NOT_SUPPORTED,
                    "no enrollment endPoint is defined for org:" + addOrgMemberMappingReq.orgId);
        }

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(addOrgMemberMappingReq.orgId,
                addOrgMemberMappingReq.targetUserId);
        if (null == orgMember) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        // TODO: need to have a API for multiple section addition
        // for now only 1st section mapping will be added
        OrgSection orgSection = checkIfAddMappingAllowed(addOrgMemberMappingReq).get(0);

        EnrollRequest enrollRequest = new EnrollRequest();
        enrollRequest.setUserId(orgMember.extUserId);
        enrollRequest.setClassCode(orgSection.code);

        JSONObject jsonRes = getExtEndPointResponse(enrollRequest,
                organization.endPoint.getEnrollmentEndPoint());
        EnrollResponse enrollResponse = new EnrollResponse(null, null, new EnrollResult());
        try {
            enrollResponse.fromJSON(jsonRes);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(enrollResponse.getErrorCode())) {
            throw new VedantuException(toVedantuErrorCode(enrollResponse.getErrorCode()),
                    enrollResponse.getErrorMessage());
        }

        OrgMemberMappingInfo orgMemberInfo = new OrgMemberMappingInfo(orgSection.programId,
                orgSection.centerId, orgSection._getStringId(), null);

        EnrollResult enrollResult = (EnrollResult) enrollResponse.getResult();
        orgMemberInfo.endTime = enrollResult.classInfo.getExpiry();

        boolean added = orgMember.add(orgMemberInfo);
        if (added) {
            SrcEntity item = new SrcEntity(EntityType.SECTION, orgSection._getStringId());
            orgMemberInfo.orderId = StringUtils.isEmpty(addOrgMemberMappingReq.transactionId) ? StringUtils.EMPTY
                    : OrderManager.markTransactionConmpleted(addOrgMemberMappingReq.transactionId,
                            item);

            orgMemberInfo.timeJoined = System.currentTimeMillis();
        }
        OrgMemberDAO.INSTANCE.save(orgMember);
        AddOrgMemberMappingRes addOrgMemberMappingRes = new AddOrgMemberMappingRes(
                orgMember._getStringId(), orgMember.recordState, added);
        return addOrgMemberMappingRes;
    }

    @Override
    public String getMemberUsername(String orgId, String memberId) {

        String memberUsername = StringUtils.lowerCase("/org" + "/" + orgId + "/" + memberId);
        return memberUsername;
    }

    @Override
    public boolean isUpdateValid(Set<String> updateList) throws VedantuException {

        boolean isUpdatable = true;
        isUpdatable &= !updateList.contains(OrgMember.FIELD_FIRST_NAME);
        isUpdatable &= !updateList.contains(OrgMember.FIELD_LAST_NAME);
        isUpdatable &= !updateList.contains(OrgMember.FIELD_MAPPINGS);
        isUpdatable &= !updateList.contains(OrgMember.FIELD_MEMBER_ID);
        isUpdatable &= !updateList.contains(OrgMember.FIELD_PROFILE);

        if (!isUpdatable) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED);

        }
        return isUpdatable;
    }

    @SuppressWarnings("unchecked")
    private void addMemberMappings(AuthResult authResult, OrgMember orgMember) {

        List<String> classIds = new ArrayList<String>();
        Map<String, ClassInfo> cCodeToClassInfoMap = new HashMap<String, ClassInfo>();
        LOGGER.debug("classes : " + authResult.getClasses());

        @SuppressWarnings("rawtypes")
        List classes = authResult.getClasses();

        for (int i = 0; i < classes.size(); i++) {
            ClassInfo cInfo = (ClassInfo) classes.get(i);
            classIds.add(cInfo.getClassCode());
            cCodeToClassInfoMap.put(cInfo.getClassCode(), cInfo);
        }

        Set<String> existingSectionIds = getSectionCodes(orgMember.mappings);

        LOGGER.debug("existingSectionIds : " + existingSectionIds + ", classIds: " + classIds
                + ", info map ; " + cCodeToClassInfoMap);

        if (CollectionUtils.disjunction(classIds, existingSectionIds).size() != 0) {
            // / update member mapping on the fly.

            List<String> removableClassIds = (List<String>) CollectionUtils.subtract(
                    existingSectionIds, classIds);
            LOGGER.debug("removableClassIds: " + removableClassIds);
            if (CollectionUtils.isNotEmpty(removableClassIds)) {
                List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionByCode(
                        organization._getStringId(), removableClassIds);

                for (OrgSection section : sections) {
                    orgMember.remove(new OrgMemberMappingInfo(section.programId, section.centerId,
                            section._getStringId(), null));
                }
            }

            List<String> remainingClassIds = (List<String>) CollectionUtils.subtract(classIds,
                    removableClassIds);
            LOGGER.debug("remainingClassIds: " + remainingClassIds);

            if (CollectionUtils.isNotEmpty(remainingClassIds)) {
                List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionByCode(
                        organization._getStringId(), remainingClassIds);

                for (OrgSection section : sections) {
                    OrgMemberMappingInfo orgMemberInfo = new OrgMemberMappingInfo(
                            section.programId, section.centerId, section._getStringId(), null);
                    orgMemberInfo.endTime = cCodeToClassInfoMap.get(section.code).getExpiry();
                    orgMember.add(orgMemberInfo);
                }
            }
        }

        orgMember.firstName = authResult.getFirstName();
        orgMember.lastName = authResult.getLastName();
        OrgMemberDAO.INSTANCE.save(orgMember);
    }

    private Set<String> getSectionCodes(Set<OrgMemberMappingInfo> mappings) {

        List<String> sectionIds = new ArrayList<String>();

        if (mappings == null) {
            return new HashSet<String>();
        }

        for (OrgMemberMappingInfo memberInfo : mappings) {
            sectionIds.add(memberInfo.sectionId);
        }

        List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionsByIds(
                organization._getStringId(), null, ObjectIdUtils.toObjectIds(sectionIds));

        Set<String> sectionCodes = new HashSet<String>();

        for (OrgSection section : sections) {
            sectionCodes.add(section.code);
        }

        return sectionCodes;
    }

    private JSONObject getExtEndPointResponse(AbstractVedantuRequest request, String endPointUrl)
            throws VedantuException {

        ExtOrgRequestModel extReq = new ExtOrgRequestModel(organization._getStringId(),
                endPointUrl, null);

        JSONObject reqJSON = request.toJSON();

        Promise<Response> externalAuthResponsePromise = WS.url(endPointUrl)
                .setContentType(CONTENT_TYPE_JSON).setFollowRedirects(true)
                .setTimeout((int) DEFAULT_TIMT_OUT * 1000).post(reqJSON.toString());

        reqJSON.remove("password");

        LOGGER.debug("request JSON : " + reqJSON.toString());

        Response externalAuthResponse = null;
        try {
            externalAuthResponse = externalAuthResponsePromise.get(DEFAULT_TIMT_OUT,
                    TimeUnit.SECONDS);
            if (externalAuthResponse.getStatus() != HttpStatus.SC_OK) {
                throw new VedantuException(VedantuErrorCode.ORG_END_POINT_NOT_REACHABLE,
                        "httpStatus : " + externalAuthResponse.getStatus());
            }
        } catch (Exception ex) {
            LOGGER.debug(" can not found organization" + organization._getStringId());
            throw new VedantuException(VedantuErrorCode.ORG_END_POINT_NOT_REACHABLE,
                    ex.getMessage());
        } finally {

            extReq.request = reqJSON.toString();
            extReq.endTime = System.currentTimeMillis();

            if (externalAuthResponse != null) {

                extReq.responseCode = externalAuthResponse.getStatus();
                extReq.responseTime = (int) (extReq.endTime - extReq.timeCreated);
                try {
                    extReq.response = externalAuthResponse.getBody();
                } catch (Throwable e) {
                    // swallow
                }
            } else {
                extReq.responseCode = HttpStatus.SC_BAD_GATEWAY;
            }
            ExtOrgRequestModelDAO.INSTANCE.save(extReq);
        }

        LOGGER.debug("response body" + externalAuthResponse.getBody());
        JSONObject jsonRes = null;
        try {
            jsonRes = new JSONObject(externalAuthResponse.getBody());
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (jsonRes == null) {
            LOGGER.debug(" no response from org endPoint " + endPointUrl);
            throw new VedantuException(VedantuErrorCode.ORG_END_POINT_NOT_REACHABLE,
                    "no response from org endpoint");
        }

        return jsonRes;
    }

    private VedantuErrorCode toVedantuErrorCode(String erroCode) {

        if (erroCode.equalsIgnoreCase(VedantuErrorCode.INVALID_USER_ID.name())) {
            return VedantuErrorCode.INVALID_USER_ID;
        } else if (erroCode.equalsIgnoreCase("INVALID_CLASS_CODE")) {
            return VedantuErrorCode.INVALID_SECTION_ID;
        }
        return null;
    }

}
