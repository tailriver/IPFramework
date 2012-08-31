public class FactorSet extends RCSet<FactorTable> {
	public enum Direction {X, Y, Z};

	private final Direction d;

	public FactorSet(Id<FactorTable> id, Id<NodeTable> num, String direction, double value) {
		super(id, num, value);
		this.d = Direction.valueOf(direction);
	}

	public Direction direction() {
		return d;
	}
}
