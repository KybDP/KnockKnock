package com.example.volumemeter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;

import android.util.Log;

public class PatternRecognizer {
	
	final long minWaitTime_ms = 150; // The time-window when knocks will NOT be acknowledged
	final long waitWindow_ms = 500; // The time-window when knocks WILL be acknowledged
	
	private ScheduledFuture<?> timerFuture = null ;
	EventGenState_t state = EventGenState_t.Wait;
	ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
	KnockDetector p = null;
	private int detectedKnockCount = 0;
	
	PatternRecognizer(KnockDetector parent){
		p = parent;
	}
	
	private enum EventGenState_t {
		Wait,
		S1,
		S2, 
		S3,
		S4,
	} 
	
	

	Runnable waitTimer = new Runnable(){
		public void run() {
			timeOutEvent();			
		}
	};

	private void startTimer(long timeToWait){
		if (timerFuture != null){
			timerFuture.cancel(false);
		}
		timerFuture = mExecutor.schedule(waitTimer, timeToWait, TimeUnit.MILLISECONDS);
	}


	public void knockEvent(){
		Log.d("pattern rec","knock event "+ state);

		switch(state){
		case Wait:
			detectedKnockCount++;
			startTimer(minWaitTime_ms);
			state =  EventGenState_t.S1;
			break;
		case S1:
			//Do nothing, ignore knock
			break;
		case S2:
			detectedKnockCount++;
			timerFuture.cancel(false);
			startTimer(minWaitTime_ms);
			state = EventGenState_t.S3;
			break;
		case S3:
			//Do nothing, ignore knock
			break;
		case S4:
			timerFuture.cancel(false);
			p.knockDetected(++detectedKnockCount);
			detectedKnockCount = 0;
			state = EventGenState_t.Wait;
			break;
		default:
			Log.d("pattern rec","error 1");
			break;
		}
	}

	
	public void timeOutEvent(){
		Log.d("pattern rec","timeoutevent");
		switch(state){
		case Wait:
			Log.d("pattern rec","error 2");
			break;
		case S1:
			startTimer(waitWindow_ms);
			state = EventGenState_t.S2;
			break;
		case S2:
			detectedKnockCount = 0;
			state = EventGenState_t.Wait;
			break;
		case S3:
			startTimer(waitWindow_ms);
			state = EventGenState_t.S4;
			break;
		case S4:
			p.knockDetected(detectedKnockCount);
			detectedKnockCount = 0;
			state = EventGenState_t.Wait;
			break;
		default:
			Log.d("pattern rec","error 3");
			break;
		}
	}
}