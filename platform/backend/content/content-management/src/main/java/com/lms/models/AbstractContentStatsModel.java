package com.lms.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractContentStatsModel extends
		AbstractBoardEntityTagModel {

	public int upVotes;// total no of upVotes
	public int views;
	public int followers;
	public int comments;
	public int shares;
	public int good;
	public int average;
	public int bad;

}
