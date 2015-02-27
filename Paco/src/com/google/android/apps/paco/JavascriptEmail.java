package com.google.android.apps.paco;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.common.base.Strings;

class JavascriptEmail {

  private Context context;

  /**
   * @param Context for accessing system services
   */
  JavascriptEmail(Context context) {
    this.context = context;
  }

  public void sendEmail(String body, String subject, String userEmail) {
    sendEmailInner(body, subject, userEmail);
  }

  private boolean sendEmailInner(String body, String subject, String userEmail) {
    userEmail = findAccount(userEmail);
    if (Strings.isNullOrEmpty(userEmail)) {
      Log.e(PacoConstants.TAG, "No Google email address found with which to send email.");
      return false;
    }
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    String aEmailList[] = { userEmail};
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    emailIntent.setType("plain/text");
    try {
      context.startActivity(emailIntent);
      return true;
    } catch (ActivityNotFoundException anf) {
      Log.i(PacoConstants.TAG, "No email client configured");
      return false;
    }
  }

  private String findAccount(String userEmail) {
    String domainName = null;
    if (userEmail.startsWith("@")) {
      domainName = userEmail.substring(1);
    }
    Account[] accounts = AccountManager.get(context).getAccounts();
    for (Account account : accounts) {
      if (userEmail == null || userEmail.length() == 0) {
        return account.name; // return first
      }

      if (domainName != null) {
        int atIndex = account.name.indexOf('@');
        if (atIndex != -1) {
          String accountDomain = account.name.substring(atIndex + 1);
          if (accountDomain.equals(domainName)) {
            return account.name;
          }
        }
      }
    }
    return "";
  }




}