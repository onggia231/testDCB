package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.OtherwiseDefinition;
import org.apache.camel.model.WhenDefinition;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

@Group("Choice-When-Otherwise")
public class ChoiceHelper extends BasedNodeHelper {
    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.CALL_SPLIT, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Choice</b>";
    }

    @Override
    public int getIndex(NamedNode parent, NamedNode child) {
        ChoiceDefinition choiceDefinition = (ChoiceDefinition) parent;

        if (child instanceof OtherwiseDefinition)
            return choiceDefinition.getWhenClauses().size();

        return choiceDefinition.getWhenClauses().indexOf(child);
    }

    @Override
    public void insertChild(JTree tree, NamedNode parent, NamedNode child, int index) {
        ChoiceDefinition choiceDefinition = (ChoiceDefinition) parent;
        if (child instanceof WhenDefinition) {
            if (index == -1)
                choiceDefinition.getWhenClauses().add((WhenDefinition) child);
            else
                choiceDefinition.getWhenClauses().add(index, (WhenDefinition) child);
            updateTree(tree);
        } else if (child instanceof OtherwiseDefinition) {
            choiceDefinition.setOtherwise((OtherwiseDefinition) child);
            updateTree(tree);
        }

    }

    @Override
    public boolean canInsertAt(NamedNode parent, NamedNode child, int index) {
        return index == -1 || (index >= 0 && index < getChildCount(parent));
    }

    @Override
    public NamedNode getChildAt(NamedNode parent, int childIndex) {
        ChoiceDefinition choiceDefinition = (ChoiceDefinition) parent;
        if (childIndex < choiceDefinition.getWhenClauses().size())
            return choiceDefinition.getWhenClauses().get(childIndex);
        if (childIndex == choiceDefinition.getWhenClauses().size())
            return choiceDefinition.getOtherwise();
        return null;
    }

    @Override
    public int getChildCount(NamedNode parent) {
        ChoiceDefinition choiceDefinition = (ChoiceDefinition) parent;
        return choiceDefinition.getWhenClauses().size() + (choiceDefinition.getOtherwise() == null ? 0 : 1);
    }

    @Override
    public List<Class> getChildTypes(NamedNode treeNode) {
        ChoiceDefinition choiceDefinition = (ChoiceDefinition) treeNode;
        if (choiceDefinition.getOtherwise() != null) {
            return Arrays.asList(WhenDefinition.class);
        } else {
            return Arrays.asList(WhenDefinition.class,
                    OtherwiseDefinition.class);
        }
    }

    @Override
    public NamedNode newInstance() {
        return new ChoiceDefinition();
    }

    @Override
    public Class getClazz() {
        return ChoiceDefinition.class;
    }

    @Override
    public void removeChild(JTree tree, NamedNode parent, NamedNode child) {
        ChoiceDefinition currentNode = (ChoiceDefinition) parent;

        if (child instanceof OtherwiseDefinition) {
            currentNode.setOtherwise(null);
            updateTree(tree);
        } else if (child instanceof WhenDefinition) {
            int i = currentNode.getWhenClauses().indexOf(child);
            if (i >= 0) {
                currentNode.getWhenClauses().remove(i);
                updateTree(tree);
            }
        }
    }
}
