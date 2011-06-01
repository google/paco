#!/bin/sh

# this is to clen up when indexes go haywire
appcfg.py -s appengine.google.com vacuum_indexes quantifiedself
