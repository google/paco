// Namespace
window.Paco = { }

// Input
Paco.InputsView = Backbone.View.extend({
  template: '#inputs-template',
  tagName: 'fieldset',
  id: 'inputs',
  count: 0,

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    var inputs = this.model.attributes.inputs;

    if (inputs) {
      for (i in inputs) {
        var model = new Paco.Input();
        this.appendInput(model);
        model.set(inputs[i]);
      }
    } else {
      var model = new Paco.Input();
      this.appendInput(model);
    }
  },

  createInput: function(model) {
    // Need to index each input for an experiment
    model.attributes.id = this.count;
    this.count += 1;

    var input = new Paco.InputView({ model: model }).render();

    input.on('add', this.addInput, this);
    input.on('remove', this.removeInput, this);

    return input;
  },

  appendInput: function(model) {
    this.$('#inputs-inputs').append(this.createInput(model).el);
  },

  addInput: function(input) {
    input.$el.after(this.createInput(new Paco.Input(input)).el);
  },

  removeInput: function(input) {
    input.$el.remove();

    if (this.$el.find('#experiment-input').length == 0) {
      this.appendInput(new Paco.Input());
    }
  }
})

Paco.Input = Backbone.Model.extend();

Paco.InputView = Backbone.View.extend({
  template: '#input-template',
  id: 'input',
  events: {
    'change #input-type':   'onTypeChange',
    'click #input-add':     'onAddClick',
    'click #input-remove':  'onRemoveClick'
  },

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.textInputView = new Paco.TextInputView({ model: this.model });
    this.likertInputView = new Paco.LikertInputView({ model: this.model });
    this.listInputView = new Paco.ListInputView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    // Update sub-views
    this.$('#input-type').val(this.model.attributes.type);
    this.onTypeChange();

    return this;
  },

  onTypeChange: function() {
    this.textInputView.$el.detach();
    this.likertInputView.$el.detach();
    this.listInputView.$el.detach();

    switch (this.$('#input-type').val()) {
      case 'text':
        this.$('#input-widget').html(this.textInputView.$el);
        break;
      case 'likert':
        this.$('#input-widget').html(this.likertInputView.$el);
        break;
      case 'list':
        this.$('#input-widget').html(this.listInputView.$el);
        break;
    }
  },

  onAddClick: function() {
    this.trigger('add', this);
  },

  onRemoveClick: function() {
    this.trigger('remove', this);
  }
});

Paco.TextInputView = Backbone.View.extend({
  template: '#textInput-template',
  id: 'textInput',

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

Paco.LikertInputView = Backbone.View.extend({
  template: '#likertInput-template',
  id: 'likertInput',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    var id      = this.model.attributes.id,
        labels  = this.model.attributes.labels;

    if (labels) {
      for (i in labels) {
        this.appendLabelView({id: id, value: labels[i]});
      }
    } else {
      this.appendLabelView({id: id});
    }

    return this;
  },

  createLabelView: function(model) {
    var labelView = new Paco.LikertInputView.LabelView({ model: model }).render();

    labelView.on('add', this.addLabelView, this);
    labelView.on('remove', this.removeLabelView, this);

    return labelView;
  },

  appendLabelView: function(model) {
    this.$('#likertInput-labels').append(this.createLabelView(model).el);
  },

  addLabelView: function(labelView) {
    labelView.$el.after(this.createLabelView({ id: this.model.attributes.id }).el);
  },

  removeLabelView: function(labelView) {
    labelView.$el.remove();

    if (this.$el.find('#likertInput-label').length == 0) {
      this.appendLabelView({id: this.model.attributes.id});
    }
  }
});

Paco.LikertInputView.LabelView = Backbone.View.extend({
  template: '#likertInput-label-template',
  events: {
    'click #add': 'onAddClick',
    'click #remove': 'onRemoveClick',
  },

  initialize: function() {
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model;
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

Paco.ListInputView = Backbone.View.extend({
  template: '#listInput-template',
  id: 'listInput',

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));

    var id      = this.model.attributes.id,
        choices = this.model.attributes.choices;

    if (choices) {
      for (i in choices) {
        this.appendChoiceView({ id: id, value: choices[i] });
      }
    } else {
      this.appendChoiceView({ id: id });
    }

    return this;
  },

  createChoiceView: function(model) {
    var choiceView = new Paco.ListInputView.ChoiceView({ model: model }).render();

    choiceView.on('add', this.addChoiceView, this);
    choiceView.on('remove', this.removeChoiceView, this);

    return choiceView;
  },

  appendChoiceView: function(model) {
    this.$('#listInput-choices').append(this.createChoiceView(model).el);
  },

  addChoiceView: function(choiceView) {
    choiceView.$el.after(this.createChoiceView({ id: this.model.attributes.id }).el);
  },

  removeChoiceView: function(choiceView) {
    choiceView.$el.remove();

    if (this.$el.find('#listInput-choice').length == 0) {
      this.appendChoiceView({ id: this.model.attributes.id });
    }
  }
});

Paco.ListInputView.ChoiceView = Backbone.View.extend({
  template: '#listInput-choice-template',
  events: {
    'click #add': 'onAddClick',
    'click #remove': 'onRemoveClick',
  },

  initialize: function() {
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model;
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
    this.$('#signal-type').val(this.model.attributes.signalSchedule.signal.type);
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

    var times = this.model.attributes.signalSchedule.signal.times;

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
    this.$el.append(this.template(this.serialize()));

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
    this.$('#schedule-type').val(this.model.attributes.signalSchedule.schedule.type);
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
    this.$el.append(this.template(this.serialize()));
    this.$('#signalSchedule-signal').html(this.signalView.$el);
    this.$('#signalSchedule-schedule').html(this.scheduleView.$el);

    return this;
  }
});

// Experiment
Paco.Experiment = Backbone.Model.extend({
  urlRoot: '/observer/experiments',
  defaults: {
    'title': '',
    'description': '',
    'consentForm': '',
    'inputs': [
      { 'type': 'text' }
    ],
    'signalSchedule': {
        'editable': true,
        'signal': {
            'type': 'random',
        },
        'schedule': {
            'type': 'daily',
        }
    },
    'feedback': 'Thanks for taking my experiment!',
    'published': false,
    'observers': null,
    'viewers': null
  }
});

Paco.ExperimentView = Backbone.View.extend({
  el: '#content',
  template: '#experiment-template',
  events: {
    'submit form': 'createExperiment'
  },

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.observersView      = new Paco.ObserversView({ model: this.model });
    this.viewersView        = new Paco.ViewersView({ model: this.model });
    this.signalScheduleView = new Paco.SignalScheduleView({ model: this.model });
    this.inputsView         = new Paco.InputsView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));
    this.$('#experiment-observers').html(this.observersView.$el);
    this.$('#experiment-viewers').html(this.viewersView.$el);
    this.$('#experiment-signalSchedule').html(this.signalScheduleView.$el);
    this.$('#experiment-inputs').html(this.inputsView.$el);

    return this;
  },

  createExperiment: function(e) {
    e.preventDefault();

    this.model.save(this.$('form').toObject());
  },
});

