/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pojos;

import java.io.Serializable;

/**
 *
 * @author anirban
 */
public class OrgInfo implements Serializable{
    String organizationId;
    String orgId;
    String userRole;
    String memberId;
    String instFullName;
    String instLogo;
    String type;
    String firstName;
    String lastName;
    String fullName;
    String profilePic;
    String orgMemberId;

    public OrgInfo(String orgId, String userRole, String memberId, String instFullName, String instLogo,
            String type, String firstName, String lastName, String fullName, String profilePic, String orgMemberId) {
        this.orgId = orgId;
        this.organizationId = orgId;
        this.userRole = userRole;
        this.memberId = memberId;
        this.instFullName = instFullName;
        this.instLogo = instLogo;
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.profilePic = profilePic;
        this.orgMemberId = orgMemberId;
    }

    public String getInstFullName() {
        return instFullName;
    }

    public String getInstLogo() {
        return instLogo;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getProfilePic() {
        return profilePic;
    }
    
    public void updateProfilePic(String pic){
        profilePic = pic;
    }

    public String getOrgMemberId() {
        return orgMemberId;
    }

    public void setOrgMemberId(String orgMemberId) {
        this.orgMemberId = orgMemberId;
    }
}
