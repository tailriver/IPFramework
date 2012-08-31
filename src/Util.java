import java.util.*;
import java.util.regex.Pattern;

class Util {
	public static final Pattern CONSTANT_PATTERN = Pattern.compile("^##\\s*(\\w+):\\s*([\\d.]+).*");
	public static final Pattern COMMENT_PATTERN = Pattern.compile("^#.*");

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
}
