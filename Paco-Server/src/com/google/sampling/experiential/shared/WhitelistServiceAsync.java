package com.google.sampling.experiential.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WhitelistServiceAsync {
    public void getWhitelist(AsyncCallback<List<String>> async);

    void addUser(String email, AsyncCallback<Void> callback);

}
