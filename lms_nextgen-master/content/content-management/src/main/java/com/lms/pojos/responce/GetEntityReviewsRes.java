package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class GetEntityReviewsRes {
    public List<GetEntityReviews> goodReviews = new ArrayList<GetEntityReviews>();
    public int totalGoodHits;
    public List<GetEntityReviews> badReviews = new ArrayList<GetEntityReviews>();
    public int totalBadHits;
    public List<GetEntityReviews> averageReviews = new ArrayList<GetEntityReviews>();
    public int totalAvgHits;
}
