#!/bin/sh

echo "enter email"
read email

curl -c ~/.paco_lh_cookie http://127.0.0.1:8888/_ah/login?continue=http%3A%2F%2F127.0.0.1%3A8888%2FMain.html -d email=$email -d isAdmin=on -d continue=http://127.0.0.1:8888/Main.html -d action="Log In"

chmod 600 ~/.paco_lh_cookie
