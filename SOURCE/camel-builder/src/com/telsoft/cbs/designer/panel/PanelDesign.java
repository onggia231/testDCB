package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.designer.action.ActionList;
import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.utils.CBRenderer;
import com.telsoft.cbs.designer.utils.TitledSeparator;
import com.telsoft.cbs.designer.utils.TreeUtils;
import com.telsoft.cbs.loader.*;
import com.telsoft.swing.MessageBox;
import com.telsoft.swing.Skin;
import lombok.Getter;
import org.apache.camel.NamedNode;
import org.apache.camel.model.RoutesDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class PanelDesign extends CBPanel {
    private static final JMenuItem menuNothing = new JMenuItem("(Nothing)");

    @Autowired
    private FileEditor fileEditor;
    private JPopupMenu menu = new JPopupMenu() {
        @Override
        public void show(java.awt.Component invoker, int x, int y) {
            if (!fileEditor.isReadOnly())
                super.show(invoker, x, y);
        }
    };
    private RoutesDefinition root = new RoutesDefinition();
    @Getter
    private CBTree tree = new CBTree(root, this);
    private JScrollPane scrollPane = new JScrollPane(tree);
    @Autowired
    private BeanManager beanManager;
    @Autowired
    private PanelEditor panelEditor;
    @Autowired
    private ActionList actionList;
    private JMenuItem menuClone = new JMenuItem();
    private JMenuItem menuRemove = new JMenuItem();
    private JMenuItem menuMoveUp = new JMenuItem();
    private JMenuItem menuMoveDown = new JMenuItem();
    private JMenu menuAdd = new JMenu("Add...");
    private JMenu menuInsert = new JMenu("Insert...");
    private JMenu menuAction = new JMenu("Extra actions...");
    @Autowired
    private FormMain formMain;
    @Getter
    private TreePath currentPath = null;
    @Getter
    private NamedNode markedNode;

    public PanelDesign() {
    }

    public static void buildMenu(JMenu menu, List children) {
        if (children != null) {
            for (Object me : children) {
                if (me instanceof JMenuItem)
                    menu.add((JMenuItem) me);
                if (me instanceof JSeparator)
                    menu.add((JSeparator) me);
                if (me instanceof JMenu)
                    menu.add((JMenu) me);

                if (me instanceof TitledSeparator)
                    menu.add((TitledSeparator) me);
            }
        }

        if (menu.getItemCount() == 0) {
            menu.add(menuNothing);
        }
    }

    @Override
    public void init() {
        super.init();
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        menuClone.setAction(actionList.getCloneAction());
        menuRemove.setAction(actionList.getRemoveAction());
        menuMoveDown.setAction(actionList.getMoveDownAction());
        menuMoveUp.setAction(actionList.getMoveUpAction());

        menu.add(menuAdd);
        menu.add(menuInsert);
        menu.addSeparator();
        menu.add(menuMoveUp);
        menu.add(menuMoveDown);
        menu.add(menuClone);
        menu.add(menuRemove);
        menu.addSeparator();
        menu.add(menuAction);


        menuNothing.setEnabled(false);
        Skin.applySkin(menu);

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                menuAdd.removeAll();
                menuInsert.removeAll();
                menuAction.removeAll();

                NodeHelper helper = Registry.getHelper(TreeUtils.getCurrentNode(getCurrentPath()));
                if (helper != null) {
                    List nodeMenu = helper.getChildMenu(tree, TreeUtils.getCurrentNode(getCurrentPath()), -1);
                    buildMenu(menuAdd, nodeMenu);

                    NodeHelper parentHelper = Registry.getHelper(TreeUtils.getCurrentParent(getCurrentPath()));
                    if (parentHelper != null) {
                        int index = parentHelper.getIndex(TreeUtils.getCurrentParent(getCurrentPath()), TreeUtils.getCurrentNode(getCurrentPath()));
                        List nodeParentMenu = parentHelper.getChildMenu(tree, TreeUtils.getCurrentParent(getCurrentPath()), index);
                        buildMenu(menuInsert, nodeParentMenu);
                    }
                    if (menuInsert.getItemCount() == 0) {
                        menuInsert.add(menuNothing);
                    }

                    actionList.getMoveUpAction().updateStatus(getCurrentPath(), helper);
                    actionList.getMoveDownAction().updateStatus(getCurrentPath(), helper);
                    actionList.getCloneAction().updateStatus(getCurrentPath(), helper);
                    actionList.getRemoveAction().updateStatus(getCurrentPath(), helper);


                    List nodeActionMenu = helper.getActionMenu(tree, TreeUtils.getCurrentNode(getCurrentPath()), -1);
                    buildMenu(menuAction, nodeActionMenu);

                    Skin.applySkin(menuAdd);
                    Skin.applySkin(menuInsert);
                    Skin.applySkin(menuAction);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });

        KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (menu != null && e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                    menu.show(e.getComponent(), 0, 0);
                }
            }

        };

        tree.setComponentPopupMenu(menu);
        tree.setEditable(false);
        tree.setDoubleBuffered(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new CBRenderer(this));
        tree.addTreeSelectionListener(e -> {
            Object obj = e.getSource();
            if (obj instanceof JTree && ((JTree) e.getSource()).getSelectionCount() > 0) {
                JTree tree = (JTree) obj;
                TreePath tp = tree.getSelectionPath();
                treeSelectionChanged(tp);
            }
        });

        tree.addKeyListener(keyListener);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent mouseEvent) {
                mousePressed(mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null && !tree.isPathSelected(path))
                    tree.setSelectionPath(path);
                showIfPopupTrigger(e);
            }

            private void showIfPopupTrigger(MouseEvent mouseEvent) {
                if (menu != null) {
                    if (menu.isPopupTrigger(mouseEvent)) {
                        menu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                    }
                }
            }
        });
    }

    @Override
    public void releaseSimulationMode() {
    }

    @Override
    public void enterSimulationMode() {
        tree.setEditable(false);
    }

    private void treeSelectionChanged(TreePath tn) {
        currentPath = tn;
        EventQueue.invokeLater(() -> showEditor(getCurrentPath()));
    }

    private void showEditor(TreePath treeNode) {
        panelEditor.edit(treeNode);
    }


    public boolean open(File file) {
        try {
            RouteLoader loader = new XmlRouteLoader();
            RoutesDefinition routesDefinition = loader.load(file);
            reload(routesDefinition, tree);

            File beanFile = new File(file.getPath() + ".bean");
            if (beanFile.exists()) {
                Beans beans = loader.loadBean(beanFile);
                beanManager.getModel().beginUpdate();
                try {
                    beanManager.getModel().clear();
                    for (Bean bean : beans.getBeans()) {
                        bean.getInfo().setId(bean.getId());
                        beanManager.getModel().addElement(bean.getInfo());
                    }
                } finally {
                    beanManager.getModel().endUpdate();
                    beanManager.getModel().update();
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.showMessageDialog(formMain, ex);
            return false;
        }
    }

    private void reload(RoutesDefinition routesDefinition, CBTree tree) {
        synchronized (tree.getTreeLock()) {
            CBTreeModel model = tree.getModel();
            root = routesDefinition;
            currentPath = null;
            EventQueue.invokeLater(() -> showEditor(getCurrentPath()));
            model.setRoot(root);
            TreePath path = new TreePath(root);
            model.nodeStructureChanged(path);
            tree.treeDidChange();
            tree.updateUI();
        }
    }

    public boolean save(File file) {
        try {
            RouteLoader loader = new XmlRouteLoader();
            loader.save(root, file);

            Beans beans = new Beans();
            List<Bean> beanList = new ArrayList<>();
            beans.setBeans(beanList);

            for (BeanInfo beanInfo : beanManager.getBeans().values()) {
                Bean bean = new Bean();
                bean.setId(beanInfo.getId());
                bean.setInfo(beanInfo);
                beanList.add(bean);
            }
            if (beans.getBeans().size() > 0) {
                File beanFile = new File(file.getPath() + ".bean");
                loader.saveBean(beans, beanFile);
            }
            return true;
        } catch (Exception ex) {
            MessageBox.showMessageDialog(formMain, ex);
            return false;
        }
    }

    public void reset() {
        reload(new RoutesDefinition(), tree);
        beanManager.getModel().clear();
    }

    public RoutesDefinition getRoutesDefinition() {
        return root;
    }

    public void mark(NamedNode node) {
        this.markedNode = node;
        tree.updateUI();
    }

    public boolean isMarked(NamedNode value) {
        return value == markedNode;
    }

    public static class CBTree extends JTree {
        @Getter
        private final PanelDesign panelDesign;

        public CBTree(RoutesDefinition routes, PanelDesign panelDesign) {
            super(new CBTreeModel(routes));
            this.panelDesign = panelDesign;
        }

        public FileEditor getFileEditor() {
            return getPanelDesign().fileEditor;
        }

        public CBTreeModel getModel() {
            return (CBTreeModel) super.getModel();
        }
    }

    public static class CBTreeModel implements TreeModel {
        protected EventListenerList listenerList = new EventListenerList();
        private RoutesDefinition routes;

        public CBTreeModel(RoutesDefinition routes) {
            this.routes = routes;
        }

        @Override
        public Object getRoot() {
            return routes;
        }

        public void setRoot(RoutesDefinition routes) {
            this.routes = routes;
        }

        @Override
        public Object getChild(Object parent, int index) {
            NodeHelper helper = Registry.getHelper((NamedNode) parent);
            return helper.getChildAt((NamedNode) parent, index);
        }

        @Override
        public int getChildCount(Object parent) {
            NodeHelper helper = Registry.getHelper((NamedNode) parent);
            if (helper != null)
                return helper.getChildCount((NamedNode) parent);
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            return getChildCount(node) == 0;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            nodeChanged(path);
        }

        public void nodeChanged(TreePath path) {
            if (listenerList != null && path != null) {
                NamedNode parent = TreeUtils.getCurrentParent(path);

                if (parent != null) {
                    NodeHelper parentNodeHelper = Registry.getHelper(parent);
                    int anIndex = parentNodeHelper.getIndex(parent, TreeUtils.getCurrentNode(path));
                    if (anIndex != -1) {
                        int[] cIndexs = new int[1];

                        cIndexs[0] = anIndex;
                        nodesChanged(path.getParentPath(), cIndexs);
                    }
                } else if (TreeUtils.getCurrentNode(path) == getRoot()) {
                    nodesChanged(path, null);
                }
            }
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            NodeHelper helper = Registry.getHelper((NamedNode) parent);
            return helper.getIndex((NamedNode) parent, (NamedNode) child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listenerList.add(TreeModelListener.class, l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listenerList.remove(TreeModelListener.class, l);
        }

        protected void fireTreeNodesChanged(Object source, Object[] path,
                                            int[] childIndices,
                                            Object[] children) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {
                    // Lazily create the event:
                    if (e == null)
                        e = new TreeModelEvent(source, path, childIndices, children);
                    ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
                }
            }
        }

        /**
         * Notifies all listeners that have registered interest for
         * notification on this event type.  The event instance
         * is lazily created using the parameters passed into
         * the fire method.
         *
         * @param source       the source of the {@code TreeModelEvent};
         *                     typically {@code this}
         * @param path         the path to the parent the nodes were added to
         * @param childIndices the indices of the new elements
         * @param children     the new elements
         */
        protected void fireTreeNodesInserted(Object source, Object[] path,
                                             int[] childIndices,
                                             Object[] children) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {
                    // Lazily create the event:
                    if (e == null)
                        e = new TreeModelEvent(source, path,
                                childIndices, children);
                    ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
                }
            }
        }

        /**
         * Notifies all listeners that have registered interest for
         * notification on this event type.  The event instance
         * is lazily created using the parameters passed into
         * the fire method.
         *
         * @param source       the source of the {@code TreeModelEvent};
         *                     typically {@code this}
         * @param path         the path to the parent the nodes were removed from
         * @param childIndices the indices of the removed elements
         * @param children     the removed elements
         */
        protected void fireTreeNodesRemoved(Object source, Object[] path,
                                            int[] childIndices,
                                            Object[] children) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {
                    // Lazily create the event:
                    if (e == null)
                        e = new TreeModelEvent(source, path,
                                childIndices, children);
                    ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
                }
            }
        }

        /**
         * Notifies all listeners that have registered interest for
         * notification on this event type.  The event instance
         * is lazily created using the parameters passed into
         * the fire method.
         *
         * @param source       the source of the {@code TreeModelEvent};
         *                     typically {@code this}
         * @param path         the path to the parent of the structure that has changed;
         *                     use {@code null} to identify the root has changed
         * @param childIndices the indices of the affected elements
         * @param children     the affected elements
         */
        protected void fireTreeStructureChanged(Object source, Object[] path,
                                                int[] childIndices,
                                                Object[] children) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {
                    // Lazily create the event:
                    if (e == null)
                        e = new TreeModelEvent(source, path,
                                childIndices, children);
                    ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
                }
            }
        }

        /**
         * Notifies all listeners that have registered interest for
         * notification on this event type.  The event instance
         * is lazily created using the parameters passed into
         * the fire method.
         *
         * @param source the source of the {@code TreeModelEvent};
         *               typically {@code this}
         * @param path   the path to the parent of the structure that has changed;
         *               use {@code null} to identify the root has changed
         */
        private void fireTreeStructureChanged(Object source, TreePath path) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = null;
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {
                    // Lazily create the event:
                    if (e == null)
                        e = new TreeModelEvent(source, path);
                    ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
                }
            }
        }

        /**
         * Invoke this method if you've modified the {@code TreeNode}s upon which
         * this model depends. The model will notify all of its listeners that the
         * model has changed below the given node.
         *
         * @param node the node below which the model has changed
         */
        public void reload(TreePath node) {
            if (node != null) {
                fireTreeStructureChanged(this, TreeUtils.getPathToRoot(routes, node), null, null);
            }
        }

        /**
         * Invoke this method after you've inserted some TreeNodes into
         * node.  childIndices should be the index of the new elements and
         * must be sorted in ascending order.
         */
        public void nodesWereInserted(TreePath node, int[] childIndices) {
            if (listenerList != null && node != null && childIndices != null && childIndices.length > 0) {
                int cCount = childIndices.length;
                Object[] newChildren = new Object[cCount];

                NodeHelper nodeHelper = Registry.getHelper(TreeUtils.getCurrentNode(node));
                for (int counter = 0; counter < cCount; counter++) {
                    newChildren[counter] = nodeHelper.getChildAt(TreeUtils.getCurrentNode(node), childIndices[counter]);
                }
                fireTreeNodesInserted(this, TreeUtils.getPathToRoot(routes, node), childIndices, newChildren);
            }
        }

        /**
         * Invoke this method after you've removed some TreeNodes from
         * node.  childIndices should be the index of the removed elements and
         * must be sorted in ascending order. And removedChildren should be
         * the array of the children objects that were removed.
         */
        public void nodesWereRemoved(TreePath node, int[] childIndices, Object[] removedChildren) {
            if (node != null && childIndices != null) {
                fireTreeNodesRemoved(this, TreeUtils.getPathToRoot(routes, node), childIndices, removedChildren);
            }
        }

        /**
         * Invoke this method after you've changed how the children identified by
         * childIndicies are to be represented in the tree.
         */
        public void nodesChanged(TreePath path, int[] childIndices) {
            if (path != null) {
                if (childIndices != null) {
                    int cCount = childIndices.length;

                    if (cCount > 0) {
                        Object[] cChildren = new Object[cCount];

                        NodeHelper nodeHelper = Registry.getHelper(TreeUtils.getCurrentNode(path));
                        for (int counter = 0; counter < cCount; counter++) {
                            cChildren[counter] = nodeHelper.getChildAt(TreeUtils.getCurrentNode(path), childIndices[counter]);
                        }
                        fireTreeNodesChanged(this, TreeUtils.getPathToRoot(routes, path), childIndices, cChildren);
                    }
                } else if (TreeUtils.getCurrentNode(path) == getRoot()) {
                    fireTreeNodesChanged(this, TreeUtils.getPathToRoot(routes, path), null, null);
                }
            }
        }

        /**
         * Invoke this method if you've totally changed the children of
         * node and its children's children...  This will post a
         * treeStructureChanged event.
         */
        public void nodeStructureChanged(TreePath node) {
            if (node != null) {
                fireTreeStructureChanged(this, TreeUtils.getPathToRoot(routes, node), null, null);
            }
        }
    }

}