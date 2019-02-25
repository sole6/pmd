/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.app;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.app.LogEntry.Category;

import javafx.stage.Stage;


/**
 * Interface for the singleton of the app.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public final class DesignerRoot {


    private final Stage mainStage;
    private final EventLogger logger;
    private final boolean developerMode;

    private final MessageChannel<Node> nodeSelectionChannel = new MessageChannel<>(Category.SELECTION_EVENT_TRACING);


    public DesignerRoot(Stage mainStage, boolean developerMode) {
        this.mainStage = mainStage;
        this.developerMode = developerMode;
        this.logger = new EventLogger(this);
    }


    /**
     * Gets the logger of the application.
     *
     * @return The logger
     */
    public EventLogger getLogger() {
        return logger;
    }


    /**
     * Gets the main stage of the application.
     *
     * @return The main stage
     */
    public Stage getMainStage() {
        return mainStage;
    }


    /**
     * If true, some more events are pushed to the event log, and
     * console streams are open. This is enabled by the -v or --verbose
     * option on command line for now.
     */
    public boolean isDeveloperMode() {
        return developerMode;
    }


    /**
     * Channel used to transmit node selection events to all interested components.
     */
    public MessageChannel<Node> getNodeSelectionChannel() {
        return nodeSelectionChannel;
    }
}
