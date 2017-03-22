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
package com.pacoapp.paco.ui;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.shared.model2.Input2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.text.Html;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;

public class InputLayout extends LinearLayout implements SpeechRecognitionListener {

  private static Logger Log = LoggerFactory.getLogger(InputLayout.class);

  public static final int CAMERA_REQUEST_CODE = 10001;
  // TODO Bob  refactor into separate classes because not every input can receive text from speech recognition

  private Input2 input;
  private View componentWithValue;
  private List<ChangeListener> inputChangeListeners;
  private TextView promptTextView;
  private Location location;

  // Choices that have been selected on a multiselect list.
  private List<Integer> checkedChoices = new ArrayList<Integer>();

  private final int IMAGE_MAX_SIZE = 600;
  protected boolean listHasBeenSelected = false;
  protected boolean setupClickHasHappened;
  private AutoCompleteTextView openTextView;
  //private AutocompleteDictionary autocompleteDatabase;

  private File file;
  private int requestCode;
  private ImageView photoView;
  protected String audioFileName;
  private static int code = 1200;
  private MediaRecorder audioRecorder = null;
  private MediaPlayer   audioPlayer = null;
  boolean mStartRecording = true;
  boolean mStartPlaying = true;
  private boolean va_scale_hasChanged = false;


  public InputLayout(ExperimentExecutor context, Input2 input2) {
    super(context);
    this.input = input2;
    setOrientation(LinearLayout.VERTICAL);
    promptTextView = getInputTextView();
    addView(promptTextView);
    componentWithValue = getInputResponseTypeView(input2);
    inputChangeListeners = new ArrayList<ChangeListener>();
    setVisible(input2.getConditional() == null || !input2.getConditional());
  }

  private void createAudioFileName() {
    audioFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    audioFileName += "/audio_"+System.currentTimeMillis() + ".mp3";
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

  public void onPause() {
    if (audioRecorder != null) {
      audioRecorder.release();
      audioRecorder = null;
    }

    if (audioPlayer != null) {
      audioPlayer.release();
      audioPlayer = null;
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

  public void setLocation(Location location) {
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
    if (input.getResponseType().equals(Input2.LIKERT_SMILEYS)) {
      return getGeistNowLikertValue();
    } else if (input.getResponseType().equals(Input2.OPEN_TEXT)) {
      return getOpenTextValue();
    } else if (input.getResponseType().equals(Input2.LIKERT)) {
      return getLikertValue();
    } else if (input.getResponseType().equals(Input2.LIST)) {
      return getListValue();
    } else if (input.getResponseType().equals(Input2.LOCATION)) {
      return getLocationValue();
    } else if (input.getResponseType().equals(Input2.NUMBER)) {
      return getNumberValue();
    } else if (input.getResponseType().equals(Input2.PHOTO)) {
      return getPhotoValue();
    } else if (input.getResponseType().equals(Input2.AUDIO)) {
      return getAudioValue();
    } else if (input.getResponseType().equals(Input2.VA_SCALE)) {
      return getVaScaleValue();
    }
    return null;
  }

  public String getValueAsString() {
    if (!isVisible()) {
      return null;
    }
    if (input.getResponseType().equals(Input2.LIKERT_SMILEYS)) {
      return intToString(getGeistNowLikertValue());
    } else if (input.getResponseType().equals(Input2.OPEN_TEXT)) {
      return getOpenTextValue();
    } else if (input.getResponseType().equals(Input2.LIKERT)) {
      return intToString(getLikertValue());
    } else if (input.getResponseType().equals(Input2.LIST)) {
      return getListValueAsString();
    } else if (input.getResponseType().equals(Input2.LOCATION)) {
      return getLocationValue();
    } else if (input.getResponseType().equals(Input2.NUMBER)) {
      return intToString(getNumberValue());
    } else if (input.getResponseType().equals(Input2.PHOTO)) {
      return getPhotoValue();
    } else if (input.getResponseType().equals(Input2.AUDIO)) {
      return getAudioValue();
    } else if (input.getResponseType().equals(Input2.VA_SCALE)) {
      return intToString(getVaScaleValue());
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
    if (!input.getMultiselect()) {
      if (!listHasBeenSelected) {
        return null;
      }
      return Integer.toString(((Spinner) componentWithValue).getSelectedItemPosition());
    }
    return getMultiSelectListValueAsString();
  }

  public Class getResponseType() {
    if (input.getResponseType().equals(Input2.LIKERT_SMILEYS)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input2.OPEN_TEXT)) {
      return String.class;
    } else if (input.getResponseType().equals(Input2.LIKERT)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input2.LIST)) {
        return List.class;
    } else if (input.getResponseType().equals(Input2.NUMBER)) {
      return Integer.class;
    } else if (input.getResponseType().equals(Input2.LOCATION)) {
      return String.class;// GeoPoint.class;
    } else if (input.getResponseType().equals(Input2.ACTIVITY)) {
      return String.class;
    } else if (input.getResponseType().equals(Input2.PHOTO)) {
      return Bitmap.class;
    } else if (input.getResponseType().equals(Input2.SOUND)) {
      return SoundPool.class; // TODO (bobevans): is this really a good idea as
                              // the storage type? probably not.
    } else if (input.getResponseType().equals(Input2.VA_SCALE)) {
      return Integer.class;
    }
    return Object.class;
  }

  public String getInputName() {
    return input.getName();
  }

  private String getAudioValue() {
    if (audioFileName != null) {
      try {
        byte[] bytes = Files.toByteArray(new File(audioFileName));
        return Base64.encodeToString(bytes, Base64.DEFAULT);
      } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(getContext(), R.string.could_not_encode_audio, Toast.LENGTH_LONG).show();
      }

    }
    return "";
  }

  private Integer getVaScaleValue() {
    SeekBar seekBar =(SeekBar) componentWithValue;
//    if(seekBar.getThumb().mutate().getAlpha() == 0)

    if(va_scale_hasChanged)
      return ((SeekBar) componentWithValue).getProgress();
    else
      return null;
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
      Toast.makeText(getContext(), R.string.missing_image_warning, Toast.LENGTH_LONG).show();
    }
    return b;
  }

