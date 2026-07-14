package com.vedantu.cmds.pojos.responses;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.cmds.pojos.BoardInfo;

public class AddMappingsRes {
    public List<BoardInfo> parentBoardDetails = new ArrayList<BoardInfo>();
    public List<BoardInfo> targetBoardDetails = new ArrayList<BoardInfo>();
}
