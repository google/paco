#!/bin/bash
#
# Copyright 2011 Google Inc. All Rights Reserved.


PROGRAM_NAME=$(basename $0)
CURRENT_DIR=$(basename $(pwd))

SRC_JAVA_PATH=java/com/google/sampling/experiential
WEB_XML=$SRC_JAVA_PATH/war/WEB-INF/appengine-web.xml

DEPLOY_DIR=deploy/war

DOMAIN=appspot.com
APP_CFG_SERVER=admin-console.$DOMAIN
APP_ID=$(grep '<application>' $WEB_XML | cut -d\> -f2 | cut -d\< -f1)
APP_VERSION=$(grep '<version>' $WEB_XML | cut -d\> -f2 | cut -d\< -f1)

echoAndExec() {
  echo -en "\n$PROGRAM_NAME: "
  echo "$*"
  echo 
  $*
}

gaeBuild() {
  echoAndExec build $SRC_JAVA_PATH || exit
}

gaeUpdate() {
  echo 
  echo "Running GAE Update ..."

  echo "Uploading to http://$APP_VERSION.latest.$APP_ID.$DOMAIN ..."
  echoAndExec appcfg.sh --enable_jar_splitting update $BUILT_DEPLOY_DIR || exit
  echo
  echo "You can now access http://$APP_VERSION.latest.$APP_ID.$DOMAIN ..."
}

echo
echo "============================================================"
echo "Paco's GAE PUSH SCRIPT"
echo "============================================================"
echo

gaeBuild
gaeUpdate
