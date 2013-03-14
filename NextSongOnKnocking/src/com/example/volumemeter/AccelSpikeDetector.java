package com.example.volumemeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Detects spikes in accelerometer data (only in z axis) and generates accelerometer events
 * the volatile spikeDetected boolean will be set when a spike is detected
 * @author tor
 *
 */
public class AccelSpikeDetector implements SensorEventListener{		
	
	public volatile boolean spikeDetected = false;
	private SensorManager mSensorManager;
	
	//Optimization parameters accelerometer
	final public float thresholdZ = 3; //Force needed to trigger event, G = 9.81 methinks
	final public float threshholdX = 5; 
	final public float threshholdY = 5;
	final public int updateFrequency = 50;
	
	
	//For high pass filter
	private float prevZVal = 0;
	private float currentZVal = 0;
	private float diffZ = 0;
	
	private float prevXVal = 0;
	private float currentXVal = 0;
	private float diffX = 0;
	
	private float prevYVal = 0;
	private float currentYVal = 0;
	private float diffY = 0;
		
	AccelSpikeDetector(SensorManager sm){
		mSensorManager = sm;
	}

	public void stopAccSensing(){
		mSensorManager.unregisterListener(this);
	}

	public void resumeAccSensing(){
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/updateFrequency);
	}

	public void onSensorChanged(SensorEvent event) {
		prevXVal = currentXVal;
		currentXVal = abs(event.values[0]); // X-axis
		diffX = currentXVal - prevXVal;
		
		prevYVal = currentYVal;
		currentYVal = abs(event.values[1]); // Y-axis
		diffY = currentYVal - prevYVal;		
		
		prevZVal = currentZVal;
		currentZVal = abs(event.values[2]); // Z-axis
		diffZ = currentZVal - prevZVal;

		//Z force must be above some limit, the other forces below some limit to filter out shaking motions
		if (currentZVal > prevZVal && diffZ > thresholdZ && diffX < threshholdX && diffY < threshholdY){
			accTapEvent();
		}

	}

	private void accTapEvent(){
		Log.d("acceltap","single tap event detected!");
		spikeDetected = true;
	}

	private float abs(float f) {
		if (f<0){
			return -f;
		}
		return f;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
