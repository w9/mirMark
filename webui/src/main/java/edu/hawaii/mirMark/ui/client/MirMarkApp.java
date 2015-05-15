package edu.hawaii.mirMark.ui.client;

import com.google.gwt.core.client.GWT;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.client.actions.ProjectActionsAsync;
import edu.hawaii.mirMark.ui.client.widgets.MainLayout;

// This is the beacon, the headquarter, the central station of all the resources.
//
// Note that everything public in this class will be visible anywhere, and can be called at anytime (at least
// after the app starts), because the singleton pattern written in MirMark class effectively renders it static.
//
// You can even, bearing the blame of clustering the code, write all these public stuff in the MirMark class directly,
// with "static" modifiers put in front of them.
public class MirMarkApp {
    // Out-sourcing messages for i18n purpose
    private final AllMessages _commonMessages = GWT.create(AllMessages.class);

    // This lets GWT create the ProjectAction interface (async calls) for the client side.
    //
    // Here, it's very interesting that we give GWT a ProjectActions class, but expect GWT to give us
    // a ProjectActionsAsync. This is possible because in front of ProjectActions class there's a
    // decorator that points to the location of the corresponding ProjectActionsAsync interface.
    // The implementation class, ProjectActionsImpl, will be found by GWT using the WEB-INF/actions-servlet.xml.
    // The location of this Spring bean xml can be found in the IDE settings (webui/webui.iml).
    private final ProjectActionsAsync _projectActions = GWT.create(ProjectActions.class);

    private MainLayout _mainLayout;

    public MirMarkApp() {
    }

    public AllMessages getCommonMessages() {
        return _commonMessages;
    }

    public ProjectActionsAsync getProjectActions() {
        return _projectActions;
    }

    // lazy initiation of _mainLayout
    public MainLayout getMainLayout() {
        if (_mainLayout == null) {
            _mainLayout = new MainLayout();
        }

        return _mainLayout;
    }
}
