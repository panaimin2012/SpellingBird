package com.pan.spellingbird;

import java.util.Locale;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Speaker implements OnInitListener {
	
	static private Speaker		_instance = null;
	private TextToSpeech		_tts = null;
	private boolean				_initialized = false;

	static public Speaker Instance() {
		if(_instance == null)
			_instance = new Speaker();
		return _instance;
	}
	
	private Speaker() {
		_tts = new TextToSpeech(SpellingBirdApp.instance(), this);
	}

	@Override
	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS) {
			_tts.setLanguage(Locale.US);
			_initialized = true;
		}
		else if (initStatus == TextToSpeech.ERROR) {
			SpellingBirdApp.instance().showToast("Text To Speech failed");
		}
	}
	
	public void speak(String s) {
		if(!_initialized)
			return;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		if(sp.getBoolean("PLAY_SOUND", true))
			_tts.speak(s, TextToSpeech.QUEUE_ADD, null);
	}

}
