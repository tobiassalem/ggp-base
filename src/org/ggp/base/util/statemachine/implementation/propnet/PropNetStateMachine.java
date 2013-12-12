package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.propnet.factory.PropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;


@SuppressWarnings("unused")
public class PropNetStateMachine extends StateMachine {
	/** The underlying proposition network  */
	private PropNet				propNet;
	/** The topological ordering of the propositions */
	private List<Proposition>	ordering;
	/** The player roles */
	private List<Role>			roles;

	/**
	 * Initializes the PropNetStateMachine. You should compute the topological
	 * ordering here. Additionally you may compute the initial state here, at
	 * your discretion.
	 */
	@Override
	public void initialize(List<Gdl> description) {
		// Use the OptimizingPropNetFactory if possible
		try {
			propNet = OptimizingPropNetFactory.create(description);
		} catch (InterruptedException e) {
			System.err.println("OptimizingPropNetFactory failed to create propNet: " + e.getMessage());
			e.printStackTrace();
			propNet = PropNetFactory.create(description);
		}
		roles = propNet.getRoles();
		ordering = getOrdering();
	}

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		// TODO: Compute whether the MachineState is terminal.
		// ISSUE: state has no method for getting proposition (which could be compared to propNet.getTerminalProposition())

		//		The implementation of isTerminal() is ok, provided that:
		//			1) Before you call it you set up the current state on the base propositions (else the terminal prop won't have the right inputs); and
		//			2) Either when setting the base props, OR as an extra step in between that calling the above routine,
		//				OR as a modification to Proposition.getValue() you perform the propagation through the network (which is most appropriate will depend on whether you';re doing froward prop, backward prop, or something more complex)

		//		function propterminalp (state,propnet)
		//		 {markbases(state,propnet);
		//		  return propmarkp(propnet.terminal)}

		// Q1: the script example is cheating! just sending state object to markbases then it's magically a list with matched indexes! Not quite Clean Code!
		// Q2: how do we build up the proper baseMarkings map needed for baseMarkings method? This is just a random guess, not likely correct!
		Map<GdlSentence, Boolean> baseMarkings = new HashMap<GdlSentence, Boolean>();

		Set<GdlSentence> contents = state.getContents();
		for (GdlSentence gdlSentence : contents) {
			// Q: from where should be read the boolean value to set? MachineState has no appropriate method!
			baseMarkings.put(gdlSentence, true);
		}

		markBases(baseMarkings);
		// TODO: perform propagation through the network - Q: same as calling propmarkp? what does it stand for, getPropositionMarking?

		Proposition terminalProposition = propNet.getTerminalProposition();
		return terminalProposition.getValue();
	}

