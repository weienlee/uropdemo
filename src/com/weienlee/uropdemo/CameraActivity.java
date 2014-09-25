package com.weienlee.uropdemo;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraActivity extends Activity implements SensorEventListener{
	private FileWriter writer;
	private Camera myCamera;
	private MyCameraSurfaceView myCameraSurfaceView;
	private MediaRecorder mediaRecorder;
	private FrameLayout myCameraPreview;
	Button myButton;
	SurfaceHolder surfaceHolder;
	private TextView x_value;
	private TextView y_value;
	private TextView z_value;
	private SensorManager sManager; //the Sensor Manager
	
	boolean recording;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		recording = false;

		setContentView(R.layout.activity_camera);
		getActionBar().hide();
		
		// set up gyroscope
        //get the TextView from the layout file
		x_value = (TextView) findViewById(R.id.x_value);
		y_value = (TextView) findViewById(R.id.y_value);
		z_value = (TextView) findViewById(R.id.z_value);
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        
		//Get Camera for preview
		myCamera = getCameraInstance();
		if(myCamera == null){
			Toast.makeText(CameraActivity.this,
					"Fail to get Camera",
					Toast.LENGTH_LONG).show();
		}

		myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
		myCameraPreview = (FrameLayout)findViewById(R.id.cameraview);
		myCameraPreview.addView(myCameraSurfaceView);

		myButton = (Button)findViewById(R.id.mybutton);
		myButton.setOnClickListener(myButtonOnClickListener);
	}

	
	
	Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(recording){
				// stop recording gyroscope data
				try {
					writer.close();
				} catch (IOException e) {
				}
				
				// stop recording and release camera
				mediaRecorder.stop();  // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object

				myButton.setText("REC");
				recording = false;
			}else{

				//Release Camera before MediaRecorder start
				releaseCamera();

				if(!prepareMediaRecorder()){
					Toast.makeText(CameraActivity.this,
							"Fail in prepareMediaRecorder()!\n - Ended -",
							Toast.LENGTH_LONG).show();
					finish();
				}

				mediaRecorder.start();
				recording = true;
				myButton.setText("STOP");
			}
		}
	};

	private Camera getCameraInstance(){
		//			TODO Auto-generated method stub
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private boolean prepareMediaRecorder(){
		myCamera = getCameraInstance();
		mediaRecorder = new MediaRecorder();

		myCamera.unlock();
		mediaRecorder.setCamera(myCamera);

		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		mediaRecorder.setOutputFile("/sdcard/uropdemo.mp4");
		mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
		mediaRecorder.setMaxFileSize(50000000); // Set max file size 5M

		mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			return false;
		}
		return true;

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// stop recording
		if (mediaRecorder != null) {
			mediaRecorder.stop();
		}
		myButton.setText("REC");
		recording = false;
		
		releaseMediaRecorder();       // if you are using MediaRecorder, release it first
		releaseCamera();              // release the camera immediately on pause event
		myCameraPreview.removeView(myCameraSurfaceView);
	}

    @Override
	protected void onResume()
	{
		super.onResume();
		/*register the sensor listener to listen to the gyroscope sensor, use the
		callbacks defined in this class, and gather the sensor information as quick
		as possible*/
		sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_UI);
		
		try {
			writer = new FileWriter("/sdcard/uropdemo.txt",false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// setup camera and surface view again
		if (myCamera == null) {
			myCamera = getCameraInstance();
			if(myCamera == null){
				Toast.makeText(CameraActivity.this,
						"Fail to get Camera",
						Toast.LENGTH_LONG).show();
			}

			myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
			myCameraPreview.addView(myCameraSurfaceView);
		}
		
	}
	
  	@Override
  	protected void onStop()
  	{
  		//unregister the sensor listener
  		sManager.unregisterListener(this);
  		super.onStop();
  	}
    
	private void releaseMediaRecorder(){
		if (mediaRecorder != null) {
			mediaRecorder.reset();   // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			myCamera.lock();           // lock camera for later use
		}
	}

	private void releaseCamera(){
		if (myCamera != null){
			myCamera.release();        // release the camera for other applications
			myCamera = null;
		}
	}

	public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

		private SurfaceHolder mHolder;
		private Camera mCamera;

		public MyCameraSurfaceView(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int weight,
				int height) {
			// If your preview can change or rotate, take care of those events here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null){
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e){
				// ignore: tried to stop a non-existent preview
			}

			// make any resize, rotate or reformatting changes here

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			} catch (Exception e){
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// The Surface has been created, now tell the camera where to draw the preview.
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			/*this.getHolder().removeCallback(this);
			mCamera.stopPreview();
			mCamera.release();*/
		}
	}

	
	// gyroscope sensor
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		//Do nothing.
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		//if sensor is unreliable, return void
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
		{
			return;
		}
		//else it will output the Roll, Pitch and Yawn values
		float x = event.values[2];
		float y = event.values[1];
		float z = event.values[0];

		// round numbers
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);
		/*tv.setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
				   "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
				   "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));
		 */
		String x_str = formatter.format(x);
		String y_str = formatter.format(y);
		String z_str = formatter.format(z);
		
		x_value.setText(x_str);
		y_value.setText(y_str);
		z_value.setText(z_str);
		if (recording) {
			try {
				writer.write(x_str+"\t"+y_str+"\t"+z_str+"\t"+event.timestamp+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}