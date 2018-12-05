package com.pacoapp.paco.net;

import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GetAuthTokenInForeground extends AbstractAuthTokenTask {


    public GetAuthTokenInForeground(NetworkClient networkClient) {
      super(networkClient);
    }

    /**
     * Get a authentication token if one is not available. If the error is not recoverable then
     * it displays the error message on parent networkClient right away.
     */
    @Override
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(networkClient.getContext(), userPrefs.getSelectedAccount(), oAuthScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present, which is
            // recoverable, so we need to show the user some UI through the networkClient.
            networkClient.handleException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        }
        return null;
    }

}
