package me.nolan.sensormouse;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends Activity implements SensorEventListener {

    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;


    //Upright position: (0,9,0)
    private float[] calibration = {0,9,0};

    private float calX = calibration[0];
    private float calY = calibration[1];
    private float calZ = calibration[2];

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;

    public Vibrator v;


    private Socket socket;
    private DataOutputStream out;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        String address = "172.19.108.188";
        int port = 5000;
        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException i) {
            System.out.println(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        } else {
            // fai! we dont have an accelerometer!
        }

        /*
        // string to read message from input
        String line = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")) {
            try {
                line = input.readLine();
                out.writeUTF(line);
            } catch (IOException i) {
                System.out.println(i);
            }
        }*/
    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = event.values[0];
        deltaY = event.values[1];
        deltaZ = event.values[2];
        if(out == null)
            return;
        try {
            if (deltaX > 0.5) {
                moveDot(Direction.LEFT, deltaX);
            } else if (deltaX < -0.5) {
                moveDot(Direction.RIGHT, deltaX);
            }

            if (deltaY > 2) {
                moveDot(Direction.DOWN, deltaY);
            } else if (deltaY < 0.5) {
                moveDot(Direction.UP, deltaY);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public void moveDot(Direction d) {
        Log.i("Info", d.toString());
    }

    public void moveDot(Direction d, float f) throws IOException {
        //set value up down left right and float is amplifier
        //set amplifier based on magnitude of acceleration
        float sensitivityModifier = Math.abs(f)*0.1f;
        out.writeUTF(d.toString());
        //somehow communicate to computer program
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }
}