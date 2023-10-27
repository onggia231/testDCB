package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface NodeHelper<T extends NamedNode> {
    List getChildMenu(JTree tree, T node, int index);

    List getActionMenu(JTree tree, T node, int index);

    T newInstance();

    Class<T> getClazz();

    boolean isCloneable();

    boolean isRemovable();

    Color getColor(T node, boolean foreground, boolean selected, boolean expanded, boolean leaf, boolean hasFocus);

    String onGetText(T node);

    Icon getIcon(T node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus);

    void removeChild(JTree tree, T parent, NamedNode child);

    void insertChild(JTree tree, T parent, NamedNode child, int index);

    boolean canInsertAt(T parent, NamedNode child, int index);

    int getIndex(T parent, NamedNode child);

    NamedNode getChildAt(T parent, int childIndex);

    int getChildCount(T parent);

    String getGroup();
}
