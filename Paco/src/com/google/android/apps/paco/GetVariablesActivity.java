// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.android.apps.paco;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.paco.questioncondparser.Binding;
import com.google.android.apps.paco.questioncondparser.Environment;
import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;


/**
 * @author aksaigal@google.com (Arun Saigal)
 *
 */
public class GetVariablesActivity extends ListActivity{

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private List<Input> inputs;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    experiment = getExperimentFromIntent();
  
    experimentProviderUtil.loadInputsForExperiment(experiment);
    
    if (experiment!= null) {
     inputs = experiment.getInputs();
     List<String> inputNames = new ArrayList<String>();
     
     for (Input inp: inputs){
       inputNames.add(inp.getName());
     }
     findViewById(R.id.TrialCheckBoxes);

     setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, inputNames));
     Toast.makeText(GetVariablesActivity.this, experiment.getTitle()+"",
       Toast.LENGTH_SHORT).show();
    }
    else{     Toast.makeText(GetVariablesActivity.this, "still null",
      Toast.LENGTH_SHORT).show();}
    
  }
  
  
  private Experiment getExperimentFromIntent() {
    Uri uri = getIntent().getData();    
    if (uri == null) {
      return null;
    }
    return experimentProviderUtil.getExperiment(uri);
  }

  
}
