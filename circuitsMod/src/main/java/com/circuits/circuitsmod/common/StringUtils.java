package com.circuits.circuitsmod.common;

public class StringUtils {
	public static String intToSecret(int val) {
		String result = "";
		String[] chars = Integer.toString(val).split("");
		for (String c : chars) {
			result += SECTION_SYMBOL + c;
		}
		return result;
	}
	public static int secretToInt(String str) {
		String toParse = "";
		String[] chars = str.split("");
		for (int i = 1; i < chars.length; i += 2) {
			toParse += chars[i];
		}
		return Integer.parseInt(toParse);
	}
	public static final String SECTION_SYMBOL = Character.toString((char)0x00a7);
	public static final String NULL_SYMBOL = Character.toString((char)0xF8);
	
	public static String sanitizeAlphaNumeric(String str) {
		return str.chars().filter((in) -> Character.isLetterOrDigit(in))
				          .mapToObj((i) -> ((Character) ((char) i)).toString())
				          .reduce((s1, s2) -> s1 + s2).orElse("");
	}
}
