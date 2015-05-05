package edu.hawaii.mirMark.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.hawaii.mirMark.ui.client.MirMark;

import java.util.HashMap;

public class MainLayout extends Composite {
    interface MainLayoutUIBinder extends UiBinder<ScrollPanel, MainLayout> {}
    private static final MainLayoutUIBinder _sUIBinder = GWT.create(MainLayoutUIBinder.class);

    @UiField Label appTitle;
    @UiField TextArea arrayResults;
    @UiField TextBox utrSelect;
    @UiField Button selectButton;
    @UiField TextBox mirSelect;
    @UiField Button selectMirButton;

    public MainLayout() {
        ScrollPanel rootElement = _sUIBinder.createAndBindUi(this);

        appTitle.setText(MirMark.APP.getCommonMessages().appTitle());

        initWidget(rootElement);
    }

    @SuppressWarnings({"unused"})
    @UiHandler({"selectButton"})
    void handleSelectClick(ClickEvent clickEvent) {
        MirMark.APP.getProjectActions().selectUTR(utrSelect.getText(), new AsyncCallback<HashMap<String, Float>>() {
            @Override
            public void onFailure(Throwable caught) {
                arrayResults.setText("Error loading");
            }

            @Override
            public void onSuccess(HashMap<String, Float> result) {
                if(result == null) {
                    arrayResults.setText("No match found");
                } else {
                    arrayResults.setText("Returned: " + result.size());
                }
            }
        });
    }

    @SuppressWarnings({"unused"})
    @UiHandler({"selectMirButton"})
    void handleSelectMirClick(ClickEvent clickEvent) {
        MirMark.APP.getProjectActions().selectMir(mirSelect.getText(), new AsyncCallback<HashMap<String, Float>>() {
            @Override
            public void onFailure(Throwable caught) {
                arrayResults.setText("Error loading");
            }

            @Override
            public void onSuccess(HashMap<String, Float> result) {
                if(result == null) {
                    arrayResults.setText("No match found");
                } else {
                    arrayResults.setText("Returned: " + result.size());
                }
            }
        });
    }
}
