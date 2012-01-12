package misc;

import java.util.Date;
import java.util.Iterator;

import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class TodoParser {
	public static TodoParser tp;
	
	JSONObject jo;
	
	public static TodoParser getInstance() {
		if(tp == null) {
			tp = new TodoParser();
		}
		return tp;
	}
	
	
	public void setJSON(JSONObject jo) {
		this.jo = jo;
	}
	
	public JSONObject getJSON() {
		return jo;
	}
	
	public void addItem(TodoItem ti) {
		String desc = ti.getDesc();
		String due = ti.getDue().toString();
		int priority = ti.getPriority();
		boolean done = ti.getDone();
		boolean weekly = ti.getWeekly();
		boolean days[] = ti.getDays();
		
		addItem(desc, due, priority, done, weekly, days);
	}
	
	public void addItem(String desc, String due, int priority, boolean done, boolean weekly, boolean[] days) {
		JSONArray jTodos = null;
		try {
			jTodos = jo.getJSONArray("todo");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		

		if(jTodos != null) {
			JSONObject todoObj = new JSONObject();
			try {
				todoObj.accumulate("desc", desc);
				todoObj.accumulate("due", due);
				todoObj.accumulate("priority", priority);
				todoObj.accumulate("done", done);
				todoObj.accumulate("weekly", weekly);
				for(int i=0; i<7; i++) {
					todoObj.accumulate("days"+(i+1), days[i]);
				}
				jo.accumulate("todo", todoObj);
			} 
			catch (JSONException e) { }
		}
	}
	
	public void editItem(TodoItem oldItem, TodoItem newItem) {
		deleteItem(oldItem);
		addItem(newItem);
	}
	
	public void deleteItem(TodoItem ti) {
		
		
		String desc = ti.getDesc();
		
		//Date oldDate = ti.getDue();
		//ti.getDue().setHours(0);
		//ti.getDue().setMinutes(0);
		//ti.getDue().setSeconds(0);
		String due = ti.getDue().toString();
		int priority = ti.getPriority();
		boolean done = ti.getDone();
		boolean weekly = ti.getWeekly();
		boolean[] days = ti.getDays();
		
		try {
			JSONArray ja = jo.getJSONArray("todo");

			JSONArray jb = new JSONArray();
			for(int i=0; i<ja.length(); i++) {
				JSONObject classObj = ja.getJSONObject(i);
				
				
				if(!  (classObj.getString("desc").equals(desc) 
					&& classObj.getString("due").equals(due)
					&& classObj.getInt("priority")==(priority)
					&& classObj.getBoolean("done")==(done)
					&& classObj.getBoolean("weekly")==(weekly)
					&& classObj.getBoolean("days1")==(days[0])
					&& classObj.getBoolean("days2")==(days[1])
					&& classObj.getBoolean("days3")==(days[2])
					&& classObj.getBoolean("days4")==(days[3])
					&& classObj.getBoolean("days5")==(days[4])
					&& classObj.getBoolean("days6")==(days[5])
					&& classObj.getBoolean("days7")==(days[6]) )) {
					
					jb.put(classObj);
					
					//Log.d("parser", "NOT deleting "+classObj.getString("desc")+", due: "+classObj.getString("due"));
				}
				else {
					//Log.d("parser", "YES deleting "+classObj.getString("desc")+", due: "+classObj.getString("due"));
				}
			}
			
			jo.remove("todo");
			jo.accumulate("todo", jb);
			 
		} catch (JSONException e) { }
	}
}

