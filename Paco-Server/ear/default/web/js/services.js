pacoApp.service('experimentService', ['$http', '$cacheFactory', 'util', 'config',
  function($http, $cacheFactory, util, config) {

    // Set this header here and it applies to all http requests
    $http.defaults.headers.common['pacoProtocol'] = 4;

    var cache = $cacheFactory.get('$http');

    return ({
      deleteExperiment: deleteExperiment,
      getExperimentList: getExperimentList,
      invalidateCachedList: invalidateCachedList,
      invalidateCachedLists: invalidateCachedLists,
      getExperiment: getExperiment,
      joinExperiment: joinExperiment,
      saveExperiment: saveExperiment,
    });

    function getExperimentList(listType, limit, cursor) {
      var endpoint = '/experiments?' + listType;

      if (limit) {
        endpoint += '&limit=' + config.listPageSize;
      }

      if (cursor !== undefined) {
        endpoint += '&cursor=' + cursor;
      }

      return $http.get(endpoint, {
        cache: true
      });
    }

    function invalidateCachedLists() {
      invalidateCachedList('admin', true);
      invalidateCachedList('joined');
      invalidateCachedList('mine', true);
    }

    function invalidateCachedList(listType, limit) {
      var endpoint = '/experiments?' + listType;

      if (limit) {
        endpoint += '&limit=' + config.listPageSize;
      }

      cache.remove(endpoint);
    }

    function getExperiment(id) {
      return $http.get('/experiments?id=' + id, {
        cache: true
      });
    }

    function joinExperiment(experiment) {
      var obj = {};
      obj.experimentId = experiment.id;
      obj.appId = 'webform';
      obj.experimentVersion = experiment.version;
      obj.experimentName = experiment.title;
      obj.responses = [{
        "name": "joined",
        "answer": true
      }];
      obj.responseTime = util.formatDate(new Date());
      var json = JSON.stringify(obj);

      return $http.post('/events', json);
    }

    function saveExperiment(experiment) {

      // The cache needs to be cleared here if the experiment title changes but
      // that feels overly aggressive to me.
      // cache.remove('/experiments?admin');
      // cache.remove('/experiments?joined');
      // cache.remove('/experiments?mine');

      // If it's not a new experiment, clear old cached definition.
      if (experiment.id) {
        cache.remove('/experiments?id=' + experiment.id);

      // If it's a new experiment, clear the cached admin list so the new
      // experiment appears.
      } else {
        var endpoint = '/experiments?admin&limit=' + config.listPageSize;
        cache.remove(endpoint);
      }

      return $http.post('/experiments', experiment);
    }

    function deleteExperiment(id) {
      return $http.post('/experiments?delete=1&id=' + id);
    }
  }
]);


pacoApp.service('dataService', ['$http', '$timeout', '$q',
  function($http, $timeout, $q) {

    return ({
      getEvents: getEvents,
      getParticipantData: getParticipantData,
    });


    function getEvents(id, user, anonymous) {

      var endpoint = '/events?q=\'experimentId=' + id;

      if (user) {
        endpoint += ':who=' + user;
      }

      endpoint += '\'&json';

      if (anonymous) {
        endpoint += '&anon=true';
      }

      return $http.get(endpoint);
    };


    /**
    * Gets stats data from PACO server endpoint. Iterates over data to
    * compute the total participant count for today and all time.
    */

    function getParticipantData(id, user) {

      var defer = $q.defer();
      var endpoint = 'participantStats?experimentId=' + id;
      if (user) {
        endpoint += '&who=' + user;
      }

      $http.get(endpoint).success(
        function(data) {
          var totalParticipantCount = 0;
          var todayParticipantCount = 0;
          for (var i = 0; i < data.participants.length; i++) {
            
            if (data.participants[i].todaySignalResponseCount > 0) {
              todayParticipantCount++;
            }
            
            if (data.participants[i].totalSignalResponseCount > 0) {
              totalParticipantCount++;
            }
          }
          data.todayParticipantCount = todayParticipantCount;
          data.totalParticipantCount = totalParticipantCount;

          defer.resolve({
            'data': data
          });
        });

      return defer.promise;
    }
  }
]);


