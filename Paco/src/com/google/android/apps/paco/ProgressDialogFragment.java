package com.google.android.apps.paco;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.pacoapp.paco.R;

public class ProgressDialogFragment extends DialogFragment {
  int mNum;
  private int dialogTypeId;

  static ProgressDialogFragment newInstance(int id) {
      ProgressDialogFragment f = new ProgressDialogFragment();

      // Supply num input as an argument.
      Bundle args = new Bundle();
      args.putInt("dialog_id", id);
      f.setArguments(args);

      return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setCancelable(true);
      int style = DialogFragment.STYLE_NORMAL, theme = 0;
      setStyle(style,theme);
  }

//  @Override
//  public View onCreateView(LayoutInflater inflater, ViewGroup container,
//          Bundle savedInstanceState) {
//      View v = inflater.inflate(R.layout.fragment_dialog, container, false);
//      View tv = v.findViewById(R.id.text);
////      ((TextView)tv).setText("Dialog #" + mNum + ": using style "
////              + "vanilla");
//
//      // Watch for button clicks.
//      Button button = (Button)v.findViewById(R.id.show);
//      button.setOnClickListener(new OnClickListener() {
//          public void onClick(View v) {
//              // When button is clicked, call up to owning activity.
//              ((FindExperimentsActivity)getActivity()).showDialog();
//          }
//      });
//
//      return v;
//  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);
    Bundle bundle = getArguments();
    int id = bundle.getInt("dialog_id");
    setDialogTypeId(id);
    Dialog dialog = null;
    switch (id) {
      case FindExperimentsActivity.REFRESHING_EXPERIMENTS_DIALOG_ID: {
        dialog = getRefreshJoinedDialog();
        break;
      } case DownloadHelper.INVALID_DATA_ERROR: {
        dialog = getUnableToJoinDialog(getString(R.string.invalid_data));
        break;
      } case DownloadHelper.SERVER_ERROR: {
        dialog = getUnableToJoinDialog(getString(R.string.dialog_dismiss));
        break;
      } case DownloadHelper.NO_NETWORK_CONNECTION: {
        dialog = getNoNetworkDialog();
        break;
      } default: {
        dialog = null;
      }
    }
    return dialog;
  }


  private void setDialogTypeId(int id) {
    this.dialogTypeId = id;
  }

  private ProgressDialog getRefreshJoinedDialog() {
    ProgressDialog pd = new ProgressDialog(getActivity());
    pd.setTitle(getString(R.string.experiment_refresh));
    pd.setMessage(getString(R.string.checking_server_for_new_and_updated_experiment_definitions));
    pd.setCancelable(true);
    pd.setIndeterminate(true);
    return pd;
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(getActivity());
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                           getActivity().setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                           getActivity().finish();
                         }
                       });
    return unableToJoinBldr.create();
  }

  private AlertDialog getNoNetworkDialog() {
    AlertDialog.Builder noNetworkBldr = new AlertDialog.Builder(getActivity());
    noNetworkBldr.setTitle(R.string.network_required)
                 .setMessage(getString(R.string.need_network_connection))
                 .setPositiveButton(R.string.go_to_network_settings, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                           ((NetworkActivityLauncher)getActivity()).showNetworkConnectionActivity();
                         }
                       })
                 .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            getActivity().setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                            getActivity().finish();
                          }
                    });
    return noNetworkBldr.create();
  }

  public int getDialogTypeId() {
    return dialogTypeId;
  }



}