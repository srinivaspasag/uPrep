/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package pojos;

import org.json.JSONObject;

/**
 *
 * @author ajith
 */
public class UserOrg {

    private String id;
    private String name;
    private String fullName;
    private String type;
    private String orgThumbnail;
    private String memberId;
    private String orgMemberId;
    private String orgUserFirstName;
    private String orgUserLastName;
    private String orgUserProfile;
    private String orgUserProfilePic;
    private boolean canImpersonate;
    private boolean showClassroomConnect;
    private String userState;
    private String authType;
    private JSONObject extraInfo;

    public UserOrg(String id, String name, String fullName, String type, String orgThumbnail,
            String memberId, String orgMemberId, String orgUserFirstName, String orgUserLastName,
            String orgUserProfile, String orgUserProfilePic, String userState, String authType, boolean showClassroomConnect) {

        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.type = type;
        this.orgThumbnail = orgThumbnail;
        this.memberId = memberId;
        this.orgMemberId = orgMemberId;
        this.orgUserFirstName = orgUserFirstName;
        this.orgUserLastName = orgUserLastName;
        this.orgUserProfile = orgUserProfile;
        this.orgUserProfilePic = orgUserProfilePic;
        this.canImpersonate = false;
        this.userState = userState;
        this.authType = authType;
        this.extraInfo = new JSONObject();
        this.setClassroomConnectStatus(showClassroomConnect);
    }

    public String getOrgId() {
        return id;
    }

    public String getName() {

        return name;
    }

    public String getFullName() {

        return fullName;
    }

    public String getType() {

        return type;
    }

    public String getOrgThumbnail() {

        return orgThumbnail;
    }

    public String getMemberId() {

        return memberId;
    }

    public String getOrgMemberId() {

        return orgMemberId;
    }

    public String getOrgUserFirstName() {

        return orgUserFirstName;
    }

    public String getOrgUserLastName() {

        return orgUserLastName;
    }

    public String getOrgUserFullName() {

        return orgUserFirstName + " " + orgUserLastName;
    }

    public String getOrgUserProfile() {

        return orgUserProfile;
    }

    public String getOrgUserProfilePic() {

        return orgUserProfilePic;
    }

    public void setCanImpersonate(boolean  canImpersonateIn) {
        canImpersonate = canImpersonateIn;
    }

    public boolean getCanImpersonate() {
        return canImpersonate;
    }

    public String getUserState() {
        return userState;
    }

    public String getAuthType(){
        return authType;
    }

    public void setExtraInfo(JSONObject extraInfoInput){
        if(extraInfoInput!=null){
            this.extraInfo = extraInfoInput;
        }
    }

    public JSONObject getExtraInfo(){
        return this.extraInfo;
    }

    public boolean getClassroomConnectStatus() {
        return showClassroomConnect;
    }

    public void setClassroomConnectStatus(boolean showClassroomConnect) {
        this.showClassroomConnect = showClassroomConnect;
    }
}
