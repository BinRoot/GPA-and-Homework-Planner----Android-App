package model;

import java.util.Date;

public class TodoItem {
	private String desc;
	private Date due;
	private int priority;
	boolean []days;
	boolean done;
	
	public TodoItem (String desc, Date due, int priority, boolean [] days, boolean done) {
		this.desc = desc;
		this.due = due;
		this.priority = priority;
		this.days = days;
		this.done = done;
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
	
	public boolean getDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
