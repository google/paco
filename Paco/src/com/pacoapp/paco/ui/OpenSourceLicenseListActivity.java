package com.pacoapp.paco.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pacoapp.paco.R;

public class OpenSourceLicenseListActivity extends AppCompatActivity {

  private ArrayList<String> sites;
  private ArrayList<String> libNames;
  private ListView list;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_opensource_libs);
    sites = new ArrayList<String>();
    sites.add("http://www.joda.org/joda-time/");
    sites.add("http://jackson.codehaus.org");
    sites.add("https://github.com/google/guava");
    sites.add("http://www.antlr.org/");
    sites.add("https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino");
    sites.add("http://commons.apache.org");
    sites.add("http://jquery.org");
    sites.add("http://angularjs.org");
    sites.add("https://material.angularjs.org/#/");
    sites.add("https://github.com/google/j2objc");

    libNames = new ArrayList<String>();
    libNames.add("Joda-Time");
    libNames.add("Jackson");
    libNames.add("Guava");
    libNames.add("ANTLR");
    libNames.add("Rhino Javascript Engine");
    libNames.add("Apache Commons");
    libNames.add("jQuery");
    libNames.add("AngularJs");
    libNames.add("Angular Material");
    libNames.add("J2ObjC");

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));
    actionBar.setDisplayHomeAsUpEnabled(true);

    list = (ListView) findViewById(R.id.libList);
    list.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, libNames));
    list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onListItemClick(list, view, position, id);

      }
    });

  }

  protected void onListItemClick(ListView l, View v, int position, long id) {
    String url = sites.get(position);
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


}
