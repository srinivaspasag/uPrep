package com.lms.board.pojos.test;

import com.lms.common.vedantu.enums.BoardType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BoardTreeRes {

    public String id;
    public String name;
    public String code;
    public BoardType type;
    public String treeName;
    public List<BoardTreeRes> children;
}
