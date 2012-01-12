package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import misc.Constants;
import misc.TodoParser;
import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GPAActivity extends Activity {

	Button transcriptBtn;
	Button homeworkBtn;
	Button settingsBtn;
	ListView lv;
	TodoMainAdapter ta;
	String period = null;

	public class MainButtonListener implements OnClickListener {

		int mode = -1;

		public MainButtonListener(int mode) {
			this.mode = mode;
		}

		
		
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
			else if(mode==Constants.ButtonSettings) {
				Intent i = new Intent(GPAActivity.this, SettingsActivity.class);
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
		
		settingsBtn = (Button) findViewById(R.id.button_main_settings);
		settingsBtn.setOnClickListener(new MainButtonListener(Constants.ButtonSettings));

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
				
				jMain.accumulate("gpa_ap", 4.0);
				jMain.accumulate("gpa_a", 4.0);
				jMain.accumulate("gpa_am", 3.7);
				jMain.accumulate("gpa_bp", 3.3);
				jMain.accumulate("gpa_b", 3.0);
				jMain.accumulate("gpa_bm", 2.7);
				jMain.accumulate("gpa_cp", 2.3);
				jMain.accumulate("gpa_c", 2.0);
				jMain.accumulate("gpa_cm", 1.7);
				jMain.accumulate("gpa_dp", 1.3);
				jMain.accumulate("gpa_d", 1.0);
				jMain.accumulate("gpa_dm", 0.7);
				jMain.accumulate("gpa_f", 0.0);
				
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

				String filename = Constants.File_Todo+"_"+period+"_"+classes.get(i);

				// ---- here we go
				// we're going to let TodoItem know about its corresponding title

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

									//Tue Jun 22 13:07:00 GMT 1999

									/* This is a case of when bad design decisions 
									 *     come to haunt you back :(
									 */
									int year = Integer.parseInt(dueStr.split(" ")[5]);
									int month = monthToNum(dueStr.split(" ")[1]);
									int date = Integer.parseInt(dueStr.split(" ")[2]);
									// yuck!

									Log.d("cal", "> "+year+"-"+month+"-"+date);

									Date due = new Date();
									due.setYear(year-1900);
									due.setMonth(month);
									due.setDate(date);
									//GregorianCalendar due = new GregorianCalendar(year, month, day);

									//Date due = new Date(year, month, day);
									//									
									//									Log.d("cal", "< "+due.getYear()+"-"
									//											+due.getMonth()+"-"
									//											+due.getDate());
									//									Log.d("cal", "< "+due.get(GregorianCalendar.YEAR)+"-"
									//											+due.get(GregorianCalendar.MONTH)+"-"
									//											+due.get(GregorianCalendar.DAY_OF_MONTH)
									//											+"="+due.get(GregorianCalendar.DAY_OF_WEEK));




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




		ArrayList<TodoItem> thingsToAdd = new ArrayList<TodoItem>();
		ArrayList<TodoItem> thingsToDel = new ArrayList<TodoItem>();
		// scan through all todo items
		// if old, and weekly, create new item
		for(TodoItem tin : todoMegaList) {

			boolean [] megaDays = new boolean[14];
			for(int i=0; i<7; i++) {
				megaDays[i] = tin.getDays()[i];
				megaDays[i+7] = tin.getDays()[i];
			}

			Date dNow = Calendar.getInstance().getTime();
			Date dDue = tin.getDue();
			Log.d("cal", "now: "+dNow.toString()+" --- due: "+tin.getDue().toString());
			//if(dNow.after(dDue)) {

			long timeDiff = dNow.getTime() - dDue.getTime();
			
			
			// is older than 7 days
			if(timeDiff >= (7*24*60*60*1000)) {
				// TODO: delete this item!
				Log.d("cal", "item is older than 7 days... will delete: "+tin.getDesc());
				/*
				TodoParser.getInstance().deleteItem(tin);
				updateTodoFile(TodoParser.getInstance().getJSON(), tin.getClassTitle());
				*/
				thingsToDel.add(tin);
				
				//todoList.remove(position);
				//ta.notifyDataSetChanged();
				
			}
			else if (tin.getWeekly() && timeDiff<0) {
				boolean restartDays = false;
				int thingsAdded = 0;
				Log.d("cal", "item is younger than 7 days and repeats weekly...");
				
				for(int i=dNow.getDay(); i<14; i++) {
					Log.d("cal", "due at day "+i+"? --> "+megaDays[i]);
					if(megaDays[i]==true) {
						
						
						int oldi = i;
						if(restartDays) {
							i = 14+ i;
						}
						Log.d("cal", "on day "+i);
						
						
						// add this item with the new date
						int daysAway = i-dNow.getDay();
						int msAway = 24*60*60*1000*daysAway;
						long totalMS = dNow.getTime()+msAway;
						Date newDate = new Date(totalMS);
						newDate.setHours(0);
						newDate.setMinutes(0);
						newDate.setSeconds(0);
						TodoItem tin2 = new TodoItem(tin);
						tin2.setDue(newDate);

						// if todoMegaList doesn't already contain newly updated item,
						// then add it to the file

						boolean contains = containsTI(tin2, todoMegaList);
						//Log.d("cal","todoMegaList contains tin: "+contains);
						if(!contains) {
							Log.d("cal", "Adding new date "+newDate.toString());
							
							thingsToAdd.add(tin2);
							thingsAdded++;
						}
						else {
							Log.d("cal", "NOT Adding new date "+newDate.toString());
						}
						
						i = oldi;
					}
					
					if(i==13 && thingsAdded<2 && restartDays==false) {
						i=0;
						restartDays = true;
					}
					
					if(restartDays) {
						if(i>=dNow.getDay()) {
							break;
						}
						
					}
				}

			}


			//}

			/*
			String dueStr = tin.getDue().toString();
			String dueArr[] = dueStr.split(" ");

			String mStr = monthToNum(dueArr[1])+"";
			if(mStr.length()==1) mStr = "0"+mStr;

			String yStr = dueArr[5];
			String dStr = dueArr[2];*/

		} // end for


		ta = new TodoMainAdapter(todoMegaList);
		lv.setAdapter(ta);

		for(TodoItem tin : thingsToAdd) {
			
			String filename = Constants.File_Todo+"_"+period+"_"+tin.getClassTitle();
			
			FileInputStream fisT = null;
			try {
				fisT = openFileInput(filename);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not find file "+filename);
			}
			
			// open file
			StringBuilder total = null;
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(fisT));
				total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}

			} catch (IOException e) {
				Log.d(getString(R.string.app_name), "Could not read buffer "+Constants.File_Todo);
			}
			String todosJSONStr = total.toString();
			
			JSONObject jMainT = null;
			try {
				jMainT = new JSONObject(todosJSONStr);
			} catch (JSONException e) {
				Log.d(getString(R.string.app_name), "not proper JSON: "+e.getMessage());
			}
			
			TodoParser.getInstance().setJSON(jMainT);
			TodoParser.getInstance().addItem(tin);
			Log.d(getString(R.string.app_name), "jobject(t) is "+TodoParser.getInstance().getJSON());
			updateTodoFile(TodoParser.getInstance().getJSON(), tin.getClassTitle());
			ta.todoList.add(tin);
			ta.notifyDataSetChanged();
		}
		
		for(TodoItem tin : thingsToDel) {
			String filename = Constants.File_Todo+"_"+period+"_"+tin.getClassTitle();
			
			FileInputStream fisT = null;
			try {
				fisT = openFileInput(filename);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not find file "+filename);
			}
			
			// open file
			StringBuilder total = null;
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(fisT));
				total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}

			} catch (IOException e) {
				Log.d(getString(R.string.app_name), "Could not read buffer "+Constants.File_Todo);
			}
			String todosJSONStr = total.toString();
			
			JSONObject jMainT = null;
			try {
				jMainT = new JSONObject(todosJSONStr);
			} catch (JSONException e) {
				Log.d(getString(R.string.app_name), "not proper JSON: "+e.getMessage());
			}
			
			// TODO: bug might be here
			TodoParser.getInstance().setJSON(jMainT);
			TodoParser.getInstance().deleteItem(tin);
			Log.d(getString(R.string.app_name), "jobject(tt) is "+TodoParser.getInstance().getJSON());
			
			updateTodoFile(TodoParser.getInstance().getJSON(), tin.getClassTitle());
			ta.todoList.remove(tin);
			ta.notifyDataSetChanged();
		}

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				SlidingDrawer sd = (SlidingDrawer)findViewById(R.id.slidingDrawer);
				sd.animateClose();

				Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable() {

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
	
	static void deleteTodoItem(TodoItem ti) {
		
	}

	boolean containsTI(TodoItem tin, ArrayList<TodoItem> todoMegaList) {

		for( TodoItem tin2 : todoMegaList ) {

			if( 	tin2.getClassTitle().equals(tin.getClassTitle()) 
					&&	tin2.getDesc().equals(tin.getDesc())
					&&  Arrays.equals(tin2.getDays(), tin.getDays())
					&&	tin2.getWeekly()==tin.getWeekly()
					&&  tin2.getPriority()==tin.getPriority()
					&&	tin2.getDue().getYear()==tin.getDue().getYear()
					&&  tin2.getDue().getMonth()==tin.getDue().getMonth()
					&&  tin2.getDue().getDate()==tin.getDue().getDate() ) {
				return true;
			}
		}

		return false;
	}

	public JSONObject setUpJMainT(String title) {
		JSONObject jMain = new JSONObject();
		JSONArray jTodos = new JSONArray();

		try {
			jMain.accumulate("todo", jTodos);
		} 
		catch (JSONException e) {
			Log.d(getString(R.string.app_name), "JSON err: "+e.getMessage());
		}
		return jMain;
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

			Log.d("cal", "UPDATING!");
			((TextView)findViewById(R.id.text_main_due_drawer)).setText(getCount()+"");
			
			Collections.sort(todoList, new Comparator<TodoItem>() {
				
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

		public int getCount() {
			return todoList.size();
		}

		public TodoItem getItem(int position) {
			return todoList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v==null) {
				v = getLayoutInflater().inflate(R.layout.todo_main_item, null);
			}

			((TextView)v.findViewById(R.id.text_todo_main_title)).setText( getItem(position).getClassTitle() );
			((TextView)v.findViewById(R.id.text_todo_main_desc)).setText( getItem(position).getDesc() );
			
			/*((LinearLayout)v.findViewById(R.id.linear_todo_main))
				.setBackgroundColor(getHashColor(getItem(position).getClassTitle()));
			*/
			((TextView)v.findViewById(R.id.text_todo_main_title))
				.setTextColor(getHashColor(getItem(position).getClassTitle()));
			
			
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


	public void updateTodoFile(JSONObject jMain, String title) {
		String filename = Constants.File_Todo+"_"+period+"_"+title;
		FileOutputStream fos = null;
		try { 
			fos = openFileOutput(filename, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not create outputstream "+filename);
		}
		try {
			fos.write(jMain.toString().getBytes());
		} catch (IOException e) {
			Log.d(getString(R.string.app_name), "Could not write outputstream "+e.getMessage());
			e.printStackTrace();
		}
		
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getHashColor(String str) {
		String hashStr = str.hashCode()+"";
		
		hashStr += hashStr + hashStr + hashStr + hashStr + hashStr;
		
		String red = hashStr.substring(1, 3);
		String green = hashStr.substring(2, 4);
		String blue = hashStr.substring(4, 6);
		
		String blueStr = "ff"+red+green+blue;
		long n = Long.parseLong(blueStr, 16);
		int nn = (int) n;
		return nn;
	}
}