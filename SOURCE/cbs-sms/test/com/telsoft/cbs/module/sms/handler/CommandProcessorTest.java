//package com.telsoft.cbs.module.sms.handler;
//
//import com.telsoft.cbs.module.sms.SmsMessage;
//
//class CommandProcessorTest {
//    static CommandProcessor commandProcessor = null;
//
//    @org.junit.jupiter.api.BeforeAll
//    static void setUp() throws Exception {
//        commandProcessor = new CommandProcessor();
//        String s = "Package==com.telsoft.cbs.module.sms.handler.cbs\n" +
//                "Handler==ANALYSIS\n" +
//                "\tOtherwise==OTHER\n" +
//                "\tHandler==SILENT_SMS\n" +
//                "\t\tCondition\n" +
//                "\t\t\tCode==SILENTSMS\n" +
//                "\tHandler==HM\n" +
//                "\t\tCondition\n" +
//                "\t\t\tCode==HM\n" +
//                "\tHandler==com.telsoft.cbs.module.sms.handler.SessionHandler\n" +
//                "\t\tCondition\n" +
//                "\t\t\tCode==ANY\n";
//        commandProcessor.configure(s);
//        commandProcessor.start();
//    }
//
//    @org.junit.jupiter.api.AfterAll
//    static void tearDown() {
//        commandProcessor.stop();
//    }
//
//    @org.junit.jupiter.api.Test
//    void testSilentSms() {
//        SmsMessage message = new SmsMessage();
//        message.setOriginator("84936190293");
//        message.setReceiver("9238");
//        message.setContent("SILENTSMS:1234567956745");
//        CommandContext commandContext = new CommandContext(message);
//        commandProcessor.process(commandContext);
//    }
//
//    @org.junit.jupiter.api.Test
//    void testOther() {
//        SmsMessage message = new SmsMessage();
//        message.setOriginator("84936190293");
//        message.setReceiver("9238");
//        message.setContent("Hello xin chao");
//        CommandContext commandContext = new CommandContext(message);
//        commandProcessor.process(commandContext);
//    }
//
//    @org.junit.jupiter.api.Test
//    void testHM() {
//        SmsMessage message = new SmsMessage();
//        message.setOriginator("84936190293");
//        message.setReceiver("9238");
//        message.setContent("HM");
//        CommandContext commandContext = new CommandContext(message);
//        commandProcessor.process(commandContext);
//    }
//
//    @org.junit.jupiter.api.Test
//    void testPerformance() {
//        long t = System.currentTimeMillis();
//        long n = 100000;
//        for (int i = 0; i < n; i++) {
//            SmsMessage message = new SmsMessage();
//            message.setOriginator("84936190293");
//            message.setReceiver("9238");
//            message.setContent("HM");
//            CommandContext commandContext = new CommandContext(message);
//            commandProcessor.process(commandContext);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Total(ms):=" + (t1 - t));
//        System.out.println("Avg(ms):=" + (t1 - t) * 1.0 / n);
//    }
//}