package net.tailriver.nl.id;

public class NodeId extends Id {
	public NodeId(int id) {
		super(id);
	}

	public NodeId(NodeId nid) {
		super(nid);
	}

	@Override
	public String toString() {
		return "N#" + id;
	}
}
