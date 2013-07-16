#/bin/sh

curl -b ~/.paco_qs_cookie -d '[{ "who" : "@gmail.com", "responseTime" : "20120801:08:38:59+0000", "scheduledTime" : "20120801:08:38:59+0000", "experimentId" : "7", "responses" : [{ "name" : "picture", "answer" : "blob", "id" : "10"}] }]'  "https://localhost:8888/events"
