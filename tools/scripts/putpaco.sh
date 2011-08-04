#/bin/sh

# this is an example of a script that posts one event to a pacoserver

curl -b ~/.paco_cookie -d '[{ "who" : "<yourid>@gmail.com", "when" : "20100225:08:38:59+0000",
"responseTime" : "20100225:08:38:59+0000", "scheduledTime" : "20100225:08:38:59+0000", "appId" : "android_paco", "pacoVersion" : "1", "experimentId" : "1", "responses" : [{ "name" : "q1", "answer" : "hi", "inputId" : "4"}] }]' http://<YourAppIDHere>.appspot.com/events
