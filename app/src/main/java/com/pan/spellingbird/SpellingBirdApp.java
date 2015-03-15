package com.pan.spellingbird;

import android.app.Application;
import android.widget.Toast;

public class SpellingBirdApp extends Application {

	private static SpellingBirdApp	_instance;
	private static String			_name = "SpellingBird";

	@Override
	public void onCreate() {
		super.onCreate();
		_instance = this;
		LogDog.instance().init(this);
	}
	
	@Override
	public void onTerminate() {
		LogDog.instance().close();
	}

	public static SpellingBirdApp instance() {
		return _instance;
	}
	
	public static String name() {
		return _name;
	}
	
	public void showToast(String s) {
		if(_toast != null)
			_toast.cancel();
		_toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
		_toast.show();
	}
	
	Toast		_toast = null;
}
