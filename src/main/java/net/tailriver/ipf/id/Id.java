package net.tailriver.ipf.id;


public class Id {
	protected final Integer id;

	protected Id(Integer id) {
		this.id = id;
	}

	protected <T extends Id> Id(T id) {
		this.id = id != null ? id.id : null;
	}

	public Integer id() {
		return id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(this.getClass()))
			return ((Id) obj).id.equals(this.id);
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
