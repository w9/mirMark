package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.util.ArrayList;

@RemoteServiceRelativePath ("Actions/Project")
public interface ProjectActions extends RemoteService {
    ArrayList<UtrLevelResultEntry> queryRefseqId(String utrSelectText, String threshold);

    ArrayList<UtrLevelResultEntry> queryMirName(String mirSelectText, String threshold);

    ArrayList<UtrLevelResultEntry> querySymbol(String query, String threshold);
}
