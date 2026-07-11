package com.lms.clusters;

import java.util.Comparator;

public class ClusterComparator implements Comparator<NewsFeedCluster> {


    public final static ClusterComparator INSTANCE = new ClusterComparator();

    private ClusterComparator() {

    }

    @Override
    public int compare(NewsFeedCluster feed1, NewsFeedCluster feed2) {

        if (feed1.lastNewsFeedTime > feed2.lastNewsFeedTime) {
            return -1;
        } else if (feed1.lastNewsFeedTime < feed2.lastNewsFeedTime) {
            return 1;
        }
        return 0;
    }
}
