package com.example.minorpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView angleTextView;
    private TextView idTextView;
    private ImageView imageView;
    private EditText displayId;
    private Button displayIdButton;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;

    long lastUpdatedTime = 0;
    float currentDegree = 0f;

    Socket mSocket;
    boolean flagToSendDataToServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        angleTextView = findViewById(R.id.xTextView);
        idTextView = findViewById(R.id.myDeviceId);
        imageView = findViewById(R.id.imageView);
        displayId = findViewById(R.id.displayId);
        displayIdButton = findViewById(R.id.displayIdButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SocketHandler.Companion.setSocket();
        SocketHandler.Companion.establishConnection();
        mSocket = SocketHandler.Companion.getSocket();

        displayIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayId.setVisibility(View.INVISIBLE);
                String displayID = displayId.getText().toString();
                if(displayID.length()>0)
                {

                    //try to send data here
                    mSocket.emit("request-display",mSocket.id(),displayID);

                    //temporary allow to send-message
                    flagToSendDataToServer = true;

                    ///on success id entry
                    Context context = getApplicationContext();
                    CharSequence text = "Connected";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
                displayId.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == accelerometerSensor){
            //System.out.println(isLastAccelerometerArrayCopied);
            System.arraycopy(sensorEvent.values,0,lastAccelerometer,0,sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        }else if(sensorEvent.sensor == magnetometerSensor){
            System.arraycopy(sensorEvent.values,0,lastMagnetometer,0,sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }

        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250){
            SensorManager.getRotationMatrix(rotationMatrix,null,lastAccelerometer,lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix,orientation);

            float azimuthalInRadian = orientation[0];
            float azimuthalInDegree = (float) Math.toDegrees(azimuthalInRadian);

            RotateAnimation rotateAnimation =
                    new RotateAnimation(currentDegree,-azimuthalInDegree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);

            currentDegree = -azimuthalInDegree;
            lastUpdatedTime = System.currentTimeMillis();

            int x = (int) azimuthalInDegree;
            if(x<0){
                angleTextView.setText(360+x +" degree ");
            }else{
                angleTextView.setText(x +" degree ");
            }

            idTextView.setText("Your ID : "+mSocket.id());

            if(flagToSendDataToServer){
                mSocket.emit("send-message",x,"");
                //Log.d("flagToSend : ",""+x);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this,accelerometerSensor,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener((SensorEventListener) this,magnetometerSensor,SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this,accelerometerSensor);
        sensorManager.unregisterListener((SensorEventListener) this,magnetometerSensor);
    }
}