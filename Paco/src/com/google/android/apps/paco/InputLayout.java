/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance  with the License.  
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.google.android.apps.paco;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;

public class InputLayout extends LinearLayout {

  private Input input;
  private View componentWithValue;
  private List<ChangeListener> inputChangeListeners;
  private TextView promptTextView;
  private File file;
  private Location location;

  // Choices that have been selected on a multiselect list.
  private List<Integer> checkedChoices = new ArrayList<Integer>();

  public InputLayout(ExperimentExecutor context, Input input) {
    super(context);
    this.input = input;
    setOrientation(LinearLayout.VERTICAL);
    promptTextView = getInputTextView();
    addView(promptTextView);
    componentWithValue = getInputResponseTypeView(input);
    inputChangeListeners = new ArrayList<ChangeListener>();
    setVisible(input.getConditional() == null || !input.getConditional());
  }

  // start location impl

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    super.onRestoreInstanceState(state);
    // TODO (bobevans): this is a super quick hack. Remove it soon. Same in
    // onSaveInstanceState.
    UserPreferences prefs = new UserPreferences(getContext());
    String filePath = prefs.getPhotoAddress();
    if (filePath != null) {
      file = new File(filePath);
      renderPhotoButton(input);
      prefs.clearPhotoAddress();
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable saveState = super.onSaveInstanceState();
    if (file != null) {
      UserPreferences prefs = new UserPreferences(getContext());
      prefs.setPhotoAddress(file.getAbsolutePath());
    }
    return saveState;
  }

  void setLocation(Location location) {
    this.location = location;
    String latLonStr = "Retrieving";
    if (location != null) {
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      String latStr = Double.toString(latitude);
      String lonStr = Double.toString(longitude);
      latLonStr = latStr.substring(0, Math.min(10, latStr.length())) + ","
          + lonStr.substring(0, Math.min(10, lonStr.length()));
    }
    ((TextView) componentWithValue).setText(latLonStr);
  }

  // end location impl

  public void addChangeListener(ChangeListener listener) {
    inputChangeListeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    inputChangeListeners.remove(listener);
  }

  /**
   * TODO (bobevans) make this handle other types as well
   * 
   * @return
   */
  public String getValue() {
    if (!isVisible()) {
      return null;
    }
    if (input.getResponseType().equals(Input.OPEN_TEXT)) {
      return getOpenTextValue();
    } else if (input.getResponseType().equals(Input.LIKERT)) {
      return getLikertValue();
    } else if (input.getResponseType().equals(Input.LIST)) {
      return getListValue();
    } else if (input.getResponseType().equals(Input.LOCATION)) {
      return getLocationValue();
    } else if (input.getResponseType().equals(Input.NUMBER)) {
      return getNumberValue();
    } else if (input.getResponseType().equals(Input.PHOTO)) {
      return getPhotoValue();
    }
    return null;
  }

