package com.weienlee.uropdemo;

import java.io.FileOutputStream;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class WriteToFile extends Activity {
    
    public static void eventToString(SensorEvent event){
        
    }
    
    public static void writeStringAsFile(final String fileContents, String fileName) {
        
        Context context = App.instance.getApplicationContext();
        File file = new File(context.getFilesDir(), fileName);
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (IOException e) {
            Logger.logError(TAG, e);
        }
    }

    public static String readFileAsString(String fileName) {
        Context context = App.instance.getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir().getAbsolutePath(), fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            Logger.logError(TAG, e);
        } catch (IOException e) {
            Logger.logError(TAG, e);
        } 

        return stringBuilder.toString();
    }
    
}