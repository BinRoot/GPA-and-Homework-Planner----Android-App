package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import misc.Constants;
import misc.TranscriptParser;
import model.ClassItem;
import model.TodoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class TranscriptActivity extends Activity {

	ListView lv;
	TranscriptAdapter ta;
	JSONObject jMain = new JSONObject();
	
	int posGradeSelected = -1;
	AlertDialog alert;
	
	String period = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transcript);
		
		
		Bundle extras = this.getIntent().getExtras();

		if(extras != null ) {
			period = extras.getString("period");
		}

		lv = (ListView) findViewById(R.id.list_transcript);
		
//		lv.setOnLongClickListener(new OnLongClickListener() {
//			
//			public boolean onLongClick(View v) {
//				Toast.makeText(TranscriptActivity.this, "long clicked", Toast.LENGTH_LONG).show();
//				return true;
//			}
//		});
		
		storageInit(); // must call after lv is initialized
		
	} // end onCreate
	
	
	private void storageInit() {
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int season = getSeason(month);
		
		ArrayList<ClassItem> classList = new ArrayList<ClassItem>();
		
		/*
		 * Open file and see list of academic periods
		 * Find most recent academic period and display it.
		 * If no file found, auto name it and make new file.
		 */
		
		FileInputStream fis = null;
		try {
			fis = openFileInput(Constants.File_TranscriptList);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+Constants.File_TranscriptList);
		}

		if(fis == null) { // File doesn't exist. Must be a new user.
			FileOutputStream fos = null;
			try {
				fos = openFileOutput(Constants.File_TranscriptList, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				Log.d(getString(R.string.app_name), "Could not create outputstream "+Constants.File_TranscriptList);
			}
			
			jMain = new JSONObject();
			JSONArray jClasses = new JSONArray();
			
			int year = cal.get(Calendar.YEAR);
			try {
				period = year+" "+season;
				jMain.accumulate(period, jClasses);
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
		
		else { // File exists, display classes of most recent year to listview
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
			
			jMain = null;
			try {
				jMain = new JSONObject(transcriptJSONStr);
			} catch (JSONException e) {
				Log.d(getString(R.string.app_name), "not proper JSON: "+transcriptJSONStr);
			}
			
			if(jMain!=null) {
				
				// Only reassign to highkey if period isn't already defined
				if(period == null) {
					Iterator itr = jMain.keys();
					
					String highKey = "0000 0";
					while(itr.hasNext()) {
						String keyItem = (String)itr.next();
						if(keyItem.compareTo(highKey) > 0) {
							highKey = keyItem;
						}
					}
					period = highKey;
				}
				
				Log.d(getString(R.string.app_name), "found key: "+period);

				
				JSONArray classListJSON = null;
				try {
					classListJSON = jMain.getJSONArray(period);
				} catch (JSONException e) {
					Log.d(getString(R.string.app_name), "JSON high key err: "+e.getMessage());
				}
				
				if(classListJSON != null) {
					for(int i=0; i<classListJSON.length(); i++) {
						JSONObject classJSON = null;
						try {
							classJSON = classListJSON.getJSONObject(i);
						} catch (JSONException e) {
							Log.d(getString(R.string.app_name), "JSON class err: "+e.getMessage());
						}
						
						if(classJSON != null) {
							try {

								String title = classJSON.getString("title");
								int gradeGoal = classJSON.getInt("gradeGoal");
								int gradeMain = classJSON.getInt("gradeMain");
								int gradeGuar = classJSON.getInt("gradeGuar");
								String credits = classJSON.getString("credits");
								
								ClassItem ci = new ClassItem(title);
								ci.setGradeMain(gradeMain);
								ci.setGradeGuar(gradeGuar);
								ci.setGradeGoal(gradeGoal);
								ci.setCredits(Float.parseFloat(credits));
								
								Log.d(getString(R.string.app_name), "adding class... "+ci);
								
								classList.add(ci);
								
							} catch (JSONException e) {
								Log.d(getString(R.string.app_name), "err: "+e.getMessage());

							}
						}
					}
				}
				
			} // end JSON not null
			
		} // end else (file exists)
		
		Log.d(getString(R.string.app_name), "class list: "+classList+"");
		TranscriptParser.getInstance().setJSON(jMain);
		ta = new TranscriptAdapter(classList);
		lv.setAdapter(ta);
		//lv.setItemsCanFocus(true);
	}



	
	public class TranscriptAdapter extends BaseAdapter {

		ArrayList<ClassItem> classList;
		public boolean addMode = false;
		
		public TranscriptAdapter(ArrayList<ClassItem> classList) {
			this.classList = classList;
		}
		
		public void addItem(ClassItem ci) {
			classList.add(ci);
		}
		
		public void itemSelectMode(int position) {
			classList.get(position).setSelectMode(true);
			classList.get(position).setEditMode(false);
		}
		public void itemEditMode(int position) {
			classList.get(position).setEditMode(true);
			classList.get(position).setSelectMode(false);
		}
		
		public int getCount() {
			return classList.size()+1;
		}

		public ClassItem getItem(int position) {
			return (position<classList.size()) ? classList.get(position) : null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			
			View v = convertView;
			
			
			if(position >= classList.size()) {
				// [+ Add Class]
				if(v==null || ((Integer)v.getTag())!=1) {
					LayoutInflater inflater = getLayoutInflater();
					v = inflater.inflate(R.layout.addclass_item, null);
					v.setTag(1);
				}
				
				if(addMode==true) {
					((LinearLayout)v.findViewById(R.id.linear_addclass_edit)).setVisibility(View.VISIBLE);
					//((EditText)v.findViewById(R.id.edit_addclass_title)).requestFocus();
					EditText et = ((EditText)v.findViewById(R.id.edit_addclass_title));
					
					
					((Button)v.findViewById(R.id.button_addclass)).setVisibility(View.GONE);
					
				}
				else if(addMode==false) {
					((LinearLayout)v.findViewById(R.id.linear_addclass_edit)).setVisibility(View.GONE);
					((Button)v.findViewById(R.id.button_addclass)).setVisibility(View.VISIBLE);
				}
				
			}
			else { // class item
				if(v==null || ((Integer)v.getTag())!=0) {
					LayoutInflater inflater = getLayoutInflater();
					v = inflater.inflate(R.layout.transcript_item, null);
					v.setTag(0);
				}
				
				if(getItem(position).getSelectMode()==true) {
					((Button)v.findViewById(R.id.button_edit_classTitle)).setBackgroundDrawable(getResources().getDrawable(R.drawable.edit));
					((Button)v.findViewById(R.id.button_edit_classTitle)).setVisibility(View.VISIBLE);
					((TextView)v.findViewById(R.id.text_transcript_classTitle)).setVisibility(View.VISIBLE);
					((EditText)v.findViewById(R.id.edit_editclass_title)).setVisibility(View.GONE);
				}
				else if(getItem(position).getEditMode()==true) {
					((Button)v.findViewById(R.id.button_edit_classTitle)).setVisibility(View.VISIBLE);
					((Button)v.findViewById(R.id.button_edit_classTitle)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bullet_add));
					((TextView)v.findViewById(R.id.text_transcript_classTitle)).setVisibility(View.GONE);
					
					EditText et = ((EditText)v.findViewById(R.id.edit_editclass_title));
					
					et.setVisibility(View.VISIBLE);
					et.setText(classList.get(position).getTitle());

				}
				else {
					((Button)v.findViewById(R.id.button_edit_classTitle)).setVisibility(View.GONE);
					((TextView)v.findViewById(R.id.text_transcript_classTitle)).setVisibility(View.VISIBLE);
					((EditText)v.findViewById(R.id.edit_editclass_title)).setVisibility(View.GONE);
				}
				
				((TextView)v.findViewById(R.id.text_transcript_classTitle)).setText(classList.get(position).getTitle());
				((TextView)v.findViewById(R.id.text_transcript_gradeMain)).setText(numToGrade(classList.get(position).getGradeMain()));
				((TextView)v.findViewById(R.id.text_transcript_credits)).setText(classList.get(position).getCredits()+"");
				
				((TextView)v.findViewById(R.id.text_transcript_classTitle)).setOnLongClickListener(new OnLongClickListener() {
					
					public boolean onLongClick(View v) {
						//Toast.makeText(TranscriptActivity.this, "long clicked", Toast.LENGTH_LONG).show();
						
						AlertDialog.Builder builder = new AlertDialog.Builder(TranscriptActivity.this);
						builder.setMessage("Do you want to remove \""+ta.classList.get(position).getTitle()+"\" from this term?")
						       .setCancelable(false)
						       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						        	   
						        	   TranscriptParser.getInstance().deleteClass(period, ta.classList.get(position).getTitle());
						        	   
						        	   String filename = Constants.File_Todo+"_"+period+"_"+ta.classList.get(position).getTitle();
						        	   deleteFile(filename);

						        	   jMain = TranscriptParser.getInstance().getJSON();
						        	   updateFile();
						        	   
						        	   classList.remove(position);
						        	   
						        	   notifyDataSetChanged();
						        	   dialog.cancel();
						           }
						       })
						       .setNegativeButton("No", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						                dialog.cancel();
						           }
						       });
						AlertDialog alert = builder.create();
						alert.show();
						
						return true;
					}
				});
				
			}
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
	
	public void addButtonClicked(final View v) {
		ta.addMode = true;
		ta.notifyDataSetChanged();
		
		final LinearLayout vP = (LinearLayout) v.getParent();
		final EditText et = (EditText)vP.findViewById(R.id.edit_addclass_title);
		
		et.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView ttv, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(ttv.getWindowToken(), 0);
					
					Log.d(getString(R.string.app_name), "clicked done on keyboard");
					addButtonApplied(vP.findViewById(R.id.button_addclass_title));
					return true;
				}
				return false;
			}
		});
		
		Log.d(getString(R.string.app_name), "Add Button Clicked");
	}
	
	public void addButtonApplied(final View v) {
		
		
		
		Log.d(getString(R.string.app_name), "Add Button Applied");
		ta.addMode = false;
		
		LinearLayout vP = (LinearLayout) v.getParent().getParent();
		
		
		EditText et = (EditText)vP.findViewById(R.id.edit_addclass_title);
		String title = et.getText().toString();
		et.setText("");
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		
		boolean contains = false;
		for(int i=0; i<ta.classList.size(); i++) {
			if( title.equals( ta.classList.get(i).getTitle()) ) {
				contains = true;
			}
		}
		
		if(!contains) {
			ClassItem ci = new ClassItem(title);
			ci.setCredits(3);
			ci.setGradeMain(Constants.GradeAf);
			ci.setGradeGoal(Constants.GradeAp);
			ci.setGradeGuar(Constants.GradeCf);
			ta.addItem(ci);
			ta.notifyDataSetChanged();
			
			TranscriptParser.getInstance().addClass(period, 
					ci.getTitle(), 
					ci.getGradeMain(), 
					ci.getGradeGoal(),
					ci.getGradeGuar(), 
					ci.getCredits());
			jMain = TranscriptParser.getInstance().getJSON();
			updateFile();
		}// TODO remove edit text when contains is true
	}
	
	public void editButtonClicked(final View v) {
		
		RelativeLayout vP = (RelativeLayout)v.getParent();
		
		TextView tv = (TextView)vP.findViewById(R.id.text_transcript_classTitle);
		String title = tv.getText().toString();
		
		
		EditText etMain = (EditText)vP.findViewById(R.id.edit_editclass_title);
		etMain.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView ttv, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(ttv.getWindowToken(), 0);
					
					Log.d(getString(R.string.app_name), "clicked done on keyboard");
					editButtonClicked(v);
					
				}
				return true;
			}
		});
		
		
		for(int i=0; i<ta.classList.size(); i++) {
			
			if(ta.classList.get(i).getTitle().equals(title)) {
				if(ta.classList.get(i).getEditMode()==false)
					ta.itemEditMode(i);
				else {
					TranscriptParser.getInstance().editClassTitle(period, ta.classList.get(i).getTitle(), ((EditText)vP.findViewById(R.id.edit_editclass_title)).getText().toString());
					jMain = TranscriptParser.getInstance().getJSON();
					updateFile();
					
					// edit todo file as well
					String filename = Constants.File_Todo+"_"+period+"_"+title;

					FileInputStream fisTodo = null;
					try {
						fisTodo = openFileInput(filename);
					} catch (FileNotFoundException e) {
						Log.d(getString(R.string.app_name), "Could not find file "+filename);
					}
					
					// open file
					StringBuilder total = null;
					try {
						BufferedReader r = new BufferedReader(new InputStreamReader(fisTodo));
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

					JSONObject jMain2 = null;
					try {
						jMain2 = new JSONObject(todosJSONStr);
					} catch (JSONException e) {
						Log.d(getString(R.string.app_name), "not proper JSON: "+e.getMessage());
					}
					
					deleteFile(filename);
					// store jMain2 to file 
					
					String newFilename = Constants.File_Todo+"_"+period+"_"
						+((EditText)vP.findViewById(R.id.edit_editclass_title)).getText().toString();
					
					FileOutputStream fos2 = null;
					try {
						fos2 = openFileOutput(newFilename, Context.MODE_PRIVATE);
					} catch (FileNotFoundException e) {
						Log.d(getString(R.string.app_name), "Could not create outputstream "+filename);
					}

					try {
						fos2.write(jMain2.toString().getBytes());
					} catch (IOException e) {
						Log.d(getString(R.string.app_name), "Could not write to fos: "+e.getMessage());
					}
					
					
					// update list
					ta.classList.get(i).setTitle(((EditText)vP.findViewById(R.id.edit_editclass_title)).getText().toString());
					ta.itemSelectMode(i);
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etMain.getWindowToken(), 0);
				}
				break;
			}
		}
		
		ta.notifyDataSetChanged();
		
		Log.d(getString(R.string.app_name), "Edit Button Clicked");
	}
	
	public void classTitledClicked(View v) {
		Log.d(getString(R.string.app_name), "text clicked");
		//Toast.makeText(TranscriptActivity.this, "Clicked", Toast.LENGTH_LONG).show();
		
		TextView tv = (TextView)v;
		String classTitle = tv.getText().toString();
		
		Intent i = new Intent(TranscriptActivity.this, TodoActivity.class);
		i.putExtra("title", classTitle);
		i.putExtra("period", period);
		startActivity(i);
	}
	
	public void gradesClicked(View v) {
		Log.d(getString(R.string.app_name), "grades clicked");
		//Toast.makeText(TranscriptActivity.this, "Grades Clicked", Toast.LENGTH_LONG).show();
		
		View infV = getLayoutInflater().inflate(R.layout.grade_popup, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(infV);
		alert = builder.create();
		alert.show();

		
		LinearLayout ll = (LinearLayout) v.getParent();
		
		
		Gallery g1 = (Gallery)infV.findViewById(R.id.gallery_grades_predicted);
		Log.d(getString(R.string.app_name), "Gallery declared: "+g1);
		g1.setAdapter(new GalleryAdapter(TranscriptActivity.this));
		
		String mainGrade = ((TextView)ll.findViewById(R.id.text_transcript_gradeMain)).getText().toString();
		
		String title = ((TextView)ll.findViewById(R.id.text_transcript_classTitle)).getText().toString();

		int gInt =0;
		float credits = 3;
		for(ClassItem ci : ta.classList) {
			if(ci.getTitle().equals(title)) {
				gInt = ci.getGradeMain();
				credits = ci.getCredits();
				break;
			}
		}
		
		g1.setSelection(gInt);
		((EditText)infV.findViewById(R.id.edit_credits)).setText(credits+"");
		
		
		((TextView) infV.findViewById(R.id.text_grades_classTitle)).setText(title);
		
		for(int i=0; i<ta.classList.size(); i++) {	
			if(ta.classList.get(i).getTitle().equals(title)) {
				posGradeSelected = i;
				break;
			}
		}
		
	}
	
	
	public void backButtonClicked(View v) {
		TranscriptActivity.this.finish();
	}
	
	public void upButtonClicked(View v) {
		
		
	}
	
	public void doneGradesClicked(View v) {
		Log.d(getString(R.string.app_name), "Done Clicked");
		
		RelativeLayout rl = (RelativeLayout)v.getParent();
		Gallery g = (Gallery)rl.findViewById(R.id.gallery_grades_predicted);
		int pos = g.getSelectedItemPosition();
		Log.d(getString(R.string.app_name), "Saved with position "+pos);
		
		EditText et = (EditText) rl.findViewById(R.id.edit_credits);
		String credits = et.getText().toString();
		Log.d(getString(R.string.app_name), "Saved with credits "+credits);
		
		ta.classList.get(posGradeSelected).setGradeMain(pos);
		ta.classList.get(posGradeSelected).setCredits(Float.parseFloat(credits));
		ta.notifyDataSetChanged();
		
		
		
		TranscriptParser.getInstance().editGrade(period, 
				ta.classList.get(posGradeSelected).getTitle(), 
				ta.classList.get(posGradeSelected).getGradeMain(), 
				ta.classList.get(posGradeSelected).getGradeGoal(), 
				ta.classList.get(posGradeSelected).getGradeGuar(), 
				ta.classList.get(posGradeSelected).getCredits() );
		jMain = TranscriptParser.getInstance().getJSON();
		updateFile();
		
		
		alert.dismiss();
		posGradeSelected = -1;
	}
	
	public void creditsPlusClicked(View v) {
		LinearLayout ll = (LinearLayout) v.getParent();
		EditText et = (EditText) ll.findViewById(R.id.edit_credits);
		String credits = et.getText().toString();
		float cf = Float.parseFloat(credits);
		cf += 0.25;
		et.setText(cf + "");
	}
	
	public void creditsMinusClicked(View v) {
		LinearLayout ll = (LinearLayout) v.getParent();
		EditText et = (EditText) ll.findViewById(R.id.edit_credits);
		String credits = et.getText().toString();
		float cf = Float.parseFloat(credits);
		cf -= 0.25;
		et.setText(cf + "");
	}
	
	public class GalleryAdapter extends BaseAdapter {

		Context c;
		
		public GalleryAdapter(Context c) {
			this.c = c;
			Log.d(getString(R.string.app_name), "gallery adapter created");
		}
		
		private Integer[] gradeImageIds = {
				R.drawable.letter_ap,
				R.drawable.letter_a,
				R.drawable.letter_am,
				R.drawable.letter_bp,
				R.drawable.letter_bf,
				R.drawable.letter_bm,
				R.drawable.letter_cp,
				R.drawable.letter_cf,
				R.drawable.letter_cm,
				R.drawable.letter_dp,
				R.drawable.letter_df,
				R.drawable.letter_dm,
				R.drawable.letter_f,
		};
		
		public int getCount() {
			return gradeImageIds.length;
		}

		public Object getItem(int position) {
			return gradeImageIds[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			//View res = convertView;
			
			Log.d(getString(R.string.app_name), "in getview");
			
			ImageView imageView = new ImageView(c);
			imageView.setImageResource(gradeImageIds[position]);

			return imageView;
		}
	}
	
	public void updateFile() {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(Constants.File_TranscriptList, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not create outputstream "+Constants.File_TranscriptList);
		}
		try {
			fos.write(jMain.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String numToGrade(String num) {
		return numToGrade(Integer.parseInt(num));
	}
	public static String numToGrade(int num) {
		
		if(num == Constants.GradeAp) 
			return "A+";
		else if(num == Constants.GradeAf) 
			return "A";
		else if(num == Constants.GradeAm) 
			return "A-";
		else if(num == Constants.GradeBp) 
			return "B+";
		else if(num == Constants.GradeBf) 
			return "B";
		else if(num == Constants.GradeBm) 
			return "B-";
		else if(num == Constants.GradeCp) 
			return "C+";
		else if(num == Constants.GradeCf) 
			return "C";
		else if(num == Constants.GradeCm) 
			return "C-";
		else if(num == Constants.GradeDp) 
			return "D+";
		else if(num == Constants.GradeDf) 
			return "D";
		else if(num == Constants.GradeDm) 
			return "D-";
		else if(num == Constants.GradeF) 
			return "F";
		else return "A";
		
	}
	
}
