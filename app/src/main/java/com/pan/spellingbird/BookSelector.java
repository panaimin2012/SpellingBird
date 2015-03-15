package com.pan.spellingbird;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class BookSelector extends ExpandableListActivity {
	
	private int				_book;
	private String			_unit;
	private BookUnitAdapter	_adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_book);
		populateBooks();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		_book = sp.getInt("BOOK", -1);
		_unit = sp.getString("UNIT", "");
		_adapter = new BookUnitAdapter();
		setListAdapter(_adapter);
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int gp, int cp, long id) {
		_book = _books[gp]._id;
		_unit = _books[gp]._units[cp];
		Vocabulary.instance().putBook(_book);
		Vocabulary.instance().putUnits(_unit);
		_adapter.notifyDataSetChanged();
		return false;
	}
	
	private class Book
	{
		public int		_id;
		public String	_title;
		public String[]	_units;
		
		public Book(int id, String title) {
			_id = id;
			_title = title;
		}
	}
	
	private Book[]	_books;
	
	private void populateBooks() {
		Cursor c1 = Vocabulary.instance().queryBooks();
		_books = new Book[c1.getCount()];
		c1.moveToFirst();
		int i = 0;
		while(!c1.isAfterLast()) {
			int id = c1.getInt(c1.getColumnIndex(Vocabulary.BooksTable._ID));
			String title = c1.getString(c1.getColumnIndex(Vocabulary.BooksTable.C_TITLE)); 
			Book book = new Book(id, title);
			Cursor c2 = Vocabulary.instance().queryUnits(id);
			book._units = new String[c2.getCount()];
			c2.moveToFirst();
			int j = 0;
			while(!c2.isAfterLast()) {
				String unit = c2.getString(c2.getColumnIndex(Vocabulary.WordsTable.C_UNIT));
				book._units[j++] = unit;
				c2.moveToNext();
			}
			c2.close();
			_books[i++] = book;
			c1.moveToNext();
		}
		c1.close();
	}
	
	private class BookUnitAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {
			return _books.length;
		}

		@Override
		public int getChildrenCount(int gp) {
			return _books[gp]._units.length;
		}

		@Override
		public Object getGroup(int gp) {
			return _books[gp];
		}

		@Override
		public Object getChild(int gp, int cp) {
			return _books[gp]._units[cp];
		}

		@Override
		public long getGroupId(int gp) {
			return _books[gp]._id;
		}

		@Override
		public long getChildId(int gp, int cp) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int gp, boolean isExpanded,View v, ViewGroup parent) {
			if(v == null) {
				LayoutInflater inflater =
					(LayoutInflater)SpellingBirdApp.instance().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.layout_book_row, parent, false);
			}
			TextView bookId = (TextView)v.findViewById(R.id.bookid);
			bookId.setText(String.valueOf(_books[gp]._id));
			TextView booktitle = (TextView)v.findViewById(R.id.booktitle);
			booktitle.setText(_books[gp]._title);
			return v;
		}

		@Override
		public View getChildView(int gp, int cp, boolean isLastChild, View v, ViewGroup parent) {
			if(v == null) {
				LayoutInflater inflater =
					(LayoutInflater)SpellingBirdApp.instance().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.layout_unit_row, parent, false);
			}
			TextView text = (TextView)v.findViewById(R.id.unit);
			text.setTextColor(0xff000000);
			String unit = _books[gp]._units[cp];
			text.setText(unit);
			if(unit.equals(_unit))
				v.setBackgroundColor(Color.CYAN);
			else
				v.setBackgroundColor(Color.LTGRAY);
			return v;
		}

		@Override
		public boolean isChildSelectable(int gp, int cp) {
			return true;
		}
		
	}
	
}
