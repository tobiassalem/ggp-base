package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * Abstract superclass holding all common logic and data for the ThorulfGamers.
 *
 * @author Tobias
 *
 */
public abstract class ThorulfGamer extends StateMachineGamer {

	protected static final int	MIN_SCORE	= 0;
	protected static final int	MAX_SCORE	= 100;
	protected static final boolean DEBUG_MODE = true;

	protected boolean isMultiplayer = false;
	protected boolean isCoop 		= false;
	protected Role	myRole			= null;
	protected Role	opponentRole	= null;
	protected Integer myRoleIndex 	= null;
	protected long terminalStateCounter = 0;

	/* ================================== [METAGAME METHODS] ============================================= */

	@Override
	/**
	 * Called during the time allotted for the start clock.
	 * This is when your player begins analyzing the game and figuring out a strategy.
	 */
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

		// Is the game multiplayer or not
		List<Role> roles = getStateMachine().getRoles();
		Map<Role, Integer> roleIndices = getStateMachine().getRoleIndices();
		myRole = getRole();
		myRoleIndex = roleIndices.get(myRole);

		if (roles.size() > 1) {
			isMultiplayer = true;
			if (myRoleIndex == 0) {
				opponentRole = roles.get(1);
			} else {
				opponentRole = roles.get(0);
			}
		}

		System.out.println(getName() + " started meta game, isMultiplayer: " +isMultiplayer + ", myRole: " +myRole);

		// TODO: Is the game co-op or not - if the score for each role in a terminal state is the same, then consider it to be a co-op game.
		//		MachineState initialState = stateMachine.getInitialState();
		//		List<Integer> goals = stateMachine.getGoals(initialState);
		//		int goal = stateMachine.getGoal(initialState, getRole());
		//		stateMachine.isTerminal(initialState);
	}

	/* =========================== [SINGLEPLAYER METHODS] ========================================= */

	protected Move selectMoveDeliberation(long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		// We get the current start time
		long start = System.currentTimeMillis();
		MachineState state = getCurrentState();

		List<Move> myLegalMoves = getStateMachine().getLegalMoves(state, getRole());
		Move action = myLegalMoves.get(0);

		int score = 0;
		for (Move move : myLegalMoves) {
			System.out.println("Considering move:  " +move+ " from state " +state + "(of "+myLegalMoves.size()+ " legal moves)");
			MachineState newState = getNextState(state,move); //getStateMachine().getNextState(state, moveList);
			int result = getMaxScoreDeliberation(newState);
			if (result == MAX_SCORE) {
				return move;
			} else if (result > score) {
				score = result;
				action = move;
			}
		}

		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(myLegalMoves, action, stop - start));
		return action;
	}

	private int getMaxScoreDeliberation(MachineState state) {
		StateMachine stateMachine = getStateMachine();
		int score = 0;

		if (stateMachine.isTerminal(state)) {
			try {
				int goalScore = stateMachine.getGoal(state, getRole());
				//System.out.println(getName() + ": State " +state+" is terminal. Got goalScore: " +goalScore);
				return goalScore;
			} catch (GoalDefinitionException e) {
				e.printStackTrace();
				return 0;
			}
		}
		try {
			List<Move> myLegalMoves = stateMachine.getLegalMoves(state, getRole());
			for (Move move : myLegalMoves) {
				MachineState newState = getNextState(state,move); //stateMachine.getNextState(state, moves);
				int result = getMaxScoreDeliberation(newState);
				if (result == MAX_SCORE) {
					return MAX_SCORE;
				} else if (result > score) {
					score = result;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}

	/* =============================== [INTERFACE METHODS] ========================================================= */

	@Override
	public void stateMachineStop() {
		// Called when the match is complete
		cleanup();
	}

	@Override
	public void stateMachineAbort() {
		// NOTE Called when the match is abruptly ended mid-way through.
		cleanup();
	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		//		Sometimes called before the match begins. Just leave this empty for now.
		//		This part of the GGP protocol is still being developed, and isn’t yet used. It’s designed to let players “preview” a game for,
		//		say, 10-15 minutes, and then rapidly play a series of short matches on that game, using insights gained during the preview phase
		//		to inform all of those matches. (This used to be called “analyze”)
	}

	private void cleanup() {
		isMultiplayer = false;
		myRole = null;
		opponentRole = null;
		isCoop = false;
		//start = 0;
		//stop = 0;
	}

	/* =========================== [UTILITY METHODS] ========================================================== */

	protected boolean timeoutReached(long timeout) {
		final int timeMargin = 1000;
		long now = System.currentTimeMillis();
		return (now > (timeout-timeMargin));
	}

	protected List<Move> createMoveList(Move... moves) {
		List<Move> moveList = new ArrayList<Move>();
		for (Move move : moves) {
			moveList.add(move);
		}
		return moveList;
	}

	protected MachineState getNextState(MachineState state, Move...moves) throws TransitionDefinitionException {
		List<Move> moveList = createMoveList(moves);
		return getStateMachine().getNextState(state, moveList);
	}

	protected int getRandomInt(final int maxValue) {
		return (int) Math.round(Math.random()*maxValue);
	}

	protected int getStateUtility(final MachineState state) {

		try {
			return getStateMachine().getGoal(state, getRole());
		} catch (GoalDefinitionException e) {
			System.err.println("getStateUtility caused Exception: " +e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	protected void debug(final String message) {
		if (DEBUG_MODE) {
			System.out.println(message);
		}
	}

	protected void debugJointMoves(List<List<Move>> legalJointMoves) {
		debug("legalJointMoves has " +legalJointMoves.size()+ " nr of lists");
		int i = 1;
		for (List<Move> list : legalJointMoves) {
			debug("list"+i+": "+list+ " size: " +list.size());
			i++;
		}
		// Example output on SINGLEPLAYER game Hunter
		//		legalJointMoves has 4 nr of lists
		//		list1: [( move 3 3 1 2 )] size: 1
		//		list2: [( move 3 3 4 1 )] size: 1
		//		list3: [( move 3 3 5 2 )] size: 1
		//		list4: [( move 3 3 2 1 )] size: 1

		// Example output on MULTIPLAYER game Alquerque
		//		legalJointMoves has 9 nr of lists
		//		list1: [( jump 5 1 4 1 3 1 ), noop] size: 2
		//		list2: [( move 4 2 3 3 ), noop] size: 2
		//		list3: [( move 4 3 3 3 ), noop] size: 2
		//		list4: [( move 4 4 3 3 ), noop] size: 2
		//		list5: [( move 4 4 3 5 ), noop] size: 2
		//		list6: [( move 4 2 3 2 ), noop] size: 2
		//		list7: [( move 4 4 3 4 ), noop] size: 2
		//		list8: [( move 4 5 3 5 ), noop] size: 2
		//		list9: [( move 4 2 3 1 ), noop] size: 2
	}

}
