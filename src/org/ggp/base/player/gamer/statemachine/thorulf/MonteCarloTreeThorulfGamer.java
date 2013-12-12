package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.ArrayList;
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
 * Implementation of MCTS - Monto Carlo Tree Search Gamer
 *
 * @author Tobias
 *
 */
public class MonteCarloTreeThorulfGamer extends ThorulfGamer {

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

		return selectMoveMonteCarloTreeSearch(timeout);
	}

	private Move selectMoveMonteCarloTreeSearch(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		long start = System.currentTimeMillis();
		terminalStateCounter = 0;

		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selectedMove = legalMoves.get(0);

		// MTCS: iterate steps 1-4 while timeout not reached (and at most ITERATION_LIMIT iterations).
		// 1) selection - of a game tree node
		// 2) expansion - of the game tree
		// 3) simulation - of game moves (aka depthcharge)
		// 4) backpropagation - update stats of the visited nodes
		// 5) select best move - given best node (i.e. the node with highest utility)

		// 0) Setup
		boolean optimalNodeReached = false;
		TreeNode rootNode = new TreeNode(getCurrentState());
		List<TreeNode> visitedNodes = new ArrayList<TreeNode>();

		// ISSUE: rootNode will not have any children here and be considered a leaf node!
		// SOLUTION: that's fine, it will be expanded in the next step.

		for (int iterationCount = 0; !timeoutReached(timeout) && !iterationLimitReached(iterationCount) && !optimalNodeReached; iterationCount++) {
			// 0) Setup
			visitedNodes.clear();
			TreeNode currentNode = rootNode;
			visitedNodes.add(rootNode);
			System.out.println("currentNode.isLeaf: " +currentNode.isLeaf() +", iterationCount: " +iterationCount);

			// 1) Selection
			while (!currentNode.isLeaf()) {
				currentNode = currentNode.selectChildNodeGivenUCT();
				visitedNodes.add(currentNode);
			}

			// 2) expansion
			TreeNode expansionNode = expandNode(currentNode);
			visitedNodes.add(expansionNode);

			// 3) simulation / depthcharge
			double depthChargeValue = doDepthChargeFromState(expansionNode.state);

			// 4) backpropagation / update stats
			for (TreeNode node : visitedNodes) {
				// would need extra logic for n-player game
				// System.out.println(node);
				node.updateStats(depthChargeValue);
				if (node.getUtility() >= MAX_SCORE) {
					optimalNodeReached = true;
				}
			}
		}

		// 5) select move given best node according to MCTS
		// Q: how do we select move based on all the visited nodes?
		// Issue: how does the nodes relate to legal moves? TreeNode is just a help class, not part of ggp. How do we get from node to legal move?
		// Solution: let each TreeNode stores its state and the move which got us there.
		// We can then iterate over rootNode.children, and ask the best node which move got us there.
		// We iterate over rootNode.chilrden (not all visitedNodes) - since we are taking ONE FIRST STEP towards the goal.
		double bestNodeUtility = 0;
		TreeNode bestNode = null;
		for (TreeNode treeNode : rootNode.children) {
			double nodeUtility = treeNode.getUtility();
			if (nodeUtility > bestNodeUtility && treeNode.move != null) {
				bestNodeUtility = nodeUtility;
				bestNode = treeNode;
				selectedMove = bestNode.move;
			}
		}

		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(legalMoves, selectedMove, stop - start));
		return selectedMove;
	}


	private boolean iterationLimitReached(int iterationCount) {
		return iterationCount > 10;
	}

	// Javascript implementation of expandNode
	//	function expand(node) {
	//	var action = findlegals(role,node.state,ruleset);
	//
	//	for (var i=0; i<actions.length; i++) {
	//		var newstate = findnext([actions[i]],state,ruleset);
	//		var newnode = makenode(newstate,0,0,node,[]);
	//		node.children[node.children.length] = newnode;
	//	}
	//	return true;
	//}
	private TreeNode expandNode(TreeNode node) throws MoveDefinitionException, TransitionDefinitionException {
		// TODO: expand this for multiplayer games
		// Alt.1 - singleplayer only
		//		List<Move> legalMoves = getStateMachine().getLegalMoves(node.state, getRole());
		//		for (Move move : legalMoves) {
		//			MachineState nextState = getNextState(node.state, move);
		//			TreeNode nextNode = new TreeNode(nextState,move);
		//			node.addChild(nextNode);
		//		}

		// Alt.2 - multiplayer
		List<List<Move>> legalJointMoves = getStateMachine().getLegalJointMoves(node.state);
		debugJointMoves(legalJointMoves);
		for (List<Move> legalJointMove : legalJointMoves) {
			MachineState nextState = getStateMachine().getNextState(node.state, legalJointMove);
			Move myMove = legalJointMove.get(myRoleIndex);
			TreeNode nextNode = new TreeNode(nextState,myMove);
			node.addChild(nextNode);
		}

		debug("expandNode - added childNodes for legalMoves.size: " +legalJointMoves.size() + ", node.children: " +node.getArity());
		return node.selectChildNodeGivenUCT();
	}


	/* ================================== [MCS IMPLEMENTATION AS REFERENCE] ============================================ */

	private int getMinScore(Move myMove, MachineState state, int level, long timeout) {
		StateMachine stateMachine = getStateMachine();
		int score = MAX_SCORE;
		int nextLevel = level + 1;

		try {

			// NOTE: If there is no opponent, move to the next max node
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
		int total = 0;
		for (int i = 0; i < DEPTH_CHARGE_ATTEMPTS; i++) {
			total += doDepthChargeFromState(state);
		}
		int averageUtility = total / DEPTH_CHARGE_ATTEMPTS;
		return averageUtility;
	}


	private int doDepthChargeFromState(MachineState curState) throws MoveDefinitionException, TransitionDefinitionException {
		StateMachine stateMachine = getStateMachine();

		try {
			while (!stateMachine.isTerminal(curState)) {
				curState = stateMachine.getRandomNextState(curState);
			}
			return stateMachine.getGoal(curState, getRole());
		} catch (GoalDefinitionException e) {
			System.err.println("doDepthChargeFromState caused Exception: " +e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	// ISSUE: get Exception in thread "main" java.lang.StackOverflowError for MCTS - never for MCS.
	// The key difference is MTCS has an outer loop for the four steps, MCS does not (when removing this loop no exception occurs).
	// SOLUTION: use an iterative version of deptchcharge, should not lead to stack
	@SuppressWarnings("unused")
	private int doDepthChargeFromStateRECURSIVE(MachineState state) throws MoveDefinitionException, TransitionDefinitionException {

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

		// NOTE: avoid StackOverflowError for MCTS during depthcharge and return random score for now.
		//return new Random().nextInt(101); // Pointless, player performs extremely poorly!

		MachineState newState = stateMachine.getRandomNextState(state); // <--- causes StackOverflowError for MCTS
		return doDepthChargeFromStateRECURSIVE(newState);
	}


	/* =============================== [INTERFACE METHODS] ========================================================= */

	// NOTE: moved up to superclass

}
