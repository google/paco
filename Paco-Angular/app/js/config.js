app.service('config', function() {

  this.groupTemplate = {
    actionTriggers: [],
    inputs: []
  };

  this.scheduleTriggerTemplate = {
    type: 'scheduleTrigger',
    actions: [{}],
    schedules: [{}]
  };

  this.eventTriggerTemplate = {
    type: 'interruptTrigger',
    actions: [{}]
  };

  this.scheduleTypes = ['Daily', 'Weekdays', 'Weekly', 'Monthly',
    'Random sampling (ESM)', 'Self Report'
  ];

  this.actionTypes = ['Create notification to participate',
    'Create notification message',
    'Log data',
    'Execute script'
  ];

  this.esmPeriods = ['Day', 'Week', 'Month'];

  this.weeksOfMonth = ['First', 'Second', 'Third', 'Fourth', 'Fifth'];

  this.responseTypes = ['likert', 'likert_smileys', 'open text', 'list',
    'photo', 'location'
  ];

});
