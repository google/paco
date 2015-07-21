#!/bin/sh


curl -i -X POST -b ~/.paco_qs_staging_cookie https://quantifiedself-staging.appspot.com/experiments -H "Content-Type: application/json" --data-binary @$1

