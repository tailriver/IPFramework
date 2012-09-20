package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.ElementId;
import net.tailriver.java.science.Point;

public class XYMapSet {
	final Point p;
	final ElementId eid;

	public XYMapSet(Point p, ElementId eid) {
		this.p = p;
		this.eid = eid;
	}

	public final Point p() {
		return p;
	}

	public final ElementId element() {
		return eid;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + p + "," + eid + "}";
	}
}
