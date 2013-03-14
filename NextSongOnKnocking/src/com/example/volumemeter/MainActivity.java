/** By Geir Turtum and Torgeir Lien
 * 
 */

package com.example.volumemeter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;


public class MainActivity extends Activity {

	//The abstract class KnockDetector requires the implementation of void knockDetected(int) method
	KnockDetector mKnockDetector = new KnockDetector(this){
		@Override
		void knockDetected(int knockCount) {
			switch (knockCount){
			case 2:
				Log.d("media","next song");
				playNextSong();
				break;
			case 3:
				Log.d("media","pause/play");
				pausePlay();
				break;
			default:
				break;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mKnockDetector.init();
	}

	public void onBackPressed (){
		super.onBackPressed();
		mKnockDetector.pause();
	}

	public void onResume(){
		super.onResume();
		mKnockDetector.resume();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;

	}

	public void playNextSong(){

		long eventtime = SystemClock.uptimeMillis(); 		

		KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0); 
		handleMediaKeyEvent(downEvent);
		KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0); 
		handleMediaKeyEvent(upEvent);

	}

	public void pausePlay(){

		long eventtime = SystemClock.uptimeMillis(); 		

		KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0); 
		handleMediaKeyEvent(downEvent);
		KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0); 
		handleMediaKeyEvent(upEvent);


	}
	
	public void handleMediaKeyEvent(KeyEvent keyEvent) {

		//This method provided by user1937272@StackOverflow
		//http://stackoverflow.com/questions/12573442/is-google-play-music-hogging-all-action-media-button-intents
		//Sends the keyevent to app currently playing media

		/*
		 * Attempt to execute the following with reflection. 
		 * 
		 * [Code]
		 * IAudioService audioService = IAudioService.Stub.asInterface(b);
		 * audioService.dispatchMediaKeyEvent(keyEvent);
		 */

		try {

			// Get binder from ServiceManager.checkService(String)
			IBinder iBinder  = (IBinder) Class.forName("android.os.ServiceManager")
					.getDeclaredMethod("checkService",String.class)
					.invoke(null, Context.AUDIO_SERVICE);

			// get audioService from IAudioService.Stub.asInterface(IBinder)
			Object audioService  = Class.forName("android.media.IAudioService$Stub")
					.getDeclaredMethod("asInterface",IBinder.class)
					.invoke(null,iBinder);

			// Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
			Class.forName("android.media.IAudioService")
			.getDeclaredMethod("dispatchMediaKeyEvent",KeyEvent.class)
			.invoke(audioService, keyEvent);            

		}  catch (Exception e1) {
			e1.printStackTrace();
		}
	}



}
