app.service('config', function() {

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
    'Phone call ended',
    'User present',
    'Paco action',
    'App used',
    'Phone call started'
  ];

  this.esmPeriods = [
    'Day',
    'Week',
    'Month'
  ];

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

  this.groupTemplate = {
    actionTriggers: [],
    name: "New Group",
    inputs: [],
    feedbackType: 0,
    feedback: {
      text: 'Thanks for Participating!',
    }
  };

  this.experimentTemplate = {
    admins: [],
    creator: "",
    contactEmail: "",
    published: false,
    publishedUsers: [],
    groups: [this.groupTemplate],
  }

  this.actionTemplate = {
    'type': 'pacoNotificationAction'
  };

  this.scheduleTriggerTemplate = {
    type: 'scheduleTrigger',
    actions: [this.actionTemplate],
    schedules: [{}]
  };

  this.eventTriggerTemplate = {
    type: 'interruptTrigger',
    actions: [this.actionTemplate],
    cues: [{}]
  };

  this.signalTimeTemplate = {
    'fixedTimeMillisFromMidnight': 0
  };
});
