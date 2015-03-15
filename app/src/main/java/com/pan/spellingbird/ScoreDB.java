package com.pan.spellingbird;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ScoreDB extends SQLiteOpenHelper {
	
	private static String	TAG = "Score";
	private static int		VERSION = 1;
	
	private static ScoreDB	_instance = null;
	private String			_today;

	// score table
	public static abstract class ScoreTable implements BaseColumns {
		public static final String TNAME	= "SCORE";
		public static final String C_DATE	= "dt";
		public static final String C_FAILED	= "failed";
		public static final String C_PASSED	= "passed";
	}
	
	public static ScoreDB instance() {
		if(_instance == null)
			_instance = new ScoreDB(SpellingBirdApp.instance());
		return _instance;
	}

	private ScoreDB(Context context) {
		super(context, TAG, null, VERSION);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		_today = "'" + sdf.format(new Date()) + "'" ;
	}
	
	private void initToday() {
		try {
			String sql = "INSERT INTO " + ScoreTable.TNAME + " ( "
				+ ScoreTable.C_DATE + ", " + ScoreTable.C_FAILED + ", " + ScoreTable.C_PASSED
				+ " ) VALUES ( " + _today + ", 0, 0 ) ";
			getWritableDatabase().execSQL(sql);
		}
		catch(Exception e) {
			LogDog.i(TAG, "Warning:" + e.getMessage());
		}
	}
	
	public void passed() {
		initToday();
		String sql = "UPDATE " + ScoreTable.TNAME
			+ " SET " + ScoreTable.C_PASSED + " = " + ScoreTable.C_PASSED + " + 1 "
			+ " WHERE " + ScoreTable.C_DATE + " = " + _today;
		LogDog.i(TAG, sql);
		getWritableDatabase().execSQL(sql);
	}

	public void failed(boolean increase) {
		initToday();
		String s = increase ? " + 1 " : " - 1 ";
		String sql = "UPDATE " + ScoreTable.TNAME
			+ " SET " + ScoreTable.C_FAILED + " = " + ScoreTable.C_FAILED + s
			+ " WHERE " + ScoreTable.C_DATE + " = " + _today;
		LogDog.i(TAG, sql);
		getWritableDatabase().execSQL(sql);
	}
	
	public Cursor queryScores() {
		SQLiteDatabase db = getReadableDatabase();
		String[] cs = { ScoreTable.C_DATE,
			ScoreTable._ID,
			ScoreTable.C_PASSED,
			ScoreTable.C_FAILED,
			ScoreTable.C_PASSED + " * 100 / ( " + ScoreTable.C_FAILED
				+ " + " + ScoreTable.C_PASSED + ") AS SCORE " };
		String order = ScoreTable.C_DATE + " DESC ";
		return db.query(false, ScoreTable.TNAME, cs, null, null, null, null, order, null, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + ScoreTable.TNAME + " ( "
			+ ScoreTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ ScoreTable.C_DATE + " TEXT, "
			+ ScoreTable.C_PASSED + " INTEGER, "
			+ ScoreTable.C_FAILED + " INTEGER ) ";
		db.execSQL(sql);
		LogDog.i(TAG, sql);
		sql = "CREATE UNIQUE INDEX " + ScoreTable.TNAME + "_ui ON " + ScoreTable.TNAME + "( "
			+ ScoreTable.C_DATE + " ) ";
		db.execSQL(sql);
		LogDog.i(TAG, sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
