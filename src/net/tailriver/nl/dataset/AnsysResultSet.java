package net.tailriver.nl.dataset;

import java.util.EnumMap;

import net.tailriver.nl.dataset.DesignSet.Component;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;

public class AnsysResultSet extends FactorId {
	private final NodeId n;
	private final EnumMap<Component, Double> s;

	public AnsysResultSet(FactorId fid, NodeId nid, Double sxx, Double syy, Double sxy) {
		super(fid);
		this.n = nid;
		this.s = new EnumMap<Component, Double>(Component.class);
		s.put(Component.XX, sxx);
		s.put(Component.YY, syy);
		s.put(Component.XY, sxy);
	}

	public NodeId node() {
		return n;
	}

	public Double s(Component c) {
		if (!s.containsKey(c))
			throw new IllegalArgumentException("unsupported component");
		return s.get(c);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + n + "," + s + "}";
	}
}