pacoApp.service('config', function() {

  this.editTabs = [
    'basics',
    'groups',
    'admin',
    'source',
    'preview'
  ];

  this.listTabs = [
    'administered',
    'joined',
    'invited'
  ];

  this.dataDeclarations = {
    1: 'App Usage and Browser History',
    2: 'Location Information',
    3: 'Phone Details (Make, Model, Carrier)',
    4: 'Apps installed on the phone'
  };

  this.ringtones = [
    'Paco Bark',
    'Paco Alternate Alert Tone'
  ];

  this.scheduleTypes = [
    'Daily',
    'Weekdays',
    'Weekly',
    'Monthly',
    'Random sampling (ESM)'
  ];

  this.actionTypes = {
    1: 'Create notification to participate',
    2: 'Create notification message',
    // 3: 'Log data',
    4: 'Execute script'
  };

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
    'Missed call',
    'Call Started (in or out)',
    'Call Ended (in or out)',
    "Experiment joined",
    "Experiment ended",
    "Response received"
  ];

  this.esmPeriods = [
    'day',
    'week',
    'month'
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
    'Static Message',
    'Retrospective (QS default)',
    'Responsive (adaptive)',
    'Custom Code',
    'Disable Feedback'
  ];

  this.dataOrder = [
    'who',
    'responseTime',
    'scheduledTime',
    'experimentGroupName',
    'responses',
    'experimentVersion',
    'actionTriggerId',
    'actionId',
    'actionTriggerSpecId',
    'referredGroup',
    'eodResponseTime',
    'appId',
    'pacoId'
  ];

  this.timeColumns = [
    'responseTime',
    'scheduledTime',
    'when'
  ];

  this.listPageSize = 5;
});


pacoApp.service('template', function() {

  this.group = {
    actionTriggers: [],
    name: 'New Group',
    inputs: [],
    feedbackType: 0,
    feedback: {
      type: 0,
      text: 'Thanks for Participating!',
    },
    fixedDuration: 'false'
  };

  this.experiment = {
    admins: [],
    creator: '',
    contactEmail: '',
    extraDataCollectionDeclarations: [],
    groups: [this.group],
    postInstallInstructions: '<b>You have successfully joined the experiment!</b><br/><br/>\nNo need to do anything else for now.<br/><br/>\nPaco will send you a notification when it is time to participate.<br/><br/>\nBe sure your ringer/buzzer is on so you will hear the notification.',
    published: false,
    publishedUsers: [],
    ringtoneUri: '/assets/ringtone/Paco Bark',
  }

  this.input = {
    likertSteps: 5,
    responseType: 'open text'
  }

  this.otherAction = {
    type: 'pacoActionAllOthers'
  };

  this.defaultAction = {
    type: 'pacoNotificationAction',
    timeout: 15
  };

  this.schedule = {
    userEditable: true,
    timeout: 15,
    repeatRate: 1
  };

  this.defaultEsmSchedule = {
    esmFrequency: 8,
    esmPeriodInDays: 0,
    esmEndHour: 61200000,
    esmStartHour: 32400000,
    esmWeekends: true,
    minimumBuffer: 59,
    repeatRate: 1,
    scheduleType: 4,
    timeout: 15,
    userEditable: true
  };

  this.cue = {};

  this.scheduleTrigger = {
    type: 'scheduleTrigger',
    actions: [this.defaultAction],
    schedules: [this.schedule]
  };

  this.eventTrigger = {
    type: 'interruptTrigger',
    actions: [this.defaultAction],
    cues: [this.cue],
    minimumBuffer: 59
  };

  this.signalTime = {
    // Set initial time to 12 PM
    fixedTimeMillisFromMidnight: 12 * 60 * 60 * 1000,
    type: 0
  };
});


pacoApp.service('util', ['$filter', function($filter) {

  this.formatDate = function(date, timezone) {
    var tz = null;
    if (timezone !== undefined) {
      tz = timezone;
    }
    return $filter('date')(date, 'yyyy/MM/dd HH:mm:ssZ', tz);
  };

}]);
