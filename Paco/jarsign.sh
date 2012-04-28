#!/bin/sh

jarsigner -verbose -keystore android_keystore/quantified-self-release-key.keystore bin/Paco-release-unsigned.apk qskeys
