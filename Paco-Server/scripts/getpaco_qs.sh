#/bin/bash

# be sure to generate a stored refresh token by running set_oauth.sh first (should only need it once unless you revoke the token)

# Read the stored refresh token. Get a new access token with it. Then, finally, doing something with a Paco endpoint.
refresh_token=`cat ~/.qs_paco`
# it's late and I can't seem to reliably cut out wrapping quotes above. Make sure they are gone here on reload. TODO fix the storage to begin with.
#export refresh_token=`sed -e 's/^"//' -e 's/"$//' <<< $refresh_token`

# get a new access token from the refresh token
export new_access_token_response=`curl https://www.googleapis.com/oauth2/v3/token \
     -d client_id=$client_id \
     -d client_secret=$client_secret \
     -d refresh_token=$refresh_token \
     -d grant_type=refresh_token`

#echo "nat: $new_access_token_response"
export access_token=`echo $new_access_token_response | grep 'access_token' | cut -d ":" -f 2 | rev | cut -c 2- | rev`
#echo "parsed at: ${access_token}"


#echo "new_access_token: $access_token"
# now you can access data with this token

echo "enter experimentId (from definition page on server)"
read experiment_id

echo "enter report format (csv,json,html)"
read report_format
 
echo "output file name"
read outfile

if [ "$report_format" = "json" ]; then
    result=`curl  -H "Authorization: Bearer $access_token" -L "https://quantifiedself.appspot.com/events?q=experimentId=$experiment_id&json"`
else
    joburl=`curl  -H "Authorization: Bearer $access_token" -L -v "https://quantifiedself.appspot.com/events?q=experimentId=$experiment_id&$report_format" 2>&1 | grep "GET /jobStatus" | cut -d " " -f 3`

    joburl="$joburl&cmdline=1"

    result=`curl  -H "Authorization: Bearer $access_token" -L "https://quantifiedself.appspot.com$joburl"`

    # refresh until the report is ready
    while [ "$result" = pending ]; do
        result=`curl  -H "Authorization: Bearer $access_token" -L "https://quantifiedself.appspot.com$joburl"`
    done
fi

echo "$result" > $outfile

