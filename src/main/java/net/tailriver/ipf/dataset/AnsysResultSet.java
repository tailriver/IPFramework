package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.OrthogonalTensor2;
import net.tailriver.ipf.science.Stress;

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

	public Double stress(OrthogonalTensor2 component) {
		return stress.get(component);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + nid + "," + stress + "}";
	}
}
