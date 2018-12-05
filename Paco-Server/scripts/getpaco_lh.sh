#/bin/bash

echo "enter experimentId"
read experiment_id

echo "enter report format"
read report_format
 
echo "output file name"
read outfile

if [ "$report_format" = "json" ]; then
    result=`curl -L -b ~/.paco_qs_cookie "https://127.0.0.1:8888/events?q=experimentId=$experiment_id&json"`
else
    joburl=`curl -L -v -b ~/.paco_lh_cookie "http://127.0.0.1:8888/events?q=experimentId=$experiment_id&$report_format" 2>&1 | grep "GET /jobStatus" | cut -d " " -f 3`

    joburl="$joburl&cmdline=1"

    result=`curl -L -b ~/.paco_lh_cookie "http://127.0.0.1:8888$joburl"`

    # refresh until the report is ready
    while [ "$result" = pending ]; do
        result=`curl -L -b ~/.paco_lh_cookie "http://127.0.0.1:8888$joburl"`
    done
fi

echo "$result" > $outfile

