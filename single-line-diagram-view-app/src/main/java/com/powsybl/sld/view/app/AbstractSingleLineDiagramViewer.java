/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.ZoneDiagram;
import com.powsybl.sld.ZoneId;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.SubstationDiagram;
import com.powsybl.sld.VoltageLevelDiagram;
import com.powsybl.sld.cgmes.layout.CgmesSubstationLayoutFactory;
import com.powsybl.sld.cgmes.layout.CgmesVoltageLevelLayoutFactory;
import com.powsybl.sld.cgmes.layout.CgmesZoneLayoutFactory;
import com.powsybl.sld.force.layout.ForceSubstationLayoutFactory;
import com.powsybl.sld.force.layout.ForceZoneLayoutFactory;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramInitialValueProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DefaultNodeLabelConfiguration;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.NodeLabelConfiguration;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import com.powsybl.sld.view.AbstractContainerDiagramView;
import com.powsybl.sld.view.DisplayVoltageLevel;
import com.powsybl.sld.view.SubstationDiagramView;
import com.powsybl.sld.view.VoltageLevelDiagramView;
import com.powsybl.sld.view.ZoneDiagramView;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractSingleLineDiagramViewer extends Application implements DisplayVoltageLevel {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractSingleLineDiagramViewer.class);

    private static final String SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY = "selectedVoltageLevelIds";
    private static final String SELECTED_SUBSTATION_IDS_PROPERTY = "selectedSubstationIds";
    private static final String SELECTED_ZONE_IDS_PROPERTY = "selectedZoneIds";

    private static final String CLOSE_TAB_ACTION = "Close tab";
    private static final String CLOSE_ALL_TABS_ACTION = "Close all tabs";

    private static final String CGMES_LAYOUT_NAME = "Cgmes";
    private static final String SMART_LAYOUT_NAME = "Smart";
    private static final String SMART_WITH_HORIZONTAL_COMPACTION_LAYOUT_NAME = "Smart with horizontal compaction";
    private static final String SMART_WITH_VERTICAL_COMPACTION_LAYOUT_NAME = "Smart with vertical compaction";

    private Map<String, VoltageLevelLayoutFactory> voltageLevelsLayouts = new LinkedHashMap<>();

    private Map<String, SubstationLayoutFactory> substationsLayouts = new LinkedHashMap<>();

    private Map<String, ZoneLayoutFactory> zonesLayouts = new LinkedHashMap<>();

    private Map<String, DiagramStyleProvider> styles = new LinkedHashMap<>();

    private final ComponentLibrary convergenceComponentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

    private final Map<String, ComponentLibrary> svgLibraries
            = ImmutableMap.of("CVG Design", convergenceComponentLibrary);

    private final ObservableList<SelectableVoltageLevel> selectableVoltageLevels = FXCollections.observableArrayList();

    private final ObservableList<SelectableSubstation> selectableSubstations = FXCollections.observableArrayList();

    private final ObservableList<SelectableZone> selectableZones = FXCollections.observableArrayList();

    private final TextField filterInput = new TextField();

    private final TreeView<ContainerDiagram> substationsTree = new TreeView<>();

    private List<String> saveSelectedSubstations = new ArrayList<>();

    private List<String> saveSelectedVoltageLevels = new ArrayList<>();

    private final TabPane diagramsPane = new TabPane();
    private Tab tabSelected;
    private Tab tabChecked;
    private final BorderPane selectedDiagramPane = new BorderPane();
    private final TabPane checkedDiagramsPane = new TabPane();
    private GridPane parametersPane;

    private final ObjectProperty<Network> networkProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<LayoutParameters> layoutParameters = new SimpleObjectProperty<>(new LayoutParameters()
            .setShowGrid(true));

    protected final Preferences preferences = Preferences.userNodeForPackage(VoltageLevelDiagramView.class);

    private final ObjectMapper objectMapper = JsonUtil.createObjectMapper();

    private final ComboBox<String> voltageLevelLayoutComboBox = new ComboBox<>();

    private final ComboBox<String> substationLayoutComboBox = new ComboBox<>();

    private final ComboBox<String> zoneLayoutComboBox = new ComboBox<>();

    private final ComboBox<String> styleComboBox = new ComboBox<>();

    private final ComboBox<String> svgLibraryComboBox = new ComboBox<>();

    private final CheckBox showNames = new CheckBox("Show names");

    private final CheckBox hideSubstations = new CheckBox("Hide substations");

    private final CheckBox zoneBuildMode = new CheckBox("Zone mode");

    private final Button createZone = new Button("Create");

    private final TextField zoneName = new TextField();

    private final ComboBox<String> diagramNamesComboBox = new ComboBox<>();

    private class ContainerDiagramPane extends BorderPane {
        private final ScrollPane flowPane = new ScrollPane();

        private final TextArea infoArea = new TextArea();

        private final VBox svgArea = new VBox();
        private final TextField svgSearchField = new TextField();
        private final Button svgSearchButton = new Button("Search");
        private final TextArea svgTextArea = new TextArea();
        private AtomicReference<Integer> svgSearchStart = new AtomicReference<>(0);
        private final Button svgSaveButton = new Button("Save");

        private final VBox metadataArea = new VBox();
        private final TextField metadataSearchField = new TextField();
        private final Button metadataSearchButton = new Button("Search");
        private final TextArea metadataTextArea = new TextArea();
        private final AtomicReference<Integer> metadataSearchStart = new AtomicReference<>(0);
        private final Button metadataSaveButton = new Button("Save");

        private final VBox jsonArea = new VBox();
        private final TextField jsonSearchField = new TextField();
        private final Button jsonSearchButton = new Button("Search");
        private final TextArea jsonTextArea = new TextArea();
        private final AtomicReference<Integer> jsonSearchStart = new AtomicReference<>(0);
        private final Button jsonSaveButton = new Button("Save");

        private final Tab tab1 = new Tab("Diagram", flowPane);

        private final Tab tab2 = new Tab("SVG", svgArea);

        private final Tab tab3 = new Tab("Metadata", metadataArea);

        private final Tab tab4 = new Tab("Graph", jsonArea);

        private final TabPane tabPane = new TabPane(tab1, tab2, tab3, tab4);

        private final TitledPane titledPane = new TitledPane("Infos", infoArea);

        private final ChangeListener<LayoutParameters> listener;

        private final ContainerDiagram diag;

        ContainerDiagramPane(ContainerDiagram c) {
            diag = c;
            createArea(svgSearchField, svgSearchButton, svgSaveButton, "SVG file", "*.svg", svgTextArea, svgArea, svgSearchStart);
            createArea(metadataSearchField, metadataSearchButton, metadataSaveButton, "JSON file", "*.json", metadataTextArea, metadataArea, metadataSearchStart);
            createArea(jsonSearchField, jsonSearchButton, jsonSaveButton, "JSON file", "*.json", jsonTextArea, jsonArea, jsonSearchStart);

            infoArea.setEditable(false);
            infoArea.setText(String.join(System.lineSeparator(),
                    "id: " + c.getId(),
                    "name: " + c.getName()));
            tabPane.setSide(Side.BOTTOM);
            tab1.setClosable(false);
            tab2.setClosable(false);
            tab3.setClosable(false);
            tab4.setClosable(false);
            setCenter(tabPane);
            setBottom(titledPane);
            listener = (observable, oldValue, newValue) -> loadDiagram(c);
            layoutParameters.addListener(new WeakChangeListener<>(listener));
            loadDiagram(c);
        }

        public ContainerDiagram getDiag() {
            return diag;
        }

        class ContainerDiagramResult {

            private final AbstractContainerDiagramView view;

            private final String svgData;

            private final String metadataData;

            private final String jsonData;

            ContainerDiagramResult(AbstractContainerDiagramView view, String svgData, String metadataData, String jsonData) {
                this.view = view;
                this.svgData = svgData;
                this.metadataData = metadataData;
                this.jsonData = jsonData;
            }

            AbstractContainerDiagramView getView() {
                return view;
            }

            String getSvgData() {
                return svgData;
            }

            String getMetadataData() {
                return metadataData;
            }

            String getJsonData() {
                return jsonData;
            }
        }

        private ScrollPane getFlowPane() {
            return flowPane;
        }

        private String getSelectedDiagramName() {
            return diagramNamesComboBox.getSelectionModel().getSelectedItem();
        }

        private ContainerDiagramResult createContainerDiagramView(ContainerDiagram c) {
            String svgData;
            String metadataData;
            String jsonData;
            try (StringWriter svgWriter = new StringWriter();
                 StringWriter metadataWriter = new StringWriter();
                 StringWriter jsonWriter = new StringWriter()) {
                DiagramStyleProvider styleProvider = styles.get(styleComboBox.getSelectionModel().getSelectedItem());
                DiagramInitialValueProvider initProvider = new DefaultDiagramInitialValueProvider(networkProperty.get());
                NodeLabelConfiguration nodeLabelConfiguration = new DefaultNodeLabelConfiguration(getComponentLibrary());
                GraphBuilder graphBuilder = new NetworkGraphBuilder(networkProperty.get());

                String dName = getSelectedDiagramName();
                LayoutParameters diagramLayoutParameters = new LayoutParameters(layoutParameters.get()).setDiagramName(dName);
                diagramLayoutParameters.setComponentsSize(getComponentLibrary().getComponentsSize());
                if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL) {
                    VoltageLevelDiagram diagram = VoltageLevelDiagram.build(graphBuilder, c.getId(), getVoltageLevelLayoutFactory(), showNames.isSelected(),
                            diagramLayoutParameters.isShowInductorFor3WT());
                    diagram.writeSvg("",
                            new DefaultSVGWriter(getComponentLibrary(), diagramLayoutParameters),
                            initProvider,
                            styleProvider,
                            nodeLabelConfiguration,
                            svgWriter,
                            metadataWriter);
                    diagram.getGraph().writeJson(jsonWriter);
                } else if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.SUBSTATION) {
                    SubstationDiagram diagram = SubstationDiagram.build(graphBuilder, c.getId(), getSubstationLayoutFactory(), getVoltageLevelLayoutFactory(), showNames.isSelected());
                    diagram.writeSvg("",
                            new DefaultSVGWriter(getComponentLibrary(), diagramLayoutParameters),
                            initProvider,
                            styleProvider,
                            nodeLabelConfiguration,
                            svgWriter,
                            metadataWriter);
                    diagram.getSubGraph().writeJson(jsonWriter);
                } else if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.ZONE) {
                    ZoneDiagram diagram = ZoneDiagram.build(graphBuilder, c.getZoneId(), getZoneLayoutFactory(), getSubstationLayoutFactory(), getVoltageLevelLayoutFactory(), showNames.isSelected());
                    diagram.writeSvg("",
                            new DefaultSVGWriter(getComponentLibrary(), diagramLayoutParameters),
                            initProvider,
                            styleProvider,
                            nodeLabelConfiguration,
                            svgWriter,
                            metadataWriter);
                    diagram.getZoneGraph().writeJson(jsonWriter);
                }

                svgWriter.flush();
                metadataWriter.flush();
                svgData = svgWriter.toString();
                metadataData = metadataWriter.toString();
                jsonData = jsonWriter.toString();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            AbstractContainerDiagramView diagramView = null;
            try (InputStream svgInputStream = new ByteArrayInputStream(svgData.getBytes(StandardCharsets.UTF_8));
                 InputStream metadataInputStream = new ByteArrayInputStream(metadataData.getBytes(StandardCharsets.UTF_8))) {
                if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL) {
                    diagramView = VoltageLevelDiagramView.load(svgInputStream, metadataInputStream, switchId -> handleSwitchPositionchange(c, switchId), AbstractSingleLineDiagramViewer.this);
                } else if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.SUBSTATION) {
                    diagramView = SubstationDiagramView.load(svgInputStream, metadataInputStream, switchId -> handleSwitchPositionchange(c, switchId), AbstractSingleLineDiagramViewer.this);
                } else if (c.getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.ZONE) {
                    diagramView = ZoneDiagramView.load(svgInputStream, metadataInputStream, switchId -> handleSwitchPositionchange(c, switchId), AbstractSingleLineDiagramViewer.this);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new ContainerDiagramResult(diagramView, svgData, metadataData, jsonData);
        }

        private void handleSwitchPositionchange(ContainerDiagram c, String switchId) {
            Switch sw = c.getNetwork().getSwitch(switchId);
            if (sw != null) {
                sw.setOpen(!sw.isOpen());
                DiagramStyleProvider styleProvider = styles.get(styleComboBox.getSelectionModel().getSelectedItem());
                styleProvider.reset();
                loadDiagram(c);
            }
        }

        private void loadDiagram(ContainerDiagram c) {
            Service<ContainerDiagramResult> loader = new Service<ContainerDiagramResult>() {
                @Override
                protected Task<ContainerDiagramResult> createTask() {
                    return new Task<ContainerDiagramResult>() {
                        @Override
                        protected ContainerDiagramResult call() {
                            return createContainerDiagramView(c);
                        }
                    };
                }
            };
            loader.setOnScheduled(event -> {
                Text loading = new Text("Loading...");
                loading.setFont(Font.font(30));
                flowPane.setContent(loading);
                svgTextArea.setText("");
                metadataTextArea.setText("");
                jsonTextArea.setText("");
            });
            loader.setOnSucceeded(event -> {
                ContainerDiagramResult result = (ContainerDiagramResult) event.getSource().getValue();
                if (result.getView() != null) {
                    flowPane.setContent(result.getView());
                }
                svgTextArea.setText(result.getSvgData());
                metadataTextArea.setText(result.getMetadataData());
                jsonTextArea.setText(result.getJsonData());
            });
            loader.setOnFailed(event -> {
                Throwable e = event.getSource().getException();
                LOGGER.error(e.toString(), e);
            });
            loader.start();
        }

        private ComponentLibrary getComponentLibrary() {
            String selectedItem = svgLibraryComboBox.getSelectionModel().getSelectedItem();
            return svgLibraries.get(selectedItem);
        }

        private SubstationLayoutFactory getSubstationLayoutFactory() {
            String selectedItem = substationLayoutComboBox.getSelectionModel().getSelectedItem();
            return substationsLayouts.get(selectedItem);
        }

        private ZoneLayoutFactory getZoneLayoutFactory() {
            String selectedItem = zoneLayoutComboBox.getSelectionModel().getSelectedItem();
            return zonesLayouts.get(selectedItem);
        }

        private void createArea(TextField searchField, Button searchButton, Button saveButton,
                                String descrSave, String extensionSave,
                                TextArea textArea, VBox area,
                                AtomicReference<Integer> searchStart) {
            HBox searchBox = new HBox();
            searchBox.setSpacing(20);
            searchBox.setPadding(new Insets(10));
            searchField.setPrefColumnCount(35);
            searchBox.getChildren().add(searchField);
            searchBox.getChildren().add(searchButton);
            searchBox.getChildren().add(saveButton);

            searchStart.set(0);
            searchButton.setOnAction(evh -> {
                String txtPattern = searchField.getText();
                Pattern pattern = Pattern.compile(txtPattern);
                Matcher matcher = pattern.matcher(textArea.getText());
                boolean found = matcher.find(searchStart.get());
                if (found) {
                    textArea.selectRange(matcher.start(), matcher.end());
                    searchStart.set(matcher.end());
                } else {
                    textArea.deselect();
                    searchStart.set(0);
                    found = matcher.find(searchStart.get());
                    if (found) {
                        textArea.selectRange(matcher.start(), matcher.end());
                        searchStart.set(matcher.end());
                    }
                }
            });
            searchField.textProperty().addListener((observable, oldValue, newValue) ->
                searchStart.set(0)
            );

            saveButton.setOnAction(evh -> {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(descrSave, extensionSave);
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(getScene().getWindow());

                if (file != null) {
                    try {
                        PrintWriter writer;
                        writer = new PrintWriter(file);
                        writer.println(textArea.getText());
                        writer.close();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            });

            area.setSpacing(8);
            area.getChildren().add(searchBox);
            area.getChildren().add(textArea);
            VBox.setVgrow(searchBox, Priority.NEVER);
            VBox.setVgrow(textArea, Priority.ALWAYS);
            textArea.setEditable(false);
        }
    }

    abstract class AbstractSelectableContainer {

        protected final String id;

        protected final String name;

        protected final BooleanProperty checkedProperty = new SimpleBooleanProperty();

        protected boolean saveDiagrams = true;

        protected boolean execListener = true;

        AbstractSelectableContainer(String id, String name) {
            this.id = id;
            this.name = name;
            checkedProperty.addListener((obs, wasSelected, isNowSelected) -> {
                if (execListener) {
                    if (isNowSelected) {
                        addDiagramTab();
                    } else {
                        removeDiagramTab();
                    }
                    if (saveDiagrams) {
                        saveSelectedDiagrams();
                    }
                }
            });
        }

        private void removeDiagramTab() {
            checkedDiagramsPane.getTabs().removeIf(tab -> tab.getText().equals(id));
        }

        abstract void addDiagramTab();

        abstract void closeTab(boolean removeFromSavedSelection);

        protected String getId() {
            return id;
        }

        protected String getName() {
            return name;
        }

        protected String getIdOrName() {
            return showNames.isSelected() ? name : id;
        }

        public BooleanProperty checkedProperty() {
            return checkedProperty;
        }

        public void setCheckedProperty(Boolean b) {
            checkedProperty.setValue(b);
        }

        public void setSaveDiagrams(boolean saveDiagrams) {
            this.saveDiagrams = saveDiagrams;
        }

        public void setExecListener(boolean execListener) {
            this.execListener = execListener;
        }

        public boolean isExecListener() {
            return execListener;
        }

        @Override
        public String toString() {
            return getIdOrName();
        }
    }

    private class SelectableVoltageLevel extends AbstractSelectableContainer {

        SelectableVoltageLevel(String id, String name) {
            super(id, name);
        }

        @Override
        public void addDiagramTab() {
            VoltageLevel vl = networkProperty.get().getVoltageLevel(id);
            if (vl != null) {
                ContainerDiagram cd = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL, Collections.singletonList(vl));
                Tab tab = new Tab(id, new ContainerDiagramPane(cd));
                tab.setTooltip(new Tooltip(getIdOrName()));
                tab.setOnCloseRequest(e -> closeTab(true));

                ContextMenu menu = new ContextMenu();
                MenuItem itemCloseTab = new MenuItem(CLOSE_TAB_ACTION);
                itemCloseTab.setOnAction(e -> closeTab(true));
                MenuItem itemCloseAllTabs = new MenuItem(CLOSE_ALL_TABS_ACTION);
                itemCloseAllTabs.setOnAction(e -> closeAllTabs());

                menu.getItems().add(itemCloseTab);
                menu.getItems().add(itemCloseAllTabs);
                tab.setContextMenu(menu);
                checkedDiagramsPane.getTabs().add(tab);
                checkedDiagramsPane.getSelectionModel().select(tab);
            } else {
                LOGGER.warn("Voltage level {} not found", id);
            }
        }

        @Override
        public void closeTab(boolean removeFromSavedSelection) {
            checkedProperty.set(false);
            checkvItemTree(id, false);
            if (removeFromSavedSelection) {
                saveSelectedVoltageLevels.remove(id);
            }
        }
    }

    private class SelectableSubstation extends AbstractSelectableContainer {
        SelectableSubstation(String id, String name) {
            super(id, name);
        }

        @Override
        public void addDiagramTab() {
            Substation s = networkProperty.get().getSubstation(id);
            if (s != null) {
                ContainerDiagram cd = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.SUBSTATION, Collections.singletonList(s));
                Tab tab = new Tab(id, new ContainerDiagramPane(cd));
                tab.setTooltip(new Tooltip(getIdOrName()));
                tab.setOnCloseRequest(e -> closeTab(true));

                ContextMenu menu = new ContextMenu();
                MenuItem itemCloseTab = new MenuItem(CLOSE_TAB_ACTION);
                itemCloseTab.setOnAction(e -> closeTab(true));

                MenuItem itemCloseAllTabs = new MenuItem(CLOSE_ALL_TABS_ACTION);
                itemCloseAllTabs.setOnAction(e -> closeAllTabs());

                menu.getItems().add(itemCloseTab);
                menu.getItems().add(itemCloseAllTabs);
                tab.setContextMenu(menu);
                checkedDiagramsPane.getTabs().add(tab);
                checkedDiagramsPane.getSelectionModel().select(tab);
            } else {
                LOGGER.warn("Substation {} not found", id);
            }
        }

        @Override
        public void closeTab(boolean removeFromSavedSelection) {
            checkedProperty.set(false);
            checksItemTree(id, false);
            if (removeFromSavedSelection) {
                saveSelectedSubstations.remove(id);
            }
        }
    }

    private class SelectableZone extends AbstractSelectableContainer {
        private ZoneId zoneId;

        SelectableZone(ZoneId zoneId, String name) {
            super(null, name);
            this.zoneId = zoneId;
        }

        public ZoneId getZoneId() {
            return zoneId;
        }

        @Override
        public void addDiagramTab() {
            List<String> ids = zoneId.getSubstationsIds();
            List<Container> containerList = new ArrayList<>();
            ids.forEach(sid -> {
                Substation s = networkProperty.get().getSubstation(sid);
                if (s != null) {
                    containerList.add(s);
                } else {
                    LOGGER.warn("Substation {} not found", sid);
                }
            });

            ContainerDiagram cd = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.ZONE, containerList);
            cd.setContainerName(getName());
            Tab tab = new Tab(getName(), new ContainerDiagramPane(cd));
            tab.setTooltip(new Tooltip(showNames.isSelected() ? cd.getName() : cd.getId()));
            tab.setOnCloseRequest(e -> closeTab(true));

            ContextMenu menu = new ContextMenu();
            MenuItem itemCloseTab = new MenuItem(CLOSE_TAB_ACTION);
            itemCloseTab.setOnAction(e -> closeTab(true));

            MenuItem itemCloseAllTabs = new MenuItem(CLOSE_ALL_TABS_ACTION);
            itemCloseAllTabs.setOnAction(e -> closeAllTabs());

            menu.getItems().add(itemCloseTab);
            menu.getItems().add(itemCloseAllTabs);
            tab.setContextMenu(menu);
            checkedDiagramsPane.getTabs().add(tab);
            checkedDiagramsPane.getSelectionModel().select(tab);
        }

        public void closeTab(boolean removeFromSavedSelection) {
            checkedProperty.set(false);
        }
    }

    private VoltageLevelLayoutFactory getVoltageLevelLayoutFactory() {
        String selectedItem = voltageLevelLayoutComboBox.getSelectionModel().getSelectedItem();
        return voltageLevelsLayouts.get(selectedItem);
    }

    private void setParameters(LayoutParameters layoutParameters) {
        this.layoutParameters.set(new LayoutParameters(layoutParameters));
    }

    private void addSpinner(String label, double min, double max, double amountToStepBy, int row,
                            ToDoubleFunction<LayoutParameters> initializer,
                            BiFunction<LayoutParameters, Double, LayoutParameters> updater) {
        Spinner<Double> spinner = new Spinner<>(min, max, initializer.applyAsDouble(layoutParameters.get()), amountToStepBy);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> setParameters(updater.apply(layoutParameters.get(), newValue)));
        parametersPane.add(new Label(label), 0, row);
        parametersPane.add(spinner, 0, row + 1);
    }

    private void addCheckBox(String label, int row,
                             Predicate<LayoutParameters> initializer,
                             BiFunction<LayoutParameters, Boolean, LayoutParameters> updater) {
        CheckBox cb = new CheckBox(label);
        cb.setSelected(initializer.test(layoutParameters.get()));
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> setParameters(updater.apply(layoutParameters.get(), newValue)));
        parametersPane.add(cb, 0, row);
    }

    private void initPositionLayoutCheckBox(Predicate<PositionVoltageLevelLayoutFactory> initializer, CheckBox stackCb) {
        VoltageLevelLayoutFactory layoutFactory = getVoltageLevelLayoutFactory();
        stackCb.setSelected(layoutFactory instanceof PositionVoltageLevelLayoutFactory && initializer.test((PositionVoltageLevelLayoutFactory) layoutFactory));
        stackCb.setDisable(!(layoutFactory instanceof PositionVoltageLevelLayoutFactory));
    }

    private void addPositionLayoutCheckBox(String label, int rowIndex, Predicate<PositionVoltageLevelLayoutFactory> initializer,
                                           BiFunction<PositionVoltageLevelLayoutFactory, Boolean, PositionVoltageLevelLayoutFactory> updater) {
        CheckBox stackCb = new CheckBox(label);
        initPositionLayoutCheckBox(initializer, stackCb);
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> initPositionLayoutCheckBox(initializer, stackCb));
        stackCb.selectedProperty().addListener((observable, oldValue, newValue) -> {
            VoltageLevelLayoutFactory layoutFactory = getVoltageLevelLayoutFactory();
            if (layoutFactory instanceof PositionVoltageLevelLayoutFactory) {
                updater.apply((PositionVoltageLevelLayoutFactory) layoutFactory, newValue);
                // just to trigger diagram update
                refreshDiagram();
            }
        });

        parametersPane.add(stackCb, 0, rowIndex);
    }

    private void createParametersPane() {
        parametersPane = new GridPane();
        parametersPane.setHgap(5);
        parametersPane.setVgap(5);
        parametersPane.setPadding(new Insets(5, 5, 5, 5));

        int rowIndex = 0;

        Button fitToContent = new Button("Fit to content");
        fitToContent.setOnAction(event -> {
            ContainerDiagramPane pane = null;
            Tab tab = diagramsPane.getSelectionModel().getSelectedItem();
            if (tab != null) {
                if (tab == tabChecked) {
                    if (checkedDiagramsPane.getSelectionModel().getSelectedItem() != null) {
                        pane = (ContainerDiagramPane) checkedDiagramsPane.getSelectionModel().getSelectedItem().getContent();
                    }
                } else {
                    pane = (ContainerDiagramPane) selectedDiagramPane.getCenter();
                }
                if (pane != null) {
                    ((AbstractContainerDiagramView) pane.getFlowPane().getContent()).fitToContent(
                            pane.getFlowPane().getViewportBounds().getWidth(), 20.,
                            pane.getFlowPane().getViewportBounds().getHeight(), 20.);
                    pane.getFlowPane().setHvalue(pane.getFlowPane().getHmin());
                    pane.getFlowPane().setVvalue(pane.getFlowPane().getVmin());
                }
            }
        });

        parametersPane.add(fitToContent, 0, rowIndex++);

        // svg library list
        svgLibraryComboBox.getItems().addAll(svgLibraries.keySet());
        svgLibraryComboBox.getSelectionModel().selectFirst();
        svgLibraryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Design:"), 0, rowIndex++);
        parametersPane.add(svgLibraryComboBox, 0, rowIndex++);

        styleComboBox.getItems().addAll(styles.keySet());
        styleComboBox.getSelectionModel().selectFirst();
        styleComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Style:"), 0, rowIndex++);
        parametersPane.add(styleComboBox, 0, rowIndex++);

        // substation layout list
        substationLayoutComboBox.getItems().addAll(substationsLayouts.keySet());
        substationLayoutComboBox.getSelectionModel().selectFirst();
        substationLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Substation Layout:"), 0, rowIndex++);
        parametersPane.add(substationLayoutComboBox, 0, rowIndex++);

        // voltageLevel layout list
        voltageLevelLayoutComboBox.getItems().addAll(voltageLevelsLayouts.keySet());
        voltageLevelLayoutComboBox.getSelectionModel().selectFirst();
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());

        parametersPane.add(new Label("VoltageLevel Layout:"), 0, rowIndex++);
        parametersPane.add(voltageLevelLayoutComboBox, 0, rowIndex++);

        // zone layout list
        zoneLayoutComboBox.getItems().addAll(zonesLayouts.keySet());
        zoneLayoutComboBox.getSelectionModel().selectFirst();
        zoneLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Zone Layout:"), 0, rowIndex++);
        parametersPane.add(zoneLayoutComboBox, 0, rowIndex++);

        //CGMES-DL diagrams names list
        parametersPane.add(new Label("CGMES-DL Diagrams:"), 0, ++rowIndex);
        parametersPane.add(diagramNamesComboBox, 0, ++rowIndex);
        diagramNamesComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        diagramNamesComboBox.setDisable(true);
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> setDiagramsNamesContent(networkProperty.get(), false));
        rowIndex += 1;

        addSpinner("Horizontal substation padding:", 50, 300, 5, rowIndex, LayoutParameters::getHorizontalSubstationPadding, LayoutParameters::setHorizontalSubstationPadding);
        rowIndex += 2;
        addSpinner("Vertical substation padding:", 50, 300, 5, rowIndex, LayoutParameters::getVerticalSubstationPadding, LayoutParameters::setVerticalSubstationPadding);
        rowIndex += 2;
        addSpinner("Initial busbar X:", 0, 100, 5, rowIndex, LayoutParameters::getInitialXBus, LayoutParameters::setInitialXBus);
        rowIndex += 2;
        addSpinner("Initial busbar Y:", 0, 500, 5, rowIndex, LayoutParameters::getInitialYBus, LayoutParameters::setInitialYBus);
        rowIndex += 2;
        addSpinner("Busbar vertical space:", 10, 100, 5, rowIndex, LayoutParameters::getVerticalSpaceBus, LayoutParameters::setVerticalSpaceBus);
        rowIndex += 2;
        addSpinner("Horizontal busbar padding:", 10, 100, 5, rowIndex, LayoutParameters::getHorizontalBusPadding, LayoutParameters::setHorizontalBusPadding);
        rowIndex += 2;
        addSpinner("Cell width:", 10, 100, 5, rowIndex, LayoutParameters::getCellWidth, LayoutParameters::setCellWidth);
        rowIndex += 2;
        addSpinner("Extern cell height:", 100, 500, 10, rowIndex, LayoutParameters::getExternCellHeight, LayoutParameters::setExternCellHeight);
        rowIndex += 2;
        addSpinner("Intern cell height:", 10, 100, 5, rowIndex, LayoutParameters::getInternCellHeight, LayoutParameters::setInternCellHeight);
        rowIndex += 2;
        addSpinner("Stack height:", 10, 100, 5, rowIndex, LayoutParameters::getStackHeight, LayoutParameters::setStackHeight);
        rowIndex += 2;
        addCheckBox("Show grid", rowIndex, LayoutParameters::isShowGrid, LayoutParameters::setShowGrid);
        rowIndex += 1;
        addCheckBox("Show internal nodes", rowIndex, LayoutParameters::isShowInternalNodes, LayoutParameters::setShowInternalNodes);
        rowIndex += 1;
        addCheckBox("Draw straight wires", rowIndex, LayoutParameters::isDrawStraightWires, LayoutParameters::setDrawStraightWires);
        rowIndex += 1;
        addPositionLayoutCheckBox("Stack feeders", rowIndex, PositionVoltageLevelLayoutFactory::isFeederStacked, PositionVoltageLevelLayoutFactory::setFeederStacked);
        rowIndex += 1;
        addPositionLayoutCheckBox("Remove fictitious nodes", rowIndex, PositionVoltageLevelLayoutFactory::isRemoveUnnecessaryFictitiousNodes, PositionVoltageLevelLayoutFactory::setRemoveUnnecessaryFictitiousNodes);
        rowIndex += 1;
        addPositionLayoutCheckBox("Substitute singular fictitious nodes", rowIndex, PositionVoltageLevelLayoutFactory::isSubstituteSingularFictitiousByFeederNode, PositionVoltageLevelLayoutFactory::setSubstituteSingularFictitiousByFeederNode);
        rowIndex += 1;
        addCheckBox("Show inductor for three windings transformers", rowIndex, LayoutParameters::isShowInductorFor3WT, LayoutParameters::setShowInductorFor3WT);
        rowIndex += 1;
        addCheckBox("Shift feeders height", rowIndex, LayoutParameters::isShiftFeedersPosition, LayoutParameters::setShiftFeedersPosition);
        rowIndex += 1;
        addSpinner("Shift feeders height scale factor:", 1, 10, 1, rowIndex, LayoutParameters::getScaleShiftFeedersPosition, LayoutParameters::setScaleShiftFeedersPosition);
        rowIndex += 2;
        addSpinner("Scale factor:", 1, 20, 1, rowIndex, LayoutParameters::getScaleFactor, LayoutParameters::setScaleFactor);
        rowIndex += 2;
        addSpinner("Arrows distance:", 0, 200, 1, rowIndex, LayoutParameters::getArrowDistance, LayoutParameters::setArrowDistance);
        rowIndex += 2;
        addCheckBox("Avoid SVG components duplication", rowIndex, LayoutParameters::isAvoidSVGComponentsDuplication, LayoutParameters::setAvoidSVGComponentsDuplication);

        rowIndex += 1;
        addCheckBox("Adapt cell height to content", rowIndex, LayoutParameters::isAdaptCellHeightToContent, LayoutParameters::setAdaptCellHeightToContent);
        rowIndex += 2;
        addSpinner("Min space between components:", 8, 60, 1, rowIndex, LayoutParameters::getMinSpaceBetweenComponents, LayoutParameters::setMinSpaceBetweenComponents);
        rowIndex += 2;
        addSpinner("Minimum extern cell height:", 80, 300, 10, rowIndex, LayoutParameters::getMinExternCellHeight, LayoutParameters::setMinExternCellHeight);
    }

    private void setDiagramsNamesContent(Network network, boolean setValues) {
        if (network != null && NetworkDiagramData.checkNetworkDiagramData(network)) {
            if (setValues) {
                diagramNamesComboBox.getItems().setAll(NetworkDiagramData.getDiagramsNames(network));
                diagramNamesComboBox.getSelectionModel().clearSelection();
                diagramNamesComboBox.setValue(null);
            }
            diagramNamesComboBox.setDisable(!(getVoltageLevelLayoutFactory() instanceof CgmesVoltageLevelLayoutFactory));
        } else {
            diagramNamesComboBox.getItems().clear();
            diagramNamesComboBox.setDisable(true);
        }
    }

    private void refreshDiagram() {
        layoutParameters.set(new LayoutParameters(layoutParameters.get()));
    }

    private void loadSelectedVoltageLevelsDiagrams() {
        String selectedIdsPropertyValue = preferences.get(SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY, null);
        if (selectedIdsPropertyValue != null) {
            try {
                Set<String> selectedIds = new HashSet<>(objectMapper.readValue(selectedIdsPropertyValue, new TypeReference<List<String>>() {
                }));
                selectableVoltageLevels.stream()
                        .filter(selectableObject -> selectedIds.contains(selectableObject.getId()))
                        .forEach(selectableVoltageLevel -> {
                            selectableVoltageLevel.setSaveDiagrams(false);
                            selectableVoltageLevel.checkedProperty().set(true);
                            selectableVoltageLevel.setSaveDiagrams(true);
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void loadSelectedSubstationsDiagrams() {
        String selectedPropertyValue = preferences.get(SELECTED_SUBSTATION_IDS_PROPERTY, null);

        if (selectedPropertyValue != null) {
            try {
                Set<String> selectedSubstationIds = new HashSet<>(objectMapper.readValue(selectedPropertyValue, new TypeReference<List<String>>() {
                }));
                selectableSubstations.stream()
                        .filter(selectableSubstation -> selectedSubstationIds.contains(selectableSubstation.getId()))
                        .forEach(selectableSubstation -> {
                            selectableSubstation.setSaveDiagrams(false);
                            selectableSubstation.checkedProperty().set(true);
                            selectableSubstation.setSaveDiagrams(true);
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void loadSelectedZonesDiagrams() {
        String selectedPropertyValue = preferences.get(SELECTED_ZONE_IDS_PROPERTY, null);

        if (selectedPropertyValue != null) {
            try {
                Set<String> selectedZoneIds = new HashSet<>(objectMapper.readValue(selectedPropertyValue, new TypeReference<List<String>>() {
                }));
                selectedZoneIds.forEach(s -> {
                    // Get the zone name and the zone substations ids
                    String zoneName = StringUtils.substringBefore(s, "[");
                    String[] substationsIds = StringUtils.split(StringUtils.substringBetween(s, "[", "]"), "\n");
                    SelectableZone sZone = new SelectableZone(ZoneId.create(Arrays.asList(substationsIds)), zoneName);
                    selectableZones.add(sZone);
                    sZone.setSaveDiagrams(false);
                    sZone.setCheckedProperty(true);
                    sZone.setSaveDiagrams(true);
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /*
        Handling the display of names/id in the substations tree
     */
    private void initTreeCellFactory() {
        substationsTree.setCellFactory(param -> {
            CheckBoxTreeCell<ContainerDiagram> treeCell = new CheckBoxTreeCell<ContainerDiagram>() {
                @Override
                public void updateItem(ContainerDiagram item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item != null) {
                        if (zoneBuildMode.isSelected()) {
                            CheckBoxTreeItem<ContainerDiagram> value = (CheckBoxTreeItem<ContainerDiagram>) treeItemProperty().getValue();
                            this.disableProperty().set(value.isLeaf());
                        } else {
                            this.disableProperty().set(false);
                        }
                    }
                }
            };

            StringConverter<TreeItem<ContainerDiagram>> strConvert = new StringConverter<TreeItem<ContainerDiagram>>() {
                @Override
                public String toString(TreeItem<ContainerDiagram> c) {
                    if (c.getValue() != null) {
                        return showNames.isSelected() ? c.getValue().getName() : c.getValue().getId();
                    } else {
                        return "";
                    }
                }

                @Override
                public TreeItem<ContainerDiagram> fromString(String string) {
                    return null;
                }
            };
            treeCell.setConverter(strConvert);
            return treeCell;
        });
    }

    @Override
    public void start(Stage primaryStage) {
        initLayoutsFactory();
        initStylesProvider();

        initTreeCellFactory();

        showNames.selectedProperty().addListener((observable, oldValue, newValue) -> {
            substationsTree.refresh();
            refreshDiagram();
        });

        hideSubstations.selectedProperty().addListener((observable, oldValue, newValue) -> {
            initSubstationsTree();
            substationsTree.refresh();
            zoneBuildMode.setDisable(newValue);
            zoneName.setDisable(newValue);
            createZone.setDisable(newValue);
        });

        zoneBuildMode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {  // Entering zone building mode
                // Saving the actual list of selected substations and voltage levels in the tree
                // Clearing all selections in the tree without closing the current displayed diagrams
                // Allowing substations selection, but with no display/undisplay feature
                // Disallowing voltage levels selection
                saveSelectedSubstations.clear();
                saveSelectedVoltageLevels.clear();

                selectableSubstations.forEach(s -> {
                    s.setExecListener(false);
                    if (s.checkedProperty().get()) {
                        s.closeTab(false);
                        saveSelectedSubstations.add(s.getId());
                    }
                });
                selectableVoltageLevels.forEach(v -> {
                    v.setExecListener(false);
                    if (v.checkedProperty().get()) {
                        v.closeTab(false);
                        saveSelectedVoltageLevels.add(v.getId());
                    }
                });

                createZone.setDisable(false);
                substationsTree.refresh();
            } else {  // Leaving zone building mode
                // Deselecting the currently selected substations
                selectableSubstations.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab(false));

                // Restoring the previously saved selections of substations and voltage levels in the tree, but with no display feature
                selectableSubstations.forEach(s -> {
                    if (saveSelectedSubstations.contains(s.getId())) {
                        s.checkedProperty().set(true);
                        checksItemTree(s.getId(), true);
                    }
                    s.setExecListener(true);
                });
                selectableVoltageLevels.forEach(v -> {
                    if (saveSelectedVoltageLevels.contains(v.getId())) {
                        v.checkedProperty().set(true);
                        checkvItemTree(v.getId(), true);
                    }
                    v.setExecListener(true);
                });

                createZone.setDisable(true);
                substationsTree.refresh();
            }
        });

        createZone.setOnAction(evh -> {
            List<String> substationsIds = selectableSubstations.stream().filter(s -> s.checkedProperty().get()).map(SelectableSubstation::getId).collect(Collectors.toList());
            if (substationsIds.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "At least one substation must be selected", ButtonType.CANCEL).show();
                return;
            }
            if (StringUtils.isEmpty(zoneName.getText())) {
                new Alert(Alert.AlertType.ERROR, "A zone name is required", ButtonType.CANCEL).show();
                return;
            }

            boolean zoneExist = false;
            for (Tab tab : checkedDiagramsPane.getTabs()) {
                if (!zoneExist && tab.getText().equals(zoneName.getText())) {
                    zoneExist = true;
                    break;
                }
            }
            if (zoneExist) {
                new Alert(Alert.AlertType.ERROR, "A zone with the same name already exists", ButtonType.CANCEL).show();
                return;
            }

            SelectableZone sZone = new SelectableZone(ZoneId.create(substationsIds), zoneName.getText());
            selectableZones.add(sZone);
            sZone.setCheckedProperty(true);

            // clearing all selections in the tree
            selectableSubstations.forEach(s -> {
                if (s.checkedProperty().get()) {
                    s.closeTab(false);
                }
            });
        });

        filterInput.textProperty().addListener(obs ->
            initSubstationsTree()
        );

        // handling the change of the network
        networkProperty.addListener((observable, oldNetwork, newNetwork) -> {
            selectableZones.clear();
            if (newNetwork == null) {
                selectableVoltageLevels.clear();
                selectableSubstations.clear();
            } else {
                selectableVoltageLevels.setAll(newNetwork.getVoltageLevelStream()
                        .map(vl -> new SelectableVoltageLevel(vl.getId(), vl.getName()))
                        .collect(Collectors.toList()));
                selectableSubstations.setAll(newNetwork.getSubstationStream()
                        .map(s -> new SelectableSubstation(s.getId(), s.getName()))
                        .collect(Collectors.toList()));
            }
        });
        diagramsPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabSelected = new Tab("Selected", selectedDiagramPane);
        tabChecked = new Tab("Checked", checkedDiagramsPane);
        diagramsPane.getTabs().setAll(tabSelected, tabChecked);

        createParametersPane();

        BorderPane voltageLevelPane = new BorderPane();
        Label filterLabel = new Label("Filter:");
        filterLabel.setMinWidth(60);
        GridPane voltageLevelToolBar = new GridPane();
        voltageLevelToolBar.setHgap(5);
        voltageLevelToolBar.setVgap(5);
        voltageLevelToolBar.setPadding(new Insets(5, 5, 5, 5));
        voltageLevelToolBar.add(showNames, 0, 0, 2, 1);
        voltageLevelToolBar.add(hideSubstations, 0, 1, 2, 1);
        voltageLevelToolBar.add(filterLabel, 0, 2, 1, 1);
        voltageLevelToolBar.add(filterInput, 1, 2, 1, 1);
        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        voltageLevelToolBar.getColumnConstraints().addAll(c0, c1);

        VBox boxZone = new VBox(10);
        createZone.setDisable(true);
        HBox boxZoneName = new HBox(10);
        Label zoneLabel = new Label("Name:");
        zoneLabel.setMinWidth(50);
        boxZoneName.getChildren().addAll(zoneLabel, zoneName);

        boxZone.getChildren().addAll(zoneBuildMode, boxZoneName, createZone);
        TitledPane zoneToolBar = new TitledPane("Zone", boxZone);
        zoneToolBar.setCollapsible(false);

        voltageLevelToolBar.add(zoneToolBar, 0, 3, 2, 1);

        voltageLevelPane.setTop(voltageLevelToolBar);
        voltageLevelPane.setCenter(substationsTree);

        SplitPane splitPane = new SplitPane(voltageLevelPane, diagramsPane, new ScrollPane(parametersPane));
        splitPane.setDividerPositions(0.2, 0.7, 0.1);

        Node casePane = createCasePane(primaryStage);
        BorderPane.setMargin(casePane, new Insets(3, 3, 3, 3));
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(splitPane);
        mainPane.setTop(casePane);

        // selected voltageLevels diagrams reloading
        selectableVoltageLevels.addListener(new ListChangeListener<SelectableVoltageLevel>() {
            @Override
            public void onChanged(Change<? extends SelectableVoltageLevel> c) {
                loadSelectedVoltageLevelsDiagrams();
                selectableVoltageLevels.remove(this);
            }
        });

        // selected substation diagrams reloading
        selectableSubstations.addListener(new ListChangeListener<SelectableSubstation>() {
            @Override
            public void onChanged(Change<? extends SelectableSubstation> c) {
                loadSelectedSubstationsDiagrams();
                selectableSubstations.remove(this);
            }
        });

        // Handling selection of a substation or a voltageLevel in the substations tree
        substationsTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ContainerDiagram>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<ContainerDiagram>> observable, TreeItem<ContainerDiagram> oldValue, TreeItem<ContainerDiagram> newValue) {
                if (newValue == null) {
                    return;
                }
                ContainerDiagram c = newValue.getValue();
                selectedDiagramPane.setCenter(new ContainerDiagramPane(c));
            }
        });

        // case reloading
        loadNetworkFromPreferences();

        Scene scene = new Scene(mainPane, 1000, 800);
        primaryStage.setTitle("Substation diagram viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected void loadNetworkFromPreferences() {
    }

    protected abstract Node createCasePane(Stage primaryStage);

    /*
        check/uncheck a voltageLevel in the substations tree
     */
    private void checkVoltageLevel(VoltageLevel v, Boolean checked) {
        selectableVoltageLevels.stream()
                .filter(selectableVoltageLevel -> selectableVoltageLevel.getIdOrName().equals(showNames.isSelected() ? v.getName() : v.getId()))
                .forEach(selectableVoltageLevel -> selectableVoltageLevel.setCheckedProperty(checked));
    }

    /*
        check/uncheck a substation in the substations tree
     */
    private void checkSubstation(Substation s, Boolean checked) {
        selectableSubstations.stream()
                .filter(selectableSubstation -> selectableSubstation.getIdOrName().equals(showNames.isSelected() ? s.getName() : s.getId()))
                .forEach(selectableSubstation -> selectableSubstation.setCheckedProperty(checked));
    }

    private void initVoltageLevelsTree(TreeItem<ContainerDiagram> rootItem,
                                       Substation s, String filter, boolean emptyFilter,
                                       Map<String, SelectableSubstation> mapSubstations,
                                       Map<String, SelectableVoltageLevel> mapVoltageLevels) {
        boolean firstVL = true;
        CheckBoxTreeItem<ContainerDiagram> sItem = null;

        for (VoltageLevel v : s.getVoltageLevels()) {
            boolean vlOk = showNames.isSelected() ? v.getName().contains(filter) : v.getId().contains(filter);

            if (!emptyFilter && !vlOk) {
                continue;
            }

            ContainerDiagram cdv = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL, Collections.singletonList(v));
            CheckBoxTreeItem<ContainerDiagram> vItem = new CheckBoxTreeItem<>(cdv);
            vItem.setIndependent(true);
            if (mapVoltageLevels.containsKey(v.getId()) && mapVoltageLevels.get(v.getId()).checkedProperty().get()) {
                vItem.setSelected(true);
            }

            if (firstVL && !hideSubstations.isSelected()) {
                ContainerDiagram cds = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.SUBSTATION, Collections.singletonList(s));
                sItem = new CheckBoxTreeItem<>(cds);
                sItem.setIndependent(true);
                sItem.setExpanded(true);
                if (mapSubstations.containsKey(s.getId()) && mapSubstations.get(s.getId()).checkedProperty().get()) {
                    sItem.setSelected(true);
                }
                rootItem.getChildren().add(sItem);
                sItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                    checkSubstation(s, newVal)
                );
            }

            firstVL = false;

            if (sItem != null) {
                sItem.getChildren().add(vItem);
            } else {
                rootItem.getChildren().add(vItem);
            }

            vItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                    checkVoltageLevel(v, newVal));
        }
    }

    private void initSubstationsTree() {
        String filter = filterInput.getText();
        boolean emptyFilter = StringUtils.isEmpty(filter);

        Network n = networkProperty.get();
        TreeItem<ContainerDiagram> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        Map<String, SelectableSubstation> mapSubstations = selectableSubstations.stream()
                .collect(Collectors.toMap(SelectableSubstation::getId, Function.identity()));
        Map<String, SelectableVoltageLevel> mapVoltageLevels = selectableVoltageLevels.stream()
                .collect(Collectors.toMap(SelectableVoltageLevel::getId, Function.identity()));

        for (Substation s : n.getSubstations()) {
            initVoltageLevelsTree(rootItem, s, filter, emptyFilter, mapSubstations, mapVoltageLevels);
        }

        if (substationsTree.getRoot() != null) {
            substationsTree.getRoot().getChildren().clear();
        }

        substationsTree.setRoot(rootItem);
        substationsTree.setShowRoot(false);
    }

    @Override
    public void display(String voltageLevelId) {
        VoltageLevel v = networkProperty.get().getVoltageLevel(voltageLevelId);
        if (diagramsPane.getSelectionModel().getSelectedItem() == tabChecked) {
            checkVoltageLevel(v, true);
            checkvItemTree(voltageLevelId, true);
            checkedDiagramsPane.getTabs().stream().forEach(tab -> {
                if (tab.getText().equals(voltageLevelId)) {
                    checkedDiagramsPane.getSelectionModel().select(tab);
                }
            });
        } else if (diagramsPane.getSelectionModel().getSelectedItem() == tabSelected) {
            ContainerDiagram cd = new ContainerDiagram(ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL, Collections.singletonList(v));
            selectedDiagramPane.setCenter(new ContainerDiagramPane(cd));
        }
    }

    private void checkvItemTree(String id, boolean selected) {
        substationsTree.getRoot().getChildren().stream().forEach(childS ->
                childS.getChildren().stream().forEach(childV -> {
                    if (childV.getValue().getId().equals(id)) {
                        ((CheckBoxTreeItem) childV).setSelected(selected);
                    }
                })
        );
    }

    private void checksItemTree(String id, boolean selected) {
        substationsTree.getRoot().getChildren().stream().forEach(child -> {
            if (child.getValue().getId().equals(id)) {
                ((CheckBoxTreeItem) child).setSelected(selected);
            }
        });
    }

    public void saveSelectedDiagrams() {
        try {
            String selectedVoltageLevelIdsPropertyValue = objectMapper.writeValueAsString(checkedDiagramsPane.getTabs().stream()
                    .filter(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.VOLTAGE_LEVEL)
                    .map(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getId())
                    .collect(Collectors.toList()));
            preferences.put(SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY, selectedVoltageLevelIdsPropertyValue);

            String selectedSubstationIdsPropertyValue = objectMapper.writeValueAsString(checkedDiagramsPane.getTabs().stream()
                    .filter(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.SUBSTATION)
                    .map(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getId())
                    .collect(Collectors.toList()));
            preferences.put(SELECTED_SUBSTATION_IDS_PROPERTY, selectedSubstationIdsPropertyValue);

            String selectedZoneIdsPropertyValue = objectMapper.writeValueAsString(checkedDiagramsPane.getTabs().stream()
                    .filter(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getContainerDiagramType() == ContainerDiagram.ContainerDiagramType.ZONE)
                    .map(tab -> ((ContainerDiagramPane) tab.getContent()).getDiag().getId())
                    .collect(Collectors.toList()));
            preferences.put(SELECTED_ZONE_IDS_PROPERTY, selectedZoneIdsPropertyValue);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void closeAllTabs() {
        selectableVoltageLevels.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab(true));
        selectableSubstations.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab(true));
        selectableZones.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab(true));
    }

    protected void setNetwork(Network network) {
        closeAllTabs();
        updateLayoutsFactory(network);
        updateStylesProvider(network);
        networkProperty.setValue(network);
        initSubstationsTree();
        setDiagramsNamesContent(network, true);
        loadSelectedZonesDiagrams();
    }

    private void initLayoutsFactory() {
        voltageLevelsLayouts.put(SMART_LAYOUT_NAME, null);
        voltageLevelsLayouts.put("Auto extensions", new PositionVoltageLevelLayoutFactory(new PositionFromExtension()));
        voltageLevelsLayouts.put("Auto without extensions", new PositionVoltageLevelLayoutFactory(new PositionFree()));
        voltageLevelsLayouts.put("Auto without extensions Clustering", new PositionVoltageLevelLayoutFactory(new PositionByClustering()));
        voltageLevelsLayouts.put("Random", new RandomVoltageLevelLayoutFactory(500, 500));
        voltageLevelsLayouts.put(CGMES_LAYOUT_NAME, null);

        substationsLayouts.put("Horizontal", new HorizontalSubstationLayoutFactory());
        substationsLayouts.put("Vertical", new VerticalSubstationLayoutFactory());

        substationsLayouts.put(CGMES_LAYOUT_NAME, null);
        substationsLayouts.put(SMART_LAYOUT_NAME, new ForceSubstationLayoutFactory(CompactionType.NONE));
        substationsLayouts.put(SMART_WITH_HORIZONTAL_COMPACTION_LAYOUT_NAME, new ForceSubstationLayoutFactory(CompactionType.HORIZONTAL));
        substationsLayouts.put(SMART_WITH_VERTICAL_COMPACTION_LAYOUT_NAME, new ForceSubstationLayoutFactory(CompactionType.VERTICAL));

        zonesLayouts.put(SMART_LAYOUT_NAME, new ForceZoneLayoutFactory(CompactionType.NONE));
        zonesLayouts.put(SMART_WITH_HORIZONTAL_COMPACTION_LAYOUT_NAME, new ForceZoneLayoutFactory(CompactionType.HORIZONTAL));
        zonesLayouts.put(SMART_WITH_VERTICAL_COMPACTION_LAYOUT_NAME, new ForceZoneLayoutFactory(CompactionType.VERTICAL));

        zonesLayouts.put(CGMES_LAYOUT_NAME, null);
    }

    private void updateLayoutsFactory(Network network) {
        voltageLevelsLayouts.put(SMART_LAYOUT_NAME, new SmartVoltageLevelLayoutFactory(network));
        voltageLevelsLayouts.put(CGMES_LAYOUT_NAME, new CgmesVoltageLevelLayoutFactory(network));

        substationsLayouts.put(CGMES_LAYOUT_NAME, new CgmesSubstationLayoutFactory(network));

        zonesLayouts.put(CGMES_LAYOUT_NAME, new CgmesZoneLayoutFactory(network));
    }

    private void initStylesProvider() {
        styles.put("Default", new DefaultDiagramStyleProvider());
        styles.put("Nominal voltage", new NominalVoltageDiagramStyleProvider());
        styles.put("Topology", new TopologicalStyleProvider(null, null));
    }

    private void updateStylesProvider(Network network) {
        styles.put("Topology", new TopologicalStyleProvider(null, network));
    }
}
