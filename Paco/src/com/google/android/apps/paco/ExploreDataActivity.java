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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 *
 */
public class ExploreDataActivity extends Activity {

  private Cursor cursor;
  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  private Experiment experiment;
  private List<Input> inputs;
  List<String> inputNames = new ArrayList<String>();
  
  // Choices that have been selected on a multiselect list.
  private HashMap<Long, List<String>> checkedChoices = new HashMap<Long, List<String>>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.explore_data, null);
    setContentView(mainLayout);
    
    Button chooseTrends = (Button)findViewById(R.id.TrendsButton);
    chooseTrends.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(1);
      }     
      });
    
    Button chooseRelationships = (Button)findViewById(R.id.RelationshipsButton);
    chooseRelationships.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(2);
      }     
      });
    
    Button chooseDistributions = (Button)findViewById(R.id.DistributionsButton);
    chooseDistributions.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(3);
      }     
      });
    
  }
  
  protected void gotoVarSelection(int which_option){
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.variable_choices, null);
    setContentView(mainLayout);
    
    final Button varOkButton = (Button) findViewById(R.id.VarOkButton);
    varOkButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //newFunction(checkedChoices));
      }
    });
    
    Intent intent = getIntent();
    intent.setData(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
    
    userPrefs = new UserPreferences(this);
    
    list = (ListView)findViewById(R.id.exploreable_experiments_list);
        
    experimentProviderUtil = new ExperimentProviderUtil(this);
    
    cursor = managedQuery(getIntent().getData(), 
        new String[] { ExperimentColumns._ID, ExperimentColumns.TITLE}, 
        null, null, null);
    
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
      android.R.layout.simple_list_item_1, cursor, 
      new String[] { ExperimentColumns.TITLE}, 
      new int[] { android.R.id.text1}) {
    
      };
      
      list.setAdapter(adapter);
      list.setOnItemClickListener(new OnItemClickListener() {
        
        public void onItemClick(AdapterView<?> listview, View textview, int position,
            long id) {
          inputNames = new ArrayList<String>();
          experiment = experimentProviderUtil.getExperiment(id);
          
          experimentProviderUtil.loadInputsForExperiment(experiment);
          
          if (experiment!= null) {
           inputs = experiment.getInputs();
           
           for (Input inp: inputs){
             inputNames.add(inp.getName());
           }
           renderMultiSelectListButton(id);
                      
           varOkButton.setVisibility(View.VISIBLE);

          }else{     Toast.makeText(ExploreDataActivity.this, "You didn't pick a proper experiment.",
            Toast.LENGTH_SHORT).show();}
          
        }
      });
  }

  private View renderMultiSelectListButton(long ID) {
    
    final Long id = ID;

    DialogInterface.OnMultiChoiceClickListener multiselectListDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked){
          if (checkedChoices.get(id) !=null){
            checkedChoices.get(id).add(inputNames.get(which));
          }
          else{
            List<String> tempList = new ArrayList<String>();
            tempList.add(inputNames.get(which));
            checkedChoices.put(id, tempList);
          }
        }
        else
          checkedChoices.get(id).remove(inputNames.get(which));
      }
    };

    
    AlertDialog.Builder builder = new AlertDialog.Builder(mainLayout.getContext());
    builder.setTitle("Make selections");

    boolean[] checkedChoicesBoolArray = new boolean[inputNames.size()];
    int count = inputNames.size();

    if (checkedChoices.get(id) !=null){
      for (int i = 0; i < count; i++) {
        checkedChoicesBoolArray[i] = checkedChoices.get(id).contains(inputNames.get(i));
      }
    }
    String[] listChoices = new String[inputNames.size()];
    inputNames.toArray(listChoices);
    builder.setMultiChoiceItems(listChoices, checkedChoicesBoolArray, multiselectListDialogListener);
    builder.setPositiveButton("OK",
      new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog,
              int whichButton) {
        Toast.makeText(ExploreDataActivity.this, checkedChoices.toString(),
          Toast.LENGTH_SHORT).show();

      }
      });
    AlertDialog multiSelectListDialog = builder.create();
    multiSelectListDialog.show();
    return multiSelectListDialog.getListView();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(mainLayout);
  }
  
  public void test(){
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);
  }
  

}