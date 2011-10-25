package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import misc.Constants;
import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.binroot.gpa.HomeworkActivity.GridAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GPAActivity extends Activity {
	
	Button transcriptBtn;
	Button homeworkBtn;
	ListView lv;
	TodoMainAdapter ta;
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
        
        //storageInit2();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	storageInit2();
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
    
    public void storageInit2() {
    	lv = (ListView)findViewById(R.id.list_main_hw);
        ArrayList<TodoItem> todoMegaList = new ArrayList<TodoItem>();
        
        
        FileInputStream fis = null;
		try {
			fis = openFileInput(Constants.File_TranscriptList);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+Constants.File_TranscriptList);
		}

		if(fis == null) { // File doesn't exist. Must be a new user.
			// TODO: Error message
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
			
			ArrayList<String> classes = new ArrayList<String>();
			
			try {
				JSONArray jClasses = jMain.getJSONArray(period);
				for(int i=0; i<jClasses.length(); i++) {
					String title = jClasses.getJSONObject(i).getString("title");
					classes.add(title);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
			for(int i=0; i<classes.size(); i++) {
				// TODO: scan through class files to get todo items
				String filename = Constants.File_Todo+"_"+period+"_"+classes.get(i);
				
				// ---- here we go
				// we're going to let TodoItem know about its cooresponding title
				
				FileInputStream fis2 = null;
				try {
					fis2 = openFileInput(filename);
				} catch (FileNotFoundException e) {
					Log.d(getString(R.string.app_name), "Could not find file "+filename);
				}

				if(fis2 == null) { // File doesn't exist. Must be a new user.
					FileOutputStream fos = null;
					try {
						fos = openFileOutput(filename, Context.MODE_PRIVATE);
					} catch (FileNotFoundException e) {
						Log.d(getString(R.string.app_name), "Could not create outputstream "+filename);
					}

					JSONObject jMain2 = new JSONObject();
					JSONArray jTodos = new JSONArray();

					try {
						jMain2.accumulate("todo", jTodos);
					} 
					catch (JSONException e) {
						Log.d(getString(R.string.app_name), "JSON err: "+e.getMessage());
					}

					try {
						fos.write(jMain2.toString().getBytes());
					} catch (IOException e) {
						Log.d(getString(R.string.app_name), "Could not write to fos: "+e.getMessage());
					}

					Log.d(getString(R.string.app_name), "saving json: "+jMain2.toString());
				}
				

				try {
					fis2 = openFileInput(filename);
				} catch (FileNotFoundException e) {
					Log.d(getString(R.string.app_name), "Could not find file "+filename);
				}
				// open file
				StringBuilder total2 = null;
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(fis2));
					total2 = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
						total2.append(line);
					}

				} catch (IOException e) {
					Log.d(getString(R.string.app_name), "Could not read buffer "+Constants.File_TranscriptList);
				}
				String todosJSONStr = total2.toString();

				Log.d(getString(R.string.app_name), "json from file: "+todosJSONStr);

				JSONObject jMain2 = null;
				try {
					jMain2 = new JSONObject(todosJSONStr);
				} catch (JSONException e) {
					Log.d(getString(R.string.app_name), "not proper JSON: "+e.getMessage());
				}

				if(jMain2!=null) {

					JSONArray todosJSON = null;
					try {
						todosJSON = jMain2.getJSONArray("todo");
					} catch (JSONException e) {
						Log.d(getString(R.string.app_name), "JSON high key err: "+e.getMessage());
					}

					if(todosJSON != null) {
						for(int j=0; j<todosJSON.length(); j++) {
							JSONObject todoItemJSON = null;
							try {
								todoItemJSON = todosJSON.getJSONObject(j);
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
									for(int k=0; k<7; k++) {
										days[k] = todoItemJSON.getBoolean("days"+(k+1));
									}

									boolean weekly = todoItemJSON.getBoolean("weekly");
									boolean done = todoItemJSON.getBoolean("done");

									TodoItem ti = new TodoItem(desc, due, priority, days, weekly, done, classes.get(i));

									Log.d(getString(R.string.app_name), "adding todo item... "+ti);

									todoMegaList.add(ti);

								} catch (JSONException e) {
									Log.d(getString(R.string.app_name), "err: "+e.getMessage());
								}
							}
						}
					}
				} // end JSON not null
				
				// ---- done grabbing todolist
			}
			
		}

        ta = new TodoMainAdapter(todoMegaList);
        
        lv.setAdapter(ta);
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				SlidingDrawer sd = (SlidingDrawer)findViewById(R.id.slidingDrawer);
				sd.animateClose();
				
				Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						String classTitle = ta.todoList.get(arg2).getClassTitle();
						Intent i = new Intent(GPAActivity.this, TodoActivity.class);
						i.putExtra("title", classTitle);
						i.putExtra("period", period);
						startActivity(i);
					}
				}, 200);
				
				
			}
        	
		});
        ta.notifyDataSetChanged();
    }
    
    public class TodoMainAdapter extends BaseAdapter {

    	ArrayList<TodoItem> todoList;
    	LayoutInflater li;
    	
    	public TodoMainAdapter(ArrayList<TodoItem> todoList) {
    		this.todoList = todoList;
    		li = getLayoutInflater();
    	}
    	
    	@Override
    	public void notifyDataSetChanged() {
    		super.notifyDataSetChanged();
    		Collections.sort(todoList, new Comparator<TodoItem>() {

				@Override
				public int compare(TodoItem o1, TodoItem o2) {
					String date1Str = o1.getDue().toString();
					String date1Arr[] = date1Str.split(" ");
					String year1Str = date1Arr[5];
					String month1Str = monthToNum(date1Arr[1])+"";
					if(month1Str.length()==1) {
						month1Str = "0"+month1Str;
					}
					String day1Str = date1Arr[2];
					date1Str = year1Str+month1Str+day1Str;
					
					String date2Str = o2.getDue().toString();
					String date2Arr[] = date2Str.split(" ");
					String year2Str = date2Arr[5];
					String month2Str = monthToNum(date2Arr[1])+"";
					if(month2Str.length()==1) {
						month2Str = "0"+month2Str;
					}
					String day2Str = date2Arr[2];
					date2Str = year2Str+month2Str+day2Str;
					
					return date1Str.compareTo(date2Str);
				}
			});
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
			
			((TextView)v.findViewById(R.id.text_todo_main_title)).setText( getItem(position).getClassTitle() );
			((TextView)v.findViewById(R.id.text_todo_main_desc)).setText( getItem(position).getDesc() );
			String dueArr[] = getItem(position).getDue().toString().split(" ");
			
			((TextView)v.findViewById(R.id.text_todo_main_due)).setText(dueArr[0]+" "+dueArr[1]+" "+dueArr[2]);

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
    
    int monthToNum(String monthStr) {
		String months[] = {"Jan", "Feb", "Mar", "Apr", 
				"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		for(int i=0; i<12; i++)
			if(monthStr.equalsIgnoreCase(months[i])) return i;
		return -1;
	}
}