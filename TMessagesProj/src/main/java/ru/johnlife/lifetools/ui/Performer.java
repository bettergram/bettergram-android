package ru.johnlife.lifetools.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public abstract class Performer implements TextView.OnEditorActionListener, View.OnFocusChangeListener  {
	public abstract void perform();
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId != EditorInfo.IME_NULL) {
			perform();
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return true;
			} else {
				return false;
			}
		} 
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			perform();
		} 
	}
}