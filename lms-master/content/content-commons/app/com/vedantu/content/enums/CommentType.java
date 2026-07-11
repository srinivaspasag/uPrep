package com.vedantu.content.enums;

public enum CommentType {
	COMMENT, REPLY, REVIEW;

	public static CommentType valueOfKey(String key) {
		CommentType commentType = COMMENT;
		try {
			commentType = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}
		return commentType;
	}
}
