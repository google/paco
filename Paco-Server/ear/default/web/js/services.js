pacoApp.service('experimentService', ['$http', '$cacheFactory', 'util',
  function($http, $cacheFactory, util) {

    var cache = $cacheFactory.get('$http');

    return ({
      deleteExperiment: deleteExperiment,
      getAdministered: getAdministered,
      getJoinable: getJoinable,
      getJoined: getJoined,
      getExperiment: getExperiment,
      joinExperiment: joinExperiment,
      saveExperiment: saveExperiment,
    });


    function getJoined(reload) {
      if (reload !== undefined && reload === true) {
        cache.remove('/experiments?joined');
      }
      return $http.get('/experiments?joined', {
        cache: true
      });
    }

    function getAdministered(reload) {
      if (reload !== undefined && reload === true) {
        cache.remove('/experiments?admin');
      }
      return $http.get('/experiments?admin', {
        cache: true
      });
    }

    function getJoinable(reload) {
      if (reload !== undefined && reload === true) {
        cache.remove('/experiments?mine');
      }
      return $http.get('/experiments?mine', {
        cache: true
      });
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

      // Need to clear all list caches in case title was changed
      cache.remove('/experiments?admin');
      cache.remove('/experiments?joined');
      cache.remove('/experiments?joinable'); 

      // If it's not a new experiment, clear old cached definition
      if (experiment.id) {
        cache.remove('/experiments?id=' + experiment.id);
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
      getCsv: getCsv,
      getParticipantData: getParticipantData,
    });

    function getCsv(id, user, anonymous) {

      var maxTries = 10;
      var startMarker = '<title>Current Status of Report Generation for job: ';
      var endMarker = '</title>';
      var endpoint = '/events?q=\'experimentId=' + id;
      var jobUrl;
      var defer = $q.defer();
      var tryCount = 0;

      if (user) {
        endpoint += ':who=' + user;
      }

      endpoint += '\'&csv';

      if (anonymous) {
        endpoint += '&anon=true';
      }

      $http.get(endpoint).success(
        function(data) {
          //TODO: endpoint should return report URL, not HTML
          startPos = data.indexOf(startMarker) + startMarker.length;
          endPos = data.indexOf(endMarker);
          if (startPos !== -1 && endPos !== -1) {
            jobUrl = '/jobStatus?jobId=' + data.substring(startPos,
              endPos) + '&cmdline=1';
            poll();
          }
        });

      var poll = function() {
        if (tryCount >= maxTries) {
          defer.resolve({
            'error': 'Exceeded max tries'
          });
          return;
        }
        tryCount++;

        $http.get(jobUrl).success(
          function(data) {
            if (data === 'pending\n') {
              $timeout(poll, 1000);
            } else {
              var csv = data.trim();
              defer.resolve({
                'data': csv
              });
            }
          }
        )
      };
      return defer.promise;
    }


    /**
    * Gets stats data from PACO server endpoint. Iterates over data to
    * compute the total participant count for today and all time.
    */

    function getParticipantData(id) {

      var defer = $q.defer();
      var url = 'participantStats?experimentId=' + id;
      $http.get(url).success(
        function(data) {
          var totalParticipantCount = 0;
          var todayParticipantCount = 0;
          for (var i = 0; i < data.participants; i++) {
            
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

  this.tabs = [
    'basics',
    'groups',
    'admin',
    'source',
    'preview'
  ];

  this.dataDeclarations = {
    1: 'App Usage and Browser History',
    2: 'Location Information',
    3: 'Phone Details (Make, Model, Carrier)'
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
    published: false,
    publishedUsers: [],
    groups: [this.group],
    dataDeclarations: [],
    ringtoneUri: '/assets/ringtone/Paco Bark'
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
    fixedTimeMillisFromMidnight: 0,
    type: 0
  };
});


pacoApp.service('util', ['$filter', function($filter) {

  this.formatDate = function(date) {
    return $filter('date')(date, 'yyyy/MM/dd HH:mm:ssZ');
  };

}]);
