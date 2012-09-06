package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.DesignId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Stress;

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
