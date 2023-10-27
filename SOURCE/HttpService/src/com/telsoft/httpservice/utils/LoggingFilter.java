//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.httpservice.utils;

import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

@PreMatching
@Priority(-2147483648)
public final class LoggingFilter implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter, ClientResponseFilter, WriterInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class.getName());
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";
    private static final String ENTITY_LOGGER_PROPERTY = LoggingFilter.class.getName() + ".entityLogger";
    private static final String LOGGING_ID_PROPERTY = LoggingFilter.class.getName() + ".id";
    private static final Comparator<Entry<String, List<String>>> COMPARATOR = new Comparator<Entry<String, List<String>>>() {
        public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {
            return ((String) o1.getKey()).compareToIgnoreCase((String) o2.getKey());
        }
    };
    private static final int DEFAULT_MAX_ENTITY_SIZE = 8192;
    private final Logger logger;
    private final AtomicLong _id;
    private final boolean printEntity;
    private final int maxEntitySize;

    public LoggingFilter() {
        this(LOGGER, false);
    }

    public LoggingFilter(Logger logger, boolean printEntity) {
        this._id = new AtomicLong(0L);
        this.logger = logger;
        this.printEntity = printEntity;
        this.maxEntitySize = 8192;
    }

    public LoggingFilter(Logger logger, int maxEntitySize) {
        this._id = new AtomicLong(0L);
        this.logger = logger;
        this.printEntity = true;
        this.maxEntitySize = Math.max(0, maxEntitySize);
    }

    private void log(StringBuilder b) {
        if (this.logger != null) {
            this.logger.info(b.toString());
        }

    }

    private StringBuilder prefixId(StringBuilder b, long id) {
        b.append(Long.toString(id)).append(" ");
        return b;
    }

    private void printRequestLine(StringBuilder b, String note, long id, String method, URI uri) {
        this.prefixId(b, id).append("* ").append(note).append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        this.prefixId(b, id).append("> ").append(method).append(" ").append(uri.toASCIIString()).append("\n");
    }

    private void printResponseLine(StringBuilder b, String note, long id, int status) {
        this.prefixId(b, id).append("* ").append(note).append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        this.prefixId(b, id).append("< ").append(Integer.toString(status)).append("\n");
    }

    private void printPrefixedHeaders(StringBuilder b, long id, String prefix, MultivaluedMap<String, String> headers) {
        Iterator var6 = this.getSortedHeaders(headers.entrySet()).iterator();

        while (true) {
            while (var6.hasNext()) {
                Entry<String, List<String>> headerEntry = (Entry) var6.next();
                List<?> val = (List) headerEntry.getValue();
                String header = (String) headerEntry.getKey();
                if (val.size() == 1) {
                    this.prefixId(b, id).append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
                } else {
                    StringBuilder sb = new StringBuilder();
                    boolean add = false;
                    Iterator var12 = val.iterator();

                    while (var12.hasNext()) {
                        Object s = var12.next();
                        if (add) {
                            sb.append(',');
                        }

                        add = true;
                        sb.append(s);
                    }

                    this.prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
                }
            }

            return;
        }
    }

    private Set<Entry<String, List<String>>> getSortedHeaders(Set<Entry<String, List<String>>> headers) {
        TreeSet<Entry<String, List<String>>> sortedHeaders = new TreeSet(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    private InputStream logInboundEntity(StringBuilder b, InputStream stream, Charset charset) throws IOException {
        if (!((InputStream) stream).markSupported()) {
            stream = new BufferedInputStream((InputStream) stream);
        }

        ((InputStream) stream).mark(this.maxEntitySize + 1);
        byte[] entity = new byte[this.maxEntitySize + 1];
        int entitySize = ((InputStream) stream).read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, this.maxEntitySize), charset));
        if (entitySize > this.maxEntitySize) {
            b.append("...more...");
        }

        b.append('\n');
        ((InputStream) stream).reset();
        return (InputStream) stream;
    }

    public void filter(ClientRequestContext context) throws IOException {
        long id = this._id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);
        StringBuilder b = new StringBuilder();
        this.printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
        this.printPrefixedHeaders(b, id, "> ", context.getStringHeaders());
        if (this.printEntity && context.hasEntity()) {
            OutputStream stream = new LoggingFilter.LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
        } else {
            this.log(b);
        }

    }

    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        long id = requestId != null ? (Long) requestId : this._id.incrementAndGet();
        StringBuilder b = new StringBuilder();
        this.printResponseLine(b, "Client response received", id, responseContext.getStatus());
        this.printPrefixedHeaders(b, id, "< ", responseContext.getHeaders());
        if (this.printEntity && responseContext.hasEntity()) {
            responseContext.setEntityStream(this.logInboundEntity(b, responseContext.getEntityStream(), MessageUtils.getCharset(responseContext.getMediaType())));
        }

        this.log(b);
    }

    public void filter(ContainerRequestContext context) throws IOException {
        long id = this._id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);
        StringBuilder b = new StringBuilder();
        this.printRequestLine(b, "Server has received a request", id, context.getMethod(), context.getUriInfo().getRequestUri());
        this.printPrefixedHeaders(b, id, "> ", context.getHeaders());
        if (this.printEntity && context.hasEntity()) {
            context.setEntityStream(this.logInboundEntity(b, context.getEntityStream(), MessageUtils.getCharset(context.getMediaType())));
        }

        this.log(b);
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        long id = requestId != null ? (Long) requestId : this._id.incrementAndGet();
        StringBuilder b = new StringBuilder();
        this.printResponseLine(b, "Server responded with a response", id, responseContext.getStatus());
        this.printPrefixedHeaders(b, id, "< ", responseContext.getStringHeaders());
        if (this.printEntity && responseContext.hasEntity()) {
            OutputStream stream = new LoggingFilter.LoggingStream(b, responseContext.getEntityStream());
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
        } else {
            this.log(b);
        }

    }

    public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
        LoggingFilter.LoggingStream stream = (LoggingFilter.LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            this.log(stream.getStringBuilder(MessageUtils.getCharset(writerInterceptorContext.getMediaType())));
        }

    }

    private class LoggingStream extends FilterOutputStream {
        private final StringBuilder b;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LoggingStream(StringBuilder b, OutputStream inner) {
            super(inner);
            this.b = b;
        }

        StringBuilder getStringBuilder(Charset charset) {
            byte[] entity = this.baos.toByteArray();
            this.b.append(new String(entity, 0, Math.min(entity.length, LoggingFilter.this.maxEntitySize), charset));
            if (entity.length > LoggingFilter.this.maxEntitySize) {
                this.b.append("...more...");
            }

            this.b.append('\n');
            return this.b;
        }

        public void write(int i) throws IOException {
            if (this.baos.size() <= LoggingFilter.this.maxEntitySize) {
                this.baos.write(i);
            }

            this.out.write(i);
        }
    }
}
