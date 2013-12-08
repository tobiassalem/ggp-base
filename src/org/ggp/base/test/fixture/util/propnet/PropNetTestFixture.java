package org.ggp.base.test.fixture.util.propnet;

import java.util.List;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.PropNetFactory;

public class PropNetTestFixture {

	protected List<Gdl> gameRules;
	protected PropNet propNet;

	public PropNetTestFixture(String gameName) {
		this.gameRules = new TestGameRepository().getGame(gameName).getRules();
		this.propNet = PropNetFactory.create(gameRules);
	}
}