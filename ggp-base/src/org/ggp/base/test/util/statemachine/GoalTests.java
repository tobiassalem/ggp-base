package org.ggp.base.test.util.statemachine;

import junit.framework.Assert;

import org.ggp.base.util.statemachine.Goal;
import org.junit.Test;

public class GoalTests {

	@Test
	public void testDefaultContructor() throws Exception {
		Goal goal = new Goal();

		Assert.assertNull(goal.getValue());
	}

	@Test
	public void testValueContructor() throws Exception {
		Goal goal1 = new Goal(1);

		Assert.assertNotNull(goal1.getValue());

		Goal goal2 = new Goal(goal1.getValue());
		Assert.assertEquals(goal1.getValue(), goal2.getValue());

		goal2.setValue(2);
		Assert.assertNotSame(goal1.getValue(), goal2.getValue());
		Assert.assertSame(1, goal1.getValue().intValue());
		Assert.assertSame(2, goal2.getValue().intValue());
	}

	@Test
	public void testCopyConstrutor() throws Exception {
		Goal goal1 = new Goal(1);

		Goal goal2 = new Goal(goal1);

		goal2.setValue(2);
		Assert.assertNotSame(goal1.getValue(), goal2.getValue());
		Assert.assertSame(1, goal1.getValue().intValue());
		Assert.assertSame(2, goal2.getValue().intValue());
	}

	@Test
	public void testIsMin() throws Exception {
		Goal goal = new Goal(Goal.MIN_VALUE);

		Assert.assertTrue(goal.isMin());
	}

	@Test
	public void testIsMax() throws Exception {
		Goal goal = new Goal(Goal.MAX_VALUE);

		Assert.assertTrue(goal.isMax());
	}
}
