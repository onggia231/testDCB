package com.telsoft.cbs.designer.nodes;

import com.telsoft.cbs.designer.App;
import com.telsoft.cbs.designer.action.ActionList;
import com.telsoft.cbs.designer.panel.PanelDesign;
import com.telsoft.cbs.designer.panel.Registry;
import com.telsoft.swing.MessageBox;
import com.telsoft.util.StringUtil;
import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.NamedNode;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public abstract class BasedNodeHelper<T extends NamedNode> implements NodeHelper<T> {

    private Icon icon = null;
    private Icon iconSelected = null;

    public BasedNodeHelper() {
    }

    private static String formatName(String simpleName) {
        if (simpleName == null)
            return null;

        if (simpleName.endsWith("Definition"))
            return simpleName.substring(0, simpleName.length() - "Definition".length());

        return simpleName;
    }

    public Icon getIcon(T node, IconCode code, boolean selected) {
        if (selected) {
            if (iconSelected == null) {
                Color color = getColor(node, true, true, false, false, false);
                iconSelected = IconFontSwing.buildIcon(code, 15, color);
            }
            return iconSelected;
        } else {
            if (icon == null) {
                Color color = getColor(node, true, false, false, false, false);
                icon = IconFontSwing.buildIcon(code, 15, color);
            }
            return icon;
        }
    }

    @Override
    public String onGetText(T node) {
        return node.toString();
    }

    @Override
    public List getChildMenu(JTree tree, T node, int index) {
        List<Class> childNodeTypes = this.getChildTypes(node);

        List<MenuInfo> miList = new ArrayList<>();
        if (childNodeTypes != null) {
            for (Class clazz : childNodeTypes) {
                MenuInfo mi = new MenuInfo();
                mi.group = Registry.getGroup(clazz);
                mi.menuItem = new AddMenuItem(clazz, this);
                mi.menuItem.setText(formatName(clazz.getSimpleName()));
                if (mi.menuItem instanceof AddMenuItem)
                    ((AddMenuItem) mi.menuItem).setIndex(index);
                miList.add(mi);
            }
        }

        miList.sort((o1, o2) -> StringUtil.nvl(o1.group, "").compareTo(StringUtil.nvl(o2.group, "")));

        List menuList = new ArrayList();
//        String curGroup = null;
        for (MenuInfo mi : miList) {
//            String group = StringUtil.nvl(mi.group, "");
//            if (!group.equals(curGroup)) {
//                if (curGroup != null)
//                    menuList.add(new JSeparator());
//                curGroup = group;
//            }
            menuList.add(mi.menuItem);
        }

        for (Object o : menuList) {
            if (o instanceof NamedNodeMenuItem) {
                ((NamedNodeMenuItem) o).setCurrentTreeNode(node);
                ((NamedNodeMenuItem) o).setTree(tree);
            }
        }
        return menuList;
    }

    @Override
    public List getActionMenu(JTree tree, T node, int index) {
        List<JMenuItem> miList = new ArrayList<>();
        NamedNodeMenuItem menuItem = new NamedNodeMenuItem();
        menuItem.setTree(tree);
        menuItem.setCurrentTreeNode(node);
        ApplicationContext context = App.getContext();
        ActionList actionList = context.getBean(ActionList.class);
        menuItem.setAction(actionList.getRoutesResetIdsAction());
        miList.add(menuItem);
        return miList;
    }

    public abstract List<Class> getChildTypes(T treeNode);

    @Override
    public boolean isCloneable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public Color getColor(T node, boolean foreground, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        if (!foreground) {
            return (selected ? SystemColor.textHighlight : SystemColor.white);
        } else {
            return (selected ? SystemColor.textHighlightText : SystemColor.controlText);
        }
    }

    @Override
    public Icon getIcon(T node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return null;
    }

    public void updateTree(JTree tree) {
        tree.treeDidChange();
        tree.updateUI();
        if (tree instanceof PanelDesign.CBTree) {
            ((PanelDesign.CBTree) tree).getFileEditor().setModified();
        }
    }

    @Override
    public String getGroup() {
        Group group = this.getClass().getAnnotation(Group.class);
        if (group != null)
            return group.value();
        return null;
    }

    static class AddMenuItem extends NamedNodeMenuItem implements ActionListener {
        @Getter
        private final Class childType;
        private final NodeHelper helper;
        @Getter
        @Setter
        private int index;


        public AddMenuItem(Class childType, NodeHelper helper) {
            this.childType = childType;
            this.helper = helper;
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                NodeHelper childHelper = Registry.getHelper(childType);
                helper.insertChild(getTree(), getCurrentTreeNode(), childHelper.newInstance(), getIndex());
            } catch (Exception e1) {
                MessageBox.showMessageDialog(e1);
            }
        }
    }

    class MenuInfo {
        String group;
        NamedNodeMenuItem menuItem;
    }
}
