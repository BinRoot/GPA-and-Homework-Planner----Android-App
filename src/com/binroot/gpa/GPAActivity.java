package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import misc.Constants;
import model.TodoItem;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GPAActivity extends Activity {
	
	Button transcriptBtn;
	Button homeworkBtn;
	ListView lv;
	String period = null;
	
	public class MainButtonListener implements OnClickListener {

		int mode = -1;
		
		 public MainButtonListener(int mode) {
			this.mode = mode;
		}
		
		@Override
		public void onClick(View v) {
			if(mode==Constants.ButtonHomework) {
				Intent i = new Intent(GPAActivity.this, HomeworkActivity.class);
				i.putExtra("period", period);
				
				Log.d(getString(R.string.app_name), "Launching HomeworkActivity... "
						+ "with period: "+period);
				startActivity(i);
			}
			else if(mode==Constants.ButtonTranscript) {
				Intent i = new Intent(GPAActivity.this, TranscriptActivity.class);
				i.putExtra("period", period);
				
				Log.d(getString(R.string.app_name), "Launching TranscriptActivity... "
						+ "with period: "+period);
				startActivity(i);
			}
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        transcriptBtn = (Button) findViewById(R.id.button_main_transcript);
        transcriptBtn.setOnClickListener(new MainButtonListener(Constants.ButtonTranscript));
        
        homeworkBtn = (Button) findViewById(R.id.button_main_homework);
        homeworkBtn.setOnClickListener(new MainButtonListener(Constants.ButtonHomework));
        
        storageInit();
        
        lv = (ListView)findViewById(R.id.list_main_hw);
        ArrayList<TodoItem> todoList = new ArrayList<TodoItem>();
        todoList.add(new TodoItem("blah1", null, 0, null, false, false));
        todoList.add(new TodoItem("blah2", null, 0, null, false, false));
        todoList.add(new TodoItem("blah3", null, 0, null, false, false));
        todoList.add(new TodoItem("blah4", null, 0, null, false, false));
        todoList.add(new TodoItem("blah5", null, 0, null, false, false));
        todoList.add(new TodoItem("blah6", null, 0, null, false, false));
        todoList.add(new TodoItem("blah7", null, 0, null, false, false));
        todoList.add(new TodoItem("blah8", null, 0, null, false, false));
        TodoMainAdapter ta = new TodoMainAdapter(todoList);
        
        lv.setAdapter(ta);
        ta.notifyDataSetChanged();
    }
    
    public void storageInit() {
    	Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int season = getSeason(month);
		
		
		FileInputStream fis = null;
		try {
			fis = openFileInput(Constants.File_MainSettings);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+Constants.File_MainSettings);
		}

		if(fis == null) { // File doesn't exist. Must be a new user.
			FileOutputStream fos = null;
			try {
				fos = openFileOutput(Constants.File_MainSettings, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not create outputstream "+Constants.File_MainSettings);
			}
			
			JSONObject jMain = new JSONObject();
			
			
			int year = cal.get(Calendar.YEAR);
			try {
				period = year+" "+season;
				jMain.accumulate("period", period);
			} 
			catch (JSONException e) {
				Log.d(getString(R.string.app_name), "JSON err: "+e.getMessage());
			}
			
			try {
				fos.write(jMain.toString().getBytes());
			} catch (IOException e) {
				Log.d(getString(R.string.app_name), "Could not write to fos: "+e.getMessage());
			}
			
		}
		
		else {
			StringBuilder total = null;
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(fis));
				total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
				    total.append(line);
				}
				//fis.read(buf);
				//bis.read(buf);
			} catch (IOException e) {
				Log.d(getString(R.string.app_name), "Could not read buffer "+Constants.File_TranscriptList);
			}
			String transcriptJSONStr = total.toString();
			
			Log.d(getString(R.string.app_name), "json from file: "+transcriptJSONStr);
			
			JSONObject jMain = null;
			try {
				jMain = new JSONObject(transcriptJSONStr);
			} catch (JSONException e) {
				Log.d(getString(R.string.app_name), "not proper JSON: "+transcriptJSONStr);
			}
			
			if(jMain!=null) {
				try {
					period = jMain.getString("period");
				} catch (JSONException e) {	}
			}
		}
    }
    
    public class TodoMainAdapter extends BaseAdapter {

    	ArrayList<TodoItem> todoList;
    	LayoutInflater li;
    	
    	public TodoMainAdapter(ArrayList<TodoItem> todoList) {
    		this.todoList = todoList;
    		li = getLayoutInflater();
    	}
    	
		@Override
		public int getCount() {
			return todoList.size();
		}

		@Override
		public TodoItem getItem(int position) {
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
				v = getLayoutInflater().inflate(R.layout.todo_main_item, null);
			}
			
			
			((TextView)v.findViewById(R.id.text_todo_main_desc)).setText( getItem(position).getDesc() );
			return v;
		}
    	
    }
    
    public static int getSeason(int month) {
		switch( month ) {
		case Calendar.JANUARY:
			return Constants.Spring;
		case Calendar.FEBRUARY:
			return Constants.Spring;
		case Calendar.MARCH:
			return Constants.Spring;
		case Calendar.APRIL:
			return Constants.Spring;
		case Calendar.MAY:
			return Constants.Summer;
		case Calendar.JUNE:
			return Constants.Summer;
		case Calendar.JULY:
			return Constants.Summer;
		case Calendar.AUGUST:
			return Constants.Fall;
		case Calendar.SEPTEMBER:
			return Constants.Fall;
		case Calendar.OCTOBER:
			return Constants.Fall;
		case Calendar.NOVEMBER:
			return Constants.Fall;
		case Calendar.DECEMBER:
			return Constants.Winter;
		default:
			return -1;
		}
	}
}