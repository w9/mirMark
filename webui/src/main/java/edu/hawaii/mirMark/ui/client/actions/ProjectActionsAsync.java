package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;
import java.util.Vector;

public interface ProjectActionsAsync {
    void executeSite(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void executeUTR(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void selectUTR(String utrSelectText, AsyncCallback<HashMap<String,Float>> asyncCallback);

    void selectMir(String mirSelectText, AsyncCallback<HashMap<String,Float>> asyncCallback);
}
