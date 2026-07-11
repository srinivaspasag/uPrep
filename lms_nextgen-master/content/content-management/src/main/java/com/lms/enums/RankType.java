package com.lms.enums;

import java.util.Calendar;

public enum RankType {
    OVERALL, MONTHLY {
        @Override
        public String identifier() {
            Calendar calendar = Calendar.getInstance();
            String identifier = MONTHLY.name() + "_"
                    + (calendar.get(Calendar.MONTH) + 1) + "_"
                    + calendar.get(Calendar.YEAR);
            return identifier;
        }
    },
    WEEKLY {
        @Override
        public String identifier() {
            Calendar calendar = Calendar.getInstance();
            String identifier = WEEKLY.name() + "_"
                    + calendar.get(Calendar.WEEK_OF_YEAR) + "_"
                    + calendar.get(Calendar.YEAR);
            return identifier;
        }
    };

    public static RankType valueOfKey(String key) {
        RankType rankType = OVERALL;
        try {
            rankType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return rankType;
    }

    public String identifier() {
        String identifier = OVERALL.name();
        return identifier;
    }
}
