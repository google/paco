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
      invalidateCachedList('public', true);
      invalidateCachedList('popular', true);
      invalidateCachedList('new', true);
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
      getParticipantStats: getParticipantStats,
      updateParticipantDateStats: updateParticipantDateStats,
    });

    function getEvents(id, user, anonymous, group, cursor) {

      var endpoint = '/events?q=\'experimentId=' + id;

      if (group && group !== 'all') {
        endpoint += ':experimentGroupName=' + group;
      }

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


    function unpackTotalStats(data, stats) {

      if (!stats.order) {
        stats.order = {};
        stats.data = [];
      }
      for (var i = 0; i < data.length; i++) {
        stats.order[data[i].who] = i;
        stats.data[i] = {'who': data[i].who};
        stats.data[i]['totalSignalMissCount'] = data[i]['missedR'];
        stats.data[i]['totalSignalResponseCount'] = data[i]['schedR'];
        stats.data[i]['totalSelfReportCount'] = data[i]['selfR'];
        stats.data[i]['totalSignalCount'] = data[i]['schedR'] + data[i]['missedR'];
        stats.data[i]['daySignalMissCount'] = 0;
        stats.data[i]['daySignalResponseCount'] = 0;
        stats.data[i]['daySelfReportCount'] = 0;
        stats.data[i]['daySignalCount'] = 0;
        stats.data[i]['lastContactDateTime'] = data[i]['lastContactDateTime'];
      }
    }

    function unpackDayStats(data, stats) {

      stats.dayParticipantCount = 0;

      for (var i = 0; i < data.length; i++) {
        stats.dayParticipantCount++;
        var row = data[i];
        var who = data[i]['who'];
        var colId = stats.order[who];

        stats.date = data[i].date;

        stats.data[colId]['daySignalMissCount'] = data[i]['missedR'];
        stats.data[colId]['daySignalResponseCount'] = data[i]['schedR'];
        stats.data[colId]['daySelfReportCount'] = data[i]['selfR'];
        stats.data[colId]['daySignalCount'] = data[i]['schedR'] + data[i]['missedR'];
      }
    }


    function statsDate(d) {
      var statsDateString = d.getFullYear() + '/' + (1 + d.getMonth()) + '/' + d.getDate();
      return statsDateString;
    }

    function zeroDateStats(stats) {
      for (var i = 0; i < stats.data.length; i++) {
        stats.data[i]['daySignalResponseCount'] = 0;
        stats.data[i]['daySelfReportCount'] = 0;
        stats.data[i]['daySignalCount'] = 0;
      }
    }

    function updateParticipantDateStats(id, date, stats) {
      var defer = $q.defer();
      var endpoint = 'participantStats?experimentId=' + id + '&statv2=1&reportType=date&date=' + statsDate(date);
      $http.get(endpoint).then(
        function(data) {
          zeroDateStats(stats);
          unpackDayStats(data.data, stats);
        });
    }

    function getParticipantStats(id, date, user, group) {
      if (user) {
        return getUserStats(id, user, group);
      }

      var defer = $q.defer();
      var endpointBase = 'participantStats?experimentId=' + id + '&statv2=1';

      if (group != 'all') {
        endpointBase += '&experimentGroupName=' + escape(group);
      }

      var endpoint1 = endpointBase + '&reportType=date&date=' + statsDate(date);
      var endpoint2 = endpointBase + '&reportType=total';

      var stats = {};
      stats.data = [];

      var p1 = $http.get(endpoint1);
      var p2 = $http.get(endpoint2);

      $q.all([p1, p2]).then(
        function(datas) {

          unpackTotalStats(datas[1].data, stats);
          unpackDayStats(datas[0].data, stats);

          defer.resolve({'data': stats});
        });

      return defer.promise;
    }

    /**
    * Gets stats data from PACO server endpoint. Iterates over data to
    * compute the total participant count for today and all time.
    */
    function getUserStats(id, user, group) {

      var defer = $q.defer();
      var endpoint = 'participantStats?experimentId=' + id + '&reportType=user&statv2=1'
      if (user) {
        endpoint += '&who=' + user;
      }

      if (group != 'all') {
        endpoint += '&experimentGroupName=' + escape(group);
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
              data[i].signals = data[i].missedR + data[i].schedR;
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

  this.hubTabs = [
      'popular',
      'new',
      'browse'
  ];

  this.dataDeclarations = {
    1: 'App Usage and Browser History',
    2: 'Location Information',
    3: 'Phone Details (Make, Model, Carrier)',
    4: 'Apps installed on the phone',
    5: 'Accessibility events'
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
    "Response received",
    "App removed",
    "App installed",
    "Permission changed",
    "View Clicked"
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
    'va_scale': 'Visual Analogue Scale',
    'likert_smileys': '5 Point Smiley Scale',
    'number': 'Number',
    'open text': 'Open Text',
    'list': 'List',
    'photo': 'Photo',
    'location': 'Location',
    'audio': 'Audio'
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
    rawDataAccess: true,
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

pacoApp.service('pubExperimentService', [ '$http', '$cacheFactory', 'util', 'config',
    function($http, $cacheFactory, util, config) {

      // Set this header here and it applies to all http requests
      $http.defaults.headers.common['pacoProtocol'] = 4;

      return ({
        getExperiment : getExperiment
      });

      function getExperiment(id) {
        return $http.get('/pubexperiments?id=' + id, {
          cache : true
        });
      }

    } ]);

