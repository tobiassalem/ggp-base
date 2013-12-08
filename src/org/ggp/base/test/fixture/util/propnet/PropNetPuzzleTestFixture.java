package org.ggp.base.test.fixture.util.propnet;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.Role;

public class PropNetPuzzleTestFixture extends PropNetTestFixture {

	protected Role role;

	public PropNetPuzzleTestFixture(String gameName, String roleName) {
		super(gameName);
		this.role = new Role(GdlPool.getConstant(roleName));
	}
}