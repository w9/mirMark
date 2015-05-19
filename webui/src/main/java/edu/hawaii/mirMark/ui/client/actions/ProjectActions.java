package edu.hawaii.mirMark.ui.client.actions;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.HashMap;

@RemoteServiceRelativePath ("Actions/Project")
public interface ProjectActions extends RemoteService {
    String executeSite(String addressId, String timeId);

    String executeUTR(String addressId, String timeId);

    HashMap<String, Float> selectUTR(String utrSelectText, String threshold);

    HashMap<String, Float> selectMir(String mirSelectText, String threshold);
}
