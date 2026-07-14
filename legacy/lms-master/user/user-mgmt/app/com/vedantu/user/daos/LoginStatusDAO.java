package com.vedantu.user.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.enums.UserStatus;
import com.vedantu.user.models.LoginStatus;

public class LoginStatusDAO extends VedantuBasicDAO<LoginStatus, ObjectId> {

    private static final ALogger       LOGGER   = Logger.of(LoginStatusDAO.class);

    public static final LoginStatusDAO INSTANCE = new LoginStatusDAO();

    private LoginStatusDAO() {

        super(LoginStatus.class);
    }

    /**
     * Login is not org specific
     * 
     * @param userId
     * @param deviceId
     * @param deviceType
     * @param expiryTimeOffset
     * @return
     * @throws VedantuException
     */
    public LoginStatus recordLogin(String userId, String deviceId, DeviceType deviceType,
            long expiryTimeOffset) throws VedantuException {

        LoginStatus status = getStatus(userId, deviceId, deviceType, UserStatus.LOGGED_IN);
        if (status != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_LOGGED_IN);
        }
        long currentTime = System.currentTimeMillis();
        LoginStatus loginStatus = new LoginStatus(userId, deviceId, deviceType);
        loginStatus.loginTime = currentTime;
        loginStatus.expiryTime = expiryTimeOffset;
        loginStatus.status = UserStatus.LOGGED_IN;
        save(loginStatus);
        return loginStatus;
    }

    /**
     * Login is not org specific
     * 
     * @param userId
     * @param deviceId
     * @param deviceType
     * @param expiryTimeOffset
     * @return
     * @throws VedantuException
     */

    public boolean recordLogout(String userId, String deviceId, DeviceType deviceType)
            throws VedantuException {

        LoginStatus status = getStatus(userId, deviceId, deviceType, UserStatus.LOGGED_IN);
        if (status == null) {
            throw new VedantuException(VedantuErrorCode.USER_LOGIN_NOT_FOUND);
        }

        status.logoutTime = System.currentTimeMillis();
        status.status = UserStatus.LOGGED_OUT;
        LoginStatusDAO.INSTANCE.save(status);
        return true;

    }

    public LoginStatus getStatus(String userId, String deviceId, DeviceType deviceType,
            UserStatus loginStatus) throws VedantuException {

        List<String> userIds = new ArrayList<String>();
        userIds.add(userId);

        MutableLong totalHits = new MutableLong();
        List<LoginStatus> status = getStatus(userIds, deviceId, deviceType, loginStatus, 0, 1,
                totalHits);
        if (CollectionUtils.isNotEmpty(status)) {
            return status.get(0);
        }
        return null;
    }

    public List<LoginStatus> getAllStatus(String userId, String deviceId, DeviceType deviceType,
            UserStatus loginStatus, int start, int size, MutableLong totalHits)
            throws VedantuException {

        List<String> userIds = new ArrayList<String>();
        userIds.add(userId);

        return getStatus(userIds, deviceId, deviceType, loginStatus, start, size, totalHits);

    }

    public List<LoginStatus> getStatus(List<String> userIds, String deviceId,
            DeviceType deviceType, UserStatus loginStatus, int start, int size,
            MutableLong totalHits) throws VedantuException {

        Query<LoginStatus> statusQuery = LoginStatusDAO.INSTANCE.getQuery();
        statusQuery.field("userId").in(userIds);
        if (StringUtils.isNotEmpty(deviceId)) {
            statusQuery.field("deviceId").equal(deviceId);
        }
        if (deviceType != null && deviceType != DeviceType.UNKNOWN) {
            statusQuery.field("deviceType").equal(deviceType);
        }
        if (loginStatus != null && loginStatus != UserStatus.UNKNOWN) {
            statusQuery.field("status").equal(loginStatus);
        }
        statusQuery.order("-loginTime");
        totalHits.setValue(statusQuery.countAll());
        return statusQuery.asList();

    }

    public List<LoginStatus> getStatus(List<String> userIds, String deviceId, DeviceType appType,
            UserStatus status) throws VedantuException {

        MutableLong totalHits = new MutableLong();
        return getStatus(userIds, deviceId, appType, status, MongoManager.NO_START,
                MongoManager.NO_LIMIT, totalHits);

    }

}
