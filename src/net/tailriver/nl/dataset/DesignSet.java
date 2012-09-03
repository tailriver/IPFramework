package net.tailriver.nl.dataset;

import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Stress;

public class DesignSet extends DesignId {
	private final NodeId nid;
	private final Stress stress;

	public DesignSet(DesignId did, NodeId nid, Stress stress) {
		super(did);
		this.nid = nid;
		this.stress = stress;
	}

	public NodeId node() {
		return nid;
	}

	public Stress stress() {
		return stress;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + nid + "," + stress + "}";
	}
}
