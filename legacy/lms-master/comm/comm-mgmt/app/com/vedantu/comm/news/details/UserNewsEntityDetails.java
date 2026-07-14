package com.vedantu.comm.news.details;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.enums.OrgMemberProfile;

public class UserNewsEntityDetails extends SrcEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String firstName;
	public String lastName;
	public String thumbnail;
	public String _id;
	public OrgMemberProfile profile;

	static {
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.USER,
				UserNewsEntityDetails.class);
	}
	
	public String get_id() {
		return _id;
	}


	public void set_id(String _id) {
		this._id = _id;
	}




	public String getFirstName() {
		return firstName;
	}


	static {
		EntityNewsDetailsFactory.INSTANCE.register(EntityType.PROGRAM,
				SectionNewsEntityDetails.class);
	}
	public String getLastName() {
		return lastName;
	}

	public String getThumbnail() {
		return thumbnail;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
    public String toString() {

        return "UserNewsEntityDetails [firstName=" + firstName + ", lastName=" + lastName
                + ", thumbnail=" + thumbnail + ", _id=" + _id + ", profile=" + profile
                + ", toString()=" + super.toString() + "]";
    }

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
//	@Override
//	public JsonElement toJsonElement() throws JsonParseException {
//		Logger.log4j.info("Serializaing ....");
//		GsonBuilder gsonBuilder = new GsonBuilder();
//		Gson gson = gsonBuilder.create();
//		return gson.toJsonTree(this);
//	}
//
//	@Override
//	public void fromJsonElement(JsonElement json) throws JsonParseException {
//		// TODO Auto-generated method stub
//		
//	}
}
