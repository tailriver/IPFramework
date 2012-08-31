abstract class RCSet<T extends Identifiable> extends Id<T> {
	private final Id<NodeTable> n;
	private final double v;

	public RCSet(Id<T> id, Id<NodeTable> num, double value) {
		super(id);
		this.n = num;
		this.v = value;
	}

	public Id<NodeTable> node() {
		return n;
	}

	public double value() {
		return v;
	}
}
