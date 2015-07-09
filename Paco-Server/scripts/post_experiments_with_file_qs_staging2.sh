#!/bin/sh


curl -i -X POST -b ~/.paco_qs_staging2_cookie https://quantifiedself-staging2.appspot.com/experiments -H "Content-Type: application/json" --data-binary @$1

