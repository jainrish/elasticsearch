/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.common.http;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Objects;

public class HttpProxy implements ToXContentFragment {

    public static final HttpProxy NO_PROXY = new HttpProxy(null, null, null);

    private static final ParseField HOST = new ParseField("host");
    private static final ParseField PORT = new ParseField("port");
    private static final ParseField SCHEME = new ParseField("scheme");

    private String host;
    private Integer port;
    private Scheme scheme;

    public HttpProxy(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public HttpProxy(String host, Integer port, Scheme scheme) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (Strings.hasText(host) && port != null) {
            builder.startObject("proxy").field("host", host).field("port", port);
            if (scheme != null) {
                builder.field("scheme", scheme.scheme());
            }
            builder.endObject();
        }
        return builder;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Scheme getScheme() {
        return scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpProxy that = (HttpProxy) o;

        return Objects.equals(port, that.port) && Objects.equals(host, that.host) && Objects.equals(scheme, that.scheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, scheme);
    }


    public static HttpProxy parse(XContentParser parser) throws IOException {
        XContentParser.Token token;
        String currentFieldName = null;
        String host = null;
        Integer port = null;
        Scheme scheme = null;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (HOST.match(currentFieldName)) {
                host = parser.text();
            } else if (SCHEME.match(currentFieldName)) {
                scheme = Scheme.parse(parser.text());
            } else if (PORT.match(currentFieldName)) {
                port = parser.intValue();
                if (port <= 0 || port >= 65535) {
                    throw new ElasticsearchParseException("Proxy port must be between 1 and 65534, but was " + port);
                }
            }
        }

        if (port == null || host == null) {
            throw new ElasticsearchParseException("Proxy must contain 'port' and 'host' field");
        }

        return new HttpProxy(host, port, scheme);
    }
}
