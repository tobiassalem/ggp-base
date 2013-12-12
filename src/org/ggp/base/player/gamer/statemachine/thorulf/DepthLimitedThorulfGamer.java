package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.List;
import java.util.Map;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class DepthLimitedThorulfGamer extends ThorulfGamer {
	
	private static final int	DEPTH_LIMT		= 2;
	
	//long						start;
	//long						stop;
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName(); // + " -==[1337]==- ";
	}
	
	@Override
	public StateMachine getInitialStateMachine() {
		return new ProverStateMachine();
	}
	
	// This is the default detail Panel
	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
	
	
	@Override
	/**
	 * Called during the time allotted for the start clock.
	 * This is when your player begins analyzing the game and figuring out a strategy.
	 * @param timeout - the timestamp (in milliseconds) when a move must have been selected
	 */
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO find out useful information about the game
		
		// Is the game multiplayer or not
		List<Role> roles = getStateMachine().getRoles();
		Map<Role, Integer> roleIndices = getStateMachine().getRoleIndices();
		if (roles.size() > 1) {
			isMultiplayer = true;
			myRole = getRole();
			Integer myRoleIndex = roleIndices.get(myRole);
			if (myRoleIndex == 0) {
				opponentRole = roles.get(1);
			} else {
				opponentRole = roles.get(0);
			}
		}
		
		long finishBy = timeout - System.currentTimeMillis();
		System.out.println(getName() + " started meta game, isMultiplayer: " + isMultiplayer + ", myRole: " + getRole() + ", opponentRole: " + opponentRole
			+ ", depthLimit: " + DEPTH_LIMT+ ", timeout: " +timeout+ ", finishBy (timeout-now): " +finishBy);
		
		// Is the game co-op or not (if the score for each role in a terminal state is the same, then consider it to be a co-op game)
		//		MachineState initialState = stateMachine.getInitialState();
		//		List<Integer> goals = stateMachine.getGoals(initialState);
		//		int goal = stateMachine.getGoal(initialState, getRole());
		//		stateMachine.isTerminal(initialState);
	}
	
	/* =============================== [SEARCH / MOVE METHODS] ========================================================= */
	
	@Override
	/**
	 * Called when your player needs to select a move. This method returns the selected move.
	 * @param timeout - the timestamp (in milliseconds) when a move must have been selected
	 * NOTE check with:
	 * int finishBy = timeout - 1000;
	 * if(System.currentTimeMillis() > finishBy) break;
	 */
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		
		//		if (isMultiplayer) {
		//			System.out.println("isMultiplayer,selecting depth limited search");
		//			return selectMoveDepthLimitedSearch(timeout);
		//		} else {
		//			System.out.println("isSinglePlayer, selecting deliberate search");
		//			return selectMoveDeliberation(timeout);
		//		}
		
		return selectMoveDepthLimitedSearch(timeout);
	}
	
	private Move selectMoveDepthLimitedSearch(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		MachineState state = getCurrentState();
		
		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		//getStateMachine().getLegalJointMoves(state, getRole(), move);
		Move selectedMove = legalMoves.get(0);
		int score = 0;
		final int initialLevel = 0;
		
		for (Move move : legalMoves) {
			//List<Move> moveList = new ArrayList<Move>();
			//moveList.add(move);
			//MachineState newState = getStateMachine().getNextState(state, moveList);
			int result = getMinScore(move, state, initialLevel, timeout);
			//System.out.println("Considering move " +move+ ", got result " +result);
			if (result > score) {
				score = result;
				selectedMove = move;
				//System.out.println("Selected move: " +selectedMove);
			}
		}
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(legalMoves, selectedMove, stop - start));
		return selectedMove;
	}
	
	private int getMinScore(Move myMove, MachineState state, int level, long timeout) {
		StateMachine stateMachine = getStateMachine();
		int score = MAX_SCORE;
		int nextLevel = level + 1;
		try {
			
			// Alt.1: using opponentRole specifically - more clear for 2 player games (as in the exercises)
			// If there is no opponent, move to the next max node
			// TODO verify logic, does not win singleplayer game atm. - UPDATE neither does alt.2
			if (opponentRole == null) {
				MachineState nextState = getNextState(state, myMove);
				return getMaxScore(nextState,nextLevel,timeout);
			}
			
			List<Move> opponentLegalMoves = stateMachine.getLegalMoves(state, opponentRole);
			for (Move opponentMove : opponentLegalMoves) {
				MachineState nextState = getNextState(state,myMove,opponentMove);
				int result = getMaxScore(nextState,nextLevel,timeout);
				if (result < score) {
					score = result;
				}
			}
			
			// Alt.2; using getLegalJointMoves - more general (works for singleplayer AND multiplayer games)
			//			List<List<Move>> jointMovesFromMinNode = stateMachine.getLegalJointMoves(state, myRole, myMove);
			//			for (List<Move> jointMove : jointMovesFromMinNode) {
			//				MachineState nextState = stateMachine.getNextState(state, jointMove);
			//				// Create MAX node <nextState>
			//				int result = getMaxScore(nextState, nextLevel, timeout);
			//				if (result < score) {
			//					score = result;
			//				}
			//			}
			//
			
		} catch (Exception e) {
			System.err.println("getMinScore caused exception: " + e.getMessage());
			e.printStackTrace();
		}
		return score;
	}
	
	private int getMaxScore(MachineState state, int level, long timeout) {
		StateMachine stateMachine = getStateMachine();
		
		if (stateMachine.isTerminal(state)) {
			try {
				int goalScore = stateMachine.getGoal(state, getRole());
				//System.out.println(getName() + " Got goalScore: " +goalScore);
				return goalScore;
			} catch (GoalDefinitionException e) {
				System.err.println("getMaxScore caused GoalDefinitionException: " + e.getMessage());
				e.printStackTrace();
				return 0;
			}
		}
		
		int score = MIN_SCORE;
		try {
			if (level > DEPTH_LIMT || timeoutReached(timeout)) {
				System.out.println("timeout or depth limit reached, level: " +level);
				return evaluateState(state);
			}
			
			List<Move> legalMoves = stateMachine.getLegalMoves(state, getRole());
			for (Move move : legalMoves) {
				// NOTE: don't calculate newState in maxScore - done in minScore
				//MachineState newState = stateMachine.getNextState(state, moves);
				int result = getMinScore(move, state, level, timeout);
				if (result > score) {
					score = result;
				}
			}
		} catch (Exception e) {
			System.err.println("getMaxScore caused exception: " + e.getMessage());
			e.printStackTrace();
		}
		return score;
	}
	
	private int evaluateState(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		//StateMachine stateMachine = getStateMachine();
		//return stateMachine.getGoal(state, getRole());
		//return getMobility(state);
		return getGoalProximity(state);
	}
	
	private int getMobility(MachineState state) throws MoveDefinitionException, TransitionDefinitionException {
		// NOTE mobility: nrOfLegalMoves / nrOfFeasibleMoves * 100
		StateMachine stateMachine = getStateMachine();
		int nrOfLegalMoves = stateMachine.getLegalMoves(state, getRole()).size();
		int nrOfFeasibleMoves = stateMachine.getNextStates(state).size();
		
		return Math.round(nrOfLegalMoves / nrOfFeasibleMoves * 100);
	}
	
	@SuppressWarnings("unused")
	private int getFocus(MachineState state) throws MoveDefinitionException, TransitionDefinitionException {
		// NOTE focus: (100 - nrOfLegalMoves / nrOfFeasibleMoves * 100) = the inverse of mobility
		return Math.round(100 - getMobility(state));
	}
	
	private int getGoalProximity(MachineState state) throws GoalDefinitionException {
		// TODO: find winning goal states, compare the given state to that state
		// To measure goal proximity, you can try using the goal value of the current state, or (harder) try to find "winning" terminal states
		// and use similarity to these states as a measure of goal proximity.
		int stateGoal = getStateMachine().getGoal(state, getRole());
		// Goal Proximity 100 is perfect
		int winningGoalDistance = Math.abs(MAX_SCORE - stateGoal);
		int goalProximity = MAX_SCORE - winningGoalDistance;
		System.out.println("Returning goal proximity: " + goalProximity);
		return goalProximity;
	}
	
	
	/* =============================== [INTERFACE METHODS] ========================================================= */
	
	// NOTE: moved up to superclass
	//	@Override
	//	public void stateMachineStop() {
	//		// Called when the match is complete
	//		cleanup();
	//	}
	//
	//	@Override
	//	public void stateMachineAbort() {
	//		// NOTE Called when the match is abruptly ended mid-way through.
	//		cleanup();
	//	}
	//
	//	@Override
	//	public void preview(Game g, long timeout) throws GamePreviewException {
	//		//		Sometimes called before the match begins. Just leave this empty for now.
	//		//		This part of the GGP protocol is still being developed, and isn’t yet used. It’s designed to let players “preview” a game for,
	//		//		say, 10-15 minutes, and then rapidly play a series of short matches on that game, using insights gained during the preview phase
	//		//		to inform all of those matches. (This used to be called “analyze”)
	//	}
	//
	//	private void cleanup() {
	//		isMultiplayer = false;
	//		myRole = null;
	//		opponentRole = null;
	//		isCoop = false;
	//		start = 0;
	//		stop = 0;
	//	}
	
}
