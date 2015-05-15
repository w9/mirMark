package edu.hawaii.mirMark.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

// This class will get instantiated when as soon as the app starts,
// GWT will find this class when looking up the manifest MirMark.gwt.xml.
//
// There are only two things we are going to do after the app starts,
// each will be accomplished by one line below:
// creation of the backend --- accomplished by initiation of the APP static variable
// creation of the frontend --- accomplished by hooking the backend-generated UI onto RootLayoutPanel
//
// Because we used Singleton pattern (using static variable initiation to create an instance for MirMarkApp),
// no matter how many instances of MirMark get created, they'll all refer to the only one MirMarkApp instance.
public class MirMark implements EntryPoint {
    public static final MirMarkApp APP = new MirMarkApp();

    // APP.getMainLayout() will get the UI from MainLayout.ui.xml
    // getMainLayout() is a getter for the internal variable _mainLayout. It also uses lazy initiation, so that
    // instead of initiating _mainLayout as soon as APP gets created (the line above),
    // it only initiates it after the first "get" call --- i.e., when onModuleLoad is called
    //
    // Note that Panel.add() expects IsWidget interface. Both
    //     com.google.gwt.user.client.ui.Composite (extends Widget (implements IsWidget))
    // and
    //     com.gwtplatform.mvp.client.ViewImpl (implements View (extends IsWidget))
    // satisfies.
	public void onModuleLoad() {
        RootLayoutPanel.get().add(APP.getMainLayout());
	}
}
