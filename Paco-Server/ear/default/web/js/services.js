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

      if (cursor !== undefined && cursor !== null) {
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
        'name': 'joined',
        'answer': true
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
      invalidateCachedLists();
      return $http.post('/experiments?delete=1&id=' + id);
    }
  }
]);


pacoApp.service('dataService', ['$http', '$timeout', '$q', 'config',
  function($http, $timeout, $q, config) {

    return ({
      getEvents: getEvents,
      getReport: getReport,
      getParticipantData: getParticipantData,
      getDashboardStats: getDashboardStats,
    });

    function getEvents(id, user, anonymous, cursor) {

      var endpoint = '/events?q=\'experimentId=' + id;

      if (user) {
        endpoint += ':who=' + user;
      }

      endpoint += '\'&json&includePhotos=true';

      if (anonymous) {
        endpoint += '&anon=true';
      }

      endpoint += '&limit=' + config.dataPageSize;

      if (cursor !== undefined) {
        endpoint += '&cursor=' + cursor;
      }

      return $http.get(endpoint);
    };


    function getReport(id, user, type, anonymous, photos) {

        var maxTries = 1000; // never give up!
        var startMarker = '<title>Current Status of Report Generation for job: ';
        var endMarker = '</title>';
        var endpoint = '/events?q=\'experimentId=' + id;
        var jobUrl;
        var defer = $q.defer();
        var tryCount = 0;

        if (user) {
          endpoint += ':who=' + user;
        }

        endpoint += '\'&' + type + '&cmdline=1';

        if (anonymous) {
          endpoint += '&anon=true';
        }

        if (photos) {
          endpoint += '&includePhotos=true';
        }

        $http.get(endpoint).success(
          function(data) {

            // JSON endpoint directly returns data. No need to ping for
            // job status.
            if (type === 'json') {
              var json = JSON.stringify(data.events);
              defer.resolve({'data': json});
            } else {
              jobUrl = '/jobStatus?jobId=' + data + '&cmdline=1';
              poll();
            }
          }
        );

        var poll = function() {
          if (tryCount >= maxTries) {
            defer.resolve({'error': 'Exceeded max tries'});
            return;
          }
          tryCount++;

          $http.get(jobUrl).success(
            function(data) {
              if (data === 'pending\n') {
                $timeout(poll, 3000);
              } else {
                var csv = data.trim();
                defer.resolve({'data': csv});
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

    function getParticipantData(id, user) {

      var defer = $q.defer();
      var endpoint = 'participantStats?experimentId=' + id;
      if (user) {
        endpoint += '&who=' + user;
      }

      $http.get(endpoint).success(
        function(data) {
          if (!user) {
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
          } else {
        	  data.responseRate = 0;
        	  data.signaledResponseCount = 0;
        	  data.missedResponseCount = 0;
        	  data.selfReportResponseCount = 0;
        	  for (var i = 0; i < data.length; i++) {
        		  data.signaledResponseCount += data[i].schedR;
        		  data.missedResponseCount += data[i].missedR;
        		  data.selfReportResponseCount += data[i].selfR;
        	  }
        	  data.totalSignalCount = data.signaledResponseCount + data.missedResponseCount;
        	  if ((data.totalSignalCount) > 0) {
        		  data.responseRate = data.signaledResponseCount / data.totalSignalCount;
        	  } else {
        		  data.responseRate = 0;
        	  }
          }
          defer.resolve({
            'data': data
          });
        });

      return defer.promise;
    }

    function getDashboardStats() {

      var fakeData = {
          'now': 1456868845883,
          'experimentTotalCount': 3554,
          'domainExperimentCount': 551,
          'domainFutureExperimentCount': 3,
          'domainPastExperimentCount': 222,
          'domainPresentExperimentCount': 0,
          'domainOngoingExperimentCount': 326,
          'domainPublishedExperimentCount': 298,
          'domainPublishedUserCounts': [
              107,0,17,1,0,263,0,0,410,0,1,4,2,2,1,0,0,0,0,69,1,0,14,0,12,0,69,5,4,205,1,2,0,1,0,482,0,0,0,1,27,0,4,0,23,0,0,162,162,331,8,8,8,0,357,0,232,1,0,33,0,0,480,476,4,232,8,1,387,338,40,2,225,3,0,221,2,235,235,0,14,15,
              14,248,247,1,7,0,0,0,18,0,0,2,0,0,0,0,0,16,5,5,1,0,0,45,45,119,0,4,20,0,18,0,18,0,0,0,37,3,12,0,0,0,11,12,0,0,0,3,0,0,18,17,6,17,18,6,0,125,1,27,28,0,0,1,0,0,0,18,18,0,58,1,0,0,0,0,0,0,0,0,2,2,2,0,1,0,0,0,0,7,31,31,31,6,31,0,27,27,27,27,108,2,28,1,0,0,0,0,0,2,0,2,0,857,0,269,0,1,1,2,0,1,1,1,11,15,0,0,0,0,1,2,4,24,0,2,0,0,14,1,0,1,2,0,
              2,1,0,2,2,4,1,1,2,1,0,4,13,1,109,1,2,0,0,0,26,2,28,0,0,2,2,0,1,2,0,0,1,0,1,1,0,0,0,2,3,91,0,0,1,0,0,0,69,2,1,0,1,1,1,31,0,0,0,0,7,291,6,5,0,2,2,1,0,108,2,1,7,0,0,20,0,2,2,18,7,2,1,178,0,2,1,27,0,0,0,1,0,0,136,1,4,3,0,0,0,1,0,0,505,1,1,1,3,0,1,1,19,4,14,2,1,1,29,0,0,8,0,0,13,0,0,0,0,1,14,0,0,71,1,0,0,2,2,73,0,0,0,0,1,3,2,0,24,33,1,0,17,0,4,8,0,13,0,0,1,0,2,10,0,1,52,1,5,0,13,0,377,11,0,1,1,0,4,2,0,0,0,29,0,0,0,2,8,1,1,1,0,66,0,1,0,34,2,0,71,2,0,14,56,0,338,21,89,2,0,9,3,0,97,1,4,857,2,0,68,49,5,16,91,0,0,3,0,0,218,0,2,2,1,0,1,7,29,6,0,2,2,0,69,0,0,0,19,2,0,0,5,0,20,14,19,5,0,0,0,0,1,0,0,1,2,1758,56,2,0,0,15,1,23,0,1,0,1,0,2,0,81,16,1758,3,0,1,0,505,0,290,0,1,25,0,9,1,4,1,0,1,0,0,52,1,0,13,1,1,0,0,0,0,0,2,0,0,1,1,3,0,0
          ],
          'nowAsString': '2016/03/01 21:47:25+0000'
      };

      return $q.resolve(fakeData);
    }
}]);


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
    'Experiment joined',
    'Experiment ended',
    'Response received'
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

  this.responseTypes = {
    'likert': 'Scale',
    'likert_smileys': '5 Point Smiley Scale',
    'open text': 'Open Text',
    'list': 'List',
    'photo': 'Photo',
    'location': 'Location'
  };

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

  this.helpLinkBase = 'https://docs.google.com/a/google.com/document/d/1o81ps90gGT3SYEKS1meHfqee-A8c65-Jailz3A1Uwmg/pub?embedded=true';

  this.helpLinks = {
    'advanced': 'h.le5i22y0oxrv',
    'app-triggers': 'h.roauu5tvawhu',
    'conditional': 'h.p8esi25lpyip',
    'experiment-groups': 'h.3xccjkfufpig',
    'inputs': 'h.rfj5zaiuklqq',
    'triggers': 'h.ax1l2jwvrkxo'
  }

  this.listPageSize = 50;
  this.dataPageSize = 100;
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
    timeout: 15,
    color: 0,
    delay: 0,
    dismissible: true
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
