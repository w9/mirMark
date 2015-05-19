package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;
import java.util.Vector;

public interface ProjectActionsAsync {
    void executeSite(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void executeUTR(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void selectUTR(String utrSelectText, String threshold, AsyncCallback<HashMap<String, Float>> asyncCallback);

    void selectMir(String mirSelectText, String threshold, AsyncCallback<HashMap<String, Float>> asyncCallback);
}
