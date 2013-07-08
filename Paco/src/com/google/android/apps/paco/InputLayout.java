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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;

public class InputLayout extends LinearLayout implements SpeechRecognitionListener {
  // TODO Bob  refactor into separate classes because not every input can receive text from speech recognition

  private static final String AUTOCOMPLETE_DATA_FILE_NAME = "autocompleteData";
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

  public View getComponentWithValue() {
    return componentWithValue;
  }
  
  public void setComponentWithValue(View componentWithValue) {
    this.componentWithValue = componentWithValue;
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
    String latLonStr = getContext().getString(R.string.retrieving_lat_lon);
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
  public Object getValue() {
    if (!isVisible()) {
      return null;
    }
    if (input.getResponseType().equals(Input.LIKERT_SMILEYS)) {
      return getGeistNowLikertValue();
    } else if (input.getResponseType().equals(Input.OPEN_TEXT)) {
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
  
  public String getValueAsString() {
    if (!isVisible()) {
      return null;
    }
    if (input.getResponseType().equals(Input.LIKERT_SMILEYS)) {
      return intToString(getGeistNowLikertValue());
    } else if (input.getResponseType().equals(Input.OPEN_TEXT)) {
      return getOpenTextValue();
    } else if (input.getResponseType().equals(Input.LIKERT)) {
      return intToString(getLikertValue());
    } else if (input.getResponseType().equals(Input.LIST)) {
      return getListValueAsString();
    } else if (input.getResponseType().equals(Input.LOCATION)) {
      return getLocationValue();
    } else if (input.getResponseType().equals(Input.NUMBER)) {
      return intToString(getNumberValue());
    } else if (input.getResponseType().equals(Input.PHOTO)) {
      return getPhotoValue();
    }
    return null;
  }


  private String intToString(Integer numberValue) {
    if (numberValue != null) {
      return Integer.toString(numberValue);
    }
    return null;
  }

  private String getListValueAsString() {
    if (!input.isMultiselect()) {
      if (!listHasBeenSelected) {
        return null;
      }
      return Integer.toString(((Spinner) componentWithValue).getSelectedItemPosition());
    }
    return getMultiSelectListValueAsString();
  }

  public Class getResponseType() {
    if (input.getResponseType().equals(Input.LIKERT_SMILEYS)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input.OPEN_TEXT)) {
      return String.class;
    } else if (input.getResponseType().equals(Input.LIKERT)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input.LIST)) {
        return List.class;
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
      if (bitmap == null) {
        return "";
      }

      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      BufferedOutputStream out = new BufferedOutputStream(bytesOut);
      bitmap.compress(CompressFormat.JPEG, 50, out);
      return Base64.encodeToString(bytesOut.toByteArray(), Base64.DEFAULT);
    }
    return "";
  }

  private final int IMAGE_MAX_SIZE = 600;
  protected boolean listHasBeenSelected = false;
  protected boolean setupClickHasHappened;
  private MultiAutoCompleteTextView openTextView;
  private List<String> autocompleteDatabase;

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
      Toast.makeText(getContext(), R.string.missing_image_warning, Toast.LENGTH_LONG);
    }
    return b;
  }

  private String getLocationValue() {
    if (location != null) {
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      return Double.toString(latitude) + "," + Double.toString(longitude);
    }
    return getContext().getString(R.string.unknown_location);
  }

  private Integer getLikertValue() {
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
    return id;
  }

  private List<Integer> getListValue() {
    if (!input.isMultiselect()) {
      ArrayList<Integer> list = new ArrayList<Integer>();
      list.add(((Spinner) componentWithValue).getSelectedItemPosition());
      return list;
    }
    return getMultiSelectListValue();
  }

  private List<Integer> getMultiSelectListValue() {
    List<Integer> list = new ArrayList<Integer>();
    for (Integer choice : checkedChoices) {      
      list.add(choice + 1);
    }
    return list;
  }
  
  private String getMultiSelectListValueAsString() {
    if (checkedChoices.isEmpty()) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Integer choice : checkedChoices) {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      buf.append(choice + 1);
    }
    return buf.toString();
  }

  private String getOpenTextValue() {
    String text = ((EditText) componentWithValue).getText().toString();
    updateAutoCompleteDatabase(text);
    return text;
  }

  private Integer getNumberValue() {
    String text = ((EditText) componentWithValue).getText().toString();
    if (text != null && text.length() > 0) {
      return Integer.parseInt(text);
    } 
    return null;
  }

  public Input getInput() {
    return input;
  }

  private View getInputResponseTypeView(Input input) {
    String questionType = input.getResponseType();
    if (questionType.equals(Input.LIKERT_SMILEYS)) {
      return renderGeistNowSmilerLikert(input.getLikertSteps());
    } else if (questionType.equals(Input.OPEN_TEXT)) {
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
          new AlertDialog.Builder(getContext()).setCancelable(true).setTitle(R.string.cannot_open_camera_warning)
              .setMessage("Error: \n" + e.getMessage()).setNegativeButton(R.string.ok, null).create().show();
        }
      }
    });
    return photoInputView;
  }

  private void startCameraForResult() throws IOException {
    Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
    String dateString = createTimeStamp();
//    file = File.createTempFile("image" + dateString, ".png");
    file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    
    // ((Activity)getContext()).startActivityForResult(i, 25);
    ((Activity) getContext()).startActivity(i);

  }
  
  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  /** Create a file Uri for saving an image or video */
  private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
  }

  /** Create a File for saving an image or video */
  private File getOutputMediaFile(int type){
      // To be safe, you should check that the SDCard is mounted
      // using Environment.getExternalStorageState() before doing this.

      File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PacoApp");
      // This location works best if you want the created images to be shared
      // between applications and persist after your app has been uninstalled.

      // Create the storage directory if it does not exist
      if (! mediaStorageDir.exists()){
          if (! mediaStorageDir.mkdirs()){
              Log.d(PacoConstants.TAG, "failed to create directory");
              return null;
          }
      }

      // Create a media file name
      String timeStamp = createTimeStamp();
      File mediaFile;
      if (type == MEDIA_TYPE_IMAGE){
          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "IMG_"+ timeStamp + ".jpg");
      } else if(type == MEDIA_TYPE_VIDEO) {
          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "VID_"+ timeStamp + ".mp4");
      } else {
          return null;
      }

      return mediaFile;
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
          checkedChoices.add(new Integer(which));          
        else
          checkedChoices.remove(new Integer(which));       
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
    builder.setTitle(R.string.make_selections);

    boolean[] checkedChoicesBoolArray = new boolean[input.getListChoices().size()];
    int count = input.getListChoices().size();

    for (int i = 0; i < count; i++) {
      checkedChoicesBoolArray[i] = checkedChoices.contains(input.getListChoices().get(i));
    }
    String[] listChoices = new String[input.getListChoices().size()];
    input.getListChoices().toArray(listChoices);
    builder.setMultiChoiceItems(listChoices, checkedChoicesBoolArray, multiselectListDialogListener);
    builder.setPositiveButton(R.string.done_button, new Dialog.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        notifyChangeListeners();
      }
    });
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
      }

      public void onNothingSelected(AdapterView<?> v) {
      }
    });
    return listView;
  }

  private View renderSingleSelectList() {
    View listView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.list_choices, this, true);
    final Spinner findViewById = (Spinner) findViewById(R.id.list);
    // Formerly android.R.layout.simple_spinner_item
    ArrayAdapter<String> choices = new ArrayAdapter<String>(getContext(), R.layout.multiline_spinner_item,
        input.getListChoices());
    String defaultListItem = getResources().getString(R.string.default_list_item);
    choices.insert(defaultListItem, 0);       // "No selection" list item.
    findViewById.setAdapter(choices);
    findViewById.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View v, int index, long id) {
        if (!setupClickHasHappened) {
          setupClickHasHappened = true;
        } else if (index != 0) {              // Option has been selected. 
          listHasBeenSelected = true;
        } else {
          listHasBeenSelected = false;
        }
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
    openTextView = (MultiAutoCompleteTextView) findViewById(R.id.open_text_answer);
    openTextView.setThreshold(1);
    // Theoretically this should allow autocorrect.  However, apparently this change is not reflected on the
    // emulator, so we need to test it on the device.
    openTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    ensureAutoCompleteDatabase();
    openTextView.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, autocompleteDatabase));
    openTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    openTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

      public void onFocusChange(View v, boolean hasFocus) {
        if (v.equals(openTextView) && !hasFocus) {
          updateAutoCompleteDatabase(openTextView.getText().toString());
          notifyChangeListeners();
        }
      }

    });
    final ImageButton micButton = (ImageButton) findViewById(R.id.micButton);
    micButton.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View v) {
        launchSpeechRecognizer();        
      }
    });
    return openTextView;
  }

  private void ensureAutoCompleteDatabase() {
    if (autocompleteDatabase == null) {
      autocompleteDatabase = loadAutocompleteDataFromDisk();
    }    
  }
  
  private void updateAutoCompleteDatabase(String responseText) {
    ensureAutoCompleteDatabase();
    addWordToAutocompleteDatabase(responseText);
    Iterable<String> words = Splitter.on(" ").trimResults().split(responseText);
    for (String word : words) {
      addWordToAutocompleteDatabase(word);
    }
    
    saveAutocompleteToDisk();    
  }

  private void addWordToAutocompleteDatabase(String word) {
    if (autocompleteDatabase.contains(word)) {
      return;
    }      
    autocompleteDatabase.add(word);
  }

  private void saveAutocompleteToDisk() {
    OutputStreamWriter f = null;
    try {
      f = new OutputStreamWriter(getContext().openFileOutput(AUTOCOMPLETE_DATA_FILE_NAME, getContext().MODE_PRIVATE));
      BufferedWriter buf = new BufferedWriter(f);
      for (String word : autocompleteDatabase) {
        f.write(word);
        f.write("\n");
      }
      f.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (f != null) {
        try {
          f.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private List<String> loadAutocompleteDataFromDisk() {
    List<String> lines = Lists.newArrayList();
    BufferedReader buf = null;
      try {
        InputStream in = getContext().openFileInput(AUTOCOMPLETE_DATA_FILE_NAME); 
        if (in != null) {
          buf = new BufferedReader(new InputStreamReader(in));                   
          String line = null; 
          while ((line = buf.readLine()) != null) {
            lines.add(line);
          }
        }
      } catch (FileNotFoundException e) {
        Log.d(PacoConstants.TAG, "No autocomplete database found yet", e);
      } catch (IOException e) {
        Log.d(PacoConstants.TAG, "Could not talk to autocomplete database", e);
      } finally {
        try {
          if (buf != null) {
            buf.close();
          }
        } catch (IOException e) {
          // Not worth it, there is no recovery.
        }
      }
      return lines;
    }
     

  private void launchSpeechRecognizer() {
    ((ExperimentExecutor)getContext()).startSpeechRecognition(this);
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
        Log.e(PacoConstants.TAG, "Parsing problem: " + iae.getMessage());
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

  private Integer getGeistNowLikertValue() {
    int value = -1;
    switch(((RadioGroup)componentWithValue).getCheckedRadioButtonId()) {
    case R.id.RadioButton_1:
      value = 1;
      break;
    case R.id.RadioButton_2:
      value = 2;
      break;
    case R.id.RadioButton_3:
      value = 3;
      break;
    case R.id.RadioButton_4:
      value = 4;
      break;
    case R.id.RadioButton_5:
      value = 5;
      break;
    default:
      //nothing is selected;
      // TODO (bobevans), deal with validation of mandatory inputs 
      value = -1;
    }
    return value;
  }

  private View renderGeistNowSmilerLikert(Integer likertSteps) {
    // create a GeistNow radio group with # of steps and images
    if (likertSteps != null && likertSteps != 5) {
      throw new RuntimeException("Currently we are only doing the GeistNow 5 step likert scale.");
    }
    View likertView = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.likert_smiley, this, true);
    RadioGroup findViewById = (RadioGroup) findViewById(R.id.GeistNowRadioGroup);
    findViewById.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(RadioGroup group, int checkedId) {
        notifyChangeListeners();
      }
      
    });
    return findViewById;
  }

  @Override
  public void speechRetrieved(List<String> text) {
    String message = openTextView.getText().toString();
    if (text.size() >= 1) {
      String bestPhrase = text.get(0);      
      message += bestPhrase;
      openTextView.setText(message);
      updateAutoCompleteDatabase(bestPhrase);
    } else {
      Toast.makeText(getContext(), "I did not understand", Toast.LENGTH_SHORT).show();
    }    
    ((ExperimentExecutor)getContext()).removeSpeechRecognitionListener(this);
  }

}
