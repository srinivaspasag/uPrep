package com.lms.pojos.responce.analytics;

import com.lms.models.EntityMeasures;
import com.lms.pojos.tests.BoardAnalyticsInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class UserBoardAnalyticsInfoRes {
    public int totalMarks;// this will only be used when returning info for a
    // single user, (for group of users the info is
    // returned in entity metadata info)
    public int rank; // this will be used when showing analytics data for
    // individual user

    public int qusCount;
    public BoardAnalyticsInfo entity;
    public EntityMeasures measures;
    public List<UserBoardAnalyticsInfoRes> children;

    public UserBoardAnalyticsInfoRes(BoardAnalyticsInfo entity,
                                     EntityMeasures measures) {
        this.entity = entity;
        this.measures = measures;
    }

    public void addChildAnalytics(UserBoardAnalyticsInfoRes child) {
        if (child == null) {
            return;
        }
        if (this.children == null) {
            this.children = new ArrayList<UserBoardAnalyticsInfoRes>();
        }
        this.children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{entity:");
        builder.append(entity);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", children:");
        builder.append(children);
        builder.append("}");
        return builder.toString();
    }


}
