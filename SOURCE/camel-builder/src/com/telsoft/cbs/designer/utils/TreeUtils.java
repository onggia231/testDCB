package com.telsoft.cbs.designer.utils;

import org.apache.camel.NamedNode;
import org.apache.camel.model.*;

import javax.swing.tree.TreePath;
import java.util.List;

public class TreeUtils {
    public static NamedNode getCurrentNode(TreePath currentPath) {
        return currentPath != null ? (NamedNode) currentPath.getLastPathComponent() : null;
    }

    public static NamedNode getCurrentParent(TreePath currentPath) {
        return currentPath != null ? (currentPath.getParentPath() != null ? (NamedNode) currentPath.getParentPath().getLastPathComponent() : null) : null;
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the TreeNode to get the path for
     */
    public static NamedNode[] getPathToRoot(Object root, TreePath aNode) {
        return getPathToRoot(root, aNode, 0);
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the TreeNode to get the path for
     * @param depth an int giving the number of steps already taken towards
     *              the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     * specified node
     */
    protected static NamedNode[] getPathToRoot(Object root, TreePath aNode, int depth) {
        NamedNode[] retNodes;
        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if (aNode == null) {
            if (depth == 0)
                return null;
            else
                retNodes = new NamedNode[depth];
        } else {
            depth++;
            if (aNode.getLastPathComponent() == root)
                retNodes = new NamedNode[depth];
            else
                retNodes = getPathToRoot(root, aNode.getParentPath(), depth);
            retNodes[retNodes.length - depth] = (NamedNode) aNode.getLastPathComponent();
        }
        return retNodes;
    }

    public static void process(OptionalIdentifiedDefinition node, NamedNodeProcessor processor) {
        if (node == null || processor == null)
            return;

        processor.process(node);

        if (node instanceof RoutesDefinition) {
            List<RouteDefinition> routes = ((RoutesDefinition) node).getRoutes();
            for (RouteDefinition route : routes) {
                process(route, processor);
            }
        } else if (node instanceof RouteDefinition) {
            FromDefinition from = ((RouteDefinition) node).getInput();
            process(from, processor);

            List<ProcessorDefinition<?>> processorDefinitions = ((RouteDefinition) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        } else if (node instanceof ChoiceDefinition) {
            List<WhenDefinition> whenDefinitions = ((ChoiceDefinition) node).getWhenClauses();
            for (WhenDefinition whenDefinition : whenDefinitions) {
                process(whenDefinition, processor);
            }

            OtherwiseDefinition otherwiseDefinition = ((ChoiceDefinition) node).getOtherwise();
            process(otherwiseDefinition, processor);

        } else if (node instanceof CircuitBreakerDefinition) {
            List<ProcessorDefinition<?>> processorDefinitions = ((CircuitBreakerDefinition) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        } else if (node instanceof ExpressionNode) {
            List<ProcessorDefinition<?>> processorDefinitions = ((ExpressionNode) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        } else if (node instanceof OutputDefinition) {
            List<ProcessorDefinition<?>> processorDefinitions = ((OutputDefinition<?>) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        } else if (node instanceof CatchDefinition) {
            List<ProcessorDefinition<?>> processorDefinitions = ((CatchDefinition) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        } else if (node instanceof OnExceptionDefinition) {
            List<ProcessorDefinition<?>> processorDefinitions = ((OnExceptionDefinition) node).getOutputs();
            for (ProcessorDefinition processorDefinition : processorDefinitions) {
                process(processorDefinition, processor);
            }
        }
    }
}