  private Bitmap decodeFileAndScaleToThumb(File f) {
    Bitmap b = null;
    try {
      // Decode image size
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(f), null, o);
      int scale = 1;
      if (o.outHeight > 100 || o.outWidth > 100) {
        int longestDimension = Math.max(o.outHeight, o.outWidth);
        scale = (int) Math.pow(2,  (int) Math.round(Math.log(100 / (double)longestDimension) / Math.log(0.5)));
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
    if (!input.getMultiselect()) {
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
    //autocompleteDatabase.updateAutoCompleteDatabase(text);
    return text;
  }

  private Integer getNumberValue() {
    String text = ((EditText) componentWithValue).getText().toString();
    if (text != null && text.length() > 0) {
      return Integer.parseInt(text);
    }
    return null;
  }

  public Input2 getInput() {
    return input;
  }

  private View getInputResponseTypeView(Input2 input2) {
    String questionType = input2.getResponseType();
    if (questionType.equals(Input2.LIKERT_SMILEYS)) {
      return renderGeistNowSmilerLikert(input2.getLikertSteps());
    } else if (questionType.equals(Input2.OPEN_TEXT)) {
      return renderOpenText();
    } else if (questionType.equals(Input2.LIKERT)) {
      return renderLikert(input2);
    } else if (questionType.equals(Input2.LIST)) {
      return renderList(input2);
    } else if (questionType.equals(Input2.LOCATION)) {
      return renderLocation(input2);
    } else if (questionType.equals(Input2.NUMBER)) {
      return renderNumber(input2);
    } else if (questionType.equals(Input2.PHOTO)) {
      return renderPhotoButton(input2);
    } else if (questionType.equals(Input2.AUDIO)) {
      return renderAudioRecorder(input2);
    } else if (questionType.equals(Input2.VA_SCALE)) {
      return renderVaScale();
    }
    return null;
  }

  private View renderPhotoButton(Input2 input2) {
    View photoInputView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.photo_input, this, true);
    photoInputView.setPadding(0, 2, 0, 8);
    Button cameraButton = (Button) findViewById(R.id.CameraButton);
    photoView = (ImageView) findViewById(R.id.CameraPreviewImage);
    if (file != null) {
      photoView.setImageBitmap(decodeFile(file));
    }
    cameraButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        renderCameraOrGalleryChooser();
      }
    });
    return photoInputView;
  }

  private void renderCameraOrGalleryChooser() {
    String title = getContext().getString(R.string.please_choose_the_source_of_your_image);
    Dialog chooserDialog = new AlertDialog.Builder(getContext()).setTitle(title)
            .setNegativeButton(getContext().getString(R.string.camera), new Dialog.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                startCameraForResult();
              }
            })
            .setPositiveButton(getContext().getString(R.string.gallery), new Dialog.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                startGalleryPicker();

              }

            }).create();
    chooserDialog.show();
  }

  private void startGalleryPicker() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    requestCode = code++;
    ExperimentExecutor activity = (ExperimentExecutor)getContext();
    activity.startActivityForResult(photoPickerIntent, requestCode);
  }

  public void galleryPicturePicked(String filepath, int requestCode) {
    if (this.requestCode == requestCode && !Strings.isNullOrEmpty(filepath)) {
      file = new File(filepath);
      photoView.setImageBitmap(decodeFileAndScaleToThumb(file));
    } else if (Strings.isNullOrEmpty(filepath)) {
      file = null;
    } // otherwise leave as it was previously
  }

  private void startCameraForResult() {
    try {
      Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
      String dateString = createTimeStamp();
      file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
      requestCode = code++;
      i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
      ((Activity) getContext()).startActivityForResult(i, CAMERA_REQUEST_CODE + requestCode);
    } catch (Exception e) {
      e.printStackTrace();
      new AlertDialog.Builder(getContext()).setCancelable(true).setTitle(R.string.cannot_open_camera_warning)
          .setMessage("Error: \n" + e.getMessage()).setNegativeButton(R.string.ok, null).create().show();
    }
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
              Log.debug("failed to create directory");
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

  private View renderNumber(Input2 input2) {
    View numberPickerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.number_picker, this, true);
    numberPickerView.setPadding(0, 2, 0, 8);
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

  private View renderLocation(Input2 input2) {
    View locationTextView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.location_text, this, true);
    locationTextView.setPadding(0, 2, 0, 16);
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

  private View renderList(Input2 input2) {
    if (input2.getMultiselect()) {
      return renderMultiSelectListButton();
    }
    return renderSingleSelectList();
  }

  private View renderMultiSelectListButton() {
    View listView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.multiselect_list_button, this, true);
    listView.setPadding(0, 2, 0, 16);
    final Button multiSelectListButton = (Button) findViewById(R.id.multiselect_list_button);

    DialogInterface.OnMultiChoiceClickListener multiselectListDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked) {
          checkedChoices.add(new Integer(which));
        } else {
          checkedChoices.remove(new Integer(which));
        }

        showChoiceCountOnButtonText(multiSelectListButton);
      }

      private void showChoiceCountOnButtonText(final Button multiSelectListButton) {
        int chosenCount = checkedChoices.size();
        if (chosenCount == 1) {
          String choicesText = Integer.toString(chosenCount) + " " + getContext().getString(R.string.multiselectListOneItemChosen);
          multiSelectListButton.setText(choicesText);
        } else if (chosenCount > 1) {
          String choicesText = Integer.toString(chosenCount) + " " + getContext().getString(R.string.multiselectListManyItemsChosen);
          multiSelectListButton.setText(choicesText);
        } else {
          multiSelectListButton.setText(R.string.make_selections);
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
    builder.setTitle(R.string.make_selections);

    boolean[] checkedChoicesBoolArray = new boolean[input.getListChoices().size()];
    int count = input.getListChoices().size();

    for (int i = 0; i < count; i++) {
      checkedChoicesBoolArray[i] = checkedChoices.contains(input.getListChoices().get(i));
    }
    List<CharSequence> listChoices = convertHtmlChoicesToTextChoices(input.getListChoices());
    CharSequence[] listChoiceArray = new CharSequence[listChoices.size()];
    listChoices.toArray(listChoiceArray);
    builder.setMultiChoiceItems(listChoiceArray, checkedChoicesBoolArray, multiselectListDialogListener);
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

  public List<CharSequence> convertHtmlChoicesToTextChoices(List<String> rawListChoices) {
    List<CharSequence> listChoices = Lists.newArrayList();
    for (String currentChoice : rawListChoices) {
      if (currentChoice == null) {
        currentChoice = "";
      }
      listChoices.add(Html.fromHtml(currentChoice));
    }
    return listChoices;
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
    listView.setPadding(0, 2, 0, 8);
    final Spinner findViewById = (Spinner) findViewById(R.id.list);
    // Formerly android.R.layout.simple_spinner_item
    List<CharSequence> listChoicesList = convertHtmlChoicesToTextChoices(input.getListChoices());

    String defaultListItem = getResources().getString(R.string.default_list_item);
    if (!listChoicesList.get(0).equals(defaultListItem)) {
      listChoicesList.add(0, defaultListItem);       // "No selection" list item.
    }
    ArrayAdapter<CharSequence> choices = new ArrayAdapter<CharSequence>(getContext(),
            //android.R.layout.simple_spinner_dropdown_item,
            R.layout.multiline_spinner_item,
            listChoicesList);


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

  private View renderLikert(Input2 input2) {
    Integer steps = input2.getLikertSteps();
    if (steps == null) {
      steps = 5;
    }
    int radioGroupLayoutId = getRadioGroupLayoutId(steps);
    View likertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        radioGroupLayoutId, this, true);
    likertView.setPadding(0, 2, 0, 16);
    String leftSideLabel = input2.getLeftSideLabel();
    if (leftSideLabel != null) {
      TextView leftSideView = (TextView) findViewById(R.id.LeftText);
      leftSideView.setText(Html.fromHtml(leftSideLabel));
    }
    String rightSideLabel = input2.getRightSideLabel();
    if (rightSideLabel != null) {
      TextView rightSideView = (TextView) findViewById(R.id.RightText);
      rightSideView.setText(Html.fromHtml(rightSideLabel));
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
      Log.error("Steps unknown or too big: " + steps);
      return R.layout.radio_group_error;
    }

  }

  private View renderVaScale() {
    View view = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
            R.layout.va_scale, this, true);
    SeekBar_api14 seekBar = (SeekBar_api14) findViewById(R.id.va_scale_input);
    seekBar.getThumb().mutate().setAlpha(0);

    seekBar.setOnSeekBarChangeListener(new SeekBar_api14.OnSeekBarChangeListener() {
      @Override
      public void onStartTrackingTouch(SeekBar_api14 s) {
        va_scale_hasChanged = true;
        s.getThumb().setAlpha(255);
        notifyChangeListeners();
      }
      @Override
      public void onStopTrackingTouch(SeekBar_api14 seekBar) {
      }
      @Override
      public void onProgressChanged(SeekBar_api14 seekBar, int progress,boolean fromUser) {
      }
    });



    return seekBar;
  }
  private View renderOpenText() {
    View likertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.open_text, this, true);
    openTextView = (AutoCompleteTextView) findViewById(R.id.open_text_answer);
    openTextView.setThreshold(1);
    openTextView.setPadding(8, 2, 0, 16);
    // Theoretically this should allow autocorrect.  However, apparently this change is not reflected on the
    // emulator, so we need to test it on the device.
    openTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    //ensureAutoCompleteDatabase();
    //openTextView.setAdapter(new AutocompleteUsageFilteringArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, autocompleteDatabase));
    //openTextView.setTokenizer(new AutoCompleteTextView(getContext()));
    openTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

      public void onFocusChange(View v, boolean hasFocus) {
        if (v.equals(openTextView) && !hasFocus) {
          //autocompleteDatabase.updateAutoCompleteDatabase(openTextView.getText().toString());
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

//  private void ensureAutoCompleteDatabase() {
//    if (autocompleteDatabase == null) {
//      autocompleteDatabase = new PersistentAutocompleteDictionary(getContext());
//    }
//  }

  private void launchSpeechRecognizer() {
    ((ExperimentExecutor)getContext()).startSpeechRecognition(this);
  }

  private TextView getInputTextView() {
    TextView inputTextView = new TextView(getContext());
    inputTextView.setPadding(0, 8, 0, 4);
    String text = input.getText();
    if (input.getResponseType().equals(Input2.LOCATION) && Strings.isNullOrEmpty(text)) {
      text = getContext().getString(R.string.location_to_be_recorded_default_prompt);
    } else if (Strings.isNullOrEmpty(text)) {
      text = "";
    }
    inputTextView.setText(Html.fromHtml(text));
    inputTextView.setTextSize(18);
    if (!Strings.isNullOrEmpty(text)) {
      inputTextView.setBackgroundColor(Color.argb(40, 200, 200, 250));
    }
    return inputTextView;
  }

  public void checkConditionalExpression(ExpressionEvaluator interpreter) {
    if (input.getConditional() != null && input.getConditional() == true) {
      boolean match = false;
      try {
        match = interpreter.parse(input.getConditionExpression());
      } catch (IllegalArgumentException iae) {
        Log.error("Parsing problem: " + iae.getMessage());
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
    likertView.setPadding(0, 2, 0, 16);
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
      //autocompleteDatabase.updateAutoCompleteDatabase(bestPhrase);
    } else {
      Toast.makeText(getContext(), R.string.i_did_not_understand, Toast.LENGTH_SHORT).show();
    }
    ((ExperimentExecutor)getContext()).removeSpeechRecognitionListener(this);
  }

  public void cameraPictureTaken(int requestCode) {
    if (this.requestCode == requestCode - CAMERA_REQUEST_CODE) {
      photoView.setImageBitmap(decodeFileAndScaleToThumb(file));
    }
  }

  private View renderAudioRecorder(Input2 input2) {
    View audioInputView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
        R.layout.audio_input, this, true);
    audioInputView.setPadding(0, 2, 0, 8);
    final Button recordButton = (Button) findViewById(R.id.AudioRecordButton);
    final Button playButton = (Button) findViewById(R.id.AudioPlayButton);
    final Button deleteButton = (Button) findViewById(R.id.AudioDeleteButton);
    toggleOtherButtons(playButton, deleteButton, file != null);

    recordButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        onRecord(mStartRecording);
        if (mStartRecording) {
            recordButton.setText("Stop");
        } else {
          recordButton.setText("Record");
        }
        toggleOtherButtons(playButton, deleteButton, !mStartRecording);
        mStartRecording = !mStartRecording;
      }
    });

    final OnCompletionListener completionListener = new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        playButton.setText("Play");
        mStartPlaying = true;
        toggleOtherButtons(recordButton, deleteButton, true);
      }
    };

    playButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onPlay(mStartPlaying, completionListener);
        if (mStartPlaying) {
          playButton.setText("Stop");
        } else {
          playButton.setText("Play");
        }
        toggleOtherButtons(recordButton, deleteButton, !mStartPlaying);
        mStartPlaying = !mStartPlaying;
      }
    });

    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (audioFileName != null) {
          deleteAudioFile();
        }
        toggleOtherButtons(playButton, deleteButton, false);
      }
    });
    return audioInputView;
  }

  private void toggleOtherButtons(Button playButton, Button deleteButton, boolean enabled) {
    playButton.setEnabled(enabled);
    deleteButton.setEnabled(enabled);
  }




  private void onRecord(boolean start) {
    if (start) {
      startRecording();
    } else {
      stopRecording();
    }
  }

  private void onPlay(boolean start, OnCompletionListener listener) {
    if (start) {
      startPlaying(listener);
    } else {
      stopPlaying();
    }
  }

  private void startPlaying(OnCompletionListener listener) {
    audioPlayer = new MediaPlayer();
    try {
      audioPlayer.setDataSource(audioFileName);
      audioPlayer.prepare();
      audioPlayer.start();
      audioPlayer.setOnCompletionListener(listener);
    } catch (IOException e) {
      Log.error("prepare() failed");
    }
  }

  private void stopPlaying() {
    audioPlayer.release();
    audioPlayer = null;
  }

  private void startRecording() {
    if (audioFileName != null) {
      deleteAudioFile();
    }
    createAudioFileName();
    audioRecorder = new MediaRecorder();
    audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4/* THREE_GPP */);
    audioRecorder.setOutputFile(audioFileName);
    audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC/* AMR_NB */);

    try {
      audioRecorder.prepare();
    } catch (IOException e) {
      Log.error("prepare() failed");
    }

    audioRecorder.start();
  }

  private void stopRecording() {
    audioRecorder.stop();
    audioRecorder.release();
    audioRecorder = null;
  }

  private void deleteAudioFile() {
    File f = new File(audioFileName);
    if (f.exists()) {
      f.delete();
    }
    audioFileName = null;
  }

}
