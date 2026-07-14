package com.lms.pojos.responce.analytics;

import com.lms.models.EntityMeasures;
import com.lms.pojos.tests.EntityInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class UserAnalyticsInfoRes {

    public int totalMarks;// this will only be used when returning info for a
    // single user, (for group of users the info is
    // returned in entity metadata info)
    public int qusCount;

    public int rank; // this will be used when showing analytics data for
    // individual user
    public EntityInfo entity;
    public long startTime;
    public long endTime;
    public EntityMeasures measures;

    public List<UserBoardAnalyticsInfoRes> boards; // different subjects in the
    // tests

    public List<UserAnalyticsInfoRes> children; // paper1/paper2

    public UserAnalyticsInfoRes() {
    }

    public UserAnalyticsInfoRes(EntityInfo entity, EntityMeasures measures) {
        super();
        this.entity = entity;
        this.measures = measures;
    }

    public void addBoardAnalytics(UserBoardAnalyticsInfoRes boardAnalytics) {

        if (this.boards == null) {
            this.boards = new ArrayList<UserBoardAnalyticsInfoRes>();
        }
        if (boardAnalytics == null) {
            return;
        }
        this.boards.add(boardAnalytics);
    }

    public void addChildAnalytics(UserAnalyticsInfoRes childAnalytics) {
        if (this.children == null) {
            this.children = new ArrayList<UserAnalyticsInfoRes>();
        }
        if (childAnalytics == null) {
            return;
        }
        this.children.add(childAnalytics);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{entity:");
        builder.append(entity);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", boards:");
        builder.append(boards);
        builder.append(", children:");
        builder.append(children);
        builder.append("}");
        return builder.toString();
    }

}
