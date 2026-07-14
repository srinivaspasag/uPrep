package com.lms.pojos.news.details;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.OrgMemberProfile;

public class UserNewsEntityDetails extends SrcEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static {
        //EntityNewsDetailsFactory.INSTANCE.register(EntityType.USER, UserNewsEntityDetails.class);
    }

    static {
        //EntityNewsDetailsFactory.INSTANCE.register(EntityType.PROGRAM, SectionNewsEntityDetails.class);
    }

    public String firstName;
    public String lastName;
    public String thumbnail;
    public String _id;
    public OrgMemberProfile profile;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {

        return "UserNewsEntityDetails [firstName=" + firstName + ", lastName=" + lastName + ", thumbnail=" + thumbnail
                + ", _id=" + _id + ", profile=" + profile + ", toString()=" + super.toString() + "]";
    }

}
