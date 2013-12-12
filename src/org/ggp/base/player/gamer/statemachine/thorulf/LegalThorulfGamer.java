package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class LegalThorulfGamer extends StateMachineGamer {
	
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
	}
	
	/* =============================== [SEARCH / MOVE METHODS] ========================================================= */
	
	@Override
	/**
	 * Called when your player needs to select a move. This method returns the selected move.
	 */
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO: Implement smarter algorithm, for now just use first legal move.
		
		return selectMoveFirstLegal(timeout);
	}
	
	private Move selectMoveFirstLegal(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// We get the current start time
		long start = System.currentTimeMillis();
		
		/**
		 * We put in memory the list of legal moves from the
		 * current state. The goal of every stateMachineSelectMove()
		 * is to return one of these moves. The choice of which
		 * Move to play is the goal of GGP.
		 */
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		
		// SampleLegalGamer is very simple : it picks the first legal move
		Move selection = moves.get(0);
		
		// We get the end time
		// It is mandatory that stop < timeout
		long stop = System.currentTimeMillis();
		
		/**
		 * These are functions used by other parts of the GGP codebase
		 * You shouldn't worry about them, just make sure that you have
		 * moves, selection, stop and start defined in the same way as
		 * this example, and copy-paste these two lines in your player
		 */
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
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
