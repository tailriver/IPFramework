class Util {
	public static <T> String join(CharSequence sep, T[] array) {
		StringBuilder sb = new StringBuilder();
		for (T s : array)
			sb.append(s).append(sep);
		return sb.substring(0, sb.length() - 1);
	}
}
