package com.pan.spellingbird;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class DocActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_doc);
		WebView webView = (WebView)findViewById(R.id.webView1);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String doc = extras.getString("DOC");
			String url = "file:///android_asset/";
			url += doc;
			url += ".html";
			webView.loadUrl(url);
			setTitle(doc);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preference, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
}
