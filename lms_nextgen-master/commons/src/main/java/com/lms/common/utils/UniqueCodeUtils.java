package com.lms.common.utils;


import java.util.Base64;
import java.util.Random;


public class UniqueCodeUtils {

    /**
     * @param type  ==> a unique key for your entity (i.e SECTION, ORG etc.)
     * @param count ==> length of the returned code
     * @return ALPH ANUMERIC case sensitive unique code
     */

    public static String generateUniqueCode(String type) {

        if (type == null) {
            type = "GLOBAL";
        }

        type = type.toUpperCase();

        String uniqueCode = null;
        synchronized (type.intern()) {
            long cTime = System.currentTimeMillis();
            while (!isUniqueCodeAvailable(type,
                    (uniqueCode = Base64.getEncoder().encodeToString(String.valueOf(cTime * new Random().nextInt(99)).getBytes())))) {
                cTime = System.currentTimeMillis();
            }

           /* VedantuUniqueCode uCode = new VedantuUniqueCode(type, uniqueCode);
            uCode.timeCreated = cTime;
            VedantuUniqueCodeDAO.INSTANCE.save(uCode);*/
        }

        return uniqueCode;
    }

    public static boolean isUniqueCodeAvailable(String type, String uniqueCode) {

       /* DBObject query = new BasicDBObject("type", type);
        query.put("code", uniqueCode);
        long count = VedantuUniqueCodeDAO.INSTANCE.count(query);
        return count == 0;*/
        return true;
    }
}
