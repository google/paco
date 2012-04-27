#!/bin/sh

jarsigner -verbose -keystore android_keystore/v2_keys bin/Paco-release-unsigned.apk signing
