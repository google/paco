#!/usr/bin/env ruby

# Run this script on a download of the logs, using a command such as:
#
#  appcfg.sh \
# --num_days=41 request_logs ../war latest_logs


require 'date'

def paco_downloads
    f = File.readlines("latest_logs")
    pl = f.select {|l| l =~ /paco\.apk/}
    o = File.open("paco_#{Date.today.to_s}.log", 'w')
    pl.reverse.each {|l| o.puts l }
    o.close
end

