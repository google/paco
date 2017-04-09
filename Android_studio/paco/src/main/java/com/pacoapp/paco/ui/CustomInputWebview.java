package com.pacoapp.paco.ui;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class CustomInputWebview extends WebView {

  public CustomInputWebview(Context context) {
    super(context);
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
    outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
    return inputConnection;
  }

//  @Override
//  public boolean onKeyUp(int keyCode, KeyEvent event) {
//    if (keyCode == KeyEvent.KEYCODE_ENTER) {
//      InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//      imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
//      return true;
//    } else {
//      return super.onKeyUp(keyCode, event);
//    }
//  }



}
