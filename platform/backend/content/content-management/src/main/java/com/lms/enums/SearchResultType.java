package com.lms.enums;

public enum SearchResultType {
    ALL,FOLLOWING,CREATED,UNSOLVED,UNATTEMPTED,ATTEMPTED
  /*  ALL {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

            // do nothing
        }
    },
    FOLLOWING {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

            ElasticSearchUtils.addFollowingQueryFilter(boolQuery, userId);
        }
    },
    CREATED {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

            boolQuery.must(QueryBuilders.fieldQuery(ConstantsGlobal.USER_ID, userId));

        }
    },
    UNSOLVED {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

        }
    },
    ATTEMPTED {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

            String childType = this.name().toLowerCase();
            boolQuery.must(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.fieldQuery(childType + "." + ConstantsGlobal.USER_ID, userId)));
        }
    },
    UNATTEMPTED {

        @Override
        public void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId) {

            String childType = ATTEMPTED.name().toLowerCase();
            boolQuery.mustNot(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.fieldQuery(childType + "." + ConstantsGlobal.USER_ID, userId)));
        }
    };

    public static SearchResultType valueOfKey(String key) {

        SearchResultType type = ALL;
        try {
            type = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
            Logger.error(" Illegal SearchResultType type key supplied" + key);
        }
        return type;
    }

    public abstract void addSearchQueryFlter(BoolQueryBuilder boolQuery, String userId);*/
}