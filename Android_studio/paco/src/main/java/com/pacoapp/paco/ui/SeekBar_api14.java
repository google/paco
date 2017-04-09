package com.pacoapp.paco.ui;

import android.widget.SeekBar;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.drawable.Drawable;

/**
 * SeekBar.getThumb() is not supported prior to SDK 16 - this class adds this functionality
 *
 * If Paco is pushed to SDK 16 (or newer) just remove this class and replace every SeekBar_api14  in
 * the code with the native SeekBar (behavior, code and function-names can stay the same)
 */



public class SeekBar_api14 extends SeekBar{
  Drawable myThumb;

  public interface OnSeekBarChangeListener {
    void onProgressChanged(SeekBar_api14 seekBar, int progress, boolean fromUser);
    void onStartTrackingTouch(SeekBar_api14 seekBar);
    void onStopTrackingTouch(SeekBar_api14 seekBar);
  }


  public class OnSeekBarChangeListener_api14 implements SeekBar.OnSeekBarChangeListener {
    SeekBar_api14 seekBar_api14;
    SeekBar_api14.OnSeekBarChangeListener listener_fu;
    OnSeekBarChangeListener_api14(SeekBar_api14 s, SeekBar_api14.OnSeekBarChangeListener l) {
      seekBar_api14 = s;
      listener_fu = l;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
      listener_fu.onProgressChanged(seekBar_api14, progress, fromUser);
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      listener_fu.onStartTrackingTouch(seekBar_api14);
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      listener_fu.onStopTrackingTouch(seekBar_api14);
    }
  }


  public void setOnSeekBarChangeListener(SeekBar_api14.OnSeekBarChangeListener l) {
    super.setOnSeekBarChangeListener(new OnSeekBarChangeListener_api14(this, l));
  }

  public SeekBar_api14(Context context) {
    super(context);
  }
  public SeekBar_api14(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  public SeekBar_api14(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }



  @Override
  public void setThumb(Drawable thumb) {
    super.setThumb(thumb);
    myThumb = thumb;
  }
  public Drawable getThumb() {
    return myThumb;
  }
}
