package com.telsoft.cbs.module.sms.handler;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class ChainCommandHandler implements CommandHandler {
    private final List<CommandHandlerDefinition> definitions = new ArrayList<>();
    @Getter
    @Setter
    private CommandHandler otherwise = null;

    public void addDefinition(CommandHandlerDefinition definition) {
        synchronized (definitions) {
            definitions.add(definition);
        }
    }

    public void removeDefinition(CommandHandlerDefinition definition) {
        synchronized (definitions) {
            definitions.remove(definition);
        }
    }

    public void preProcess(CommandContext context) {

    }

    public abstract void doProcess(CommandContext context);

    public void postProcess(CommandContext context) {
        invokeChain(context);
    }

    public void invokeChain(CommandContext context) {
        synchronized (definitions) {
            for (CommandHandlerDefinition definition : definitions) {
                if (definition.getKeyValues().size() == 0) {
                    definition.getCommandHandler().process(context);
                    return;
                } else {
                    for (CommandHandlerDefinition.KeyValue kv : definition.getKeyValues()) {
                        String ctxValue = context.getAttributes().get(kv.getK());
                        if (ctxValue.equals(kv.getV())) {
                            definition.getCommandHandler().process(context);
                            return;
                        }
                    }
                }
            }
            if (getOtherwise() != null) {
                getOtherwise().process(context);
            }
        }
    }

    @Override
    public final void process(CommandContext context) {
        preProcess(context);
        doProcess(context);
        postProcess(context);
    }


}
