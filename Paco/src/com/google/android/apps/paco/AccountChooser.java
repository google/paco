package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.corp.productivity.specialprojects.android.comm.LoginRedirectHandler;
import com.pacoapp.paco.R;

public class AccountChooser extends ListActivity {

  public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
  public static final String ACCOUNT_TYPE = "accountType";
  private static final String GOOGLE_ACCOUNT = "com.google";

  private String accountType = GOOGLE_ACCOUNT;
  private Account[] accounts;
  private UserPreferences userPrefs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.account_chooser_list);

  }

  @Override
  protected void onResume() {
    super.onResume();
    userPrefs = new UserPreferences(this);
    String chosenAccount = userPrefs.getSelectedAccount();
    if (chosenAccount != null) {
      TextView acctTextView = ((TextView)findViewById(R.id.ChosenAccountText));
      acctTextView.setText(getString(R.string.current_account) + ": " + chosenAccount);
    }
    AccountManager am = AccountManager.get(this);
    accounts = am.getAccountsByType(accountType);
    if (accounts == null || accounts.length == 0) {
      am.addAccount(accountType, LoginRedirectHandler.AUTH_TOKEN_TYPE, null, null, null, null, null);
    } else {
      List<String> accountNames = new ArrayList<String>();
      for (Account acct : accounts) {
        accountNames.add(acct.name);
      }
      setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, accountNames));
    }
  }



  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    //super.onListItemClick(l, v, position, id);
    Account acct = accounts[position];
    setChosenAccountNameAsResult(acct.name);
    finish();
  }

  private void setChosenAccountNameAsResult(String accountName) {
    userPrefs.saveSelectedAccount(accountName);
    Intent result = new Intent();
    result.putExtra(ACCOUNT_NAME, accountName);
    setResult(0, result);
  }






}
