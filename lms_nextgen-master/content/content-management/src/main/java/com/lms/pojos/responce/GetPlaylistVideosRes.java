package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetPlaylistVideosRes {
    public List<GetVideoRes> videoIds = new ArrayList<GetVideoRes>();
}
