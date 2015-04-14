pacoApp.service('config', function() {

  this.tabs = [
    'basics',
    'groups',
    'admin',
    'source'
  ];

  this.scheduleTypes = [
    'Daily',
    'Weekdays',
    'Weekly',
    'Monthly',
    'Random sampling (ESM)',
    'Self Report'
  ];

  this.actionTypes = [
    'Create notification to participate',
    'Create notification message',
    'Log data',
    'Execute script'
  ];

  this.cueTypes = [
    'HANGUP (deprecated)',
    'USER_PRESENT',
    'Paco action',
    'App Started',
    'App Stopped',
    'Music Started',
    'Music Stopped',
    'Incoming call started',
    'Incoming call ended',
    'Outgoing call started',
    'Outgoing call ended',
    'Missed call'
  ];

  this.esmPeriods = {
    1: 'day',
    7: 'week',
    30: 'month'
  };

  this.weeksOfMonth = [
    'First',
    'Second',
    'Third',
    'Fourth',
    'Fifth'
  ];

  this.responseTypes = [
    'likert',
    'likert_smileys',
    'open text',
    'list',
    'photo',
    'location'
  ];

  this.feedbackTypes = [
    'Static Message Feedback',
    'Retrospective Feedback (QS default)',
    'Responsive Feedback (adaptive)',
    'Custom Code Feedback',
    'Disable Feedback Message'
  ];
});


pacoApp.service('template', function() {

  this.group = {
    actionTriggers: [],
    name: 'New Group',
    inputs: [],
    feedbackType: 0,
    feedback: {
      text: 'Thanks for Participating!',
    }
  };

  this.experiment = {
    admins: [],
    creator: '',
    contactEmail: '',
    published: false,
    publishedUsers: [],
    groups: [this.group],
  }

  this.action = {
    type: 'pacoNotificationAction',
    actionCode: ''
  };

  this.schedule = {
    scheduleType: '',
    userEditable: true,
    timeout: 15
  };

  this.cue = {
    cueCode: ''
  };

  this.scheduleTrigger = {
    type: 'scheduleTrigger',
    actions: [this.action],
    schedules: [this.schedule]
  };

  this.eventTrigger = {
    type: 'interruptTrigger',
    actions: [this.action],
    cues: [this.cue]
  };

  this.signalTime = {
    'fixedTimeMillisFromMidnight': 0
  };
});
