package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.util.ArrayList;

public interface ProjectActionsAsync {
    void executeSite(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void executeUTR(String addressId, String timeId, AsyncCallback<String> asyncCallback);

    void querySymbol(String query, String threshold, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);

    void queryRefseqId(String utrSelectText, String threshold, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);

    void queryMirName(String mirSelectText, String threshold, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);
}
