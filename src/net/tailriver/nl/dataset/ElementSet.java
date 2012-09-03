package net.tailriver.nl.dataset;

import net.tailriver.nl.id.ElementId;
import net.tailriver.nl.id.NodeId;

public class ElementSet extends ElementId {
	private final NodeId[] nodes;

	public ElementSet(ElementId eid, NodeId[] nodes) {
		super(eid);
		this.nodes = nodes;
	}

	public NodeId[] nodes() {
		return nodes;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + nodes + "}";
	}
}
