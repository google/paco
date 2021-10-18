package com.pacoapp.paco.ui;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.util.SchedulePrinter;

/**
 * A list fragment representing a list of Schedules. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ScheduleDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ScheduleListFragment extends ListFragment {

  /**
   * The serialization (saved instance state) Bundle key representing the
   * activated item position. Only used on tablets.
   */
  private static final String STATE_ACTIVATED_POSITION = "activated_position";

  /**
   * The current activated item position. Only used on tablets.
   */
  private int mActivatedPosition = ListView.INVALID_POSITION;


  /**
   * The fragment's current callback object, which is notified of list item
   * clicks.
   */
  private Callbacks callbacks = sDummyCallbacks;

  /**
   * A callback interface that all activities containing this fragment must
   * implement. This mechanism allows activities to be notified of item
   * selections.
   */
  public interface Callbacks {
    /**
     * Callback for when an item has been selected.
     */
    public void onItemSelected(ScheduleBundle scheduleBundle);

    public Experiment getExperiment();

    public void saveExperiment();
  }

  /**
   * A dummy implementation of the {@link Callbacks} interface that does
   * nothing. Used only when this fragment is not attached to an activity.
   */
  private static Callbacks sDummyCallbacks = new Callbacks() {

    @Override
    public Experiment getExperiment() {
      return null;
    }

    @Override
    public void onItemSelected(ScheduleBundle scheduleBundle) {
    }

    @Override
    public void saveExperiment() {
    }

  };

  private Map<String, ScheduleBundle> scheduleMap;


  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ScheduleListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //showSchedulesForGroups();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
    showSchedulesForGroups();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }
    callbacks = (Callbacks) activity;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    callbacks = sDummyCallbacks;
  }


  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);
    String scheduleDescription = (String) getListAdapter().getItem(position);
    callbacks.onItemSelected(scheduleMap.get(scheduleDescription));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mActivatedPosition != ListView.INVALID_POSITION) {
      outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
    }
  }

  /**
   * Turns on activate-on-click mode. When this mode is on, list items will be
   * given the 'activated' state when touched.
   */
  public void setActivateOnItemClick(boolean activateOnItemClick) {
    // When setting CHOICE_MODE_SINGLE, ListView will automatically
    // give items the 'activated' state when touched.
    getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
  }

  private void setActivatedPosition(int position) {
    if (position == ListView.INVALID_POSITION) {
      getListView().setItemChecked(mActivatedPosition, false);
    } else {
      getListView().setItemChecked(position, true);
    }

    mActivatedPosition = position;
  }

  //

  private void showSchedulesForGroups() {
    List<String> scheduleDescriptions = buildListData();
    setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.fragment_schedule_list,
            R.id.scheduleTextView, scheduleDescriptions));
    ((ArrayAdapter) getListAdapter()).notifyDataSetChanged();
  }

  public List<String> buildListData() {
    scheduleMap = Maps.newHashMap();
    List<String> scheduleDescriptions = Lists.newArrayList();
    for (ExperimentGroup experimentGroup2 : callbacks.getExperiment().getExperimentDAO().getGroups()) {
      if (experimentGroup2 != null) {
        for (ActionTrigger actionTrigger : experimentGroup2.getActionTriggers()) {
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger) actionTrigger;
            List<Schedule> schedules = scheduleTrigger.getSchedules();
            for (Schedule schedule : schedules) {
              final String scheduleDescription = SchedulePrinter.toPrettyString(schedule);
              ScheduleBundle sb = new ScheduleBundle(experimentGroup2, scheduleTrigger, schedule);
              scheduleMap.put(scheduleDescription, sb);
              scheduleDescriptions.add(scheduleDescription);
            }
          }
        }
      }
    }
    return scheduleDescriptions;
  }

}
