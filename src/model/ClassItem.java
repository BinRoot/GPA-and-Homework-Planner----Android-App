package model;

import misc.Constants;
import android.view.View;
import android.widget.EditText;

import com.binroot.gpa.R;

public class ClassItem {
	private String title = "Enter Class Title";
	private int gradeGuar = Constants.GradeAm;
	private int gradeMain = Constants.GradeAf;
	private int gradeGoal = Constants.GradeAp;
	private float credits = 3;
	private boolean selectMode = true;
	private boolean editMode = false;
	public EditText et;
	
	
	public ClassItem(String title) {
		this.title = title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	
	public void setGradeGuar(int gradeGuar) {
		this.gradeGuar = gradeGuar;
	}
	public int getGradeGuar() {
		return gradeGuar;
	}
	
	public void setGradeMain(int gradeMain) {
		this.gradeMain = gradeMain;
	}
	public int getGradeMain() {
		return gradeMain;
	}
	
	public void setGradeGoal(int gradeGoal) {
		this.gradeGoal = gradeGoal;
	}
	public int getGradeGoal() {
		return gradeGoal;
	}
	
	public void setCredits(float credits) {
		this.credits = credits;
	}
	public float getCredits() {
		return credits;
	}
	
	public void setSelectMode(boolean mode) {
		selectMode = mode;
	}
	public boolean getSelectMode() {
		return selectMode;
	}
	
	public void setEditMode(boolean mode) {
		editMode = mode;
	}
	public boolean getEditMode() {
		return editMode;
	}
	
	public void normalize() {
		selectMode = false;
		editMode = false;
	}
}
