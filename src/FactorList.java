import java.util.ArrayList;


@SuppressWarnings("serial")
public class FactorList<T extends FactorSet> extends ArrayList<T> {
	private Id<FactorTable> fid;

	public FactorList(Id<FactorTable> fid) {
		this.fid = fid;
	}

	public FactorList(int fid) {
		this(new Id<FactorTable>(fid));
	}

	public Id<FactorTable> id() {
		return fid;
	}

	@Override
	public boolean add(T e) {
		if (e.id() != fid.id())
			throw new IllegalArgumentException("try to add a different FactorID set");
		return super.add(e);
	}
}
