package net.tailriver.nl.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.nl.dataset.AnsysResultSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.sql.FactorResultTable;
import net.tailriver.nl.util.Stress;
import net.tailriver.nl.util.Tensor2;
import net.tailriver.nl.util.Util;


public class AnsysResultParser extends Parser {
	static final Pattern fidFromFileNamePattern =
			Pattern.compile(".*[^\\d](\\d+)\\.txt$");
	static final Pattern skipStopPattern =
			Pattern.compile("\\s*NODE\\s+SX\\s+SY\\s+SZ");
	static final Pattern stressPattern =
			Pattern.compile("\\s*(\\d+)" + Util.repeat("\\s+([-+.E\\d]+)", 6));

	private FactorId fid;
	private boolean isSkipMode;
	private Collection<AnsysResultSet> parsed;

	public AnsysResultParser() {
		isSkipMode = true;
		parsed = new ArrayList<AnsysResultSet>();
	}

	@Override
	protected void parseBeforeHook(String filename) throws Exception {
		Matcher filenameMatcher = fidFromFileNamePattern.matcher(filename);
		if (filenameMatcher.matches())
			fid = new FactorId(Integer.valueOf(filenameMatcher.group(1)));
		else
			throw new ParserException("fail to extract factor id from the file name");

		parsed.clear();
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (isSkipMode) {
			Matcher skipStopMatcher = skipStopPattern.matcher(line);
			if (skipStopMatcher.lookingAt())
				isSkipMode = false;
			return true;
		}

		final Matcher stressMatcher = stressPattern.matcher(line);
		if (stressMatcher.lookingAt()) {
			NodeId nid = new NodeId(Integer.valueOf(stressMatcher.group(1)));
			Stress s = new Stress();
			s.put(Tensor2.XX, Double.valueOf(stressMatcher.group(2)));
			s.put(Tensor2.YY, Double.valueOf(stressMatcher.group(3)));
			s.put(Tensor2.XY, Double.valueOf(stressMatcher.group(5)));

			AnsysResultSet ars = new AnsysResultSet(fid, nid, s);
			parsed.add(ars);
		}
		else {
			isSkipMode = true;
		}
		return true;
	}

	@Override
	public void save(Connection conn) throws SQLException {
		FactorResultTable frt = new FactorResultTable(conn);
		frt.insert(parsed);
		conn.commit();
	}
}
