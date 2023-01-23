package es.bbw.ch.level;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalTime;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Notification definitons
    private static final String CHANNEL_ID = "defaultChannel";
    private static final String CHANNEL_NAME = "Default Channel";
    private static final String CHANNEL_DESC = "some description";
    private NotificationManager notificationManager;

    //Sensor definitions
    private SensorManager sensorManager;
    private Sensor sensor;
    private final static int SAMPLING_RATE = 200000 ; // in microseconds

    //Field definitions
    TextView highScoreValue;
    TextView value;
    ConstraintLayout layout;

    //timer definition
    private LocalTime startTime = null;
    private LocalTime endTime = null;
    private String time;
    private Duration duration;
    private float diffInMillis;

    //score definition
    private String highScore = "0";
    private float savedHighScoreValue = 0;
    private double scoreDef = 0;
    SharedPreferences sharedPref;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.baseLayout);
        this.highScoreValue = findViewById(R.id.highScoreValue);
        this.sharedPref = getPreferences(MODE_PRIVATE);
        this.value = findViewById(R.id.value);

        if(savedInstanceState != null && savedInstanceState.containsKey("savedHighScoreValue")) {
            this.savedHighScoreValue = savedInstanceState.getFloat("savedHighScoreValue");
        } else {
            this.savedHighScoreValue = sharedPref.getFloat("savedHighScoreValue", this.savedHighScoreValue);
        }

        highScore = String.valueOf(savedHighScoreValue);
        highScoreValue.setText(Float.toString(savedHighScoreValue) + "s");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SAMPLING_RATE);



    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        if (Math.abs(sensorEvent.values[1]) > Math.abs(sensorEvent.values[0])){
            scoreDef = sensorEvent.values[1] * 90 / 9.81;
        } else {
            scoreDef = sensorEvent.values[0] * 90 / 9.81;
        }
        Log.d("score", ((int) Math.round(scoreDef)) + "");


        value.setText(String.valueOf((int) Math.round(scoreDef)) + "°");

        if (isPlanar((int) Math.round(scoreDef)) && startTime == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startTime = LocalTime.now();
            }
        }

        if (isPlanar((int) Math.round(scoreDef)) == false && startTime != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                endTime = LocalTime.now();
                duration = Duration.between(startTime, endTime);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    diffInMillis = duration.toMillis();
                    DecimalFormat df = new DecimalFormat("#.###");
                    time = df.format(diffInMillis / 1000);
                }
            }
            if (Float.parseFloat(time) > Float.parseFloat(highScore)) {
                highScore = time;
                highScoreValue.setText(highScore + "s");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("savedHighScoreValue", Float.parseFloat(highScore));
                editor.apply();
                sendNotification(highScore);
            }
            startTime = null;
            endTime = null;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void createNotificationChannel() {
        this.notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String time) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Congrats")
                .setContentText("You have reached a new Highscore! " + time + " seconds")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(0, builder.build());
    }

    private boolean isPlanar(int scoreDef){
        if(scoreDef == 0) {
            layout.setBackgroundColor(getResources().getColor(R.color.green, null));
            return true;
        }
        layout.setBackgroundColor(android.R.attr.colorBackground);
        return false;
    }


}
