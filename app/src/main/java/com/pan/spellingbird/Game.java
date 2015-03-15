package com.pan.spellingbird;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.GridLayout.Spec;
import android.view.View.OnLayoutChangeListener;
import android.os.Handler;
import android.preference.PreferenceManager;

public class Game implements OnLayoutChangeListener, View.OnClickListener, OnSharedPreferenceChangeListener {
	
	static private String	TAG = "GAME";
	private final int		DEFAULT_ROWS = 8;
	private final int		DEFAULT_COLUMNS = 5;
	
	static private Game		_instance = null;
	private int				_ROWS;
	private int				_COLUMNS;
	private int				_BACK_COLOR = Color.GRAY;
	private int				_BACK_COLOR_CLICKED = Color.LTGRAY;
	private final String	_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	
	private View 			_view;
	private GridLayout		_grid;
	private int				_unitWidth = 0;
	private int				_unitHeight = 0;
	private ArrayList<Button>	_buttons = new ArrayList<Button>();
	private ImageButton		_retry;
	private TextView		_display;

	private String			_word;
	private String			_wordSoFar;
	private int				_lastRow = -1;
	private int				_lastColumn = -1;
	private boolean			_inGame = false;
	
	private Handler			_handler = new Handler();
	final private int		_TIME = 5000;
	final private int		_RETRY_TIMER = 3000;
	private Runnable		_clearTextRunnable = new Runnable() {
		@Override
		public void run() {
			if(_wordSoFar.isEmpty()) {
				_display.setText(_wordSoFar);
			}
		}
	};
	
	static public Game Instance() {
		return _instance;
	}
	
	public String TargetWord() { return _word; }

	public Game(View v) {
		_instance = this;
		_view = v;
		_view.addOnLayoutChangeListener(this);
		_retry = (ImageButton)_view.findViewById(R.id.retry1);
		_retry.setOnClickListener(this);
		_grid = (GridLayout)_view.findViewById(R.id.grid);
		_display = (TextView)v.findViewById(R.id.game_word);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		sp.registerOnSharedPreferenceChangeListener(this);
		initView();
	}
	
