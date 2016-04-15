#!/bin/sh

# using instructions from
# https://developers.google.com/identity/protocols/OAuth2InstalledApp

# #1 generate credential -- see above page for steps to generate client id & secret
# hint: use the installed app option (Other under API/Credentials in Google dev console)

echo "enter client id from Google developer console"
read client_id

open "https://accounts.google.com/o/oauth2/auth?response_type=code&scope=email&redirect_uri=urn:ietf:wg:oauth:2.0:oob&client_id=$client_id"

echo "enter authcode from browser authentication page"
read auth_code

echo "enter client secret from Google Developer console"
read client_secret

# instead of the all-in-one-line approach here, we might want to capture the response then slice and dice in case there was an error
export refresh_token=`curl https://www.googleapis.com/oauth2/v3/token \
     -d code=$auth_code \
     -d client_id=$client_id \
     -d client_secret=$client_secret \
     -d redirect_uri=urn:ietf:wg:oauth:2.0:oob \
     -d grant_type=authorization_code | grep 'refresh_token' | cut -d ":" -f 2 | cut -c 2- | rev | cut -c 2- | rev`

echo "refresh token: $refresh_token"

# save the refresh token somewhere to prevent having to redo this step for each request.
# then individual request files can read the refresh token and request access_tokens (which expire)
echo $refresh_token > ~/.qs_paco
chmod 600 ~/.qs_paco

echo "tried to save refresh token in ~/.qs_paco. Now you should be able to run one of the Paco cmdline scripts"
