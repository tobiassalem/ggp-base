package org.ggp.base.test.tests.util.propnet.factory;

import java.util.List;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.PropNetFactory;
import org.junit.Assert;
import org.junit.Test;



public class PropNetFactoryTests
{
	
	@Test
	public void PropNetFactory_create() throws Exception {
		
		List<Gdl> gameRules = new TestGameRepository().getGame("ticTacToe").getRules();		
		
		PropNet result = PropNetFactory.create(gameRules);
		
		Assert.assertNotNull(result);
		
	}
}