	public void initView() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		try {
			String s = sp.getString("GAME_ROWS", "");
			if(s.isEmpty())
				_ROWS = DEFAULT_ROWS;
			else
				_ROWS = Integer.parseInt(s);
		} catch (Exception e) {
			_ROWS = DEFAULT_ROWS;
		}
		try {
			String s = sp.getString("GAME_COLUMNS", "");
			if(s.isEmpty())
				_COLUMNS = DEFAULT_COLUMNS;
			else
				_COLUMNS = Integer.parseInt(s);
		} catch (Exception e) {
			_COLUMNS = DEFAULT_COLUMNS;
		}
		String s = sp.getString("BACK_COLOR", "GRAY");
		if(s.equals("GRAY"))
			_BACK_COLOR_CLICKED = Color.LTGRAY;
		else if(s.equals("RED"))
			_BACK_COLOR_CLICKED = Color.RED;
		else if(s.equals("GREEN"))
			_BACK_COLOR_CLICKED = Color.GREEN;
		else if(s.equals("BLUE"))
			_BACK_COLOR_CLICKED = Color.BLUE;
		else if(s.equals("CYAN"))
			_BACK_COLOR_CLICKED = Color.CYAN;
		else if(s.equals("MAGENTA"))
			_BACK_COLOR_CLICKED = Color.MAGENTA;
		else if(s.equals("YELLOW"))
			_BACK_COLOR_CLICKED = Color.YELLOW;
		_BACK_COLOR = ((_BACK_COLOR_CLICKED & 0x00fcfcfc) / 4 * 3) | 0xff000000;
		_grid.removeAllViews();
		_grid.setRowCount(_ROWS);
		_grid.setColumnCount(_COLUMNS);
		for(int i = _buttons.size(); i < _ROWS * _COLUMNS; ++i)
			_buttons.add(new Button(_view.getContext()));
		LogDog.i(TAG, "grid size " + _buttons.size());
		for(int i = 0; i < _ROWS; ++i)
			for(int j = 0; j < _COLUMNS; ++j) {
				LogDog.i(TAG, "cell " + i + ", " + j);
				Button button = getButton(i, j);
				button.setBackgroundColor(_BACK_COLOR);
				button.setText("");
				button.setOnClickListener(this);
				Spec rowSpec = GridLayout.spec(i, 1);
				Spec colSpec = GridLayout.spec(j, 1);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
				params.setGravity(Gravity.CENTER | Gravity.FILL);
				_grid.addView(button, params);
			}
	}
	
	private Button getButton(int i, int j) {
		return _buttons.get(i * _COLUMNS + j);
	}
	
	@Override
	public void onClick(View v) {
		if(!_inGame) {
			Game.Instance().start();
			return;
		}
		if(v.getId() == R.id.retry1) {
			initGame(true);
			return;
		}
		int column = v.getLeft() / _unitWidth;
		int row = v.getTop() / _unitHeight;
		if(_lastRow < 0 || _lastColumn < 0) {
			_wordSoFar = "";
		}
		else if(!(_lastColumn == column && Math.abs(_lastRow - row) == 1)
			&& !(_lastRow == row && Math.abs(_lastColumn - column) == 1))
			return;
		_lastRow = row;
		_lastColumn = column;
		onButtonClicked((Button)v);
	}
	
	private void onButtonClicked(Button b) {
		String s = b.getText().toString();
		char ch = s.charAt(0);
		if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
			Speaker.Instance().speak(s);
		_wordSoFar += s;
		b.setBackgroundColor(_BACK_COLOR_CLICKED);
		_display.setText(_wordSoFar);
		if(!_word.startsWith(_wordSoFar)) {
			Speaker.Instance().speak(_word);
			_display.setTextColor(Color.RED);
			_display.setText(_word);
			_retry.setImageResource(R.drawable.ic_bad);
			_inGame = false;
			return;
		}
		if(_word.compareTo(_wordSoFar) == 0) {
			Speaker.Instance().speak(_word);
			_display.setTextColor(Color.GREEN);
			_retry.setImageResource(R.drawable.ic_good);
			Vocabulary.instance().failed(false);
			Vocabulary.instance().passed();
			_inGame = false;
		}
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		int width = right - left;
		int height = bottom - top - _display.getHeight();
		int unitWidth = width / _COLUMNS;
		int unitHeight = height / _ROWS;
		if(_unitWidth == unitWidth && _unitHeight == unitHeight)
			return;
		_unitWidth = width / _COLUMNS;
		_unitHeight = height / _ROWS;
		float fontSize = (float)(_unitHeight * 0.5);
		float scaledDensity = SpellingBirdApp.instance().getResources().getDisplayMetrics().scaledDensity;
	    fontSize /= scaledDensity;
	    LogDog.i(TAG, "font size:" + fontSize);
		for(int i = 0; i < _ROWS; ++i)
			for(int j = 0; j < _COLUMNS; ++j) {
				Button button = getButton(i, j);
				button.setHeight(_unitHeight);
				button.setWidth(_unitWidth);
				button.setTextSize(fontSize);
			}
	}
	
	public void start() {
		LogDog.i(TAG, "start game");
		_word = Vocabulary.instance().queryWord();
		HashSet<Integer> cells = new HashSet<Integer>();
		char[] cs = _word.toCharArray();
		int row = (int)(Math.random() * _ROWS);
		int col = (int)(Math.random() * _COLUMNS);
		for(int i = 0; i < cs.length; ++i) {
			cells.add(row * 10 + col);
			getButton(row, col).setText(cs, i, 1);
			int direction = (int)(Math.random() * 4);
			int originalDirection = direction;
			boolean found = false;
			do {
				if(direction == 0) { // East
					if(col < _COLUMNS - 1 && !cells.contains(row * 10 + col + 1)) {
						col += 1;
						found = true;
						break;
					} else
						direction = 1;
				}
				else if(direction == 1) { // South
					if(row < _ROWS - 1 && !cells.contains((row + 1) * 10 + col)) {
						found = true;
						row += 1;
						break;
					} else
						direction = 2;
				}
				else if(direction == 2) { // West
					if(col > 0 && !cells.contains(row * 10 + col - 1)) {
						found = true;
						col -= 1;
						break;
					} else
						direction = 3;
				}
				else if(direction == 3) { // North
					if(row > 0 && !cells.contains((row - 1) * 10 + col)) {
						found = true;
						row -= 1;
						break;
					} else
						direction = 0;
				}
			} while (direction != originalDirection);
			if(found)
				continue;
			LogDog.e(TAG, "Unable to initialize game, starting again");
			start();
			return;
		}
		// randomly set the other characters
		HashMap<Integer, String> bad = new HashMap<Integer, String>();
		for(Integer i: cells) {
			int r = i / 10;
			int c = i % 10;
			if(r > 0) {
				String s = "";
				if(bad.containsKey((r - 1) * 10 + c))
					s = bad.get((r - 1) * 10 + c);
				if(c > 0 && cells.contains(r * 10 + c - 1))
					s += getButton(r, c - 1).getText();
				if(c < _COLUMNS - 1 && cells.contains(r * 10 + c + 1))
					s += getButton(r, c + 1).getText();
				if(r < _ROWS + 1 && cells.contains((r + 1) * 10 + c))
					s += getButton(r + 1, c).getText();
				bad.put((r - 1) * 10 + c, s);
			}
			if(r < _ROWS - 1) {
				String s = "";
				if(bad.containsKey((r + 1) * 10 + c))
					s = bad.get((r + 1) * 10 + c);
				if(c > 0 && cells.contains(r * 10 + c - 1))
					s += getButton(r, c - 1).getText();
				if(c < _COLUMNS - 1 && cells.contains(r * 10 + c + 1))
					s += getButton(r, c + 1).getText();
				if(r > 0 && cells.contains((r - 1) * 10 + c))
					s += getButton(r - 1, c).getText();
				bad.put((r + 1) * 10 + c, s);
			}
			if(c > 0) {
				String s = "";
				if(bad.containsKey(r * 10 + c - 1))
					s = bad.get(r * 10 + c - 1);
				if(c < _COLUMNS - 1 && cells.contains(r * 10 + c + 1))
					s += getButton(r, c + 1).getText();
				if(r > 0 && cells.contains((r - 1) * 10 + c))
					s += getButton(r - 1, c).getText();
				if(r < _ROWS + 1 && cells.contains((r + 1) * 10 + c))
					s += getButton(r + 1, c).getText();
				bad.put(r * 10 + c - 1, s);
			}
			if(c < _COLUMNS - 1) {
				String s = "";
				if(bad.containsKey(r * 10 + c + 1))
					s = bad.get(r * 10 + c + 1);
				if(c > 0 && cells.contains(r * 10 + c - 1))
					s += getButton(r, c - 1).getText();
				if(r > 0 && cells.contains((r - 1) * 10 + c))
					s += getButton(r - 1, c).getText();
				if(r < _ROWS + 1 && cells.contains((r + 1) * 10 + c))
					s += getButton(r + 1, c).getText();
				bad.put(r * 10 + c + 1, s);
			}
		}
		for(int i = 0; i < _ROWS; ++i)
			for(int j = 0; j < _COLUMNS; ++j) {
				if(!cells.contains(i * 10 + j)) {
					int any = (int)(Math.random() * 26);
					String candidate = _ALPHABET.substring(any, any + 1);
					while(_word.startsWith(candidate)
						|| (bad.containsKey(i * 10 + j) && bad.get(i * 10 + j).indexOf(candidate) >= 0)) {
						any = (int)(Math.random() * 26);
						candidate = _ALPHABET.substring(any, any + 1);
					}
					getButton(i, j).setText(candidate);
				}
			}
		Vocabulary.instance().failed(true);
		initGame(false);
	}
	
	private void initGame(boolean retry) {
		_display.setText("");
		_display.setTextColor(Color.BLACK);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpellingBirdApp.instance());
		boolean sound = sp.getBoolean("RETRY_SOUND", true);
		boolean display = sp.getBoolean("RETRY_DISPLAY", false);
		if(!retry || display) {
			_display.setText(_word);
			_handler.postDelayed(_clearTextRunnable, retry ? _TIME : _RETRY_TIMER);
		}
		if(!retry || sound)
			Speaker.Instance().speak(_word);
		for(int i = 0; i < _ROWS; ++i)
			for(int j = 0; j < _COLUMNS; ++j)
				getButton(i, j).setBackgroundColor(_BACK_COLOR);
		_retry.setImageResource(R.drawable.ic_launcher);
		_wordSoFar = "";
		_lastRow = -1;
		_lastColumn = -1;
		_inGame = true;
		_view.postInvalidate();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("GAME_ROWS") || key.equals("GAME_COLUMNS") || key.equals("BACK_COLOR")) {
			LogDog.i(TAG, "Preference change:" + key);
			_inGame = false;
			initView();
		}
	}
}
