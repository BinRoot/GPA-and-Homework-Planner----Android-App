package model;

import java.util.Arrays;
import java.util.Date;

public class TodoItem {
	private String desc;
	private Date due;
	private int priority;
	boolean []days;
	private boolean weekly;
	private boolean done;
	
	public TodoItem (String desc, Date due, int priority, boolean [] days, boolean weekly, boolean done) {
		this.desc = desc;
		this.due = due;
		this.priority = priority;
		this.days = days;
		this.setDone(done);
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Date getDue() {
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean[] getDays() {
		return days;
	}

	public void setDays(boolean[] days) {
		this.days = days;
	}
	
	public boolean getWeekly() {
		return isWeekly();
	}

	public void setWeekley(boolean weekly) {
		this.setWeekly(weekly);
	}
	
	public boolean getDone() {
		return isDone();
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o instanceof TodoItem) {
			TodoItem ti = (TodoItem)o;
			if(desc.equals(ti.desc) && due.equals(ti.due) && priority==ti.priority
					&& days==ti.days && isWeekly()==ti.isWeekly() && isDone()==ti.isDone()) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "TodoItem [days=" + Arrays.toString(days) + ", desc=" + desc
				+ ", done=" + isDone() + ", due=" + due + ", priority=" + priority
				+ ", weekly=" + isWeekly() + "]";
	}

	public void setWeekly(boolean weekly) {
		this.weekly = weekly;
	}

	public boolean isWeekly() {
		return weekly;
	}

	public boolean isDone() {
		return done;
	}
}
