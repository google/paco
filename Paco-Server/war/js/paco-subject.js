// Namespace
window.Paco = { };

// Handlebars
Handlebars.registerHelper('each_with_name', function(context, options) {
  var fn = options.fn, inverse = options.inverse;
  var ret = "";

  if(context && context.length > 0) {
    for(var i=0, j=context.length; i<j; i++) {
      ret = ret + fn({ name: this.name, value: context[i] });
    }
  } else {
    ret = inverse(this);
  }
  return ret;
});

// Input
Paco.Input = Backbone.Model.extend();

Paco.InputsView = Backbone.View.extend({
  template: '#inputs-template',
  tagName: 'fieldset',
  id: 'inputs',

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
        this.appendInput(inputs[i].type, model);
        model.set(inputs[i]);
      }
    }
  },

  createInput: function(type, model) {
    switch (type) {
      case 'text':    return new Paco.TextInputView({ model: model });
      case 'list':    return new Paco.ListInputView({ model: model });
      case 'likert':  return new Paco.LikertInputView({ model: model });

      default: 
        console.log('Unable to render unknown type: ' + type);
        return null;
    }
  },

  appendInput: function(type, model) {
    this.$('#inputs-inputs').append(this.createInput(type, model).el);
  }
});

Paco.InputView = Backbone.View.extend({
  template: '#input-template',
  id: 'input',

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

    return this;
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

    return this;
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

    return this;
  }
});

// Subject Experiment
Paco.SubjectExperiment = Backbone.Model.extend({
  urlRoot: '/subject/experiments'
});

Paco.SubjectExperimentView = Backbone.View.extend({
  el: '#content',
  template: '#experiment-template',
  events: {
    'submit form': 'joinExperiment'
  },

  initialize: function() {
    this.model.bind('change', this.render, this);
    this.template = Handlebars.compile($(this.template).html());

    // Create sub-views
    this.inputsView = new Paco.InputsView({ model: this.model });
  },

  serialize: function() {
    return this.model.toJSON();
  },

  render: function() {
    this.$el.html(this.template(this.serialize()));
    this.$('#experiment-inputs').html(this.inputsView.$el);

    return this;
  },

  joinExperiment: function(e) {
    e.preventDefault();

    this.model.save(this.$('form').toObject());
  },
});

// Subject Experiments
Paco.SubjectExperiments = Backbone.Collection.extend({
  model: Paco.SubjectExperiment,
  url: '/subject/experiments'
});

Paco.SubjectExperimentsView = Backbone.View.extend({
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
    ':id':     'getSubjectExperiment',
    '' :        'getSubjectExperiments'
  },

  getSubjectExperiments: function() {
    this.model  = new Paco.SubjectExperiments();
    this.view   = new Paco.SubjectExperimentsView({ model: this.model });

    this.model.fetch();
  },
 
  getSubjectExperiment: function(id) {
    this.model = new Paco.SubjectExperiment({ id: id });
    this.view  = new Paco.SubjectExperimentView({ model: this.model });

    this.model.fetch();
  }
});

var paco_router = new Paco.Router();
Backbone.history.start();
