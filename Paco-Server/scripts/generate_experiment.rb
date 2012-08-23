#!/usr/bin/env ruby

# Dependencies:
#   gem install json populator

require 'rubygems'
require 'json'
require 'populator/random'
require 'random_data'

class Random
  def Random.time
    "1980-01-01T%02d:%02d:%02dZ" % [rand(24), rand(60), rand(60)]
  end
end

def signal
  [random_signal, fixed_signal].rand
end

def random_signal
  {
    :type       => 'random',
    :startTime  => Random.time,
    :endTime    => Random.time,
    :frequency  => rand(30)
  }
end

def fixed_signal
  {
    :type   => 'fixed',
    :times  => rand(8).times.to_a.map { Random.time }
  }
end

def schedule
  startDate = Random.date
  endDate   = startDate + rand(30)
  {
    :startDate  => startDate, 
    :endDate    => endDate
  }.merge([daily_schedule, weekly_schedule, monthly_schedule].rand)
end

def daily_schedule
  {
    :type   => 'daily',
    :every  => rand(30),
  }
end

def weekly_schedule
  {
    :type       => 'weekly',
    :every      => rand(30),
    :dayRepeat  => rand(127)
  }
end

def monthly_schedule
  {
    :type       => 'monthly',
    :every      => rand(30),
    :dayRepeat  => rand(127),
    :byDay      => (rand > 0.5),
    :weekRepeat => rand(127)
  }
end

def signal_schedule 
  {
    :editable => (rand > 0.5),
    :signal   => signal,
    :schedule => schedule,
  }
end

def text_input
  {
    :type                   => 'text',
    :question   => Populator.words(5..10),
    :multiline  => (rand > 0.5),
  }
end

def likert_input
  {
    :type     => 'likert',
    :question => Populator.words(5..10),
    :smileys  => (rand > 0.5),
    :labels   => rand(5).times.to_a.map { Populator.words(2..4) }
  }
end

def list_input
  {
    :type         => 'list',
    :question     => Populator.words(5..10),
    :multiselect  => (rand > 0.5),
    :choices      => rand(5).times.to_a.map { Populator.words(2..4) }
  }
end

def inputs
  rand(10).times.to_a.map do
    {
      :name                   => Populator.words(1..5).gsub(' ', '_'),
      :required               => (rand > 0.5),
      :conditionalExpression  => ''
    }.merge([text_input, likert_input, list_input].rand)
  end
end

def experiment
   {
    :title          => Populator.words(5..10),
    :description    => Populator.paragraphs(1..2),
    :creator        => Populator.words(2..5),
    :consentForm    => Populator.paragraphs(1..3),
    :feedback       => Populator.paragraphs(1),
    :signalSchedule => signal_schedule,
    :inputs         => inputs,
    :viewers        => (rand > 0.5) ? nil : rand(5).times.to_a.map { Random.email },
    :observers      => (rand > 0.5) ? nil : rand(5).times.to_a.map { Random.email }
  }
end

puts experiment.to_json
