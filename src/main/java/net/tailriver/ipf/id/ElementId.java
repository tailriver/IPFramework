package net.tailriver.ipf.id;

public class ElementId extends Id {
	public ElementId(int id) {
		super(id);
	}

	public ElementId(ElementId eid) {
		super(eid);
	}

	@Override
	public String toString() {
		return "E#" + id;
	}
}
