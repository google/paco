#!/bin/sh

if [ ! -f cookies.txt ];
then
  echo "Getting cookie..."
  curl -c cookies.txt -X POST -d "email=dxoigmn@gmail.com&isAdmind=true&continue=null" http://localhost:8080/_ah/login
fi

echo "Posting experiment..."
./generate_experiment.rb | curl -v -i -b cookies.txt -X POST -d @- -H "Content-Type: application/json" http://localhost:8080/observer/experiments
