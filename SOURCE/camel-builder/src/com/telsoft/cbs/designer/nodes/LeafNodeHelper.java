package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;

import javax.swing.*;
import java.util.List;

public abstract class LeafNodeHelper<T extends NamedNode> extends BasedNodeHelper<T> {
    @Override
    public List<Class> getChildTypes(T treeNode) {
        return null;
    }

    @Override
    public void removeChild(JTree tree, T parent, NamedNode child) {
    }

    @Override
    public int getIndex(T parent, NamedNode child) {
        return -1;
    }

    @Override
    public void insertChild(JTree tree, T parent, NamedNode child, int index) {

    }

    @Override
    public boolean canInsertAt(T parent, NamedNode child, int index) {
        return false;
    }

    @Override
    public NamedNode getChildAt(T parent, int childIndex) {
        return null;
    }

    @Override
    public int getChildCount(T parent) {
        return 0;
    }
}
