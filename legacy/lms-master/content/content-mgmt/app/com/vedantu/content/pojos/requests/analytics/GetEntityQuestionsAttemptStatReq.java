package com.vedantu.content.pojos.requests.analytics;

public class GetEntityQuestionsAttemptStatReq extends GetUserEntityAnalyticsReq {

    public String  setName;          // if the question paper have multiple sets
    public String  brdId;            // filter for only this board

    public String  orderBy;          // for Most Attempted orderBy=attempts,
                                      // sortOrder=DESC, ASC - for list attempted, most
                                      // correct orderBy=correct
    public String  sortOrder;
    public int     start;
    public int     size;
    public boolean downloadQuestions;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{setName:").append(setName).append(", brdId:").append(brdId)
                .append(", orderBy:").append(orderBy).append(", sortOrder:").append(sortOrder)
                .append(", start:").append(start).append(", size:").append(size)
                .append(", entity:").append(entity).append(", targetUserId:").append(targetUserId)
                .append(", orgId:").append(orgId).append(", downloadQuestions:")
                .append(downloadQuestions).append(", orgMemberProfile:").append(orgMemberProfile)
                .append(", callingUserId:").append(callingUserId).append(", userId:")
                .append(userId).append(", callingApp:").append(callingApp)
                .append(", callingAppId:").append(callingAppId).append("}");
        return builder.toString();
    }

}
