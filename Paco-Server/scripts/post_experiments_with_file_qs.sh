#!/bin/sh


curl -i -X POST -b ~/.paco_qs_cookie https://quantifiedself.appspot.com/experiments -H "Content-Type: application/json" --data-binary @$1

