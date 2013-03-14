package com.example.volumemeter;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;


public class SoundKnockDetector {
	/**
	 * Triggers a volume event if the sound detected is the maximum volume possible on the device
	 */

	//VOLUM STUFF
	private static final String TAG = "VolRec";
	private MediaRecorder mRecorder = null;
	private Timer mTimer = new Timer();
	private TimerTask volListener = null;
	public volatile boolean spikeDetected = false;
	
	//VOLUM STUFF END

	//Starts sensor measurements
	public void startVolKnockListener(){

		this.vol_start();
		
		getAmplitude();
		

		volListener = new TimerTask(){

			
			int MAX_VAL = 32767;
			int THRESHOLD = 16000;		

			@Override
			public void run() {
				Integer amp = getAmplitude();

				if(amp>THRESHOLD){

					//					accelSet = true;
					Log.i("volumeEvent","set");
					spikeDetected = true;
					//					Log.d(TAG, "amplitude="+amp.toString());


				}

			}

		};
		mTimer.scheduleAtFixedRate(volListener, 0, 20); //start after 0 ms
	}

	//Stops sensor measurements
	public void stopVolKnockListener(){
		volListener.cancel();
	}

	public void vol_start() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();

			Integer audioMax = mRecorder.getAudioSourceMax ();

			Log.d(TAG, "audio src: " + audioMax);

			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //MIC

			//mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER); //MIC

			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // .AMR_NB);

			//TODO handle so that the audio file doesn't overflow memory
			mRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+"/both"); 


			//Use this two functions to reduce computation intensity
			//Testing didn't show any difference in file size after using these two functions
			//mRecorder.setAudioEncodingBitRate(800); //Set as low as possible
			//mRecorder.setAudioSamplingRate(8000); //Set as low as possible

			try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mRecorder.start();

		}
	}

	public void vol_stop() {
		if (mRecorder != null) {
			mRecorder.stop();       
			mRecorder.release();
			mRecorder = null;
		}
	}

	public int getAmplitude() {
		if (mRecorder != null)
			return  mRecorder.getMaxAmplitude(); ///2700.0);
		else
			return 0;

	}


}
