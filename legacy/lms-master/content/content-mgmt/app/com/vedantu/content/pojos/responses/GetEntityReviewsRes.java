package com.vedantu.content.pojos.responses;

import java.util.ArrayList;
import java.util.List;

public class GetEntityReviewsRes {
    public List<GetEntityReviews> goodReviews = new ArrayList<GetEntityReviews>();
    public int totalGoodHits;
    public List<GetEntityReviews> badReviews = new ArrayList<GetEntityReviews>();
    public int totalBadHits;
    public List<GetEntityReviews> averageReviews = new ArrayList<GetEntityReviews>();
    public int totalAvgHits;
}
