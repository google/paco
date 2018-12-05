#!/bin/sh


curl -i -X POST -b ~/.paco_lh_cookie http://127.0.0.1:8888/experiments -H "Content-Type: application/json" --data-binary @$1

