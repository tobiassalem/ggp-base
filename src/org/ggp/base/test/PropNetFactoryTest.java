package org.ggp.base.test;

import java.util.List;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.propnet.factory.PropNetFactory;
import org.junit.Assert;
import org.junit.Test;

public class PropNetFactoryTest {

	@Test
	public void PropNetFactory_create() throws Exception {

		List<Gdl> gameRules = new TestGameRepository().getGame("ticTacToe").getRules();
		PropNet result = PropNetFactory.create(gameRules);

		Assert.assertNotNull(result);
	}

	@Test
	public void OptimizingPropNetFactory_create() throws Exception {

		List<Gdl> gameRules = new TestGameRepository().getGame("ticTacToe").getRules();
		PropNet result = OptimizingPropNetFactory.create(gameRules);

		Assert.assertNotNull(result);
	}

}