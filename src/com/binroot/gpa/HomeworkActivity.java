package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;

import misc.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class HomeworkActivity extends Activity {
	
	String period = null;
	
	GridView g;
	GridAdapter ga;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homework);
		
		Bundle extras = this.getIntent().getExtras();
		if(extras!=null) {
			period = extras.getString("period");
		}
		
		// TODO: storage init
		
		g = (GridView) findViewById(R.id.grid_hw);
		
		storageInit();
		
	} // end onCreate
	
	public void storageInit() {
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
			
			ga = new GridAdapter(classes);
			g.setAdapter(ga);
			g.setOnItemClickListener(ga);
		}
	}
	
	
	public class GridAdapter extends BaseAdapter implements OnItemClickListener{

		ArrayList<String> classes;
		
		public GridAdapter(ArrayList<String> classes) {
			this.classes = classes;
		}
		
		@Override
		public int getCount() {
			return classes.size();
		}

		@Override
		public String getItem(int position) {
			return classes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View v = convertView;
			
			if(v==null) {
				v = getLayoutInflater().inflate(R.layout.class_grid_item, null);
			}
			
			String title = classes.get(position);
			
			((TextView) v.findViewById(R.id.text_hw_grid_class)).setText(title);
			RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.relative_grid_class);
			
			int randCol = getHashColor(title);
			
			//int col = -1*Integer.parseInt(color, 16);
			
			
			Log.d(getString(R.string.app_name),"color: ");
			
			rl.setBackgroundColor(randCol);
		
			
			return v;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			String title = getItem(pos);
			Intent i = new Intent(HomeworkActivity.this, TodoActivity.class);
			i.putExtra("title", title);
			i.putExtra("period", period);
			startActivity(i);
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
	
	public void logoClicked(View v) {
		HomeworkActivity.this.finish();
	}
}
