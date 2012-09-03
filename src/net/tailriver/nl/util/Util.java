package net.tailriver.nl.util;

import java.util.List;

public class Util {
	public static <T> String join(CharSequence sep, T[] array) {
		StringBuilder sb = new StringBuilder();
		for (T s : array)
			sb.append(s).append(sep);
		return sb.substring(0, sb.length() - sep.length());
	}

	@SuppressWarnings("unchecked")
	public static <T> String join(CharSequence sep, List<T> list) {
		return Util.<T>join(sep, (T[]) list.toArray() );
	}

	public static String repeat(String s, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++)
			sb.append(s);
		return sb.toString();
	}

	public static String getCallerClass() {
		String fqclass = Thread.currentThread().getStackTrace()[3].getClassName();
		return fqclass.substring(fqclass.lastIndexOf(".") + 1);
	}
}
