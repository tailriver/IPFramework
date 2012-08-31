package net.tailriver.nl.dataset;
import net.tailriver.nl.id.*;

public class DesignSet extends DesignId {
	public enum Component {XX, YY, ZZ, XY, YZ, ZX};

	private final NodeId n;
	private final Component d;
	private final double w;

	public DesignSet(DesignId did, NodeId node, String component, double weight) {
		super(did);
		this.n = node;
		this.d = Component.valueOf(component);
		this.w = weight;
	}

	public NodeId node() {
		return n;
	}

	public Component component() {
		return d;
	}

	public double weight() {
		return w;
	}
}
