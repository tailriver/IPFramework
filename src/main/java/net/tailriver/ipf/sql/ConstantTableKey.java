package net.tailriver.ipf.sql;


public enum ConstantTableKey {
	RADIUS(1),
	THICKNESS(1),
	MAX_CYCLE_DEGREE(180);

	final double defaultValue;

	ConstantTableKey(double defaultValue) {
		this.defaultValue = defaultValue;
	}
}
