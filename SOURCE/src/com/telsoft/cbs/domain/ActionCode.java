package com.telsoft.cbs.domain;

import telsoft.gateway.core.log.MessageContext;

public enum ActionCode {
    REGISTER {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    UNREGISTER {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    CHARGE {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    REFUND {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    BLOCK {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    UNBLOCK {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    SEND_SMS {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    RECEIVE_SMS {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    },
    OTHER {
        @Override
        public String getMessage(MessageContext context) {
            return null;
        }
    };

    public abstract String getMessage(MessageContext context);
}
