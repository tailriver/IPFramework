class Util {
	/** Cylindrical 2D */
	public enum C2D {r, t}

	/** Cylindrical 3D */
	public enum C3D {r, t, z}

	/** Cartesian  */
	public enum XYZ {x, y, z}

	/** Tensor 2D */
	public enum T2D {xx, yy, zz, xy, yz, zx, yx}

	public static <T> String join(CharSequence sep, T[] array) {
		StringBuilder sb = new StringBuilder();
		for (T s : array)
			sb.append(s).append(sep);
		return sb.substring(0, sb.length() - 1);
	}
}
