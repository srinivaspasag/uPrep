package com.vedantu.comm.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

	public static long toMilliseconds(Date date) {
		return date.getTime();
	}

	public static long toMilliseconds(String dateIn, String format) {

		try {
			DateFormat formatter = new SimpleDateFormat(format);
			Date date = formatter.parse(dateIn);
			return date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
