package com.pan.spellingbird;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class Score extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_score);
		Cursor c = ScoreDB.instance().queryScores();
		String[] cs = { ScoreDB.ScoreTable.C_DATE,
			ScoreDB.ScoreTable.C_PASSED,
			ScoreDB.ScoreTable.C_FAILED,
			"SCORE"
		};
		int[] ts = { R.id.textView_date,
			R.id.textView_passed,
			R.id.textView_failed,
			R.id.textView_score
		};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
			SpellingBirdApp.instance(), R.layout.layout_score_row,
			c, cs, ts, 0);
		setListAdapter(adapter);
	}
}
