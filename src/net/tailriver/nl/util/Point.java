package net.tailriver.nl.util;


public class Point {
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

	private final Coordinate c;
	private final double[] x;
	private Double[] box;

	public Point(Coordinate c, double... x) {
		this(c, x, null);
	}

	public Point(Point p) {
		this(p.c, p.x, p.box);
	}

	private Point(Coordinate c, double[] x, Double[] box) {
		int dimension = c.getDimension();
		if (dimension != x.length)
			throw new IllegalArgumentException("length of x must be same as the number of dimension");

		this.c = c;
		this.x = x;
		this.box = box;
	}

	public Double[] x() {
		if (box == null) {
			box = new Double[c.getDimension()];
			for (int i = 0; i < c.getDimension(); i++)
				box[i] = x[i];
		}
		return box;
	}

	public double x(int i) {
		return x[i];
	}

	public Coordinate coordinate() {
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point p = (Point) o;
			return this.c.equals(p.c) && this.x.equals(p.x);
		}
		return false;
	}

	public boolean equals(Coordinate c) {
		return this.c == c;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(c.name()).append(": ").append(Util.<Double>join(",", x())).append("]");
		return sb.toString();
	}
}