Paco.ObserversView = Backbone.View.extend({
  id: 'observers',

  initialize: function() {
    this.model.bind('change', this.render, this);
  },

  render: function() {
    var observers = this.model.attributes.observers;

    if (observers) {
      for (i in observers) {
        this.appendObserverView({ value: observers[i] });
      }
    } else {
      this.appendObserverView({});
    }

    return this;
  },

  createObserverView: function(model) {
    var observerView = new Paco.ObserversView.ObserverView({ model: model }).render();

    observerView.on('add', this.addObserverView, this);
    observerView.on('remove', this.removeObserverView, this);

    return observerView;
  },

  appendObserverView: function(model) {
    this.$el.append(this.createObserverView(model).el);
  },

  addObserverView: function(observerView) {
    observerView.$el.after(this.createObserverView({}).el);
  },

  removeObserverView: function(observerView) {
    observerView.$el.remove();

    if (this.$el.children().length == 0) {
      this.appendObserverView({});
    }
  }
});

Paco.ObserversView.ObserverView = Backbone.View.extend({
  template: '#experiment-observer-template',
  events: {
    'click #add': 'onAddClick',
    'click #remove': 'onRemoveClick',
  },

  initialize: function() {
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model;
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

Paco.ViewersView = Backbone.View.extend({
  id: 'viewers',

  initialize: function() {
    this.model.bind('change', this.render, this);
  },

  render: function() {
    var viewers = this.model.attributes.viewers;
    
    if (viewers) {
      for (i in viewers) {
        this.appendViewerView({ value: viewers[i] });
      }
    } else {
      this.appendViewerView({});
    }

    return this;
  },

  createViewerView: function(model) {
    var viewerView = new Paco.ViewersView.ViewerView({ model: model }).render();

    viewerView.on('add', this.addViewerView, this);
    viewerView.on('remove', this.removeViewerView, this);

    return viewerView;
  },

  appendViewerView: function(model) {
    this.$el.append(this.createViewerView(model).el);
  },

  addViewerView: function(viewerView) {
    viewerView.$el.after(this.createViewerView({}).el);
  },

  removeViewerView: function(viewerView) {
    viewerView.$el.remove();

    if (this.$el.children().length == 0) {
      this.appendViewerView({});
    }
  }
});

Paco.ViewersView.ViewerView = Backbone.View.extend({
  template: '#experiment-viewer-template',
  events: {
    'click #add': 'onAddClick',
    'click #remove': 'onRemoveClick',
  },

  initialize: function() {
    this.template = Handlebars.compile($(this.template).html());
  },

  serialize: function() {
    return this.model;
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

// Experiments
Paco.Experiments = Backbone.Collection.extend({
  model: Paco.Experiment,
  url: '/observer/experiments'
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
    'observer/experiments/create':  'createObserverExperiment',
    'observer/experiments/:id':     'getObserverExperiment',
    'observer/experiments' :        'getObserverExperiments'
  },

  createObserverExperiment: function() {
    this.model = new Paco.Experiment();
    this.view = new Paco.ExperimentView({ model: this.model });

    this.model.trigger('change');
  },

  getObserverExperiments: function() {
    this.model  = new Paco.Experiments();
    this.view   = new Paco.ExperimentsView({ model: this.model });

    this.model.fetch();
  },
 
  getObserverExperiment: function(id) {
    this.model = new Paco.Experiment({ id: id });
    this.view  = new Paco.ExperimentView({ model: this.model });

    this.model.fetch();
  }
});

var paco_router = new Paco.Router();
Backbone.history.start();
