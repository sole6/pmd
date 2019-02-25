/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.controls;

import static net.sourceforge.pmd.internal.util.IteratorUtil.toIterable;
import static net.sourceforge.pmd.util.fxdesigner.util.DesignerIteratorUtil.parentIterator;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.reactfx.EventSource;
import org.reactfx.EventStreams;
import org.reactfx.SuspendableEventStream;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.app.DesignerRoot;
import net.sourceforge.pmd.util.fxdesigner.app.NodeSelectionSource;

import javafx.beans.NamedArg;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;


/**
 * @author Clément Fournier
 * @since 6.12.0
 */
public class AstTreeView extends TreeView<Node> implements NodeSelectionSource {


    private final TreeViewWrapper<Node> myWrapper = new TreeViewWrapper<>(this);

    private ASTTreeItem selectedTreeItem;
    private final SuspendableEventStream<Node> selectionEvents;
    private final DesignerRoot designerRoot;


    /** Only provided for scenebuilder, not used at runtime. */
    public AstTreeView() {
        designerRoot = null;
        selectionEvents = null;
    }


    public AstTreeView(@NamedArg("designerRoot") DesignerRoot root) {
        designerRoot = root;

        EventSource<Node> eventSink = new EventSource<>();
        selectionEvents = eventSink.suppressible();

        initNodeSelectionHandling(root, selectionEvents, false);

        // this needs to be done even if the selection originates from this node
        EventStreams.changesOf(getSelectionModel().selectedItemProperty())
                    .subscribe(item -> highlightFocusNodeParents((ASTTreeItem) item.getOldValue(), (ASTTreeItem) item.getNewValue()));

        // push a node selection event whenever...
        //  * The selection changes
        EventStreams.valuesOf(getSelectionModel().selectedItemProperty())
                    .filterMap(Objects::nonNull, TreeItem::getValue)
                    .subscribe(eventSink::push);

        //  * the currently selected cell is explicitly clicked
        setCellFactory(tv -> new ASTTreeCell(n -> {
            // only push an event if the node was already selected
            if (selectedTreeItem != null && selectedTreeItem.getValue() != null && selectedTreeItem.getValue().equals(n)) {
                eventSink.push(n);
            }
        }));

    }



    /**
     * Focus the given node, handling scrolling if needed.
     */
    @Override
    public void setFocusNode(Node node) {
        SelectionModel<TreeItem<Node>> selectionModel = getSelectionModel();

        if (selectedTreeItem == null && node != null
            || selectedTreeItem != null && !Objects.equals(node, selectedTreeItem.getValue())) {
            // node is different from the old one
            // && node is not null

            ASTTreeItem found = ((ASTTreeItem) getRoot()).findItem(node);

            if (found != null && found.equals(selectedTreeItem)) {
                return;
            }

            if (found != null) {
                // don't fire any selection event while itself setting the selected item
                selectionEvents.suspendWhile(() -> selectionModel.select(found));
            }

            selectedTreeItem = found;

            getFocusModel().focus(selectionModel.getSelectedIndex());
            if (!isIndexVisible(selectionModel.getSelectedIndex())) {
                scrollTo(selectionModel.getSelectedIndex());
            }
        }
    }


    private void highlightFocusNodeParents(ASTTreeItem oldSelection, ASTTreeItem newSelection) {
        if (oldSelection != null) {
            // remove highlighting on the cells of the item
            sideEffectParents(oldSelection, (item, depth) -> item.setStyleClasses());
        }

        if (newSelection != null) {
            // 0 is the deepest node, "depth" goes up as we get up the parents
            sideEffectParents(newSelection, (item, depth) -> item.setStyleClasses("ast-parent", "depth-" + depth));
        }
    }


    private void sideEffectParents(ASTTreeItem deepest, BiConsumer<ASTTreeItem, Integer> itemAndDepthConsumer) {

        int depth = 0;
        for (TreeItem<Node> item : toIterable(parentIterator(deepest, true))) {
            // the depth is "reversed" here, i.e. the deepest node has depth 0
            itemAndDepthConsumer.accept((ASTTreeItem) item, depth++);
        }

    }


    /**
     * Returns true if the item at the given index
     * is visible in the TreeView.
     */
    private boolean isIndexVisible(int index) {
        return myWrapper.isIndexVisible(index);
    }


    @Override
    public DesignerRoot getDesignerRoot() {
        return designerRoot;
    }


}
