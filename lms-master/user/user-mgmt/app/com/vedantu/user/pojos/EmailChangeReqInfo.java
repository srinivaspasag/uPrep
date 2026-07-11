package com.vedantu.user.pojos;

public class EmailChangeReqInfo {
	public String email;
	public String verificationCode;
	
	public EmailChangeReqInfo() {
		super();
	}

	public EmailChangeReqInfo(String email, String verificationCode) {
		super();
		this.email = email;
		this.verificationCode = verificationCode;
	}
}