  public Class getResponseType() {
    if (input.getResponseType().equals(Input.OPEN_TEXT)) {
      return String.class;
    } else if (input.getResponseType().equals(Input.LIKERT)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input.LIST)) {
      if (input.isMultiselect()) {
        return String.class;
      } else {
        return Integer.class;
      }
    } else if (input.getResponseType().equals(Input.NUMBER)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input.LOCATION)) {
      return String.class;// GeoPoint.class;
    } else if (input.getResponseType().equals(Input.ACTIVITY)) {
      return String.class;
    } else if (input.getResponseType().equals(Input.PHOTO)) {
      return Bitmap.class;
    } else if (input.getResponseType().equals(Input.SOUND)) {
      return SoundPool.class; // TODO (bobevans): is this really a good idea as
                              // the storage type? probably not.
    }
    return Object.class;
  }

  public String getInputName() {
    return input.getName();
  }

  private String getPhotoValue() {
    // Load data from this.file if it is non-null
    // Base64 encode the data and return it
    if (file != null) {
      Bitmap bitmap = decodeFile(file);
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      BufferedOutputStream out = new BufferedOutputStream(bytesOut);
      bitmap.compress(CompressFormat.JPEG, 50, out);
      return Base64.encodeToString(bytesOut.toByteArray(), Base64.DEFAULT);
    }
    return "No File found for the photo";
  }

  private final int IMAGE_MAX_SIZE = 600;

  private Bitmap decodeFile(File f) {
    Bitmap b = null;
    try {
      // Decode image size
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(f), null, o);
      int scale = 1;
      if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
        scale = (int) Math.pow(2,
            (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
      }

      // Decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
    } catch (FileNotFoundException e) {
      Toast.makeText(getContext(), "Cannot find image file from camera!", Toast.LENGTH_LONG);
    }
    return b;
  }

  private String getLocationValue() {
    if (location != null) {
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      return Double.toString(latitude) + "," + Double.toString(longitude);
    }
    return "Unknown";
  }

  private String getLikertValue() {
    RadioGroup radioGroup = (RadioGroup) componentWithValue;
    int id = radioGroup.getCheckedRadioButtonId();
    if (id == -1) {
      return null;
    }
    switch (id) {
    case R.id.rb1:
      id = 1;
      break;
    case R.id.rb2:
      id = 2;
      break;
    case R.id.rb3:
      id = 3;
      break;
    case R.id.rb4:
      id = 4;
      break;
    case R.id.rb5:
      id = 5;
      break;
    case R.id.rb6:
      id = 6;
      break;
    case R.id.rb7:
      id = 7;
      break;
    case R.id.rb8:
      id = 8;
      break;
    case R.id.rb9:
      id = 9;
      break;
    case R.id.rb10:
      id = 10;
      break;
    default:
      throw new IllegalArgumentException("impossible");
    }
    return Integer.toString(id);
  }

  private String getListValue() {
    if (!input.isMultiselect()) {
      return Integer.toString(((Spinner) componentWithValue).getSelectedItemPosition() + 1);
    }
    return getMultiSelectListValue();
  }

  private String getMultiSelectListValue() {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Integer choice : checkedChoices) {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      buf.append(choice);
    }
    return buf.toString();
  }

  private String getOpenTextValue() {
    return ((EditText) componentWithValue).getText().toString();
  }

  private String getNumberValue() {
    return ((EditText) componentWithValue).getText().toString();
  }

  public Input getInput() {
    return input;
  }

  private View getInputResponseTypeView(Input input) {
    String questionType = input.getResponseType();
    if (questionType.equals(Input.OPEN_TEXT)) {
      return renderOpenText();
    } else if (questionType.equals(Input.LIKERT)) {
      return renderLikert(input);
    } else if (questionType.equals(Input.LIST)) {
      return renderList(input);
    } else if (questionType.equals(Input.LOCATION)) {
      return renderLocation(input);
    } else if (questionType.equals(Input.NUMBER)) {
      return renderNumber(input);
    } else if (questionType.equals(Input.PHOTO)) {
      return renderPhotoButton(input);
    }
    return null;
  }

  private View renderPhotoButton(Input input2) {
    View photoInputView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.photo_input, this, true);
    Button cameraButton = (Button) findViewById(R.id.CameraButton);
    ImageView photoView = (ImageView) findViewById(R.id.CameraPreviewImage);
    if (file != null) {
      photoView.setImageBitmap(decodeFile(file));
    }
    cameraButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          startCameraForResult();
        } catch (IOException e) {
          e.printStackTrace();
          new AlertDialog.Builder(getContext()).setCancelable(true).setTitle("Cannot Open Camera")
              .setMessage("Error: \n" + e.getMessage()).setNegativeButton("OK", null).create().show();
        }
      }
    });
    return photoInputView;
  }

  private void startCameraForResult() throws IOException {
    Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
    String dateString = createTimeStamp();
    file = File.createTempFile("image" + dateString, ".png");
    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    // ((Activity)getContext()).startActivityForResult(i, 25);
    ((Activity) getContext()).startActivity(i);

  }

  private String createTimeStamp() {
    SimpleDateFormat df = new SimpleDateFormat("MMddyyyyhhmmsss");
    return (df.format(new Date()));
  }

  // protected void onActivityResult(int requestCode, int resultCode, Intent
  // data) {
  // super.onActivityResult(requestCode, resultCode, data);
  // TODO (bobevans): do something interesting
  // check that the result code was good, otherwise show an error.
  // Maybe data has a thumbnail that we can show in a popup.
  // }

  private View renderNumber(Input input2) {
    View numberPickerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.number_picker, this, true);
    final EditText numberText = (EditText) findViewById(R.id.timepicker_input);
    final Button inc = (Button) findViewById(R.id.increment);
    final Button dec = (Button) findViewById(R.id.decrement);
    OnClickListener incDecListener = new OnClickListener() {

      public void onClick(View v) {
        String text = numberText.getText().toString();
        int currentValue = 0;
        if (text != null && text.length() > 0) {
          try {
            currentValue = Integer.parseInt(text);
          } catch (NumberFormatException nfe) {
          }
        }

        if (v.equals(inc)) {
          numberText.setText(Integer.toString(currentValue + 1));
        } else {
          numberText.setText(Integer.toString(currentValue - 1));
        }
        notifyChangeListeners();
      }
    };
    inc.setOnClickListener(incDecListener);
    dec.setOnClickListener(incDecListener);

    numberText.setOnFocusChangeListener(new OnFocusChangeListener() {

      public void onFocusChange(View v, boolean hasFocus) {
        if (v.equals(numberText) && !hasFocus) {
          notifyChangeListeners();
        }
      }

    });
    return numberText;
  }

  private View renderLocation(Input input2) {
    View locationTextView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.location_text, this, true);
    final TextView findViewById = (TextView) findViewById(R.id.location_display);
    Button gpsSettings = (Button) findViewById(R.id.settings_button);
    gpsSettings.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        ((ExperimentExecutor) getContext()).launchGpsSettings();
      }
    });
    // findViewById.setOnFocusChangeListener(new OnFocusChangeListener() {
    // public void onFocusChange(View v, boolean hasFocus) {
    // if (v.equals(findViewById) && !hasFocus) {
    // notifyChangeListeners();
    // }
    // }
    //
    // });
    return findViewById;
  }

  private View renderList(Input input2) {
    if (input2.isMultiselect()) {
      return renderMultiSelectListButton();
    }
    return renderSingleSelectList();
  }

  private View renderMultiSelectListButton() {
    View listView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.multiselect_list_button, this, true);
    Button multiSelectListButton = (Button) findViewById(R.id.multiselect_list_button);

    DialogInterface.OnMultiChoiceClickListener multiselectListDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked)
          checkedChoices.add(which);
        else
          checkedChoices.remove(which);
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
    builder.setTitle("Make selections");

    boolean[] checkedChoicesBoolArray = new boolean[input.getListChoices().size()];
    int count = input.getListChoices().size();

    for (int i = 0; i < count; i++) {
      checkedChoicesBoolArray[i] = checkedChoices.contains(input.getListChoices().get(i));
    }
    String[] listChoices = new String[input.getListChoices().size()];
    input.getListChoices().toArray(listChoices);
    builder.setMultiChoiceItems(listChoices, checkedChoicesBoolArray, multiselectListDialogListener);
    final AlertDialog multiSelectListDialog = builder.create();

    multiSelectListButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        multiSelectListDialog.show();
      }
    });

    return multiSelectListDialog.getListView();
  }

  private View renderMultiSelectListDialog() {
    View listViewLayout = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.list_choices_multiselect, this, true);
    ListView listView = (ListView) findViewById(R.id.list);
    ArrayAdapter<String> choices = new ArrayAdapter<String>(getContext(),
        android.R.layout.simple_list_item_multiple_choice, input.getListChoices());
    listView.setAdapter(choices);
    listView.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View v, int arg2, long arg3) {
        notifyChangeListeners();
      }

      public void onNothingSelected(AdapterView<?> v) {
        notifyChangeListeners();
      }
    });
    return listView;
  }

  private View renderSingleSelectList() {
    View listView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.list_choices, this, true);
    final Spinner findViewById = (Spinner) findViewById(R.id.list);
    ArrayAdapter<String> choices = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,
        input.getListChoices());
    findViewById.setAdapter(choices);
    findViewById.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View v, int arg2, long arg3) {
        notifyChangeListeners();
      }

      public void onNothingSelected(AdapterView<?> v) {
        notifyChangeListeners();
      }
    });
    return findViewById;
  }

  private View renderLikert(Input input2) {
    Integer steps = input2.getLikertSteps();
    if (steps == null) {
      steps = 5;
    }
    int radioGroupLayoutId = getRadioGroupLayoutId(steps);
    View likertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        radioGroupLayoutId, this, true);
    String leftSideLabel = input2.getLeftSideLabel();
    if (leftSideLabel != null) {
      TextView leftSideView = (TextView) findViewById(R.id.LeftText);
      leftSideView.setText(leftSideLabel);
    }
    String rightSideLabel = input2.getRightSideLabel();
    if (rightSideLabel != null) {
      TextView rightSideView = (TextView) findViewById(R.id.RightText);
      rightSideView.setText(rightSideLabel);
    }
    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.LikertRadioGroup);
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(RadioGroup group, int checkedId) {
        notifyChangeListeners();
      }

    });
    // turn off labels on middle buttons.
    // for (int i = 1;i<steps - 1;i++) {
    // ((RadioButton)radioGroup.getChildAt(i)).setText(null);
    // }
    // ((RadioButton)radioGroup.getChildAt(0)).setGravity(Gravity.LEFT);
    // ((RadioButton)radioGroup.getChildAt(steps -
    // 1)).setGravity(Gravity.RIGHT);
    // for (int i= steps; i < 10; i++) {
    // radioGroup.getChildAt(i).setVisibility(GONE);
    // }
    return radioGroup;
  }

  protected void notifyChangeListeners() {
    for (ChangeListener listener : inputChangeListeners) {
      listener.onChange(this);
    }
  }

  private int getRadioGroupLayoutId(Integer steps) {

    switch (steps) {
    case 2:
      return R.layout.radio_group_2;
    case 3:
      return R.layout.radio_group_3;
    case 4:
      return R.layout.radio_group_4;
    case 5:
      return R.layout.radio_group_5;
    case 6:
      return R.layout.radio_group_6;
    case 7:
      return R.layout.radio_group_7;
    case 8:
      return R.layout.radio_group_8;
    case 9:
      return R.layout.radio_group_9;
    case 10:
      return R.layout.radio_group_10;
    default:
      throw new IllegalArgumentException("Steps unknown or too big: " + steps);
    }

  }

  private View renderOpenText() {
    View likertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.open_text, this, true);
    final EditText findViewById = (EditText) findViewById(R.id.open_text_answer);
    findViewById.setOnFocusChangeListener(new OnFocusChangeListener() {

      public void onFocusChange(View v, boolean hasFocus) {
        if (v.equals(findViewById) && !hasFocus) {
          notifyChangeListeners();
        }
      }

    });
    return findViewById;
  }

  private TextView getInputTextView() {
    TextView inputTextView = new TextView(getContext());
    inputTextView.setText(input.getText());
    return inputTextView;
  }

  public void checkConditionalExpression(ExpressionEvaluator interpreter) {
    if (input.getConditional() != null && input.getConditional() == true) {
      boolean match = false;
      try {
        match = interpreter.parse(input.getConditionExpression());
      } catch (IllegalArgumentException iae) {
        match = false;
      }
      setVisible(match);
    }
  }

  private void setVisible(boolean match) {
    boolean previousVisibility = isVisible();
    if (match) {
      setVisibility(VISIBLE);
      if (!previousVisibility) {
        notifyChangeListeners();
      }
    } else {
      setVisibility(GONE);
      if (previousVisibility) {
        notifyChangeListeners();
      }
    }
  }

  private boolean isVisible() {
    return getVisibility() == VISIBLE;
  }

}
