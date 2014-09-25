package com.weienlee.uropdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AccessGyroscope extends Activity implements SensorEventListener {
	//a TextView
	private TextView tv;
	//the Sensor Manager
	private SensorManager sManager;
	
	private BufferedWriter bufferedWriter;
	private OutputStreamWriter outputWriter;
	private PrintWriter printWriter;
	File file;
	FileOutputStream outputStream;
	
	EditText textData;
	Button startButton;
	Button stopButton;
	
	private float[] gyro = new float[3];
	boolean stop = false;
    boolean start = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_gyroscope);
        
        // file name
        textData = (EditText) findViewById(R.id.editText2);
        textData.setHint("Enter File Name Here: ");
        
        // start button
        startButton = (Button) findViewById(R.id.button1);
        startButton.setOnClickListener(new OnClickListener(){
            
            public void onClick(View v) {
                Context context = App.instance.getApplicationContext();
                // start recording data
                try {
                    file = new File(context.getFilesDir() + textData.getText() + ".txt");
                    file.createNewFile();
                    
                    outputStream = new FileOutputStream(file);
                    outputWriter = new OutputStreamWriter(outputStream);
                    bufferedWriter = new BufferedWriter(myOutWriter);
                    printWriter = new PrintWriter(bufferedWriter);
                    
                    Toast.makeText(getBaseContext(), "Starting recording...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    start = true;
                }
            }
            
        });
        
        //stop button
        stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new OnClickListener(){
           
            public void onClick(View v){
                //stop recording
                try {
                    stop = true;
                    Toast.makeText(getBaseContext(), "Finished recording", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //get the TextView from the layout file
        tv = (TextView) findViewById(R.id.tv);

        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    //when this Activity starts
    @Override
	protected void onResume()
	{
		super.onResume();
		/*register the sensor listener to listen to the gyroscope sensor, use the
		callbacks defined in this class, and gather the sensor information as quick
		as possible*/
		sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);
	}

  //When this Activity isn't visible anymore
	@Override
	protected void onStop()
	{
		//unregister the sensor listener
		sManager.unregisterListener(this);
		super.onStop();
	}
	
	private void save() {
        printWriter.write(gyro[1] + '\n');
    }
	
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
		
		tv.setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
				   "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
				   "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));

		if (start) {
		    gyro[0] = event.values[2];
	        gyro[1] = event.values[1];
	        gyro[2] = event.values[0];
	        for (int i = 0; i<1; i++){
	            if (!stop){
	                save();
	            }
	            else {
	                try {
	                    outputWriter.close();
	                } catch (IOException e){
	                    e.printStackTrace();
	                } catch (NullPointerException e){
	                    e.printStackTrace();
	                }
	                try {
	                    outputStream.close();
	                } catch (IOException e){
	                    e.printStackTrace();
	                } catch (NullPointerException e){
	                    e.printStackTrace();
	                }
	            }
	        }
		}
		
	}
	
	
}