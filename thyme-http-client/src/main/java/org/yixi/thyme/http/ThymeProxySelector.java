package org.yixi.thyme.http;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ThymeProxySelector extends ProxySelector {

  private static final Logger logger = LoggerFactory.getLogger(OkHttpCookieManager.class);

  private final ConcurrentMap<String, List<Proxy>> proxyMap = new ConcurrentHashMap<>();
  private final List<Proxy> proxies = new ArrayList<>();

  @Override
  public List<Proxy> select(URI uri) {
    logger.debug("request host: {}", uri.getHost());
    List<Proxy> newList = Lists.newArrayList();
    List<Proxy> targets = proxyMap.get(uri.getHost());
    if (targets != null && !targets.isEmpty()) {
      newList.addAll(targets);
    }
    newList.addAll(proxies);
    return newList;
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    // ignore
  }

  public ThymeProxySelector addProxyHost(String host, Proxy.Type type, String proxyHost,
    Integer port) {
    if (StringUtils.isBlank(host) || "all".equals(host)) {
      proxies.add(new Proxy(type, new InetSocketAddress(proxyHost, port)));
    } else {
      List<Proxy> exists = proxyMap.computeIfAbsent(host, k -> new ArrayList<>());
      exists.add(new Proxy(type, new InetSocketAddress(proxyHost, port)));
    }
    return this;
  }
}
