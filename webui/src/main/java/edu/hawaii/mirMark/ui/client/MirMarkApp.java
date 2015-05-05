package edu.hawaii.mirMark.ui.client;

import com.google.gwt.core.client.GWT;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.client.actions.ProjectActionsAsync;
import edu.hawaii.mirMark.ui.client.widgets.MainLayout;

public class MirMarkApp {
    private final AllMessages _commonMessages = GWT.create(AllMessages.class);

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

    // Make sure that main
    public MainLayout getMainLayout() {
        if (_mainLayout == null) {
            _mainLayout = new MainLayout();
        }

        return _mainLayout;
    }
}
