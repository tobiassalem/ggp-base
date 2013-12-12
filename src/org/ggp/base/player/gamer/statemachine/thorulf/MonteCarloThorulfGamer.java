package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * Implementation of MCS - Monto Carlo Search Gamer
 * 
 * @author Tobias
 *
 */
public class MonteCarloThorulfGamer extends ThorulfGamer {
	
	private static final int	DEPTH_LIMIT	= 2;
	
	/*
	NOTE on probeCount/depthChargeAttempts
	Do NOT update while the loop is being done. But, as the game progresses, your program might want to increase the "count"
	since normally the time to reach a terminal state decreases as the game progresses.
	Or, your program might set the "count" to some initial value, and increase/decrease the "count" if the program sees
	that it has more time available or less time available after it has completed "count" number of iterations.
	
	Other variations are possible also. You might have some heuristic for a given state that says the state is worthwhile
	for consideration and increase the "count" for that state, but decrease the "count" for a state that the heuristic says
	is not worthwhile for consideration.
	 */
	private static final int	DEPTH_CHARGE_ATTEMPTS = 32;
	
	private final int[]	depth	= new int[1];
	
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
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
	
	
	/* =============================== [SEARCH / MOVE METHODS] ========================================================= */
	
	@Override
	/**
	 * Called when your player needs to select a move. This method returns the selected move.
	 */
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		
		return selectMoveMonteCarloSearch(timeout);
	}
	
	private Move selectMoveMonteCarloSearch(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		MachineState state = getCurrentState();
		terminalStateCounter = 0;
		
		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		//getStateMachine().getLegalJointMoves(state, getRole(), move);
		Move selectedMove = legalMoves.get(0);
		final int initialLevel = 0;
		int score = 0;
		int loopCount = 0;
		
		for (Move move : legalMoves) {
			loopCount++;
			if (timeoutReached(timeout)) {
				System.err.println("TIMEOUT REACHED during selectMoveMonteCarloSearch! loopCount: " +loopCount+ ", selectedMove= " +selectedMove);
				break;
			}
			int result = getMinScore(move, state, initialLevel, timeout);
			if (result > score) {
				score = result;
				selectedMove = move;
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
			
			// TODO: verify logic of alt.2 - does not win singlePlayer game atm.
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
	
	private int getMaxScore(MachineState state, int level, long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		int score = 0;
		StateMachine stateMachine = getStateMachine();
		
		if (stateMachine.isTerminal(state)) {
			try {
				terminalStateCounter++;
				System.out.println("terminalStateCounter: " +terminalStateCounter);
				//debug("terminalStateCounter: " +terminalStateCounter);
				//				if (terminalStateCounter % 100 == 0) {
				//					System.out.println("terminalStateCounter: " +terminalStateCounter);
				//				}
				return stateMachine.getGoal(state, getRole());
			} catch (GoalDefinitionException e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		if (timeoutReached(timeout)) {
			int stateUtility = getStateUtility(state);
			System.err.println("TIMEOUT REACHED during getMaxScore! level: " +level + ", returning stateUtility: " +stateUtility);
			return stateUtility;
		}
		
		if (level > DEPTH_LIMIT) {
			return getMonteCarloScore(state);
		}
		
		try {
			
			List<Move> legalMoves = stateMachine.getLegalMoves(state, getRole());
			for (Move move : legalMoves) {
				//List<Move> moves = new ArrayList<Move>();
				//moves.add(move);
				// NOTE: don't calculate newState in maxScore - done in minScore
				// MachineState newState = stateMachine.getNextState(state, moves);
				int result = getMinScore(move,state,level,timeout);
				if (result == MAX_SCORE) {
					return MAX_SCORE;
				} else if (result > score) {
					score = result;
				}
			}
		} catch (Exception e) {
			System.err.println("getMaxScore caused Exception: " +e.getMessage());
			e.printStackTrace();
		}
		return score;
	}
	
	/**
	 * Returns the average utility from a set of n probes, where n is a global parameter count
	 * @param state
	 * @return
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 */
	private int getMonteCarloScore(MachineState state) throws MoveDefinitionException, TransitionDefinitionException {
		// Javascript implemententation
		//		var total = 0;
		//		for (var i=0; i<count; i++) {
		//			total = total + depthcharge(state);
		//		}
		//		return total/count;
		//System.out.println("Performing " +DEPTH_CHARGE_ATTEMPTS+ " nr of depth charges");
		int total = 0;
		for (int i = 0; i < DEPTH_CHARGE_ATTEMPTS; i++) {
			total += performDepthChargeFromState(state);
		}
		int averageUtility = total / DEPTH_CHARGE_ATTEMPTS;
		//System.out.println("Depth Charges DONE - returning: " + averageUtility);
		return averageUtility;
	}
	
	private int performDepthChargeFromState(MachineState state) throws MoveDefinitionException, TransitionDefinitionException {
		StateMachine stateMachine = getStateMachine();
		if (stateMachine.isTerminal(state)) {
			try {
				return stateMachine.getGoal(state, getRole());
			} catch (GoalDefinitionException e) {
				System.err.println("performDepthChargeFromState caused Exception: " +e.getMessage());
				e.printStackTrace();
				return 0;
			}
		}
		
		// Javascript implementation
		//		var move = seq();
		//		for (var i=0; i<roles.length; i++) {
		//			var options = findlegals(roles[i],state,library); // joint moves
		//			var best = randomindex(options.length);
		//			move[i] = options[best];
		//		}
		//		var newstate = findnexts(move,state,library);
		//		return depthcharge(newstate);
		
		// Implementation always times out, why?
		//		List<Move> moves = new ArrayList<Move>();
		//		List<Role> roles = stateMachine.getRoles();
		//		for (Role role : roles) {
		//			List<Move> legalMoves = stateMachine.getLegalMoves(state, role);
		//			int randomIndex  = getRandomInt(legalMoves.size()-1);
		//			Move bestMove = legalMoves.get(randomIndex);
		//			moves.add(bestMove);
		//		}
		
		//MachineState newState = stateMachine.getNextState(state, moves);
		// Q: does this work instead?
		MachineState newState = stateMachine.getRandomNextState(state);
		return performDepthChargeFromState(newState);
	}
	
	/* ================================== [ALT MCS IMPLEMENTATION AS REFERENCE] ============================================ */
	
	@SuppressWarnings("unused")
	private Move selectMoveMonteCarloSearch_ALT(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		StateMachine theMachine = getStateMachine();
		long start = System.currentTimeMillis();
		long finishBy = timeout - 1000;
		
		List<Move> moves = theMachine.getLegalMoves(getCurrentState(), getRole());
		Move selection = moves.get(0);
		if (moves.size() > 1) {
			int[] moveTotalPoints = new int[moves.size()];
			int[] moveTotalAttempts = new int[moves.size()];
			
			// Perform depth charges for each candidate move, and keep track
			// of the total score and total attempts accumulated for each move.
			for (int i = 0; true; i = (i + 1) % moves.size()) {
				if (System.currentTimeMillis() > finishBy) {
					break;
				}
				
				int theScore = performDepthChargeFromMove(getCurrentState(), moves.get(i));
				moveTotalPoints[i] += theScore;
				moveTotalAttempts[i] += 1;
			}
			
			// Compute the expected score for each move.
			double[] moveExpectedPoints = new double[moves.size()];
			for (int i = 0; i < moves.size(); i++) {
				moveExpectedPoints[i] = (double) moveTotalPoints[i] / moveTotalAttempts[i];
			}
			
			// Find the move with the best expected score.
			int bestMove = 0;
			double bestMoveScore = moveExpectedPoints[0];
			for (int i = 1; i < moves.size(); i++) {
				if (moveExpectedPoints[i] > bestMoveScore) {
					bestMoveScore = moveExpectedPoints[i];
					bestMove = i;
				}
			}
			selection = moves.get(bestMove);
		}
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}
	
	
	private int performDepthChargeFromMove(MachineState theState, Move myMove) {
		StateMachine theMachine = getStateMachine();
		try {
			MachineState finalState = theMachine.performDepthCharge(theMachine.getRandomNextState(theState, getRole(), myMove), depth);
			return theMachine.getGoal(finalState, getRole());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/* =============================== [INTERFACE METHODS] ========================================================= */
	
	// NOTE: moved up to superclass
	
}
