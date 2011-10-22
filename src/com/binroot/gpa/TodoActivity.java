package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import misc.Constants;
import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TodoActivity extends Activity {
	
	String title = null;
	String period = null;
	ListView lv;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);
		
		Bundle b = getIntent().getExtras();
		if(b!=null) {
			title = b.getString("title");
			period = b.getString("period");
			((TextView)findViewById(R.id.text_todo_title)).setText(title);
		}
		
		storageInit();
	}
	
	public void storageInit() {
		String filename = Constants.File_Todo+"_"+period+"_"+title;
		
		ArrayList<TodoItem> todoList = new ArrayList<TodoItem>();
		
		FileInputStream fis = null;
		try {
			fis = openFileInput(filename);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+filename);
		}

		if(fis == null) { // File doesn't exist. Must be a new user.
			FileOutputStream fos = null;
			try {
				fos = openFileOutput(filename, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not create outputstream "+filename);
			}
			
			JSONObject jMain = new JSONObject();
			JSONArray jTodos = new JSONArray();
			
			try {
				jMain.accumulate("todo", jTodos);
			} 
			catch (JSONException e) {
				Log.d(getString(R.string.app_name), "JSON err: "+e.getMessage());
			}
			
			try {
				fos.write(jMain.toString().getBytes());
			} catch (IOException e) {
				Log.d(getString(R.string.app_name), "Could not write to fos: "+e.getMessage());
			}
			
			
			try {
				fis = openFileInput(filename);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not find file "+filename);
			}
		}
		
		
		// open file
		StringBuilder total = null;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(fis));
			total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
			    total.append(line);
			}

		} catch (IOException e) {
			Log.d(getString(R.string.app_name), "Could not read buffer "+Constants.File_TranscriptList);
		}
		String todosJSONStr = total.toString();
		
		Log.d(getString(R.string.app_name), "json from file: "+todosJSONStr);
		
		JSONObject jMain = null;
		try {
			jMain = new JSONObject(todosJSONStr);
		} catch (JSONException e) {
			Log.d(getString(R.string.app_name), "not proper JSON: "+todosJSONStr);
		}
		
		if(jMain!=null) {

			JSONArray todosJSON = null;
			try {
				todosJSON = jMain.getJSONArray("todo");
			} catch (JSONException e) {
				Log.d(getString(R.string.app_name), "JSON high key err: "+e.getMessage());
			}
			
			if(todosJSON != null) {
				for(int i=0; i<todosJSON.length(); i++) {
					JSONObject todoItemJSON = null;
					try {
						todoItemJSON = todosJSON.getJSONObject(i);
					} catch (JSONException e) {
						Log.d(getString(R.string.app_name), "JSON class err: "+e.getMessage());
					}
					
					if(todoItemJSON != null) {
						try {

							String desc = todoItemJSON.getString("desc");
							String dueStr = todoItemJSON.getString("due");
							Date due = new Date(dueStr);
							
							int priority = todoItemJSON.getInt("priority");
							
							boolean []days = new boolean[7];
							for(int j=1; j<=7; j++) {
								days[j] = todoItemJSON.getBoolean("days"+j);
							}
							
							boolean done = todoItemJSON.getBoolean("done");
							
							TodoItem ti = new TodoItem(desc, due, priority, days, done);
							
							Log.d(getString(R.string.app_name), "adding todo item... "+ti);
							
							todoList.add(ti);
							
						} catch (JSONException e) {
							Log.d(getString(R.string.app_name), "err: "+e.getMessage());
						}
					}
				}
			}
		} // end JSON not null
		
		
		lv = (ListView) findViewById(R.id.list_todo);
		
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		todoList.add(new TodoItem("", null, 0, null, false));
		
		lv.setAdapter(new TodoListAdapter(todoList));
		
	} // end storageInit
	
	
	public class TodoListAdapter extends BaseAdapter {

		ArrayList<TodoItem> todoList;
		
		public TodoListAdapter(ArrayList<TodoItem> todoList) {
			this.todoList = todoList;
		}
		
		@Override
		public int getCount() {
			return todoList.size();
		}

		@Override
		public Object getItem(int position) {
			return todoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView; 
			
			if(v==null) {
				v = getLayoutInflater().inflate(R.layout.todo_item, null);
			}
			
			return v;
		}
		
	}
	
	public void logoClicked(View v) {
		TodoActivity.this.finish();
	}
}
