package com.example.volumemeter;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.SensorManager;


abstract public class KnockDetector {
	
	/**
	 * Makes sure that accelerometer event and sound event only triggers a knock event 
	 * if and only if they happen at the same time or very close together in time
	 */
	
	private MainActivity parentActivity;	
	private TimerTask eventGen = null;
	private Timer mTimer = new Timer();
	private final int MaxTimeBetweenEvents = 25;
	private int period = MaxTimeBetweenEvents; 
	
	private AccelSpikeDetector mAccelSpikeDetector;
	private SoundKnockDetector mSoundKnockDetector = new SoundKnockDetector();
	private PatternRecognizer mPatt = new PatternRecognizer(this);
	
	abstract void knockDetected(int knockCount);

	private enum EventGenState_t {
		NoneSet,
		VolumSet,
		AccelSet
	} 
	
	KnockDetector(MainActivity parent){
		parentActivity = parent;
	}

	public void init(){
		mSoundKnockDetector.startVolKnockListener();
		mAccelSpikeDetector = new AccelSpikeDetector((SensorManager) parentActivity.getSystemService(Context.SENSOR_SERVICE));
		mAccelSpikeDetector.resumeAccSensing();
		eventGenerator();
	}
	
	public void pause(){
		mSoundKnockDetector.vol_stop();
		mAccelSpikeDetector.stopAccSensing();
	}
	
	public void resume(){
		mSoundKnockDetector.vol_start();
		mAccelSpikeDetector.resumeAccSensing();
	}
	
	private void eventGenerator(){
		eventGen = new TimerTask(){

			int nrTicks = 0;

			EventGenState_t state = EventGenState_t.NoneSet;

			@Override
			public void run() {

				switch(state){
				//None of the bools set
				case NoneSet:
					if		( mSoundKnockDetector.spikeDetected && !mAccelSpikeDetector.spikeDetected) state = EventGenState_t.VolumSet;
					else if	(!mSoundKnockDetector.spikeDetected &&  mAccelSpikeDetector.spikeDetected) state = EventGenState_t.AccelSet; 
					else if	( mSoundKnockDetector.spikeDetected &&  mAccelSpikeDetector.spikeDetected){

						mSoundKnockDetector.spikeDetected = false;
						mAccelSpikeDetector.spikeDetected = false;
						state =  EventGenState_t.NoneSet;
						//generate knock event
						mPatt.knockEvent();
					}

					

					nrTicks = 0;
					break;
					//volum set
				case VolumSet:
					if(mAccelSpikeDetector.spikeDetected){
						mSoundKnockDetector.spikeDetected = false;
						mAccelSpikeDetector.spikeDetected = false;
						state =  EventGenState_t.NoneSet;
						//generate knock event
						mPatt.knockEvent();
						break;
					}else{
						nrTicks+=1;
						if(nrTicks > period){
							nrTicks = 0;
							mSoundKnockDetector.spikeDetected = false;
							state = EventGenState_t.NoneSet;
						}
					}


					break;

					//accsel set
				case AccelSet:
					if(mSoundKnockDetector.spikeDetected){
						mSoundKnockDetector.spikeDetected = false;
						mAccelSpikeDetector.spikeDetected = false;
						state =  EventGenState_t.NoneSet;
						//generate knock event
						mPatt.knockEvent();
						break;
					}else{
						nrTicks+=1;
						if(nrTicks > period){
							nrTicks = 0;
							mAccelSpikeDetector.spikeDetected = false;
							state = EventGenState_t.NoneSet;
						}
					}						
					break;
				}
			}
		};
		mTimer.scheduleAtFixedRate(eventGen, 0, period); //start after 0 ms
	}
}
