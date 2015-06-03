package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.util.ArrayList;

public interface ProjectActionsAsync {
    void querySymbol(String query, String threshold, String methodOfChoice, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);

    void queryRefseqId(String utrSelectText, String threshold, String methodOfChoice, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);

    void queryMirName(String mirSelectText, String threshold, String methodOfChoice, AsyncCallback<ArrayList<UtrLevelResultEntry>> async);

    void getSymbols(AsyncCallback<String[]> asyncCallback);

    void getRefseqs(AsyncCallback<String[]> asyncCallback);

    void getMirNames(AsyncCallback<String[]> asyncCallback);
}
