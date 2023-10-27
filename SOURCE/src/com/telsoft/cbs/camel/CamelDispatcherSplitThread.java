package com.telsoft.cbs.camel;

import com.telsoft.cbs.domain.*;
import com.telsoft.util.AppException;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.processor.DefaultExchangeFormatter;
import org.apache.camel.util.StopWatch;
import org.apache.camel.util.StringHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.excp.CommandFailureException;
import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.queue.ChannelNotExisted;
import telsoft.gateway.core.translator.Translator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class CamelDispatcherSplitThread extends Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(CamelDispatcherSplitThread.class);
    private DefaultCamelContext context;
    private ProducerTemplate template;

    public CamelDispatcherSplitThread(DispatcherManager mgr) {
        super(mgr);
    }

    public static StringBuilder dumpMessageHistoryStacktrace(Exchange exchange, ExchangeFormatter exchangeFormatter, boolean logStackTrace) {
        try {
            return doDumpMessageHistoryStacktrace(exchange, exchangeFormatter, logStackTrace);
        } catch (Throwable var4) {
            return new StringBuilder();
        }
    }

    public static StringBuilder doDumpMessageHistoryStacktrace(Exchange exchange, ExchangeFormatter exchangeFormatter, boolean logStackTrace) {
        List<MessageHistory> list = (List) exchange.getProperty("CamelMessageHistory", List.class);
        if (list != null && !list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("Message History\n");
            sb.append("---------------\n");
            sb.append(String.format("%-18s|%-18s|%-78s|%-10s", "RouteId", "ProcessorId", "Processor", "Elapsed (ms)"));
            sb.append("\n");
            String routeId = exchange.getFromRouteId();
            String label = "";
            if (exchange.getFromEndpoint() != null) {
                label = URISupport.sanitizeUri(exchange.getFromEndpoint().getEndpointUri());
            }

            long elapsed = 0L;
            Date created = exchange.getCreated();
            if (created != null) {
                elapsed = (new StopWatch(created)).taken();
            }
            sb.append(String.format("%-18.18s|%-18.18s|%-78.78s|%10.10s", routeId, routeId, label, elapsed));
            sb.append("\n");
            Iterator var13 = list.iterator();

            while (var13.hasNext()) {
                MessageHistory history = (MessageHistory) var13.next();
                int level = getLevel(history);
                routeId = history.getRouteId() != null ? history.getRouteId() : "";
                String id = history.getNode().getId();
                label = URISupport.sanitizeUri(StringHelper.limitLength(history.getNode().getLabel(), 100));
                elapsed = history.getElapsed();
                sb.append(String.format("%-18.18s|%-18.18s|%-78.78s|%10.10s", routeId, fill(' ', level) + id, label, elapsed));
                sb.append("\n");
            }

/*            if (exchangeFormatter != null) {
                sb.append("\nExchange\n");
                sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");
                sb.append(exchangeFormatter.format(exchange));
                sb.append("\n");
            }*/

//            if (logStackTrace) {
//                sb.append("\nStacktrace\n");
//                sb.append("---------------------------------------------------------------------------------------------------------------------------------");
//            }
            sb.append("---------------\n");
            return sb;
        } else {
            return null;
        }
    }

    public static StringBuilder dumpMessageExchange(Exchange exchange, ExchangeFormatter exchangeFormatter) {
        StringBuilder sb = new StringBuilder();
        if (exchangeFormatter != null) {
            sb.append("\nExchange\n");
            sb.append("---------------\n");
            sb.append(exchangeFormatter.format(exchange));
            sb.append("\n");
        }
        return sb;
    }

    private static int getLevel(MessageHistory history) {
        int level = 0;
        NamedNode nn = history.getNode();
        while (nn.getParent() != null) {
            nn = nn.getParent();
            level++;
        }
        return level;
    }

    static String fill(char c, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    @Override
    public void open() throws Exception {
        template = context.createProducerTemplate();
    }

    @Override
    public void close() {
        try {
            template.stop();
            template = null;
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getProtocolName() {
        return "CAMEL";
    }

    @Override
    public GatewayMessage processMessage(MessageContext ctx, GatewayMessage request) throws Exception {
        CBRequest cbRequest = (CBRequest) ctx.getRequest();
        if (getDispatcherManager().isDebug()) {
            logMonitor("REQUEST:" + cbRequest);
        }

        Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
        exchange.setProperty(CbsContansts.COMMAND, cbRequest.getCommand());
        exchange.setProperty(CbsContansts.CB_MSG_CONTEXT, ctx);
        exchange.setProperty(CbsContansts.CB_REQUEST, cbRequest);
        exchange.getIn().setBody(cbRequest);

        ctx.setProperty(CbsContansts.CB_MSG_EXCHANGE,exchange);

        Map mapFullRequest = (ConcurrentHashMap) ctx.getProperty(CbsContansts.MAP_FULL_REQUEST);
        exchange.setProperty(CbsContansts.MAP_FULL_REQUEST,mapFullRequest);
        //Add GatewayManager
        exchange.setProperty(CbsContansts.GATEWAY_MANAGER,getGatewayManager());

        String command = cbRequest.getCommandName();
        String storeCode = cbRequest.get(CbsContansts.STORE_CODE);
        template.send("direct:" + command + "_" + storeCode + "?block=false", exchange);
        if (exchange.getFromRouteId() == null) {
            CBResponse response = cbRequest.createResponse();
            response.setCode(CBCode.UNKNOWN_COMMAND);
            return response;
        }

        StringBuilder camelHistory = dumpMessageHistoryStacktrace(exchange, null, false);
        ctx.setProperty(CbsContansts.CB_CAMEL_HISTORY, camelHistory);
        if (getDispatcherManager().isDebug()) {
            DefaultExchangeFormatter formatter = new DefaultExchangeFormatter() {
                @Override
                protected Map<String, Object> filterHeaderAndProperties(Map<String, Object> map) {
                    Map map1 = new HashMap();
                    map1.putAll(map);
                    map1.remove("CamelMessageHistory");
                    return map1;
                }
            };
            formatter.setShowHeaders(true);
            formatter.setShowExchangeId(true);
            formatter.setShowProperties(true);
            formatter.setStyle(DefaultExchangeFormatter.OutputStyle.Fixed);
            formatter.setMultiline(true);
            StringBuilder dumpWithExchange = new StringBuilder(camelHistory).append(dumpMessageExchange(exchange, formatter));
            logMonitor(dumpWithExchange.toString());
        }

        if (exchange.getException() != null) {
            throw new CommandFailureException(exchange.getException().getMessage());
        }

        CBMessage message = (CBMessage) ctx.getClientResponse();
        if (message == null) {
            throw new CommandFailureException("Internal Error");
        }

        if (getDispatcherManager().isDebug()) {
            logMonitor("RESPONSE:" + message);
        }
        return message;
    }

    @Override
    public boolean isOpen() {
        return super.isOpen() && template != null;
    }

    // Fix timeout status
    /**
     * @param msgContext MessageContext
     */
    protected void dispatchMessage(final MessageContext msgContext) {
        // Set dispatcher id for this record
        msgContext.setDispatcherID(getDispatcherManager().getThreadID());
        msgContext.setServerProtocol(getProtocolName());

        try {
            // Process message
            GatewayMessage msgResponse = null;
            boolean bLogCommand = getDispatcherManager().isLogCommandProcess();
            if (bLogCommand) {
                logMonitor("Processing message : \r\n" + msgContext.getRequest().getContent());
            }
            try {
                // Get channel
                MessageChannel channel = (MessageChannel) msgContext.getProperty(MessageContext.MESSAGE_CHANNEL);

                // Make sure channel is open
                if (channel == null) {
                    throw new CommandFailureException("Channel is not present in message");
                }
                if (!channel.isOpen()) {
                    throw new CommandFailureException("Channel is closed");
                }

                // Get translator
                /**
                 * @todo non-threadsafe
                 */
                Translator translator = getTranslator(channel);
                if (translator == null) {
                    throw new AppException("Translator for channel " + channel.toString() + " is not available");
                }

                msgResponse = translator.processMessage(msgContext, this);
                if (bLogCommand) {
                    logMonitor("Processed : \r\n" + msgResponse.getContent());
                }
            } catch (CommandFailureException ex) {
                String s = GWUtil.decodeException(ex);
                log.debug("Command error:", ex);
                if (bLogCommand) {
                    logMonitor("Failure: \r\n" + s);
                }
                msgContext.setException(ex);
                msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
            } catch (AppException ex) {
                log.debug("Command error:", ex);
                String s = GWUtil.decodeException(ex);
                if (bLogCommand) {
                    logMonitor("Failure: \r\n" + s);
                }
                msgContext.setException(ex);
                msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
            }
            msgContext.setClientResponse(msgResponse);// 20160902-TODO: client response time is incorrect here
            attachResponseMessage(msgContext);
        } catch (Throwable e) {
            log.error("dispatch message", e);
            msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
            // 20160902-don't attach again to queue while ChannelNotExisted
            // TODO while ChannelNotExisted Message have to added to LogQueue here
            if (e instanceof ChannelNotExisted) {
                msgContext.setStatus(REQUEST_STATUS.TIMEOUT.getCode());
//                getGatewayManager().getLogger().attachLogRecord(msgContext);
            } else {
                attachResponseMessage(msgContext, new Exception(e));
            }
            logMonitor("Error occurred: " + GWUtil.decodeException(e));
            if (!(e instanceof ChannelNotExisted)) { // Don't restart dispatcher
                close();
            }
        }
    }
}
