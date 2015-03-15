package com.pan.spellingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SpellingActivity extends Activity {
	
	private Game					_game = null;
	final private int				_TTS_REQUEST = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		View v = findViewById(R.id.RelativeLayout1);
		if(_game == null) {
			_game = new Game(v);
			// check if text to speech is available
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, _TTS_REQUEST);
			Vocabulary.instance();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == _TTS_REQUEST) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				Speaker.Instance();
			}
			else {
				SpellingBirdApp.instance().showToast("Installing text to speech");
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent().setClass(this, SettingsActivity.class);
			startActivity(i);
			return true;
		}
		else if (id == R.id.action_about) {
			Intent i = new Intent().setClass(this, DocActivity.class);
			i.putExtra("DOC", "about");
			startActivity(i);
			return true;
		}
		else if(id == R.id.action_book) {
			Intent i = new Intent().setClass(this, BookSelector.class);
			startActivity(i);
			return true;
		}
		else if(id == R.id.action_score) {
			Intent i = new Intent().setClass(this, Score.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
