package uicom.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtil {

	private static final String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static final Pattern pattern = Pattern.compile(regex);

	public static boolean isValidURL(String url) {
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}

	public static boolean containURL(String content) {
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}

	public static void main(String[] args) {
		String content = "this is testing for url https://img.vedantu.org.in";
		System.out.println("content [ " + content + "]contains : "
				+ containURL(content));

	}
}