//	function propmarkp (p)
//	 {if (p.type=='base') {return p.mark};
//	  if (p.type=='input') {return p.mark};
//	  if (p.type=='view') {return propmarkp(p.source)};
//	  if (p.type=='negation') {return propmarknegation(p)};
//	  if (p.type=='conjunction') {return propmarkconjunction(p)};
//	  if (p.type=='disjunction') {return propmarkdisjunction(p)};
//	  return false}
	private boolean getPropositionMarking(Proposition prop) {
		// TODO confirm logic - proposition has no type, how to check it?
		if (prop.getName().equals("base")) {
			return prop.getValue();
		}
		if (prop.getName().equals("input")) {
			return prop.getValue();
		}
		if (prop.getName().equals("view")) {
			//return getPropositionMarking(prop.getSingleInput());
		}
		if (prop.getName().equals("base")) {
			return prop.getValue();
		}
		return false;
	}

	private boolean getPropositionMarkingConjuction(Proposition prop) {
		// TODO implement
		return false;
	}

	private boolean getPropositionMarkingDisjuction(Proposition prop) {
		// TODO implement
		return false;
	}


	/**
	 * Computes the goal for a role in the current state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		// TODO: Compute the goal for role in state.
		//		In regard to getGoal(), do what you'er doing and when you find a goal prop that is true (i.e. - a goal that is met by the current state)
		//		then call getName() on it to retrieve its associated GDL sentence - that can then be parsed to recover the goal value.

		int goalValue = -1;
		Map<Role, Set<Proposition>> goalPropositions = propNet.getGoalPropositions();
		Set<Proposition> set = goalPropositions.get(role);
		for (Proposition proposition : set) {
			// Q1: how do we get the value of the goal proposition? There is no appropriate method! getValue returns a boolean (assuming if the prop is true or not)
			// Q2: how do we parse the actual value from the proposition or GdlSentence? The method names and return values makes no sense!
			if (proposition.getValue()) {
				goalValue = getGoalValue(proposition);
			}
			;
		}
		return goalValue;
	}

	/**
	 * Returns the initial state. The initial state can be computed
	 * by only setting the truth value of the INIT proposition to true,
	 * and then computing the resulting state.
	 */
	@Override
	public MachineState getInitialState() {
		// TODO: Compute the initial state.
		Proposition initProposition = propNet.getInitProposition();
		initProposition.setValue(true);

		return null;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		// TODO: Compute legal moves.
		List<Move> result = new ArrayList<Move>();
		Map<Role, Set<Proposition>> legalPropsMap = this.propNet.getLegalPropositions();
		Set<Proposition> legalProps = legalPropsMap.get(role);

		for (Proposition p : legalProps) {

			List<GdlTerm> body = p.getName().getBody();
			if (role.getName().equals(body.get(0))) {
				GdlTerm t = body.get(1);
				Move m = PropNetStateMachine.getMoveFromProposition(p);
				result.add(m);
			}
		}
		return result;
	}

	/**
	 * Computes the next state given state and the list of moves.
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		// TODO: Compute the next state.
		//		function propnexts (move,state,propnet)
		//		 {markactions(move,propnet);
		//		  markbases(state,propnet);
		//		  var bases = propnet.bases;
		//		  var nexts = seq();
		//		  for (var i=0; i<bases.length; i++)
		//		      {nexts[i] = propmarkp(bases[i].source.source)};
		//		  return nexts}

		for (Move move : moves) {
			GdlTerm contents = move.getContents();
		}

		Map<GdlSentence, Boolean> actionMarkings = new HashMap<GdlSentence, Boolean>();
		// TODO: build up actionMarkings somehow

		markActions(actionMarkings);

		return null;
	}

	/**
	 * This should compute the topological ordering of propositions.
	 * Each component is either a proposition, logical gate, or transition.
	 * Logical gates and transitions only have propositions as inputs.
	 *
	 * The base propositions and input propositions should always be exempt
	 * from this ordering.
	 *
	 * The base propositions values are set from the MachineState that
	 * operations are performed on and the input propositions are set from
	 * the Moves that operations are performed on as well (if any).
	 *
	 * @return The order in which the truth values of propositions need to be set.
	 */
	public List<Proposition> getOrdering()
	{
		// List to contain the topological ordering.
		List<Proposition> order = new LinkedList<Proposition>();

		// All of the components in the PropNet
		List<Component> components = new ArrayList<Component>(propNet.getComponents());

		// All of the propositions in the PropNet.
		List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

		// TODO: Compute the topological ordering.

		return order;
	}

	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/* ============================= [Helper methods] ====================================================  */

	/**
	 * NOTE:
	 * In our approach to General Game Playing using propnets, we use input markings in place of moves and base markings in place of states.
	 * In order to compute the various attributes of a game on a given step, we typically mark the input propositions and base propositions
	 * of the propnet and then compute the corresponding view marking. We then read the view marking to compute the desired attributes.
	 */

	//	function markbases (vector,propnet)
	//	 {var props = propnet.bases;
	//	  for (var i=0; i<props.length; i++)
	//	      {props[i].mark = vector[i]};
	//	  return true}

	private boolean markBases(Map<GdlSentence, Boolean> baseMarkings) {
		// TODO: implement
		// Q: how do we get the argument to this method? Has to be a corresponding Map. Extract it from MachineState somehow?
		Map<GdlSentence, Proposition> basePropositions = propNet.getBasePropositions();

		for (GdlSentence key : baseMarkings.keySet()) {
			Boolean value = baseMarkings.get(key);
			Proposition proposition = basePropositions.get(key);
			proposition.setValue(value);
		}

		return true;
	}

	private void clearBasePropositionsOfThePropNet() {
		Map<GdlSentence, Proposition> basePropositions = propNet.getBasePropositions();

		for (GdlSentence key : basePropositions.keySet()) {
			Proposition baseProp = basePropositions.get(key);
			baseProp.setValue(false);
		}
	}

	//	function markactions (vector,propnet)
	//	 {var props = propnet.actions;
	//	  for (var i=0; i<props.length; i++)
	//	      {props[i].mark = vector[i]};
	//	  return true}

	private boolean markActions(Map<GdlSentence, Boolean> actionMarkings) {
		// Q1: WHAT DO YOU MEAN WITH ACTIONS IN THE SCRIPT VERSION?? NOT MENTIONED IN LECTURE! Assume MOVES = INPUT MARKINGS !?
		// Q2: how do we get the argument to this method? Has to be a corresponding Map. Extract it from MachineState somehow?

		Map<GdlSentence, Proposition> inputPropositions = propNet.getInputPropositions();

		for (GdlSentence key : actionMarkings.keySet()) {
			Boolean value = actionMarkings.get(key);
			Proposition proposition = inputPropositions.get(key);
			proposition.setValue(value);
		}

		return true;
	}

	/**
	 * The Input propositions are indexed by (does ?player ?action).
	 *
	 * This translates a list of Moves (backed by a sentence that is simply ?action)
	 * into GdlSentences that can be used to get Propositions from inputPropositions.
	 * and accordingly set their values etc.  This is a naive implementation when coupled with
	 * setting input values, feel free to change this for a more efficient implementation.
	 *
	 * @param moves
	 * @return
	 */
	private List<GdlSentence> toDoes(List<Move> moves)
	{
		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();

		for (int i = 0; i < roles.size(); i++)
		{
			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
		}
		return doeses;
	}

	/**
	 * Takes in a Legal Proposition and returns the appropriate corresponding Move
	 * @param p
	 * @return a PropNetMove
	 */
	public static Move getMoveFromProposition(Proposition p)
	{
		return new Move(p.getName().get(1));
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
	private int getGoalValue(Proposition goalProposition)
	{
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}

	/**
	 * A Naive implementation that computes a PropNetMachineState
	 * from the true BasePropositions.  This is correct but slower than more advanced implementations
	 * You need not use this method!
	 * @return PropNetMachineState
	 */
	public MachineState getStateFromBase()
	{
		Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (Proposition p : propNet.getBasePropositions().values())
		{
			p.setValue(p.getSingleInput().getValue());
			if (p.getValue())
			{
				contents.add(p.getName());
			}

		}
		return new MachineState(contents);
	}
}