package com.google.android.apps.paco;

import com.pacoapp.paco.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class ContactOptionsActivity extends Activity {
  private static final String DEFAULT_FEEDBACK_EMAIL_ADDRESS = "paco-opensource@google.com";
  private String feedbackEmailAddress;
  private ImageButton emailButton;
  private ImageButton userGroupButton;
  private ImageButton websiteButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_options);
    emailButton = (ImageButton)findViewById(R.id.emailButton);
    userGroupButton = (ImageButton)findViewById(R.id.usergroupButton);
    websiteButton = (ImageButton)findViewById(R.id.websiteButton);
    
    feedbackEmailAddress = (String) getText(R.string.contact_email);
    if (feedbackEmailAddress == null) {
      feedbackEmailAddress = DEFAULT_FEEDBACK_EMAIL_ADDRESS;
    }

    emailButton.setOnClickListener(new OnClickListener() {      
      @Override
      public void onClick(View v) {
        createEmailIntent();        
      }
    });
    
    userGroupButton.setOnClickListener(new OnClickListener() {      
      @Override
      public void onClick(View v) {
        goToUserGroup();        
      }
    });
    
    websiteButton.setOnClickListener(new OnClickListener() {      
      @Override
      public void onClick(View v) {
        gotoWebsite();        
      }
    });
    
  }

  protected void gotoWebsite() {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + (String)getText(R.string.about_weburl) +"/")));
  }

  protected void goToUserGroup() {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String)getText(R.string.usergroup_website))));    
  }

  private void createEmailIntent() {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    String aEmailList[] = { feedbackEmailAddress };
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Paco Feedback");
    emailIntent.setType("plain/text");
    startActivity(emailIntent);
  }

}
