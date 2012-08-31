public enum Coordinate {
	Cartesian2(2), Polar(2), Cartesian3(3), Cylindrical(3);

	private int d;
	private Coordinate(int d) {
		this.d = d;
	}

	public int getDimension() {
		return d;
	}
}
