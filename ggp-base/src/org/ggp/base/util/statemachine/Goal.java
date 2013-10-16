package org.ggp.base.util.statemachine;

public class Goal implements Comparable<Goal> {

	public static final int MIN_VALUE = 0;
	public static final int AVG_VALUE = 50;
	public static final int MAX_VALUE = 100;

	protected Integer value;

	public Goal() {
		value = null;
	}

	public Goal(int value) {
		setValue(value);
	}

	public Goal(Integer value) {
		this.value = value;
	}

	public Goal(Goal other) {
		Integer value = other.getValue();
		if (null == value) {
			this.value = null;
		} else {
			this.value = value;
		}
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = new Integer(value);
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Boolean isMin() {
		return Goal.MIN_VALUE == this.value;
	}

	public Boolean isMax() {
		return Goal.MAX_VALUE == this.value;
	}

	public String toString() {

		if (null == this.value) {
			return "";
		} else {
			return this.value.toString();
		}
	}

	@Override
	public int compareTo(Goal arg0) {
		return this.value.compareTo(arg0.getValue());
	}

	@Override
	public boolean equals(Object o) {
		if ((o != null) && (o instanceof Goal)) {
			Goal goal = (Goal) o;
			return this.value.equals(goal.value);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}
}
