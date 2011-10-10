package com.binroot.gpa;

import misc.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GPAActivity extends Activity {
	
	Button transcriptBtn;
	Button homeworkBtn;
	
	public class MainButtonListener implements OnClickListener {

		int mode = -1;
		
		public MainButtonListener(int mode) {
			this.mode = mode;
		}
		
		@Override
		public void onClick(View v) {
			if(mode==Constants.ButtonHomework) {
				Log.d(getString(R.string.app_name), "Launching HomeworkActivity...");
			}
			else if(mode==Constants.ButtonTranscript) {
				Log.d(getString(R.string.app_name), "Launching TranscriptActivity...");
				startActivity(new Intent(GPAActivity.this, TranscriptActivity.class));
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
    }
}