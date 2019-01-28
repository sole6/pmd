/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.reactfx.value.Val;

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.util.fxdesigner.model.XPathEvaluationException;
import net.sourceforge.pmd.util.fxdesigner.popups.EventLogController;
import net.sourceforge.pmd.util.fxdesigner.util.AbstractController;
import net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil;
import net.sourceforge.pmd.util.fxdesigner.util.LimitedSizeStack;
import net.sourceforge.pmd.util.fxdesigner.util.SoftReferenceCache;
import net.sourceforge.pmd.util.fxdesigner.util.TextAwareNodeWrapper;
import net.sourceforge.pmd.util.fxdesigner.util.beans.SettingsPersistenceUtil;
import net.sourceforge.pmd.util.fxdesigner.util.beans.SettingsPersistenceUtil.PersistentProperty;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Duration;


/**
 * Main controller of the app. Mediator for subdivisions of the UI.
 *
 * @author Clément Fournier
 * @see NodeInfoPanelController
 * @see SourceEditorController
 * @see EventLogController
 * @see XPathPanelController
 * @since 6.0.0
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class MainDesignerController extends AbstractController {

    /**
     * Callback to the owner.
     */
    private final DesignerRoot designerRoot;


    /* Menu bar */
    @FXML
    private MenuItem setupAuxclasspathMenuItem;
    @FXML
    public MenuItem openEventLogMenuItem;
    @FXML
    private MenuItem openFileMenuItem;
    @FXML
    private MenuItem licenseMenuItem;
    @FXML
    private Menu openRecentMenu;
    @FXML
    private Menu fileMenu;
    /* Center toolbar */
    @FXML
    private ToggleButton bottomTabsToggle;
    /* Bottom panel */
    @FXML
    private TabPane bottomTabPane;
    @FXML
    private Tab xpathEditorTab;
    @FXML
    private SplitPane mainHorizontalSplitPane;
    /* Children */
    @FXML
    private NodeInfoPanelController nodeInfoPanelController;
    @FXML
    private XPathPanelController xpathPanelController;
    @FXML
    private SourceEditorController sourceEditorController;
    // we cache it but if it's not used the FXML is not created, etc
    private final SoftReferenceCache<EventLogController> eventLogController;

    // Other fields
    private final Stack<File> recentFiles = new LimitedSizeStack<>(5);


    public MainDesignerController(DesignerRoot owner) {
        this.designerRoot = owner;
        eventLogController = new SoftReferenceCache<>(() -> new EventLogController(owner, this));
    }

    @Override
    protected void beforeParentInit() {
        try {
            SettingsPersistenceUtil.restoreProperties(this, DesignerUtil.getSettingsFile());
        } catch (Exception e) {
            // shouldn't prevent the app from opening
            // in case the file is corrupted, it will be overwritten on shutdown
            e.printStackTrace();
        }

        initializeViewAnimation();

        licenseMenuItem.setOnAction(e -> showLicensePopup());
        openFileMenuItem.setOnAction(e -> onOpenFileClicked());
        openRecentMenu.setOnAction(e -> updateRecentFilesMenu());
        openRecentMenu.setOnShowing(e -> updateRecentFilesMenu());
        fileMenu.setOnShowing(e -> onFileMenuShowing());

        setupAuxclasspathMenuItem.setOnAction(e -> sourceEditorController.showAuxclasspathSetupPopup(designerRoot));

        openEventLogMenuItem.setOnAction(e -> eventLogController.getValue().showPopup());
        openEventLogMenuItem.textProperty().bind(
            designerRoot.getLogger().numNewLogEntriesProperty().map(i -> "Exception log (" + (i > 0 ? i : "no") + " new)")
        );

    }


    @Override
    protected void afterChildrenInit() {
        updateRecentFilesMenu();
        refreshAST(); // initial refreshing
        sourceEditorController.moveCaret(0, 0);
    }


    private void initializeViewAnimation() {

        // gets captured in the closure
        final double defaultMainHorizontalSplitPaneDividerPosition
                = mainHorizontalSplitPane.getDividerPositions()[0];


        // show/ hide bottom pane
        bottomTabsToggle.selectedProperty().addListener((observable, wasExpanded, isNowExpanded) -> {
            KeyValue keyValue = null;
            DoubleProperty divPosition = mainHorizontalSplitPane.getDividers().get(0).positionProperty();
            if (wasExpanded && !isNowExpanded) {
                keyValue = new KeyValue(divPosition, 1);
            } else if (!wasExpanded && isNowExpanded) {
                keyValue = new KeyValue(divPosition, defaultMainHorizontalSplitPaneDividerPosition);
            }

            if (keyValue != null) {
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), keyValue));
                timeline.play();
            }
        });
    }


    public void shutdown() {
        try {
            SettingsPersistenceUtil.persistProperties(this, DesignerUtil.getSettingsFile());
        } catch (IOException ioe) {
            // nevermind
            ioe.printStackTrace();
        }
    }


    /**
     * Attempts to refresh the AST with the up-to-date source,
     * also updating XPath results.
     */
    public void refreshAST() {
        Optional<Node> root = sourceEditorController.refreshAST();

        if (root.isPresent()) {
            xpathPanelController.evaluateXPath(root.get(), getLanguageVersion());
        } else {
            xpathPanelController.invalidateResults(true);
        }
    }

    /**
     * Refreshes the XPath results if the compilation unit is valid.
     * Otherwise does nothing.
     */
    public void refreshXPathResults() {
        sourceEditorController.getCompilationUnit().ifPresent(root -> xpathPanelController.evaluateXPath(root, getLanguageVersion()));
    }


    /**
     * Returns a wrapper around the given node that gives access
     * to its textual representation in the editor area.
     */
    public TextAwareNodeWrapper wrapNode(Node node) {
        return sourceEditorController.wrapNode(node);
    }


    /**
     * Executed when the user selects a node in a treeView or listView.
     */
    public void onNodeItemSelected(Node selectedValue) {
        onNodeItemSelected(selectedValue, false);
    }


    /**
     * Executed when the user selects a node in a treeView or listView.
     *
     * @param isFromNameDecl Whether the node was selected in the scope hierarchy treeview
     */
    public void onNodeItemSelected(Node selectedValue, boolean isFromNameDecl) {
        // doing that in parallel speeds it up
        Platform.runLater(() -> nodeInfoPanelController.setFocusNode(selectedValue, isFromNameDecl));
        Platform.runLater(() -> sourceEditorController.setFocusNode(selectedValue));
    }


    /**
     * Highlight a list of name occurrences.
     *
     * @param occurrences May be empty but never null.
     */
    public void highlightAsNameOccurences(List<NameOccurrence> occurrences) {
        sourceEditorController.highlightNameOccurrences(occurrences);
    }

    /**
     * Runs an XPath (2.0) query on the current AST.
     * Performs no side effects.
     *
     * @param query the query
     * @return the matched nodes
     * @throws XPathEvaluationException if the query fails
     */
    public List<Node> runXPathQuery(String query) throws XPathEvaluationException {
        return sourceEditorController.getCompilationUnit()
                                     .map(n -> xpathPanelController.runXPathQuery(n, getLanguageVersion(), query))
                                     .orElseGet(Collections::emptyList);
    }


    /**
     * Handles nodes that potentially caused an error.
     * This can for example highlight nodes on the
     * editor. Effects can be reset with {@link #resetSelectedErrorNodes()}.
     *
     * @param n Node
     */
    public void handleSelectedNodeInError(List<Node> n) {
        resetSelectedErrorNodes();
        sourceEditorController.highlightErrorNodes(n);
    }

    public void resetSelectedErrorNodes() {
        sourceEditorController.clearErrorNodes();
    }

    public void resetXPathResults() {
        sourceEditorController.clearXPathHighlight();
    }

    /** Replaces previously highlighted XPath results with the given nodes. */
    public void highlightXPathResults(List<Node> nodes) {
        sourceEditorController.highlightXPathResults(nodes);
    }

    private void showLicensePopup() {
        Alert licenseAlert = new Alert(AlertType.INFORMATION);
        licenseAlert.setWidth(500);
        licenseAlert.setHeaderText("License");

        ScrollPane scroll = new ScrollPane();
        try {
            scroll.setContent(new TextArea(IOUtils.toString(getClass().getResourceAsStream("LICENSE"),
                    StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        licenseAlert.getDialogPane().setContent(scroll);
        licenseAlert.showAndWait();
    }


    private void onFileMenuShowing() {
        openRecentMenu.setDisable(recentFiles.isEmpty());
    }


    private void onOpenFileClicked() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load source from file");
        File file = chooser.showOpenDialog(designerRoot.getMainStage());
        loadSourceFromFile(file);
    }

    private void loadSourceFromFile(File file) {
        if (file != null) {
            try {
                String source = IOUtils.toString(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
                sourceEditorController.setText(source);
                LanguageVersion guess = DesignerUtil.getLanguageVersionFromExtension(file.getName());
                if (guess != null) { // guess the language from the extension
                    sourceEditorController.setLanguageVersion(guess);
                    refreshAST();
                }

                recentFiles.push(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void updateRecentFilesMenu() {
        List<MenuItem> items = new ArrayList<>();
        List<File> filesToClear = new ArrayList<>();

        for (final File f : recentFiles) {
            if (f.exists() && f.isFile()) {
                CustomMenuItem item = new CustomMenuItem(new Label(f.getName()));
                item.setOnAction(e -> loadSourceFromFile(f));
                item.setMnemonicParsing(false);
                Tooltip.install(item.getContent(), new Tooltip(f.getAbsolutePath()));
                items.add(item);
            } else {
                filesToClear.add(f);
            }
        }
        recentFiles.removeAll(filesToClear);

        if (items.isEmpty()) {
            openRecentMenu.setDisable(true);
            return;
        }

        Collections.reverse(items);

        items.add(new SeparatorMenuItem());
        MenuItem clearItem = new MenuItem();
        clearItem.setText("Clear menu");
        clearItem.setOnAction(e -> {
            recentFiles.clear();
            openRecentMenu.setDisable(true);
        });
        items.add(clearItem);

        openRecentMenu.getItems().setAll(items);
    }


    public void invalidateAst() {
        nodeInfoPanelController.setFocusNode(null);
        xpathPanelController.invalidateResults(false);
        sourceEditorController.setFocusNode(null);
    }


    public LanguageVersion getLanguageVersion() {
        return sourceEditorController.getLanguageVersion();
    }


    public void setLanguageVersion(LanguageVersion version) {
        sourceEditorController.setLanguageVersion(version);
    }


    public Val<LanguageVersion> languageVersionProperty() {
        return sourceEditorController.languageVersionProperty();
    }


    @PersistentProperty
    public String getRecentFiles() {
        return recentFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator));
    }


    public void setRecentFiles(String files) {
        Arrays.stream(files.split(File.pathSeparator)).map(File::new).forEach(recentFiles::push);
    }


    @PersistentProperty
    public boolean isMaximized() {
        return designerRoot.getMainStage().isMaximized();
    }


    public void setMaximized(boolean b) {
        designerRoot.getMainStage().setMaximized(!b); // trigger change listener anyway
        designerRoot.getMainStage().setMaximized(b);
    }


    @PersistentProperty
    public boolean isBottomTabExpanded() {
        return bottomTabsToggle.isSelected();
    }


    public void setBottomTabExpanded(boolean b) {
        bottomTabsToggle.setSelected(b);
    }


    @PersistentProperty
    public int getBottomTabIndex() {
        return bottomTabPane.getSelectionModel().getSelectedIndex();
    }


    public void setBottomTabIndex(int i) {
        if (i >= 0 && i < bottomTabPane.getTabs().size()) {
            bottomTabPane.getSelectionModel().select(i);
        }
    }


    @Override
    public List<AbstractController> getChildren() {
        return Arrays.asList(xpathPanelController, sourceEditorController, nodeInfoPanelController);
    }
}
