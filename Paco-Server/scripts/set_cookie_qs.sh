#!/bin/sh

echo "enter email"
read email

echo "enter password"
stty_orig=`stty -g`
stty -echo
read secret
stty $stty_orig

export GDATA_AUTH=`curl 2>/dev/null https://www.google.com/accounts/ClientLogin \
  -d Email=$email -d Passwd=$secret -d accountType=HOSTED_OR_GOOGLE -d source=curlExample \
  -d service=ah | grep '^Auth=' | cut -c 6-`
echo $GDATA_AUTH

curl -c ~/.paco_qs_cookie "https://quantifiedself.appspot.com/_ah/login?auth=$GDATA_AUTH"

chmod 600 ~/.paco_qs_cookie


