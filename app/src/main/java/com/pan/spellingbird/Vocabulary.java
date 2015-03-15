package com.pan.spellingbird;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

public class Vocabulary extends SQLiteOpenHelper {
	
	static private String			TAG = "DB";
	
	static private Vocabulary		_instance = null;
	// version is vocabulary version, not sqlite database version
	// to upgrade, change
	// 1. _version here, so database file is copied
	// 2. execute
	//    PRAGMA user_version = {#_version}
	//    in the pre-prepared database file, to avoid onUpgrade
	static private final int		_version = 8;
	static private final String		DB_NAME = "vocabulary";
	
	private int						_book = -1;
	private String					_unit = "";
	private LinkedList<String>		_lastWords = new LinkedList<String>();
	private int						_lastBook;
	private String					_lastUnit;
	private String					_lastWord;
	
	// book table
	public static abstract class BooksTable implements BaseColumns {
		public static final String TNAME	= "BOOKS";
		public static final String C_TITLE	= "title";
	}
	
	// word table
	public static abstract class WordsTable implements BaseColumns {
		public static final String TNAME 	= "WORDS";
		public static final String C_BOOKID	= "bookid";
		public static final String C_UNIT 	= "unit";
		public static final String C_WORD 	= "word";
		public static final String C_FAILED	= "failed";
		public static final String C_PASSED	= "passed";
	}

	static public Vocabulary instance() {
		if(_instance == null) {
			SpellingBirdApp app = SpellingBirdApp.instance();
			_instance = new Vocabulary(app, _version);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(app);
			int currentVersion = sp.getInt("VOCABULARY_VERSION", 0);
			if(_version > currentVersion) {
				try {
					SQLiteDatabase db = _instance.getReadableDatabase();
					db.close();
					InputStream is = app.getAssets().open(DB_NAME);
					String file = app.getDatabasePath(DB_NAME).getAbsolutePath();
					OutputStream os = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer))>0) {
						os.write(buffer, 0, length);
					}
					os.flush();
					os.close();
					is.close();
					Editor editor = sp.edit();
					editor.putInt("VOCABULARY_VERSION", _version);
					editor.commit();
					LogDog.i(TAG, "version upgrade to " + _version);
				}
				catch(IOException e) {
					LogDog.i(TAG, "failed to upgrade version to " + _version);
				}
			}
		}
		return _instance;
	}
	
	private Vocabulary(Context context, int version) {
		super(context, DB_NAME, null, version);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		_book = sp.getInt("BOOK", -1);
		_unit = sp.getString("UNIT", "");
	}

	public void putBook(int book) {
		_book = book;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		Editor editor = sp.edit();
		editor.putInt("BOOK", _book);
		editor.commit();
		LogDog.i(TAG, "putBook:" + _book);
	}
	
	public void putUnits(String unit) {
		_unit = unit;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		Editor editor = sp.edit();
		editor.putString("UNIT", _unit);
		editor.commit();
		LogDog.i(TAG, "putUnit:" + _unit);
	}
	
	public Cursor queryBooks() {
		String[] columns = { BooksTable._ID, BooksTable.C_TITLE };
		return getReadableDatabase().query(
			true, BooksTable.TNAME, columns, null, null, null, null, null, null);
	}

	public Cursor queryUnits(int book) {
		SQLiteDatabase db = getReadableDatabase();
		String[] c = { WordsTable.C_UNIT };
		String where = WordsTable.C_BOOKID + " = " + book;
		return db.query(true, WordsTable.TNAME, c, where, null, null, null, null, null);
	}
	
	public String queryWord() {
		SQLiteDatabase db = getWritableDatabase();
		String[] columns = {
			WordsTable.C_BOOKID,
			WordsTable.C_UNIT,
			WordsTable.C_WORD };
		StringBuilder where = new StringBuilder();
		if(_book >= 0)
			where.append(WordsTable.C_BOOKID + " = " + _book);
		if(!_unit.isEmpty()) {
			if(!where.equals(""))
				where.append(" AND ");
			where.append(WordsTable.C_UNIT);
			where.append(" = '" + _unit + "'");
		}
		String orderBy = WordsTable.C_FAILED + " - " + WordsTable.C_PASSED + " DESC, "
			+ WordsTable.C_PASSED;
		Cursor c = db.query(
			WordsTable.TNAME,
			columns,
			where.equals("") ? null : where.toString(),
			null,
			null,
			null,
			orderBy);
		LogDog.i(TAG, "query " + _book + "," + _unit + "=" + c.getCount());
		c.moveToFirst();
		if(c.getCount() == 0 ) {
			LogDog.i(TAG, "redirect to all words");
			_book = -1;
			_unit = "";
			c.close();
			c = db.query(
				WordsTable.TNAME,
				columns,
				null,
				null,
				null,
				null,
				orderBy);
			c.moveToFirst();
		}
		String ret = c.getString(2);
		while(_lastWords.contains(ret) && !c.isAfterLast()) {
			c.moveToNext();
			ret = c.getString(2);
		}
		if(_lastWords.size() >= 3)
			_lastWords.removeLast();
		_lastWords.addFirst(ret);
		_lastWord = ret;
		_lastBook = c.getInt(0);
		_lastUnit = c.getString(1);
		return ret;
	}
	
	public void passed() {
		String sql = "UPDATE " + WordsTable.TNAME
			+ " SET " + WordsTable.C_PASSED + " = " + WordsTable.C_PASSED + " + 1 "
			+ " WHERE " + WordsTable.C_BOOKID + " = " + _lastBook + " "
			+ " AND " + WordsTable.C_UNIT + " = '" + _lastUnit + "'"
			+ " AND " + WordsTable.C_WORD + " = '" + _lastWord + "' ";
		getWritableDatabase().execSQL(sql);
		ScoreDB.instance().passed();
	}

	public void failed(boolean increase) {
		String s = increase ? " + 1 " : " - 1 ";
		String sql = "UPDATE " + WordsTable.TNAME
			+ " SET " + WordsTable.C_FAILED + " = " + WordsTable.C_FAILED + s
			+ " WHERE " + WordsTable.C_BOOKID + " = " + _lastBook + " "
			+ " AND " + WordsTable.C_UNIT + " = '" + _lastUnit + "'"
			+ " AND " + WordsTable.C_WORD + " = '" + _lastWord + "' ";
		getWritableDatabase().execSQL(sql);
		ScoreDB.instance().failed(increase);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
}
