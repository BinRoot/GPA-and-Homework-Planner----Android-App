package misc;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TranscriptParser {
	public static TranscriptParser tp;
	
	JSONObject jo;
	
	public static TranscriptParser getInstance() {
		if(tp == null) {
			tp = new TranscriptParser();
		}
		return tp;
	}
	
	
	public void setJSON(JSONObject jo) {
		this.jo = jo;
	}
	
	public JSONObject getJSON() {
		return jo;
	}
	
	public void addClass(String period, String title, int gradeMain, int gradeGoal, int gradeGuar, float credits) {
		JSONArray classArrJSON = null;
		try {
			classArrJSON = jo.getJSONArray(period);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(classArrJSON != null) {
			JSONObject classJSON = new JSONObject();
			try {
				classJSON.accumulate("title", title);
				classJSON.accumulate("gradeMain", gradeMain+"");
				classJSON.accumulate("gradeGoal", gradeGoal+"");
				classJSON.accumulate("gradeGuar", gradeGuar+"");
				classJSON.accumulate("credits", credits+"");
				jo.accumulate(period, classJSON);
			} 
			catch (JSONException e) { }
		}
	}
	
	public void editGrade(String period, String title, int gradeMain, int gradeGoal, int gradeGuar, float credits) {
		try {
			JSONArray ja = jo.getJSONArray(period);

			for(int i=0; i<ja.length(); i++) {
				JSONObject classObj = ja.getJSONObject(i);

				if(classObj.getString("title").equals(title)) {
					classObj.put("gradeGoal", gradeGoal+"");
					classObj.put("gradeMain", gradeMain+"");
					classObj.put("gradeGuar", gradeGuar+"");
					classObj.put("credits", credits+"");
					break;
				}
			}
			
			jo.remove(period);
			jo.accumulate(period, ja);

		} catch (JSONException e) { }
	}
	
	public void editClassTitle(String period, String oldTitle, String newTitle) {
		System.out.println("editing json...");
		try {
			JSONArray ja = jo.getJSONArray(period);

			System.out.println("got json from period");
			
			for(int i=0; i<ja.length(); i++) {
				JSONObject classObj = ja.getJSONObject(i);

				System.out.println("look at class json object... "+i+", "+classObj.getString("title")+" == "+(oldTitle));
				
				if(classObj.getString("title").equals(oldTitle)) {
					classObj.put("title", newTitle);
					System.out.println("updated class Obj");
					break;
				}
				
			}
			
			jo.remove(period);
			jo.accumulate(period, ja);

		} catch (JSONException e) { }

	}
	
}
