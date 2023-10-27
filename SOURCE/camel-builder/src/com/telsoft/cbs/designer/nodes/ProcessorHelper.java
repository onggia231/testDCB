package com.telsoft.cbs.designer.nodes;

import org.apache.camel.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ProcessorHelper<T extends ProcessorDefinition> extends BasedNodeHelper<T> {
    @Override
    public List<Class> getChildTypes(T treeNode) {
        return new ArrayList<>(Arrays.asList(
                ProcessDefinition.class,
                LogDefinition.class,
                ChoiceDefinition.class,
                TryDefinition.class,
                LoopDefinition.class,
                ToDefinition.class,
                ToDynamicDefinition.class,
                ThrowExceptionDefinition.class,
                SetBodyDefinition.class,
                SetHeaderDefinition.class,
                RemoveHeaderDefinition.class,
                DelayDefinition.class,
                StopDefinition.class,
                SetPropertyDefinition.class,
                RemovePropertyDefinition.class,
                TransactedDefinition.class,
                RollbackDefinition.class,
                CircuitBreakerDefinition.class,
                ScriptDefinition.class,
                MulticastDefinition.class,
                PipelineDefinition.class));
    }
}
