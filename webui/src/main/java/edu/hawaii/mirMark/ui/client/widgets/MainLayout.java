package edu.hawaii.mirMark.ui.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;
import org.gwtbootstrap3.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.hawaii.mirMark.ui.client.MirMark;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;

import java.util.ArrayList;

public class MainLayout extends ViewImpl {
    private final NumberFormat decimalFormat = NumberFormat.getFormat("0.000");

    // It seems to me that GWT can infer the name of the *.ui.xml file by the class name (in this case MainLayout)
    interface MainLayoutUIBinder extends UiBinder<Container, MainLayout> {}

    // (Supposedly) this GWT.create statement will let GWT create a UiBinder who has a reference to the
    // corresponding *.ui.xml file.
    //
    // Note that _sUIBinder is a singleton.
    private static final MainLayoutUIBinder _sUIBinder = GWT.create(MainLayoutUIBinder.class);

    @UiField TextBox queryRefseqTextBox;
    @UiField Button queryRefseqButton;
    @UiField TextBox queryMirNameTextBox;
    @UiField Button queryMirNameButton;
    @UiField(provided = true)
    DataGrid<UtrLevelResultEntry> resultTable = new DataGrid<>(Integer.MAX_VALUE);
    @UiField
    TextBox fThreshold;
    @UiField
    TextBox querySymbolTextBox;
    @UiField
    Button querySymbolButton;

    private ListDataProvider<UtrLevelResultEntry> dataProvider = new ListDataProvider<>();

    public MainLayout() {
        // This is the widget---the puppet to be manipulated by this view.
        //
        // Container is a IsWidget:
        //     Container
        //     e Div
        //       e ComplexWidget
        //         e ComplexPanel (gwt)
        //           e Widget
        //             i IsWidget
        //
        // the UiBinder.createAndBindUi() method clearly compiles the xml file that it already knows, and
        // return the root element, the class of that root element is inferred using the tag of the root
        // element.
        Container rootElement = _sUIBinder.createAndBindUi(this);

        // Here we can write some java-coded contents for the front-end---logic that needs to be
        // done before the displaying of the view.
        // ...

        // This function was implemented in the parent class "ViewImpl", it takes in an IsWidget and
        // hook it to its internal widget variable. This class also inherits an "asWidget()" method,
        // which will return said widget when requested. It looks like this ViewImpl class serves as a middleman
        // between a widget and its user. To be more precise, it defines the logic and actions of the widget,
        // and provides handles to the user.
        initWidget(rootElement);

        resultTable.addColumn(new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.geneRefseqId;
            }
        }, "RefSeq ID");


        resultTable.addColumn(new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.geneSymbol;
            }
        }, "Gene Symbol");


        resultTable.addColumn(new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.mirName;
            }
        }, "MiR Name");

        resultTable.addColumn(new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return decimalFormat.format(object.probability);
            }
        }, "Probability");

        dataProvider.addDataDisplay(resultTable);
    }

    // GWT recognize that this event handler is for click event based on its type.
    // GWT separated events into many types like KeyDownEvent, MouseOutEvent, etc.r
    @SuppressWarnings({"unused"})
    @UiHandler({"queryRefseqButton"})
    void handleSelectClick(ClickEvent clickEvent) {
        // "MirMark.APP.get..." is just an idiom:
        // "MirMark.APP" sends you to the "resource center"; "get..." usually just gets you the private variable,
        // thus if you put projectActions as a public static variable in MirMark class, you can get it by just
        // MirMark.projectActions, instead of this scary phrase.
        MirMark.APP.getProjectActions().queryRefseqId(queryRefseqTextBox.getText(), fThreshold.getText(), new ResultTableCallback());
    }

    @SuppressWarnings({"unused"})
    @UiHandler({"queryMirNameButton"})
    void handleSelectMirClick(ClickEvent clickEvent) {
        MirMark.APP.getProjectActions().queryMirName(queryMirNameTextBox.getText(), fThreshold.getText(), new ResultTableCallback());
    }

    @SuppressWarnings({"unused"})
    @UiHandler("querySymbolButton")
    public void handleClick(ClickEvent event) {
        MirMark.APP.getProjectActions().querySymbol(querySymbolTextBox.getText(), fThreshold.getText(), new ResultTableCallback());
    }

    private class ResultTableCallback implements AsyncCallback<ArrayList<UtrLevelResultEntry>> {
        @Override
        public void onFailure(Throwable caught) {
            Log.error("Error loading");
        }

        @Override
        public void onSuccess(ArrayList<UtrLevelResultEntry> result) {
            if(result == null) {
                Log.error("No match found");
            } else {
                dataProvider.getList().clear();
                dataProvider.setList(result);
                dataProvider.flush();
                Log.info("Returned: " + result.size());
            }
        }
    }
}
