#!/bin.sh

# example script for generating an android key for signing your apk.
# without the angles < >, this generates a default debug key.

keytool -genkey -v -keystore <debug.keystore> -alias <androiddebugkey> -storepass <android> -keypass <android> -keyalg RSA -validity 14000