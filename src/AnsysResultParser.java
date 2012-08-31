import java.sql.*;
import java.util.*;
import java.util.regex.*;


public class AnsysResultParser extends Parser {
	static final Pattern fidFromFileNamePattern =
			Pattern.compile(".*[^\\d](\\d+)\\.txt$");
	static final Pattern skipStopPattern =
			Pattern.compile("\\s*NODE\\s+SX\\s+SY\\s+SZ\\s+.*");
	static final Pattern stressPattern =
			Pattern.compile("^\\s*(\\d+)" + Util.repeat("\\s+([-+.E\\d]+)", 6) + ".*");

	private Id<FactorTable> fid;
	private String filename;
	protected List<AnsysResultSet> arsList;
	protected boolean isSkipMode;

	AnsysResultParser() {
		arsList = new ArrayList<AnsysResultSet>();
		isSkipMode = true;
	}

	@Override
	protected void parseBeforeHook(String filename) throws Exception {
		this.filename = filename;
		arsList.clear();

		Matcher filenameMatcher = fidFromFileNamePattern.matcher(this.filename);
		if (filenameMatcher.matches())
			fid = new Id<FactorTable>(Integer.valueOf(filenameMatcher.group(1)));
		else
			throw new ParserException("fail to extract factor id from the file name");
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (isSkipMode) {
			Matcher skipStopMatcher = skipStopPattern.matcher(line);
			if (skipStopMatcher.matches())
				isSkipMode = false;
			return true;
		}

		final Matcher stressMatcher = stressPattern.matcher(line);
		if (stressMatcher.matches()) {
			Id<NodeTable> node = new Id<NodeTable>(Integer.valueOf(stressMatcher.group(1)));
			Double sxx = Double.valueOf(stressMatcher.group(2));
			Double syy = Double.valueOf(stressMatcher.group(3));
			Double sxy = Double.valueOf(stressMatcher.group(5));

			AnsysResultSet ars = new AnsysResultSet(fid, node, sxx, syy, sxy);
			arsList.add(ars);
		}
		else {
			isSkipMode = true;
		}
		return true;
	}

	@Override
	public void save(Connection conn) throws SQLException {
		FactorResultTable frt = new FactorResultTable(conn);
		frt.insert(arsList);
		conn.commit();
	}
}
