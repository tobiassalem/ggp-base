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

public class MinMaxThorulfGamer extends StateMachineGamer {
	
	private static final int	MIN_SCORE	= 0;
	private static final int	MAX_SCORE	= 100;
	boolean	isMultiplayer	= false;
	boolean	isCoop			= false;
	Role myRole = null;
	Role opponentRole = null;
	
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
			myRole = getRole();
			//roles.remove(myRole);
			opponentRole = roles.get(1);
		}
		
		System.out.println(getName() + " started meta game, isMultiplayer: " +isMultiplayer + ", myRole: " +getRole() + ", opponentRole: " +opponentRole);
		
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
		
		return selectMoveMinMax(timeout);
	}
	
	private Move selectMoveMinMax(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		MachineState state = getCurrentState();
		
		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		//getStateMachine().getLegalJointMoves(state, getRole(), move);
		Move selectedMove = legalMoves.get(0);
		int score = 0;
		
		// TODO: implement
		for (Move move : legalMoves) {
			//List<Move> moveList = new ArrayList<Move>();
			//moveList.add(move);
			//MachineState newState = getStateMachine().getNextState(state, moveList);
			int result = getMinScore(move, state);
			System.out.println("Considering move " +move+ ", got result " +result);
			if (result > score) {
				score = result;
				selectedMove = move;
				System.out.println("Selected move: " +selectedMove);
			}
		}
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(legalMoves, selectedMove, stop - start));
		return selectedMove;
	}
	
	private int getMinScore(Move myMove, MachineState state) {
		StateMachine stateMachine = getStateMachine();
		int score = 100;
		
		try {
			// javascript version from lecture:
			// var actions = findlegals(opponent,state,ruleset);
			// var score = 100;
			//		for (var i=0; i<actions.length; i++) {
			//			var move = [action, actions[i]];
			//			var newstate = findnexts(move,state,ruleset);
			//			var result = maxscore(newstate);
			//			if (result < score) {
			//				score = result;
			//			}
			//		}
			
			// TODO: get legal moves for the OPPONENT?
			List<Move> opponentLegalMoves = stateMachine.getLegalMoves(state, opponentRole);
			//List<List<Move>> legalJointMoves = stateMachine.getLegalJointMoves(state, getRole(), myMove);
			//List<Move> opponentLegalMoves = legalJointMoves.get(0);
			
			for (Move opponentMove : opponentLegalMoves) {
				System.out.println("Considering opponentMove: " +opponentMove+ ", given myMove " +myMove);
				List<Move> moveList = new ArrayList<Move>();
				moveList.add(myMove);
				moveList.add(opponentMove);
				MachineState newState = stateMachine.getNextState(state,moveList);
				int result = getMaxScore(newState);
				if (result == MIN_SCORE) {
					return MIN_SCORE;
				}
				if (result < score) {
					score = result;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}
	
	private int getMaxScore(MachineState state) {
		int score = 0;
		StateMachine stateMachine = getStateMachine();
		
		if (stateMachine.isTerminal(state)) {
			try {
				return stateMachine.getGoal(state, getRole());
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
				// NOTE: don't calculate newState in maxScore for MiniMax!
				//MachineState newState = stateMachine.getNextState(state, moves);
				int result = getMinScore(move,state);
				if (result == MAX_SCORE) {
					return MAX_SCORE;
				}
				if (result > score) {
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
