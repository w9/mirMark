package edu.hawaii.mirMark.ui.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import org.gwtbootstrap3.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.hawaii.mirMark.ui.client.MirMark;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainLayout extends ViewImpl {
    // It seems to me that GWT can infer the name of the *.ui.xml file by the class name (in this case MainLayout)
    interface MainLayoutUIBinder extends UiBinder<Container, MainLayout> {}

    // (Supposedly) this GWT.create statement will let GWT create a UiBinder who has a reference to the
    // corresponding *.ui.xml file.
    //
    // Note that _sUIBinder is a singleton.
    private static final MainLayoutUIBinder _sUIBinder = GWT.create(MainLayoutUIBinder.class);

    @UiField TextBox utrSelect;
    @UiField Button selectButton;
    @UiField TextBox mirSelect;
    @UiField Button selectMirButton;
    @UiField(provided = true)
    DataGrid<ResultData> resultTable = new DataGrid<>(Integer.MAX_VALUE);
    @UiField
    Pagination resultTablePagination;

    private ListDataProvider<ResultData> dataProvider = new ListDataProvider<>();

    private Logger logger = Logger.getLogger("FrontPageLogger");

    public class ResultData {
        String mir;
        String utr;
        Float probability;
        public ResultData(String mir, String utr, Float probability) {
            this.mir = mir;
            this.utr = utr;
            this.probability = probability;
        }
    }

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


        resultTable.addColumn(new TextColumn<ResultData>() {
            @Override
            public String getValue(ResultData object) {
                return object.mir;
            }
        }, "UTR ID");

        resultTable.addColumn(new TextColumn<ResultData>() {
            @Override
            public String getValue(ResultData object) {
                return object.utr;
            }
        }, "Mir ID");

        resultTable.addColumn(new TextColumn<ResultData>() {
            @Override
            public String getValue(ResultData object) {
                return Float.toString(object.probability);
            }
        }, "Probability");

        dataProvider.addDataDisplay(resultTable);
    }

    // GWT recognize that this event handler is for click event based on its type.
    // GWT separated events into many types like KeyDownEvent, MouseOutEvent, etc.
    @SuppressWarnings({"unused"})
    @UiHandler({"selectButton"})
    void handleSelectClick(ClickEvent clickEvent) {
        // "MirMark.APP.get..." is just an idiom:
        // "MirMark.APP" sends you to the "resource center"; "get..." usually just gets you the private variable,
        // thus if you put projectActions as a public static variable in MirMark class, you can get it by just
        // MirMark.projectActions, instead of this scary phrase.
        MirMark.APP.getProjectActions().selectUTR(utrSelect.getText(), new AsyncCallback<HashMap<String, Float>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Error loading");
            }

            @Override
            public void onSuccess(HashMap<String, Float> result) {
                if(result == null) {
                    logger.log(Level.SEVERE, "No match found");
                } else {
                    Set<String> keys = result.keySet();
                    int count = 0;
                    for (String key : keys) {
                        count++;
                        dataProvider.getList().add(new ResultData(utrSelect.getText(), key, result.get(key)));
                        logger.log(Level.INFO, Float.toString(result.get(key)));
                    }
                    dataProvider.flush();
                    logger.log(Level.INFO, "Returned: " + result.size());
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
                logger.log(Level.SEVERE, "Error loading");
            }

            @Override
            public void onSuccess(HashMap<String, Float> result) {
                if(result == null) {
                    logger.log(Level.SEVERE, "No match found");
                } else {
                    Set<String> keys = result.keySet();
                    int count = 0;
                    for (String key : keys) {
                        count++;
                        dataProvider.getList().add(new ResultData(key, mirSelect.getText(), result.get(key)));
                        logger.log(Level.INFO, Float.toString(result.get(key)));
                    }
                    dataProvider.flush();
                    logger.log(Level.INFO, "Returned: " + result.size());
                }
            }
        });
    }
}
