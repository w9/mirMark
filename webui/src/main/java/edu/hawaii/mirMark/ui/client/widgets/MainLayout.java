package edu.hawaii.mirMark.ui.client.widgets;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import edu.hawaii.mirMark.ui.client.MirMark;
import edu.hawaii.mirMark.ui.server.actions.ProjectActionsImpl;
import edu.hawaii.mirMark.ui.shared.SiteLevelResultEntry;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.typeahead.client.base.StringDataset;
import org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead;

import java.util.*;

@SuppressWarnings("Convert2Lambda")
public class MainLayout extends ViewImpl {
    private final NumberFormat decimalFormat = NumberFormat.getFormat("0.000");
    @SuppressWarnings("FieldCanBeLocal") private final TextColumn<UtrLevelResultEntry> mirTarBasePositiveCol;
    @SuppressWarnings("FieldCanBeLocal") private final TextColumn<UtrLevelResultEntry> miRandaScoreCol;
    @SuppressWarnings("FieldCanBeLocal") private final TextColumn<UtrLevelResultEntry> targetScanProbCol;
    @SuppressWarnings("FieldCanBeLocal") private final TextColumn<UtrLevelResultEntry> mirNameCol;
    @SuppressWarnings("FieldCanBeLocal") private final TextColumn<UtrLevelResultEntry> geneSymbolCol;
    @SuppressWarnings("FieldCanBeLocal") private final Column<UtrLevelResultEntry, String> refseqIdCol;
    private String methodOfChoice;
    private ArrayList<UtrLevelResultEntry> currentResult;

    // It seems to me that GWT can infer the name of the *.ui.xml file by the class name (in this case MainLayout)
    interface MainLayoutUIBinder extends UiBinder<Container, MainLayout> {}

    // (Supposedly) this GWT.create statement will let GWT create a UiBinder who has a reference to the
    // corresponding *.ui.xml file.
    //
    // Note that _sUIBinder is a singleton.
    private static final MainLayoutUIBinder _sUIBinder = GWT.create(MainLayoutUIBinder.class);

    @UiField Button queryRefseqButton;
    @UiField Button queryMirNameButton;
    @UiField(provided = true) DataGrid<UtrLevelResultEntry> resultTable = new DataGrid<>(Integer.MAX_VALUE);
    @UiField TextBox fThreshold;
    @UiField Button querySymbolButton;
    @UiField InlineRadio fMethodOfChoiceMirMark;
    @UiField InlineRadio fMethodOfChoiceTargetScan;
    @UiField InlineRadio fMethodOfChoiceMiRanda;
    @SuppressWarnings("Convert2Diamond") @UiField(provided = true) Typeahead<String> querySymbolTypeahead = new Typeahead<>(new StringDataset(new ArrayList<String>()));
    @SuppressWarnings("Convert2Diamond") @UiField(provided = true) Typeahead<String> queryRefseqTypeahead = new Typeahead<>(new StringDataset(new ArrayList<String>()));
    @SuppressWarnings("Convert2Diamond") @UiField(provided = true) Typeahead<String> queryMirNameTypeahead = new Typeahead<>(new StringDataset(new ArrayList<String>()));
    @UiField HTMLPanel fSiteLevelCanvas;
    @UiField AnchorElement fDownloadCSVLink;
    @UiField TextBox fFilenameTextBox;
    @UiField Button fHideSiteLevelCanvasButton;
    @UiField Div fSiteLevelPanel;
    @UiField TextBox fSiteLevelThreshold;

    SiteLevelIllustration siteLevelIllustration = null;

    TextColumn<UtrLevelResultEntry> mirMarkProbCol;

    private final ListDataProvider<UtrLevelResultEntry> dataProvider = new ListDataProvider<>();

    private final MySortHandler sortHandler = new MySortHandler();

    // TODO: Handle a list of request instead of just one gene/miR

    // I had to copy the definition of ListHander<T> class here to fix that missing flush bug.
    // Now it has been heavily modified and localized.
    public class MySortHandler implements ColumnSortEvent.Handler {
        private final Map<Column<?, ?>, Comparator<UtrLevelResultEntry>> comparators = new HashMap<>();
        private List<UtrLevelResultEntry> list;

