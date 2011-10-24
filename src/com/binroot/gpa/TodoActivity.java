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
import java.util.zip.DataFormatException;

import misc.Constants;
import misc.TodoParser;
import misc.TranscriptParser;
import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TodoActivity extends Activity {

	String title = null;
	String period = null;
	ListView lv;
	Button date;
	int mYear;
	int mMonth;
	int mDay;
	static final int DATE_DIALOG_ID = 0;
	boolean weekly = false;
	TodoListAdapter ta;
	boolean editing = false;

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
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//Toast.makeText(TodoActivity.this, "selected "+arg2, Toast.LENGTH_LONG).show();
				
				addClickedDefaults(null, ta.getItem(arg2));
			}
		});
		
		
		
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

			Log.d(getString(R.string.app_name), "saving json: "+jMain.toString());
		}
		

		try {
			fis = openFileInput(filename);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+filename);
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
			Log.d(getString(R.string.app_name), "not proper JSON: "+e.getMessage());
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
							for(int j=0; j<7; j++) {
								days[j] = todoItemJSON.getBoolean("days"+(j+1));
							}

							boolean weekly = todoItemJSON.getBoolean("weekly");
							boolean done = todoItemJSON.getBoolean("done");

							TodoItem ti = new TodoItem(desc, due, priority, days, weekly, done);

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

		//todoList.add(new TodoItem("", null, 0, null, false));
		TodoParser.getInstance().setJSON(jMain);
		ta = new TodoListAdapter(todoList);
		lv.setAdapter(ta);

	} // end storageInit


	public class TodoListAdapter extends BaseAdapter {

		ArrayList<TodoItem> todoList;

		public TodoListAdapter(ArrayList<TodoItem> todoList) {
			this.todoList = todoList;
			notifyDataSetChanged();
		}
		
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			
			Log.d(getString(R.string.app_name), "sorting!");
			
			Collections.sort(todoList, new Comparator<TodoItem>() {
				@Override
				public int compare(TodoItem o1, TodoItem o2) {
					String date1Str = o1.getDue().toString();
					String date1Arr[] = date1Str.split(" ");
					String year1Str = date1Arr[5];
					String month1Str = monthToNum(date1Arr[1])+"";
					String day1Str = date1Arr[2];
					date1Str = year1Str+month1Str+day1Str;
					
					String date2Str = o2.getDue().toString();
					String date2Arr[] = date2Str.split(" ");
					String year2Str = date2Arr[5];
					String month2Str = monthToNum(date2Arr[1])+"";
					String day2Str = date2Arr[2];
					date2Str = year2Str+month2Str+day2Str;
					
					return date1Str.compareTo(date2Str);
				}
			});
			
		}
		
		public void addItem(TodoItem ti) {
			todoList.add(ti);
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView; 

			if(v==null) {
				v = getLayoutInflater().inflate(R.layout.todo_item, null);
			}
			
			((TextView)v.findViewById(R.id.text_todo_details)).setText(todoList.get(position).getDesc());
			
			String [] dateArr = getItem(position).getDue().toString().split(" ");
			String outDateStr = dateArr[1]+" "+dateArr[2];
			
			((TextView)v.findViewById(R.id.text_todo_item_every)).setText(outDateStr);

			
			Button cb = (Button)v.findViewById(R.id.button_check_todo);
			cb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TodoParser.getInstance().deleteItem(todoList.get(position));
					updateFile(TodoParser.getInstance().getJSON());
					
					todoList.remove(position);
					ta.notifyDataSetChanged();
					
					
				}
			}); 
				
			
			
			return v;
		}

	}

	public void logoClicked(View v) {
		TodoActivity.this.finish();
	}
	
	AlertDialog alert;
	
	public void addClickedDefaults(View v, final TodoItem ti) {
		// TODO: fix edit vs add
		if(v==null) {
			editing = true;
		}
		else {
			editing = false;
		}
		
		final View todoView = this.getLayoutInflater().inflate(R.layout.todo_popup, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(todoView);
		alert = builder.create();
		alert.show();

		date = (Button) todoView.findViewById(R.id.button_todo_popup_date);
		date.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		// get the current date
		final Calendar c = Calendar.getInstance();
		if(ti==null) {
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
		}
		else {
			Log.d("GPA date", ti.getDue().getYear()+", "+ti.getDue().getMonth()+", "+ti.getDue().getDay());
		
			
			Log.d("GPA date", ti.getDue()+"");
			String monthStr = ti.getDue().toString().split(" ")[1];
			String dayStr = ti.getDue().toString().split(" ")[2];
			String yearStr = ti.getDue().toString().split(" ")[5];
			
			int monthVal = monthToNum(monthStr);
			
			// black box bug fixing lol
			mYear = Integer.parseInt(yearStr) - 1900;
			mMonth = monthVal;
			mDay = Integer.parseInt(dayStr);
		}

		// display the current date (this method is below)
		updateDisplay();

		final Spinner sp = (Spinner) todoView.findViewById(R.id.spinner_todo_pop_rep);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.rep_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);

		final CharSequence [] days = 
		{"Sunday", "Monday", "Tuesday", "Wednesday", 
				"Thursday", "Friday", "Saturday"};
		final boolean daysPicked[] = {false, false, false, false, false, false, false};
		
		if(ti!=null) {
			if(ti.getWeekly()) {
				sp.setSelection(2);
			}
			
			boolean daily = true;
			for(int i=0; i<ti.getDays().length; i++) {
				if(ti.getDays()[i]==false) {
					daily = false;
				}
			}
			
			if(daily) {
				sp.setSelection(1);
			}
			
			if(!daily && !ti.getWeekly()) {
				sp.setSelection(0);
			}
			
			for(int i=0; i<7; i++) {
				daysPicked[i] = ti.getDays()[i];
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("( ");
			for(int i=0; i<daysPicked.length; i++) {
				if(daysPicked[i]) {
					sb.append(days[i]+" ");
				}
			}
			sb.append(")");
			((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setText(sb.toString());
			
			((RatingBar)todoView.findViewById(R.id.rating_todo_popup)).setRating(ti.getPriority());
			
			((EditText)todoView.findViewById(R.id.edit_todo_pop_desc)).setText(ti.getDesc());

		}
		
		
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.d(getString(R.string.app_name), "selected "+arg2);
				if(arg2==2) {
					AlertDialog.Builder builder = new AlertDialog.Builder(TodoActivity.this);
					
					builder.setMultiChoiceItems(days, daysPicked, new OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							Log.d(TodoActivity.this.getString(R.string.app_name), "selected "+which+", "+isChecked);
							daysPicked[which] = isChecked;
						}
					});
					builder.setTitle("Due every week on:");
					
					builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							weekly = true;
							((Button)todoView.findViewById(R.id.button_todo_pop_rep_days)).setVisibility(View.VISIBLE);
							((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setVisibility(View.VISIBLE);
							StringBuilder sb = new StringBuilder();
							sb.append("( ");
							for(int i=0; i<daysPicked.length; i++) {
								if(daysPicked[i]) {
									sb.append(days[i]+" ");
								}
							}
							sb.append(")");
							((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setText(sb.toString());
						}
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							sp.setSelection(0);
						}
					});
					
					AlertDialog alert = builder.create();
					alert.show();
				}
				else if(arg2==1) {
					weekly = true;
					for(int i=0; i<7; i++) {
						daysPicked[i] = true;
					}
					
					((Button)todoView.findViewById(R.id.button_todo_pop_rep_days)).setVisibility(View.GONE);
					((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setVisibility(View.VISIBLE);
					StringBuilder sb = new StringBuilder();
					sb.append("( ");
					for(int i=0; i<daysPicked.length; i++) {
						if(daysPicked[i]) {
							sb.append(days[i]+" ");
						}
					}
					sb.append(")");
					((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setText(sb.toString());
				}
				else {
					weekly = false;
					((Button)todoView.findViewById(R.id.button_todo_pop_rep_days)).setVisibility(View.GONE);
					((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setVisibility(View.GONE);
					
					for(int i=0; i<7; i++) {
						daysPicked[i] = false;
					}
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		Button editDays = (Button)todoView.findViewById(R.id.button_todo_pop_rep_days);
		editDays.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(TodoActivity.this);
				final CharSequence [] days = 
				{"Sunday", "Monday", "Tuesday", "Wednesday", 
						"Thursday", "Friday", "Saturday"};
				builder.setMultiChoiceItems(days, daysPicked, new OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						Log.d(TodoActivity.this.getString(R.string.app_name), "selected "+which+", "+isChecked);
						daysPicked[which] = isChecked;
					}
				});
				builder.setTitle("Due every week on:");
				
				builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						((Button)todoView.findViewById(R.id.button_todo_pop_rep_days)).setVisibility(View.VISIBLE);
						((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setVisibility(View.VISIBLE);
						StringBuilder sb = new StringBuilder();
						sb.append("( ");
						for(int i=0; i<daysPicked.length; i++) {
							if(daysPicked[i]) {
								sb.append(days[i]+" ");
							}
						}
						sb.append(")");
						((TextView)todoView.findViewById(R.id.text_todo_pop_rep_days)).setText(sb.toString());
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
		
		
		Button doneButton = (Button)todoView.findViewById(R.id.button_todo_popup_done);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String desc = 
					((EditText)todoView.findViewById(R.id.edit_todo_pop_desc)).getText().toString();
				
				Log.d(getString(R.string.app_name),"done clicked");
				Log.d(getString(R.string.app_name),"desc: "+desc);
				
				Date d = new Date(mYear, mMonth, mDay);
				RatingBar rb = ((RatingBar)todoView.findViewById(R.id.rating_todo_popup));
				int priority = (int)rb.getRating();
				boolean done = false;
				boolean days[] = daysPicked;
			
				if(!weekly) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					int dow = cal.get(Calendar.DAY_OF_WEEK);
					
					for(int i=0; i<7; i++) {
						if(i==dow) {
							days[i]=true;
						}
						else days[i]=false;
					}
				}
				
				
				TodoItem newti = new TodoItem(desc, d, priority, days, weekly, done);
				Log.d(getString(R.string.app_name), "ti1: "+newti);
				
				for(int i=0; i<ta.todoList.size(); i++) {
					Log.d(getString(R.string.app_name), "ti2: "+ta.todoList.get(i));
				}
				
				
				// TODO: fix bug: should not be able to add identical items
				
				if(!editing) {
					TodoParser.getInstance().addItem(newti);
					Log.d(getString(R.string.app_name), "jobject is "+TodoParser.getInstance().getJSON());
					updateFile(TodoParser.getInstance().getJSON());
					ta.addItem(newti);
					ta.notifyDataSetChanged();
				}
				else {
					//Toast.makeText(TodoActivity.this, "editing", Toast.LENGTH_LONG).show();
					
					TodoParser.getInstance().editItem(ti, newti);
					Log.d(getString(R.string.app_name), "jobject is "+TodoParser.getInstance().getJSON());
					updateFile(TodoParser.getInstance().getJSON());
					ta.todoList.remove(ti);
					ta.addItem(newti);
					ta.notifyDataSetChanged();
				}
				
				TodoActivity.this.alert.dismiss();
				
			}
			
			
		});
	}
	
	public void addClicked(View v) {
		addClickedDefaults(v, null);
		
	}
	
	public void updateFile(JSONObject jMain) {
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
	}

	// updates the date in the TextView
	private void updateDisplay() {
		
		Date d = new Date();
		d.setYear(mYear);
		d.setMonth(mMonth);
		d.setDate(mDay);
		
		String dStr = d.toString();
		
		String [] dArr = dStr.split(" ");
		
		date.setGravity(Gravity.LEFT);
		date.setGravity(Gravity.CENTER_VERTICAL);
		date.setText(dArr[0]+", "+dArr[1] +" "+dArr[2]);
	}
	

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}

	int monthToNum(String monthStr) {
		String months[] = {"Jan", "Feb", "Mar", "Apr", 
				"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		for(int i=0; i<12; i++)
			if(monthStr.equalsIgnoreCase(months[i])) return i;
		return -1;
	}
	
}
