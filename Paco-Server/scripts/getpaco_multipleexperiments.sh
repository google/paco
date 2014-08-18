#/bin/bash

echo "enter experimentIds (e.g. 1 or 1,2,3) (these are from the definition page on server)"
read experiment_id

echo "enter report format (csv,json,html)"
read report_format
 
echo "output file name"
read outfile

#if [ "$experiment_id" =~ "," ]; then
experiment_ids=$(echo $experiment_id  | tr "," "\n")
#else
#  experiment_ids = experiment_id
#fi

for curr_experiment_id in $experiment_ids; do

echo "retrieving $curr_experiment_id"

if [ "$report_format" = "json" ]; then
    result=`curl -L -b ~/.paco_qs_cookie "https://quantifiedself.appspot.com/events?q=experimentId=$curr_experiment_id&json"`
else
    joburl=`curl -L -v -b ~/.paco_qs_cookie "https://quantifiedself.appspot.com/events?q=experimentId=$curr_experiment_id&$report_format" 2>&1 | grep "GET /jobStatus" | cut -d " " -f 3`

    joburl="$joburl&cmdline=1"

    result=`curl -L -b ~/.paco_qs_cookie "https://quantifiedself.appspot.com$joburl"`

    # refresh until the report is ready
    while [ "$result" = pending ]; do
        result=`curl -L -b ~/.paco_qs_cookie "https://quantifiedself.appspot.com$joburl"`
    done
fi

echo "$result" >> $outfile

done

