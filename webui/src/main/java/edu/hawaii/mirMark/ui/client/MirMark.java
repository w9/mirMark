package edu.hawaii.mirMark.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class MirMark implements EntryPoint {
    public static final MirMarkApp APP = new MirMarkApp();

    // Upon loaded: give GWT the root layout, which will be extracted from MainLayout.ui.xml
	public void onModuleLoad() {
        RootLayoutPanel.get().add(APP.getMainLayout());
	}
}
