package net.tailriver.nl.dataset;

import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Point;

public class NodeSet extends NodeId {
	private final Point p;

	public NodeSet(NodeId nid, Point p) {
		super(nid);
		this.p = p;
	}

	public Point p() {
		return p;
	}

	public double p(int i) {
		return p.x(i);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + p + "}";
	}
}
