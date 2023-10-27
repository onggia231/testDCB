package com.telsoft.cbs.module.sms.handler;

public class SessionHandler extends ChainCommandHandler implements SessionAware {
    @Override
    public void stop() {
        System.out.println("Stop called");
    }

    @Override
    public void start() throws Exception {
        System.out.println("Start called");
    }

    @Override
    public void doProcess(CommandContext context) {
        System.out.println("process called");
    }
}
