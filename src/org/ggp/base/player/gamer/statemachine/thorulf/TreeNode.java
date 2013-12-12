package org.ggp.base.player.gamer.statemachine.thorulf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;

/**
 * Class representing in TreeNode of a game tree - used in Monte Carlo Tree Search (MCTS)
 * A TreeNode has a state and 0..n chilren (where n is a positive integer).
 * 
 * @author Tobias
 *
 */
public class TreeNode {
	static Random r = new Random();
	static int nActions = 5;
	private static final double epsilon = 1e-6;
	
	// Q: should TreeNode hold it's corresponding state and move which got us here?
	// A: Assume yes. How else can we actually select a move once we find the "best node"?
	MachineState state;
	Move move;
	List<TreeNode> children = new ArrayList<TreeNode>();
	double nodeVisits = 0;
	double nodeUtility = 0;
	
	public TreeNode(MachineState state, Move move) {
		this.state = state;
		this.move = move;
	}
	
	/**
	 * Constructor for the root node which will not have a move.
	 * @param state
	 */
	public TreeNode(MachineState state) {
		this(state,null);
	}
	
	public void selectAction() {
		// 0) SETUP
		//		List<TreeNode> visited = new LinkedList<TreeNode>();
		//		TreeNode cur = this;
		//		visited.add(this);
		//
		// 1) SELECTION
		//		while (!cur.isLeaf()) {
		//			cur = cur.selectChildNodeGivenUCT();
		//			// System.out.println("Adding: " + cur);
		//			visited.add(cur);
		//		}
		//
		// 2) EXPANSION
		//		cur.expand();
		//		TreeNode newNode = cur.selectChildNodeGivenUCT();
		//		visited.add(newNode);
		// 3) SIMULATION ?
		//		double value = rollOut(newNode);
		//
		// 4) BACKPROPAGATION?
		//		for (TreeNode node : visited) {
		//			// would need extra logic for n-player game
		//			// System.out.println(node);
		//			node.updateStats(value);
		//		}
	}
	
	
	public TreeNode selectChildNodeGivenUCT() {
		TreeNode selected = null;
		double bestValue = Double.MIN_VALUE;
		for (TreeNode child : children) {
			double uctValue =
				child.nodeUtility / (child.nodeVisits + epsilon) +
				Math.sqrt(Math.log(nodeVisits+1) / (child.nodeVisits + epsilon)) +
				r.nextDouble() * epsilon;
			// small random number to break ties randomly in unexpanded nodes
			// System.out.println("UCT value = " + uctValue);
			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}
		System.out.println("Returning selectChildNodeGivenUCT: " + selected);
		return selected;
	}
	
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	public double rollOut(TreeNode tn) {
		// ultimately a roll out will end in some value
		// assume for now that it ends in a win or a loss
		// and just return this at random
		return r.nextInt(2);
	}
	
	public void updateStats(double value) {
		nodeVisits++;
		nodeUtility += value;
	}
	
	public int getArity() {
		return children.size();
	}
	
	public double getUtility() {
		return nodeUtility;
	}
	
	public void addChild(TreeNode child) {
		children.add(child);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("TreeNode with ");
		sb.append("visits: " + nodeVisits + ", ");
		sb.append("value: " + nodeUtility + ", ");
		sb.append("children.size: " +getArity() + ", ");
		sb.append("state: " +state+ ", ");
		sb.append("move which got us here: " +move);
		return sb.toString();
	}
}

