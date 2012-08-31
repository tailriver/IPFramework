public class FactorSet extends Id<FactorTable> {
	public enum Direction {X, Y, Z};

	private final Id<NodeTable> n;
	private final Direction d;
	private final double v;

	public FactorSet(Id<FactorTable> id, Id<NodeTable> num, String direction, double value) {
		super(id);
		this.n = num;
		this.d = Direction.valueOf(direction);
		this.v = value;
	}

	public Id<NodeTable> node() {
		return n;
	}

	public Direction direction() {
		return d;
	}

	public double value() {
		return v;
	}
}
