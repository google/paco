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
    outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_ACTION_DONE;
    return inputConnection;
  }
}
