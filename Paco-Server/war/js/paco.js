// Namespace
window.Paco = { }

// Signal
Paco.SignalView = Backbone.View.extend({
  template: '#signal-template',
  id: 'signal',
  events: {
    'change #signal-type': 'onTypeChange',
  },
  
  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.fixedSignalView = new Paco.FixedSignalView({ model: this.model });
    this.randomSignalView = new Paco.RandomSignalView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    // Update sub-views
    this.$('#signal-type').val(this.model.get('signal').type);
    this.onTypeChange();

    return this;
  },

  onTypeChange: function() {
    this.fixedSignalView.$el.detach();
    this.randomSignalView.$el.detach();

    switch (this.$('#signal-type').val()) {
      case 'fixed':
        this.$('#signal-widget').html(this.fixedSignalView.$el);
        break;
      case 'random':
        this.$('#signal-widget').html(this.randomSignalView.$el);
        break;
    }
  }
});

Paco.FixedSignalView = Backbone.View.extend({
  template: '#fixedSignal-template',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    var times = this.model.get('signal').times;

    if (times) {
      for (i in times) {
        this.appendTimeView(times[i]);
      }
    } else {
      this.appendTimeView();
    }

    return this;
  },

  createTimeView: function(time) {
    var timeView = new Paco.FixedSignalView.TimeView({ model: time }).render();

    timeView.on('add', this.addTimeView, this);
    timeView.on('remove', this.removeTimeView, this);

    return timeView;
  },

  appendTimeView: function(time) {
    this.$('#fixedSignal-times').append(this.createTimeView(time).el);
  },

  addTimeView: function(timeView) {
    timeView.$el.after(this.createTimeView().el);
  },

  removeTimeView: function(timeView) {
    timeView.$el.remove();

    if (this.$el.find('#fixedSignal-time').length == 0) {
      this.appendTimeView();
    }
  }
});

Paco.FixedSignalView.TimeView = Backbone.View.extend({
  template: '#fixedSignal-time-template',
  events: {
    'click #add': 'onAddClick',
    'click #remove': 'onRemoveClick',
  },

  initialize: function() {
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return {value: this.model};
  },

  render: function() {
    this.setElement(this.template(this.serialize()));

    return this;
  },

  onAddClick: function() {
    this.trigger('add', this);
  },

  onRemoveClick: function() {
    this.trigger('remove', this);
  }
});

Paco.RandomSignalView = Backbone.View.extend({
  template: '#randomSignal-template',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    return this;
  }
});

// Schedules
Paco.ScheduleView = Backbone.View.extend({
  template: '#schedule-template',
  id: 'schedule',
  events: {
    'change #schedule-type': 'onTypeChange',
  },

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.dailyScheduleView = new Paco.DailyScheduleView({ model: this.model });
    this.weeklyScheduleView = new Paco.WeeklyScheduleView({ model: this.model });
    this.monthlyScheduleView = new Paco.MonthlyScheduleView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    // Update sub-views
    this.$('#schedule-type').val(this.model.get('schedule').type);
    this.onTypeChange();

    return this;
  },

  onTypeChange: function() {
    this.dailyScheduleView.$el.detach();
    this.weeklyScheduleView.$el.detach();
    this.monthlyScheduleView.$el.detach();

    switch (this.$('#schedule-type').val()) {
      case 'daily':
        this.$('#schedule-widget').html(this.dailyScheduleView.$el);
        break;
      case 'weekly':
        this.$('#schedule-widget').html(this.weeklyScheduleView.$el);
        break;
      case 'monthly':
        this.$('#schedule-widget').html(this.monthlyScheduleView.$el);
        break;
    }
  }
});

Paco.DailyScheduleView = Backbone.View.extend({
  id: 'schedule-dailySchedule',
  template: '#dailySchedule-template',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    return this;
  }
});


Paco.WeeklyScheduleView = Backbone.View.extend({
  id: 'schedule-weeklySchedule',
  template: '#weeklySchedule-template',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    return this;
  }
});


Paco.MonthlyScheduleView = Backbone.View.extend({
  id: 'schedule-monthlySchedule',
  template: '#monthlySchedule-template',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    return this;
  }
});

Paco.SignalScheduleView = Backbone.View.extend({
  template: '#signalSchedule-template',
  id: 'signalSchedule',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.signalView = new Paco.SignalView({ model: this.model });
    this.scheduleView = new Paco.ScheduleView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));
    this.$('#signalSchedule-signal').html(this.signalView.$el);
    this.$('#signalSchedule-schedule').html(this.scheduleView.$el);

    return this;
  }
});

Paco.SignalSchedule = Backbone.Model.extend({
  urlRoot: '/experiments'
});

// Experiment
Paco.Experiment = Backbone.Model.extend({
  urlRoot: '/experiments'
});

Paco.ExperimentView = Backbone.View.extend({
  el: '#content',
  template: '#experiment-template',
  events: {
    'submit form': 'joinExperiment'
  },

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.signalSchedule = new Paco.SignalSchedule({ id: this.model.id });
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.signalScheduleView = new Paco.SignalScheduleView({ model: this.signalSchedule });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.signalSchedule.set(this.model.get('signalSchedule'));

    this.$el.html(this.template(this.serialize()));
    this.$('#experiment-signalSchedule').html(this.signalScheduleView.$el);

    return this;
  },

  joinExperiment: function(e) {
    e.preventDefault();

    this.signalSchedule.save(this.$('form').toObject());
  },
});

// Experiments
Paco.Experiments = Backbone.Collection.extend({
  model: Paco.Experiment,
  url: '/experiments'
});

Paco.ExperimentsView = Backbone.View.extend({
  el: '#content',
  template: '#experiments-template',

  initialize: function() {
    this.model.bind('reset', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return { experiments: this.model.toJSON() };
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    return this;
  }
});

Paco.Router = Backbone.Router.extend({
  routes: {
    ':id':     'getExperiment',
    '' :        'getExperiments'
  },

  getExperiments: function() {
    this.model  = new Paco.Experiments();
    this.view   = new Paco.ExperimentsView({ model: this.model });

    this.model.fetch();
  },
 
  getExperiment: function(id) {
    this.model = new Paco.Experiment({ id: id });
    this.view  = new Paco.ExperimentView({ model: this.model });

    this.model.fetch();
  }
});

var paco_router = new Paco.Router();
Backbone.history.start();
