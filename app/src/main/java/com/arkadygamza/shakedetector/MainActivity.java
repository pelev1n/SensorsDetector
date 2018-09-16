package com.arkadygamza.shakedetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    private final List<SensorPlotter> mPlotters = new ArrayList<>(3);

    private Observable<?> mShakeObservable;
    private Subscription mShakeSubscription;
    public String state="DEFAULT";
    public Map<String,Integer> increaseValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        increaseValue.put("X",0);
        increaseValue.put("Y",0);
        increaseValue.put("Z",0);

        EditText editValue = (EditText) findViewById(R.id.value_edit);
        Button btnX = (Button) findViewById(R.id.btn_x);
        Button btnY = (Button) findViewById(R.id.btn_y);
        Button btnZ = (Button) findViewById(R.id.btn_z);
        Button btnAll = (Button) findViewById(R.id.btn_all);
        Button btnCancel = (Button) findViewById(R.id.btn_cancel);

        setupPlotters();
        mShakeObservable = ShakeDetector.create(this);
        btnX.setOnClickListener(view -> {
            updateIncValue("X",editValue.getText().toString());
        });

        btnY.setOnClickListener(view -> {
            updateIncValue("Y",editValue.getText().toString());
        });

        btnZ.setOnClickListener(view -> {
            updateIncValue("Z",editValue.getText().toString());
        });

        btnAll.setOnClickListener(view -> {
            updateIncValue("X",editValue.getText().toString());
            updateIncValue("Y",editValue.getText().toString());
            updateIncValue("Z",editValue.getText().toString());
        });

        btnCancel.setOnClickListener(view -> {
            updateIncValue("X","0");
            updateIncValue("Y","0");
            updateIncValue("Z","0");
        });

    }

    public void updateIncValue(String line, String value) {
        increaseValue.put(line,Integer.valueOf(value));
        changeIncValue(increaseValue);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.line_x:
                state ="X";
                changeState(state);
                return true;

            case  R.id.line_y:
                state ="Y";
                changeState(state);
                return true;

            case R.id.line_z:
                state ="Z";
                changeState(state);
                return true;

            case R.id.line_default:
                state ="DEFAULT";
                changeState(state);
                return true;
            default:
                state ="DEFAULT";
                changeState(state);
                return true;
        }
    }

    public void changeState(String state) {
        mPlotters.get(0).setState(state);
        mPlotters.get(1).setState(state);
        mPlotters.get(2).setState(state);
    }

    public void changeIncValue(Map<String,Integer> value) {
        mPlotters.get(0).setIncValue(value);
        mPlotters.get(1).setIncValue(value);
        mPlotters.get(2).setIncValue(value);
    }

    private void setupPlotters() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> gravSensors = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        List<Sensor> accSensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> linearAccSensors = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);

        mPlotters.add(new SensorPlotter("GRAV", (GraphView) findViewById(R.id.graph1), SensorEventObservableFactory.createSensorEventObservable(gravSensors.get(0), sensorManager), state,increaseValue));
        mPlotters.add(new SensorPlotter("ACC", (GraphView) findViewById(R.id.graph2), SensorEventObservableFactory.createSensorEventObservable(accSensors.get(0), sensorManager), state,increaseValue));
        mPlotters.add(new SensorPlotter("LIN", (GraphView) findViewById(R.id.graph3), SensorEventObservableFactory.createSensorEventObservable(linearAccSensors.get(0), sensorManager), state,increaseValue));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.from(mPlotters).subscribe(SensorPlotter::onResume);
        mShakeSubscription = mShakeObservable.subscribe((object) -> Utils.beep());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Observable.from(mPlotters).subscribe(SensorPlotter::onPause);
        mShakeSubscription.unsubscribe();
    }
}
