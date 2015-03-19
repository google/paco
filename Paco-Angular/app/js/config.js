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
    inputs: [],
    customRendering: false,
    fixedDuration: false,
    logActions: false,
    backgroundListen: false,
    endOfDayGroup: false,
    feedbackType: 0,
    feedback: {
      text: 'Thanks for Participating!',
    }
  };

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

});
