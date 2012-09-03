package net.tailriver.nl.dataset;

import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Stress;
import net.tailriver.nl.util.Tensor2;

public class AnsysResultSet extends FactorId {
	private final NodeId nid;
	private final Stress stress;

	public AnsysResultSet(FactorId fid, NodeId nid, Stress stress) {
		super(fid);
		this.nid = nid;
		this.stress = stress;
	}

	public NodeId node() {
		return nid;
	}

	public Stress stress() {
		return stress;
	}

	public Double stress(Tensor2 component) {
		return stress.get(component);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + nid + "," + stress + "}";
	}
}
