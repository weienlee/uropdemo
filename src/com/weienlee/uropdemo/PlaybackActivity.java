package com.weienlee.uropdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.annotation.SuppressLint;
import android.graphics.Color;


public class PlaybackActivity extends Activity {
	private VideoView video;
	private Button button;
	private BufferedReader reader;
	private TextView x_tv;
	private TextView y_tv;
	private TextView z_tv;
	private long currentTime;
	private boolean isPlaying;
	private Thread thread;
	private List<String[]> lines;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);
		// Show the Up button in the action bar.
		getActionBar().hide();
		
		// setup video and button
		video=(VideoView)findViewById(R.id.videoview);
		button = (Button)findViewById(R.id.playbackButton);
		button.setOnClickListener(buttonOnClickListener);
		
		lines = new ArrayList<String[]>();
		
		// check for recorded video and sensor data
		File videoFile = new File("/sdcard/uropdemo.mp4");
		File dataFile = new File("/sdcard/uropdemo.txt");
		if(videoFile.exists() && dataFile.exists()){
			// if it exists, read in sensor data to lines
			video.setVideoPath("/sdcard/uropdemo.mp4");	
		    try {
		    	String line;
		    	reader = new BufferedReader(new FileReader(dataFile));
		    	while ((line = reader.readLine()) != null){
		    		lines.add(line.split("\\t"));
		    	}
		    	reader.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		} else {
			// otherwise, exit activity
			Toast.makeText(PlaybackActivity.this,
					"Please record a video first",
					Toast.LENGTH_LONG).show();
			finish();
		}
		
		// setup value textviews
		x_tv = (TextView) findViewById(R.id.x_value);
		y_tv = (TextView) findViewById(R.id.y_value);
		z_tv = (TextView) findViewById(R.id.z_value);
	}
	
	
	// button listener
	Button.OnClickListener buttonOnClickListener = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			// not playing yet
			if (!isPlaying){
				button.setText("RESET");
				isPlaying = true;
				video.start();
				final Handler handler = new Handler(); 
				currentTime = Long.parseLong(lines.get(0)[3]);
				
				// separated thread to update the sensor data
				thread = new Thread(new Runnable() {		    	
					public void run() {
						for (int i=0; i<lines.size(); i++){
							// exit thread if we stop the video
							if (!isPlaying){return;}
							final String x_val = lines.get(i)[0];
							final String y_val = lines.get(i)[1];
							final String z_val = lines.get(i)[2];
							
							// use handler to modify view from thread
							handler.post(new Runnable(){
								@Override
								public void run() {
									x_tv.setText(x_val);
									y_tv.setText(y_val);
									z_tv.setText(z_val);
									
									// threshold: |y|>20
									// make y red if more than 20 degrees from horizontal
									if (Math.abs(Float.parseFloat(y_val)) > 20){
										y_tv.setTextColor(Color.RED);
									} else {
										y_tv.setTextColor(Color.BLACK);
									}
								}
							}); 
							// sync up sensor data by waiting amount of time
							// as indicated by the sensor data timestamps
							if (i>0) {
								Long newTime = Long.parseLong(lines.get(i)[3]);
								Long delay = newTime - currentTime;
								currentTime = newTime;
								try {
									// subtract 5 to allow for thread processing time
									Thread.sleep(Math.max(0,(long)(delay/(Math.pow(10,6))-5)));
								} catch (InterruptedException e) {
								}
							}
						} // end for
						
						// reset video if done with sensor data
						video.stopPlayback();
						isPlaying = false;
						handler.post(new Runnable(){
							@Override
							public void run() {
								button.setText("PLAY");
								video.setVideoPath("/sdcard/uropdemo.mp4");			
							}
						});
					}
				});
				
				// run thread on button click if video not yet playing
				thread.start();
			} else {
				// this stops and resets the video
				thread.interrupt();
				thread = null;
				video.stopPlayback();
				video.setVideoPath("/sdcard/uropdemo.mp4");
			    button.setText("PLAY");
			    isPlaying = false;
			}
		}
	};
}
