package com.binroot.gpa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;

import misc.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	
	String period;
	
	EditText edit_ap;
	EditText edit_a;
	EditText edit_am;
	EditText edit_bp;
	EditText edit_b;
	EditText edit_bm;
	EditText edit_cp;
	EditText edit_c;
	EditText edit_cm;
	EditText edit_dp;
	EditText edit_d;
	EditText edit_dm;
	EditText edit_f;
	
	JSONObject jMainT;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		Bundle extras = this.getIntent().getExtras();
		if(extras!=null) {
			period = extras.getString("period");
		}
		
		edit_ap = (EditText) findViewById(R.id.edit_settings_ap);
		edit_a = (EditText) findViewById(R.id.edit_settings_a);
		edit_am = (EditText) findViewById(R.id.edit_settings_am);
		edit_bp = (EditText) findViewById(R.id.edit_settings_bp);
		edit_b = (EditText) findViewById(R.id.edit_settings_b);
		edit_bm = (EditText) findViewById(R.id.edit_settings_bm);
		edit_cp = (EditText) findViewById(R.id.edit_settings_cp);
		edit_c = (EditText) findViewById(R.id.edit_settings_c);
		edit_cm = (EditText) findViewById(R.id.edit_settings_cm);
		edit_dp = (EditText) findViewById(R.id.edit_settings_dp);
		edit_d = (EditText) findViewById(R.id.edit_settings_d);
		edit_dm = (EditText) findViewById(R.id.edit_settings_dm);
		edit_f = (EditText) findViewById(R.id.edit_settings_f);
		
	} // end onCreate
	
	public void onPause() {
		super.onPause();
		
		// TODO: save data
		saveData();
	}
	
	public void onResume() {
		super.onResume();
		storageInit();
	}
	
	public void saveData() {
		
		Iterator it = jMainT.keys();
		while(it.hasNext()) {
			String key = it.next().toString();
			if(key.startsWith("gpa_")) {
				it.remove();
			}
		}
		
		try {
			jMainT.accumulate("gpa_ap", edit_ap.getText() );
			jMainT.accumulate("gpa_a", edit_a.getText() );
			jMainT.accumulate("gpa_am", edit_am.getText() );
			jMainT.accumulate("gpa_bp", edit_bp.getText() );
			jMainT.accumulate("gpa_b", edit_b.getText() );
			jMainT.accumulate("gpa_bm", edit_bm.getText() );
			jMainT.accumulate("gpa_cp", edit_cp.getText() );
			jMainT.accumulate("gpa_c", edit_c.getText() );
			jMainT.accumulate("gpa_cm", edit_cm.getText() );
			jMainT.accumulate("gpa_dp", edit_dp.getText() );
			jMainT.accumulate("gpa_d", edit_d.getText() );
			jMainT.accumulate("gpa_dm", edit_dm.getText() );
			jMainT.accumulate("gpa_f", edit_f.getText() );
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(Constants.File_MainSettings, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not create outputstream "+Constants.File_MainSettings);
		}
		try {
			fos.write(jMainT.toString().getBytes());
		} catch (IOException e) {
			Log.d(getString(R.string.app_name), "Could not write to fos: "+e.getMessage());
		}
		
	}
	
	public void storageInit() {
		
		FileInputStream fis = null;
		try {
			fis = openFileInput(Constants.File_MainSettings);
		} catch (FileNotFoundException e) {
			Log.d(getString(R.string.app_name), "Could not find file "+Constants.File_MainSettings);
		}

		
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
			jMainT = jMain;
			try {
				//period = jMain.getString("period");
				edit_ap.setText( jMain.getString("gpa_ap") );
				edit_a.setText( jMain.getString("gpa_a") );
				edit_am.setText( jMain.getString("gpa_am") );
				edit_bp.setText( jMain.getString("gpa_bp") );
				edit_b.setText( jMain.getString("gpa_b") );
				edit_bm.setText( jMain.getString("gpa_bm") );
				edit_cp.setText( jMain.getString("gpa_cp") );
				edit_c.setText( jMain.getString("gpa_c") );
				edit_cm.setText( jMain.getString("gpa_cm") );
				edit_dp.setText( jMain.getString("gpa_dp") );
				edit_d.setText( jMain.getString("gpa_d") );
				edit_dm.setText( jMain.getString("gpa_dm") );
				edit_f.setText( jMain.getString("gpa_f") );
				
			} catch (JSONException e) {	}
		}
	}
	
	
	
	public void apclickedm(View v) {
		double val = Double.parseDouble( edit_ap.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_ap.setText(df.format(val));
	}
	public void apclickedp(View v) {
		double val = Double.parseDouble( edit_ap.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_ap.setText(df.format(val));
	}
	
	public void aclickedm(View v) {
		double val = Double.parseDouble( edit_a.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_a.setText(df.format(val));
	}
	public void aclickedp(View v) {
		double val = Double.parseDouble( edit_a.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_a.setText(df.format(val));
	}
	
	public void amclickedm(View v) {
		double val = Double.parseDouble( edit_am.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_am.setText(df.format(val));
	}
	public void amclickedp(View v) {
		double val = Double.parseDouble( edit_am.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_am.setText(df.format(val));
	}
	
	public void bpclickedm(View v) {
		double val = Double.parseDouble( edit_bp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_bp.setText(df.format(val));
	}
	public void bpclickedp(View v) {
		double val = Double.parseDouble( edit_bp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_bp.setText(df.format(val));
	}
	
	public void bclickedm(View v) {
		double val = Double.parseDouble( edit_b.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_b.setText(df.format(val));
	}
	public void bclickedp(View v) {
		double val = Double.parseDouble( edit_b.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_b.setText(df.format(val));
	}
	
	public void bmclickedm(View v) {
		double val = Double.parseDouble( edit_bm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_bm.setText(df.format(val));
	}
	public void bmclickedp(View v) {
		double val = Double.parseDouble( edit_bm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_bm.setText(df.format(val));
	}
	
	public void cpclickedm(View v) {
		double val = Double.parseDouble( edit_cp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_cp.setText(df.format(val));
	}
	public void cpclickedp(View v) {
		double val = Double.parseDouble( edit_cp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_cp.setText(df.format(val));
	}
	
	public void cclickedm(View v) {
		double val = Double.parseDouble( edit_c.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_c.setText(df.format(val));
	}
	public void cclickedp(View v) {
		double val = Double.parseDouble( edit_c.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_c.setText(df.format(val));
	}
	
	public void cmclickedm(View v) {
		double val = Double.parseDouble( edit_cm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_cm.setText(df.format(val));
	}
	public void cmclickedp(View v) {
		double val = Double.parseDouble( edit_cm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_cm.setText(df.format(val));
	}
	
	public void dpclickedm(View v) {
		double val = Double.parseDouble( edit_dp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_dp.setText(df.format(val));
	}
	public void dpclickedp(View v) {
		double val = Double.parseDouble( edit_dp.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_dp.setText(df.format(val));
	}
	
	public void dclickedm(View v) {
		double val = Double.parseDouble( edit_d.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_d.setText(df.format(val));
	}
	public void dclickedp(View v) {
		double val = Double.parseDouble( edit_d.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_d.setText(df.format(val));
	}
	
	public void dmclickedm(View v) {
		double val = Double.parseDouble( edit_dm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_dm.setText(df.format(val));
	}
	public void dmclickedp(View v) {
		double val = Double.parseDouble( edit_dm.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_dm.setText(df.format(val));
	}
	
	public void fclickedm(View v) {
		double val = Double.parseDouble( edit_f.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val-=0.1;
		edit_f.setText(df.format(val));
	}
	public void fclickedp(View v) {
		double val = Double.parseDouble( edit_f.getText().toString() );
		DecimalFormat df = new DecimalFormat("0.0##");
		val+=0.1;
		edit_f.setText(df.format(val));
	}
	
}
