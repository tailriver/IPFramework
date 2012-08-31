package net.tailriver.nl.dataset;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Point;

public class NodeSet extends NodeId {
	private final Point p;

	public NodeSet(NodeId num, Point p) {
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
