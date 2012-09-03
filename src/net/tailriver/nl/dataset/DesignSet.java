package net.tailriver.nl.dataset;

import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;

public class DesignSet extends DesignId {
	public enum Component {XX, YY, ZZ, XY, YZ, ZX};

	private final NodeId n;
	private final Component c;
	private final double w;

	public DesignSet(DesignId did, NodeId nid, String component, double weight) {
		super(did);
		this.n = nid;
		this.c = Component.valueOf(component);
		this.w = weight;
	}

	public NodeId node() {
		return n;
	}

	public Component component() {
		return c;
	}

	public double weight() {
		return w;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" +
				super.toString() + "," + n + ",c=" + c + ",w=" + w + "}";
	}
}
