#!/bin/sh

jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore android_keystore/quantified-self-release-key.keystore bin/Paco-release-unsigned.apk qskeys
