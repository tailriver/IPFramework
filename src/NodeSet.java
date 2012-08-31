public class NodeSet extends Id<NodeTable> {
	private final Point p;

	public NodeSet(Id<NodeTable> num, Point p) {
		super(num);
		this.p = p;
	}

	public Point p() {
		return p;
	}

	public double p(int i) {
		return p.x(i);
	}
}
