package org.ggp.base.test.tests.util.statemachine.implementation.propnet;

import java.util.Collections;
import java.util.List;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.implementation.propnet.PropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



public class PropNetStateMachineTests
{
	protected List<Gdl> gameRules;	
	protected ProverStateMachine expectedStateMachine;
	protected PropNetStateMachine testStateMachine;

	
	@Before
	public void setup() {
		List<Gdl> gameRules = new TestGameRepository().getGame("ticTacToe").getRules();		
		
		expectedStateMachine = new ProverStateMachine();
		expectedStateMachine.initialize(gameRules);
		
		testStateMachine = new PropNetStateMachine();
		testStateMachine.initialize(gameRules);
	}
	
	
	@Test
	public void PropNetStateMachine_getInitialState() throws Exception {
		
		MachineState expectedState = expectedStateMachine.getInitialState();
		
		MachineState testState = testStateMachine.getInitialState();
		
		Assert.assertNotNull(expectedState);
		Assert.assertNotNull(testState);
		
		Assert.assertEquals(expectedState, testState);
		
		MachineState testState2 = testStateMachine.getInitialState();
		Assert.assertNotNull(testState2);
		
		Assert.assertEquals(expectedState, testState2);			
	}
	
	
	@Test
	public void PropNetStateMachine_getRoles() throws Exception {
		
		List<Role> expectedRoles = expectedStateMachine.getRoles();
		List<Role> testRoles = testStateMachine.getRoles();
		
		Assert.assertNotNull(expectedRoles);
		Assert.assertNotNull(testRoles);		
		
		Assert.assertEquals(expectedRoles, testRoles);				
	}

	@Test
	public void PropNetStateMachine_getLegalMoves() throws Exception {
		
		MachineState expectedState = expectedStateMachine.getInitialState();
		MachineState testState = testStateMachine.getInitialState();
		
		Assert.assertNotNull(expectedState);
		Assert.assertNotNull(testState);
		
		List<Role> expectedRoles = expectedStateMachine.getRoles();
		List<Role> testRoles = testStateMachine.getRoles();
		
		Assert.assertNotNull(expectedRoles);
		Assert.assertNotNull(testRoles);
		
		List<Move> expectedMoves = expectedStateMachine.getLegalMoves(expectedState, expectedRoles.get(0));
	    List<Move> testMoves = testStateMachine.getLegalMoves(testState, testRoles.get(0));
		
	    Collections.sort(expectedMoves);
	    Collections.sort(testMoves);
	    
	    
		Assert.assertNotNull(expectedMoves);
		Assert.assertNotNull(testMoves);
		
		Assert.assertEquals(expectedMoves, testMoves);
		
		
		expectedMoves = expectedStateMachine.getLegalMoves(expectedState, expectedRoles.get(1));
	    testMoves = testStateMachine.getLegalMoves(testState, testRoles.get(1));
		
	    Collections.sort(expectedMoves);
	    Collections.sort(testMoves);
	    
	    
		Assert.assertNotNull(expectedMoves);
		Assert.assertNotNull(testMoves);
		
		Assert.assertEquals(expectedMoves, testMoves);
		
	}	
	
}