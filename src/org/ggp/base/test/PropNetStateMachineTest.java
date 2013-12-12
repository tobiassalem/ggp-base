package org.ggp.base.test;

import java.util.ArrayList;
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

public class PropNetStateMachineTest {

	private ProverStateMachine	expectedStateMachine;
	private PropNetStateMachine	testStateMachine;

	@Before
	public void setup() {
		List<Gdl> gameRules = new TestGameRepository().getGame("ticTacToe").getRules();
		expectedStateMachine = new ProverStateMachine();
		expectedStateMachine.initialize(gameRules);

		testStateMachine = new PropNetStateMachine();
		testStateMachine.initialize(gameRules);
	}

	@Test
	public void testGetLegalMoves() throws Exception {

		MachineState expectedState = expectedStateMachine.getInitialState();
		MachineState testState = testStateMachine.getInitialState();

		Assert.assertNotNull(expectedState);
		Assert.assertNotNull(testState);

		List<Role> expectedRoles = expectedStateMachine.getRoles();
		List<Role> testRoles = testStateMachine.getRoles();

		Assert.assertNotNull(expectedRoles);
		Assert.assertNotNull(testRoles);
		Assert.assertEquals(expectedRoles, testRoles);

		List<Move> expectedMoves = expectedStateMachine.getLegalMoves(expectedState, expectedRoles.get(0));
		List<Move> testMoves = testStateMachine.getLegalMoves(testState, testRoles.get(0));

		// ISSUE: Move does not implement Comparable and cannot be sorted like this
		//Collections.sort(expectedMoves);
		//Collections.sort(testMoves);

		Assert.assertNotNull(expectedMoves);
		Assert.assertNotNull(testMoves);

		Assert.assertEquals(expectedMoves, testMoves);
	}

	@Test
	public void testIsTerminalShouldBeTrue() throws Exception {

		MachineState expectedState = expectedStateMachine.getInitialState();
		MachineState testState = testStateMachine.getInitialState();

		// TODO: implement
		List<Move> moves = new ArrayList<Move>();

		while (!expectedStateMachine.isTerminal(expectedState)) {
			expectedStateMachine.getNextState(expectedState, moves);
		}
	}

	@Test
	public void testIsTerminalShouldBeFalse() throws Exception {

		MachineState expectedState = expectedStateMachine.getInitialState();
		MachineState testState = testStateMachine.getInitialState();

		// TODO: implement
		List<Move> moves = new ArrayList<Move>();

		while (!expectedStateMachine.isTerminal(expectedState)) {
			expectedStateMachine.getNextState(expectedState, moves);
		}
	}
}
