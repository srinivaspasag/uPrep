package com.vedantu.user.utils;


public class PasswordUtils {

    private static final int DEFAULT_PASSWORD_SIZE = 8;

    public static String generateRandomPassword() {

        return generateRandomPassword(DEFAULT_PASSWORD_SIZE);
    }

    public static String generateRandomPassword(int numChars) {

        String universe = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$";
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < numChars; i++) {
            int index = (int) ((Math.random() * 1000) % (universe.length()));
            result.append(universe.charAt(index));
        }
        return result.toString();
    }

        
}
