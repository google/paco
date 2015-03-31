package com.pacoapp.paco.ui;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentLoadingActivity;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.R;

public class SchedulePagerActivity extends ActionBarActivity implements ScheduleDetailFragment.Callbacks, ExperimentLoadingActivity {
    ViewPager mViewPager;
    private ExperimentProviderUtil experimentProviderUtil;
    private ExperimentGroup experimentGroup;
    private ScheduleTrigger scheduleTrigger;
    private List<Schedule> schedules;
    private Experiment experiment;
    private Schedule schedule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        experimentProviderUtil = new ExperimentProviderUtil(this);
        IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
        loadSchedulesFromIntent();

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return schedules.size();
            }
            @Override
            public Fragment getItem(int pos) {
                Long scheduleId =  schedules.get(pos).getId();
                ScheduleDetailFragment fragment = new ScheduleDetailFragment();
                fragment.setArguments(getIntent().getExtras());
                return fragment;
            }
        });

        Long scheduleId = getIntent().getLongExtra(ScheduleDetailFragment.EXTRA_SCHEDULE_ID, -1l);
        if (scheduleId != -1) {
          for (int i = 0; i < schedules.size(); i++) {
              if (schedules.get(i).getId().equals(scheduleId)) {
                  mViewPager.setCurrentItem(i);
                  break;
              }
          }
        }
    }

    private void loadSchedulesFromIntent() {
      if (getIntent().getExtras() != null) {
        long scheduleTriggerId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID);
        Long scheduleId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_ID);
        scheduleTrigger = (ScheduleTrigger)experimentGroup.getActionTriggerById(scheduleTriggerId);
        schedules = scheduleTrigger.getSchedules();
      }
    }

    @Override
    public void saveSchedule() {
      // TODO Auto-generated method stub

    }

    @Override
    public void setExperiment(Experiment experimentByServerId) {
      this.experiment = experimentByServerId;
    }

    @Override
    public Experiment getExperiment() {
      return experiment;
    }

    @Override
    public void setExperimentGroup(ExperimentGroup groupByName) {
      this.experimentGroup = groupByName;

    }

    @Override
    public Schedule getSchedule() {
      return schedule;
    }
}