        public void sortColumn(Column<?, ?> column, boolean isSortAscending) {
            list = dataProvider.getList();

            // Get the comparator.
            final Comparator<UtrLevelResultEntry> comparator = comparators.get(column);
            if (comparator == null) {
                MyNotify.notify("The comparator is not found.", NotifyType.DANGER);
                return;
            }

            // Sort using the comparator.
            if (isSortAscending) {
                Collections.sort(list, comparator);
            } else {
                Collections.sort(list, new Comparator<UtrLevelResultEntry>() {
                    public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                        return -comparator.compare(o1, o2);
                    }
                });
            }

            // !!! This is what I added !!!
            dataProvider.refresh();
        }

        public void onColumnSort(ColumnSortEvent event) {
            // Get the sorted column.
            Column<?, ?> column = event.getColumn();
            if (column == null) {
                return;
            }
            sortColumn(column, event.isSortAscending());
        }

        public void setComparator(Column<UtrLevelResultEntry, ?> column, Comparator<UtrLevelResultEntry> comparator) {
            comparators.put(column, comparator);
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor Begins
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public MainLayout() {

        MirMark.APP.getProjectActions().getSymbols(new AsyncCallback<String[]>() {
            @Override
            public void onFailure(Throwable caught) {
                MyNotify.notify("Error Loading!", NotifyType.DANGER);
            }

            @Override
            public void onSuccess(String[] result) {
                querySymbolTypeahead.setDatasets(new StringDataset(Arrays.asList(result)));
                querySymbolTypeahead.reconfigure();
            }
        });



        MirMark.APP.getProjectActions().getRefseqs(new AsyncCallback<String[]>() {
            @Override
            public void onFailure(Throwable caught) {
                MyNotify.notify("Error Loading!", NotifyType.DANGER);
            }

            @Override
            public void onSuccess(String[] result) {
                queryRefseqTypeahead.setDatasets(new StringDataset(Arrays.asList(result)));
                queryRefseqTypeahead.reconfigure();
            }
        });



        MirMark.APP.getProjectActions().getMirNames(new AsyncCallback<String[]>() {
            @Override
            public void onFailure(Throwable caught) {
                MyNotify.notify("Error Loading!", NotifyType.DANGER);
            }

            @Override
            public void onSuccess(String[] result) {
                queryMirNameTypeahead.setDatasets(new StringDataset(Arrays.asList(result)));
                queryMirNameTypeahead.reconfigure();
            }
        });



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


//        refseqIdCol = new TextColumn<UtrLevelResultEntry>() {
//            @Override
//            public String getValue(UtrLevelResultEntry object) {
//                return object.geneRefseqId;
//            }
//        };
        refseqIdCol = new Column<UtrLevelResultEntry, String>(new ButtonCell()) {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.geneRefseqId;
            }
        };
        refseqIdCol.setFieldUpdater(new FieldUpdater<UtrLevelResultEntry, String>() {
            @Override
            public void update(int index, UtrLevelResultEntry object, String value) {
                MirMark.APP.getProjectActions().querySiteLevel(value, fSiteLevelThreshold.getText(), new AsyncCallback<ArrayList<SiteLevelResultEntry>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        MyNotify.notify("Site Level: Error Loading!", NotifyType.DANGER);
                    }

                    @Override
                    public void onSuccess(ArrayList<SiteLevelResultEntry> result) {
                        drawSiteLevelResults(result);
                        MainLayout.this.fSiteLevelPanel.removeStyleName("hidden");
                    }
                });
            }
        });
        refseqIdCol.setSortable(true);
        sortHandler.setComparator(refseqIdCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                return o1.geneRefseqId.compareTo(o2.geneRefseqId);
            }
        });
        resultTable.addColumn(refseqIdCol, "RefSeq ID");


        geneSymbolCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.geneSymbol;
            }
        };
        geneSymbolCol.setSortable(true);
        sortHandler.setComparator(geneSymbolCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                return o1.geneSymbol.compareTo(o2.geneSymbol);
            }
        });
        resultTable.addColumn(geneSymbolCol, "Gene Symbol");


        mirNameCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return object.mirName;
            }
        };
        mirNameCol.setSortable(true);
        sortHandler.setComparator(mirNameCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                return o1.mirName.compareTo(o2.mirName);
            }
        });
        resultTable.addColumn(mirNameCol, "MiR Name");


        resultTable.addColumnSortHandler(sortHandler);
        mirMarkProbCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                return decimalFormat.format(object.mirMarkProb);
            }
        };
        sortHandler.setComparator(mirMarkProbCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                return Float.compare(o1.mirMarkProb, o2.mirMarkProb);
            }
        });
        mirMarkProbCol.setSortable(true);
        resultTable.addColumn(mirMarkProbCol, "MirMark Prob.");

        targetScanProbCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                if (Float.isNaN(object.targetScanProb)) {
                    return "Not Sure";
                } else {
                    return decimalFormat.format(object.targetScanProb);
                }
            }
        };
        targetScanProbCol.setSortable(true);
        sortHandler.setComparator(targetScanProbCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                // Since for targetScan score, the larger the better,
                // in order to put NaN as the worst, we need it to be -Inf
                float s1 = Float.isNaN(o1.targetScanProb) ? Float.NEGATIVE_INFINITY : o1.targetScanProb;
                float s2 = Float.isNaN(o2.targetScanProb) ? Float.NEGATIVE_INFINITY : o2.targetScanProb;
                return Float.compare(s1, s2);
            }
        });
        resultTable.addColumn(targetScanProbCol, "TargetScan Prob.");


        miRandaScoreCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                if (Float.isNaN(object.miRandaScore)) {
                    return "Not Sure";
                } else {
                    return decimalFormat.format(object.miRandaScore);
                }
            }
        };
        sortHandler.setComparator(miRandaScoreCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                // Since for miRanda score, the smaller (more negative) the better,
                // in order to put NaN as the worst, we need it to be +Inf
                float s1 = Float.isNaN(o1.miRandaScore) ? Float.POSITIVE_INFINITY : o1.miRandaScore;
                float s2 = Float.isNaN(o2.miRandaScore) ? Float.POSITIVE_INFINITY : o2.miRandaScore;
                return Float.compare(s1, s2);
            }
        });
        miRandaScoreCol.setSortable(true);
        resultTable.addColumn(miRandaScoreCol, "MiRanda Score");


        mirTarBasePositiveCol = new TextColumn<UtrLevelResultEntry>() {
            @Override
            public String getValue(UtrLevelResultEntry object) {
                if (object.mirTarBasePositive > 0) {
                    return "Found";
                } else {
                    return "-";
                }
            }
        };
        sortHandler.setComparator(mirTarBasePositiveCol, new Comparator<UtrLevelResultEntry>() {
            @Override
            public int compare(UtrLevelResultEntry o1, UtrLevelResultEntry o2) {
                return Float.compare(o1.mirTarBasePositive, o2.mirTarBasePositive);
            }
        });
        mirTarBasePositiveCol.setSortable(true);
        resultTable.addColumn(mirTarBasePositiveCol, "MirTarBase");


        dataProvider.addDataDisplay(resultTable);

        // Select MirMark as the method of choice
        fMethodOfChoiceMirMark.setValue(true, true);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor Ends
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    void drawSiteLevelResults(ArrayList<SiteLevelResultEntry> entries) {
        fSiteLevelCanvas.clear();
        fSiteLevelCanvas.getElement().setInnerHTML("");
        //***MyNotify.notify("entries.size() = " + entries.size());
        for (SiteLevelResultEntry entry : entries) {
            siteLevelIllustration = new SiteLevelIllustration(fSiteLevelCanvas, entry.start, entry.end, entry.chr);
            //***MyNotify.notify("entry.sites.size() = " + entry.sites.size());
            for (SiteLevelResultEntry.Site site : entry.sites) {
                siteLevelIllustration.drawSite(site.mirName, site.start, site.end, site.probability);
            }
        }
    }


    // GWT recognize that this event handler is for click event based on its type.
    // GWT separated events into many types like KeyDownEvent, MouseOutEvent, etc.r
    @SuppressWarnings({"unused"})
    @UiHandler({"queryRefseqButton"})
    void handleQueryRefseqClick(ClickEvent clickEvent) {
        // "MirMark.APP.get..." is just an idiom:
        // "MirMark.APP" sends you to the "resource center"; "get..." usually just gets you the private variable,
        // thus if you put projectActions as a public static variable in MirMark class, you can get it by just
        // MirMark.projectActions, instead of this scary phrase.
        MirMark.APP.getProjectActions().queryRefseqId(queryRefseqTypeahead.getText(), fThreshold.getText(), methodOfChoice, new ResultTableCallback());
    }

    @SuppressWarnings({"unused"})
    @UiHandler({"queryMirNameButton"})
    void handleQueryMirNameClick(ClickEvent clickEvent) {
        MirMark.APP.getProjectActions().queryMirName(queryMirNameTypeahead.getText(), fThreshold.getText(), methodOfChoice, new ResultTableCallback());
    }

    @SuppressWarnings({"unused"})
    @UiHandler("querySymbolButton")
    public void handleQuerySymbolClick(ClickEvent event) {
        MirMark.APP.getProjectActions().querySymbol(querySymbolTypeahead.getText(), fThreshold.getText(), methodOfChoice, new ResultTableCallback());
    }

    @UiHandler("fMethodOfChoiceMirMark")
    public void handleMethodOfChoiceMirMarkValueChange(@SuppressWarnings("UnusedParameters") ValueChangeEvent<Boolean> event) {
        fThreshold.setText("1");
        methodOfChoice = ProjectActionsImpl.Methods.MIR_MARK;
    }

    @UiHandler("fMethodOfChoiceTargetScan")
    public void handleMethodOfChoiceTargetScanValueChange(@SuppressWarnings("UnusedParameters") ValueChangeEvent<Boolean> event) {
        fThreshold.setText("0");
        methodOfChoice = ProjectActionsImpl.Methods.TARGET_SCAN;
    }

    @UiHandler("fMethodOfChoiceMiRanda")
    public void handleMethodOfChoiceMiRandaValueChange(@SuppressWarnings("UnusedParameters") ValueChangeEvent<Boolean> event) {
        fThreshold.setText("-999");
        methodOfChoice = ProjectActionsImpl.Methods.MIRANDA;
    }

    @UiHandler("fMethodOfChoiceMirTarBase")
    public void handleMethodOfChoiceMirTarBaseValueChange(@SuppressWarnings("UnusedParameters") ValueChangeEvent<Boolean> event) {
        fThreshold.setText("0.5");
        methodOfChoice = ProjectActionsImpl.Methods.MIRTARBASE;
    }

    @UiHandler("fFilenameTextBox")
    public void handleFilenameTextBoxValueChange(@SuppressWarnings("UnusedParameters") ValueChangeEvent<String> event) {
        fDownloadCSVLink.setAttribute("download", fFilenameTextBox.getValue());
    }

    @UiHandler("fHideSiteLevelCanvasButton")
    public void handleHideSiteLevelCanvasButtonClick(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        fSiteLevelPanel.addStyleName("hidden");
    }

    private class ResultTableCallback implements AsyncCallback<ArrayList<UtrLevelResultEntry>> {

        @Override
        public void onFailure(Throwable caught) {
            MyNotify.notify("Error Loading.", NotifyType.WARNING);
        }

        @Override
        public void onSuccess(ArrayList<UtrLevelResultEntry> result) {
            MainLayout.this.currentResult = result;
            if(result == null) {
                dataProvider.getList().clear();
                dataProvider.flush();
                MyNotify.notify("No match found.", NotifyType.WARNING);
            } else {
                // Update the View using the Model
                dataProvider.setList(result);
                dataProvider.refresh();

                // Sort it by mirMark probabilities
                resultTable.getColumnSortList().clear();
                sortHandler.sortColumn(mirMarkProbCol, false);
                MyNotify.notify("Returned: " + result.size(), NotifyType.SUCCESS);
            }



            String encodedCSVTable = "";
            for (UtrLevelResultEntry entry : currentResult) {
                encodedCSVTable += refseqIdCol.getValue(entry) + "," +
                        geneSymbolCol.getValue(entry) + "," +
                        mirNameCol.getValue(entry) + "," +
                        mirMarkProbCol.getValue(entry) + "," +
                        targetScanProbCol.getValue(entry) + "," +
                        miRandaScoreCol.getValue(entry) + "," +
                        mirTarBasePositiveCol.getValue(entry) + "\n";
            }
            fDownloadCSVLink.setHref(URL.encode("data:text/csv;charset=utf-8," + encodedCSVTable));
        }
    }
}
