pacoApp.service('config', function() {

  this.tabs = [
    'basics',
    'groups',
    'admin',
    'source'
  ];

  this.dataDeclarations = {
    1: 'App Usage and Browser History',
    2: 'Location Information',
    3: 'Phone Details (Make, Model, Carrier)'
  };

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
    'Static Message',
    'Retrospective (QS default)',
    'Responsive (adaptive)',
    'Custom Code',
    'Disable Feedback'
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
    },
    fixedDuration: 'false'
  };

  this.experiment = {
    admins: [],
    creator: '',
    contactEmail: '',
    published: false,
    publishedUsers: [],
    groups: [this.group],
    dataDeclarations: [],
  }

  this.input = {
    responseType: 'open text'
  }

  this.action = {
    type: 'pacoNotificationAction',
    actionCode: ''
  };

  this.defaultScheduleAction = {
    type: 'pacoNotificationAction',
    actionCode: 0,
    timeout: 15
  };

  this.schedule = {
    scheduleType: '',
    userEditable: true,
    timeout: 15,
    repeatRate: 1
  };

  this.defaultEsmSchedule = {
    scheduleType: '4',
    userEditable: true,
    timeout: 15,
    esmPeriodInDays: '1',
    esmFrequency: 8,
    repeatRate: 1
  };

  this.cue = {
    cueCode: ''
  };

  this.scheduleTrigger = {
    type: 'scheduleTrigger',
    actions: [this.defaultScheduleAction],
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
