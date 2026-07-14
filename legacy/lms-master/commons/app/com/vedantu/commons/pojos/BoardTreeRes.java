package com.vedantu.commons.pojos;

import java.util.List;

import com.vedantu.commons.enums.boards.BoardType;

public class BoardTreeRes {

	public String id;
	public String name;
	public String code;
	public BoardType type;
	public String treeName;
	public List<BoardTreeRes> children;
}
