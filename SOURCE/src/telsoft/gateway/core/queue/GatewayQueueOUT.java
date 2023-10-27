package telsoft.gateway.core.queue;

import com.telsoft.queue.Attributable;
import telsoft.gateway.core.GWEvent;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.excp.ExceptionHelper;
import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.log.MessageContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class GatewayQueueOUT extends AbstractQueue implements QueueOUT {
    private final Map<String, MessageQueue> mapQueue = new ConcurrentHashMap<String, MessageQueue>();

    public GatewayQueueOUT(GatewayManager gateway) {
        super(gateway);
    }

    /**
     * @param msg     Attributable
     * @param bCreate boolean
     * @return VectorBoundedIndexedQueue
     * @throws Exception
     */
    private MessageQueue getQueue(Attributable msg, boolean bCreate) throws Exception {
        String strSessionID = msg.getAttribute(MessageChannel.INDEX_CHANNEL).toString();
        MessageQueue queue = mapQueue.get(strSessionID);
        if (queue == null && bCreate) {
            String sub = (String) msg.getAttribute("$SUB");
            if (sub != null) {
                queue = new MessageQueue(strSessionID);
                queue.setMaxQueueSize(getMaxQueueSize());
                mapQueue.put(strSessionID, queue);
            } else {
                MessageChannel channel = getManager().getMessageChannel(strSessionID);
                if (channel != null) {
                    queue = new MessageQueue(strSessionID);
                    queue.setMaxQueueSize(getMaxQueueSize());
                    mapQueue.put(strSessionID, queue);
                } else {
                    throw new ChannelNotExisted((MessageContext) msg,
                            "Channel " + strSessionID + " isn't existed or destroyed");
                }
            }
        }
        return queue;
    }

    /**
     * @param msg  Attributable
     * @param excp Exception
     * @throws Exception
     */
    public void attach(Attributable msg, Exception excp) {
        if (msg instanceof MessageContext) {
            ((MessageContext) msg).setException(excp);
        }
        try {
            attach(msg);
        } catch (Exception ex) {
            ExceptionHelper.printStackTrace(this, ex);
        }
    }

    /**
     * @param msg Attributable
     * @throws Exception
     */
    public void attach(Attributable msg) throws Exception {
        if (msg != null && msg instanceof MessageContext) {
            if (((MessageContext) msg).debug) {
                ((MessageContext) msg)._add_trace("Attach to queue OUT");
            }
        }
        getManager().getEvent().fireQueueOUTEvent(GWEvent.QUEUE_ATTACH, this);
        MessageQueue queue = getQueue(msg, true);
        if (queue != null) {
            queue.enqueue(msg);
        }
    }

    public String toString() {
//        StringBuilder buf = new StringBuilder();
//        MessageQueue[] ls = new MessageQueue[mapQueue.size()];
//        mapQueue.values().toArray(ls);
//        for (int i = 0; i < ls.length; i++) {
//            MessageQueue queue = ls[i];
//            if (queue.getQueueSize() > 0) {
//                buf.append(queue.toString());
//            }
//        }
//        buf.append("\r\nTotal:").append(ls.length);
//        return buf.toString();
        return "QueueOUT:" + getQueueSize();
    }

    /**
     * @param strSessionID String
     * @return MessageContext
     */
    public MessageContext detach(String strSessionID) {
        MessageQueue queue = (MessageQueue) mapQueue.get(strSessionID);
        if (queue != null) {
            Object attr = queue.dequeue();
            if (attr != null) {
                getManager().getEvent().fireQueueOUTEvent(GWEvent.QUEUE_DETACH, this);
            }
            if (attr != null && ((MessageContext) attr).debug) {
                ((MessageContext) attr)._add_trace("Detach from queue OUT");
            }
            return (MessageContext) attr;
        }
        return null;
    }

    /**
     * @param strSessionID String
     */
    public void remove(String strSessionID) {
        MessageQueue queue = (MessageQueue) mapQueue.get(strSessionID);
        if (queue != null) {
            queue.clear();
            mapQueue.remove(strSessionID);
            queue = null;
        }
    }

    public void detachAll() {
        Iterator iter = mapQueue.values().iterator();
        while (iter.hasNext()) {
            MessageQueue queue = (MessageQueue) iter.next();
            queue.clear();
        }
    }

    public int getQueueSize() {
        int iSize = 0;
        for (MessageQueue queue : mapQueue.values()) {
            iSize += queue.getQueueSize();
        }
        return iSize;
    }

    public void checkTimedOut() {
        Iterator iter = mapQueue.values().iterator();
        while (iter.hasNext()) {
            MessageQueue queue = (MessageQueue) iter.next();
            queue.checkTimeout();
        }
    }

    public String getName() {
        return "GW_QUEUE_OUT";
    }

    public String getQueueInfo(boolean removeNullSize) {
        StringBuilder buf = new StringBuilder();
        Iterator iter = mapQueue.values().iterator();
        while (iter.hasNext()) {
            MessageQueue queue = (MessageQueue) iter.next();
            int i = queue.getQueueSize();
            if (i > 0 || !removeNullSize) {
                buf.append(queue.mKey.toString()).append(":").append(i).append("\r\n");
            }
        }
        return buf.toString();
    }

    class MessageQueue extends VectorBoundedIndexedQueue {

        public MessageQueue(Object key) {
            super(key);
        }

        protected Attributable onMessageTimedOut(Attributable msg) {
            if (msg instanceof MessageContext) {
                getManager().getEvent().fireQueueOUTEvent(GWEvent.QUEUE_TIMEOUT, GatewayQueueOUT.this);
                ((MessageContext) msg).setException(new Exception("Message timeout"));
                ((MessageContext) msg).setProcessed(true);
                getManager().getLogger().attachLogRecord((MessageContext) msg);
            }
            return null;
        }

        public void onRemoveMessage(Attributable msg, Exception excReason) {
            if (excReason != null && msg instanceof MessageContext) {
                getManager().getEvent().fireQueueOUTEvent(GWEvent.QUEUE_REMOVED, GatewayQueueOUT.this);
                ((MessageContext) msg).setException(new Exception("Message removed"));
                ((MessageContext) msg).setProcessed(true);
                getManager().getLogger().attachLogRecord((MessageContext) msg);
            }
        }
    }
}
