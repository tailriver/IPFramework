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

	public Integer[] nodes_id() {
		Integer[] nodes_id = new Integer[nodes.length];
		for (int i = 0; i < nodes.length; i++)
			nodes_id[i] = nodes[i].id();
		return nodes_id;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + nodes + "}";
	}
}
