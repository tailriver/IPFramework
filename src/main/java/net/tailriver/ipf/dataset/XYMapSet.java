package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.ElementId;
import net.tailriver.java.science.Point;

public class XYMapSet extends Point {
	private ElementId eid;

	public XYMapSet(Point p) {
		this(p, null);
	}

	public XYMapSet(Point p, ElementId eid) {
		super(p);
		setElementId(eid);
	}

	public void setElementId(ElementId eid) {
		this.eid = eid;
	}

	public ElementId element() {
		return eid;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + eid + "}";
	}
}
