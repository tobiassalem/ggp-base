package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
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
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class DeliberateThorulfGamer extends StateMachineGamer {
	
	private static final int	MAX_SCORE	= 100;
	boolean	isMultiplayer	= false;
	boolean	isCoop			= false;
	
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
	 */
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO find out useful information about the game
		
		// Is the game multiplayer or not
		List<Role> roles = getStateMachine().getRoles();
		if (roles.size() > 1) {
			isMultiplayer = true;
		}
		
		System.out.println(getName() + " started meta game, isMultiplayer: " +isMultiplayer);
		
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
	 */
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO: Implement smarter algorithm, for now just use first legal move.
		
		if (isMultiplayer) {
			return selectMoveMonteCarloSearch(timeout);
		} else {
			return selectMoveDeliberation(timeout);
		}
	}
	
	private Move selectMoveDeliberation(long timeout) throws MoveDefinitionException, TransitionDefinitionException {
		// We get the current start time
		long start = System.currentTimeMillis();
		MachineState state = getCurrentState();
		
		List<Move> moves = getStateMachine().getLegalMoves(state, getRole());
		Move action = moves.get(0);
		List<Move> moveList = new ArrayList<Move>();
		
		int score = 0;
		for (Move move : moves) {
			moveList.clear();
			moveList.add(move);
			System.out.println("Considering move:  " +move+ " from state " +state);
			MachineState newState = getStateMachine().getNextState(state, moveList);
			int result = getMaxScore(newState);
			if (result == MAX_SCORE) {
				return move;
			} else if (result > score) {
				score = result;
				action = move;
			}
			
		}
		
		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(moves, action, stop - start));
		return action;
	}
	
	private int getMaxScore(MachineState state) {
		int score = 0;
		StateMachine stateMachine = getStateMachine();
		
		if (stateMachine.isTerminal(state)) {
			try {
				int goalScore = stateMachine.getGoal(state, getRole());
				System.out.println(getName() + " Got goalScore: " +goalScore);
				return goalScore;
			} catch (GoalDefinitionException e) {
				e.printStackTrace();
				return 0;
			}
		}
		try {
			List<Move> legalMoves = stateMachine.getLegalMoves(state, getRole());
			for (Move move : legalMoves) {
				List<Move> moves = new ArrayList<Move>();
				moves.add(move);
				MachineState newState = stateMachine.getNextState(state, moves);
				int result = getMaxScore(newState);
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
	
	private Move selectMoveMonteCarloSearch(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
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
	
	private final int[]	depth	= new int[1];
	
	int performDepthChargeFromMove(MachineState theState, Move myMove) {
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
	
	@Override
	public void stateMachineStop() {
		// TODO Called when the match is complete
		isMultiplayer = false;
		isCoop = false;
	}
	
	@Override
	public void stateMachineAbort() {
		// TODO Called when the match is abruptly ended mid-way through.
		isMultiplayer = false;
		isCoop = false;
	}
	
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		//		Sometimes called before the match begins. Just leave this empty for now.
		//		This part of the GGP protocol is still being developed, and isn’t yet used. It’s designed to let players “preview” a game for,
		//		say, 10-15 minutes, and then rapidly play a series of short matches on that game, using insights gained during the preview phase
		//		to inform all of those matches. (This used to be called “analyze”)
	}
	
}
