package net.tailriver.ipf.id;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdTest {

	@Test
	public void testEqualsObject() {
		Id id127 = new Id(127);
		Id id128 = new Id(128);
		assertFalse(id127.equals(new Id(127)));
		assertFalse(id128.equals(new Id(128)));
		assertTrue(id127.equals(new Id(128)));
		assertTrue(id128.equals(new Id(129)));
	}
}
