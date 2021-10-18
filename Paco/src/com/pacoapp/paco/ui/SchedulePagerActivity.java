package com.pacoapp.paco.ui;

import java.util.List;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import androidx.appcompat.app.AppCompatActivity;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.utils.IntentExtraHelper;

public class SchedulePagerActivity extends AppCompatActivity implements ScheduleDetailFragment.Callbacks, ExperimentLoadingActivity {
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
