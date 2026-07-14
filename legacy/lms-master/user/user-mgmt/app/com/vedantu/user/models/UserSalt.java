package com.vedantu.user.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value="usersalts", noClassnameStored=true)
public class UserSalt extends VedantuBaseMongoModel{
	
	@Indexed(unique = true)
	public String username;
    public String salt;
    
    public UserSalt() {
    	
    }
    
    public UserSalt(String username, String salt) {
    	this.username = username;
    	this.salt = salt;
    }

}
