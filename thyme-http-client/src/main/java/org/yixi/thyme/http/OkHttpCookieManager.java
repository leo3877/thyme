package org.yixi.thyme.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class OkHttpCookieManager implements CookieJar {

  private static final Logger logger = LoggerFactory.getLogger(OkHttpCookieManager.class);

  private final ConcurrentMap<String, CookieDomain> cookieMap = new ConcurrentHashMap<>();

  @Override
  public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
    logger.debug("Set-Cookie: url: {}, cookies: {}", url, cookies);
    cookies.forEach(c -> {
      String domainName = c.domain();
      if (domainName.startsWith("www.")) {
        domainName = domainName.substring(4);
      } else if (domainName.startsWith(".")) {
        domainName = domainName.substring(1);
      }
      CookieDomain exists = cookieMap.get(domainName);
      if (exists == null) {
        exists = new CookieDomain(domainName);
        cookieMap.put(domainName, exists);
      }
      exists.addCookie(c);

      String parent = domainName.substring(domainName.indexOf(".") + 1);
      if (parent.indexOf(".") > 0) {
        CookieDomain cookieDomain = cookieMap.get(parent);
        if (cookieDomain != null) {
          exists.setParent(cookieDomain);
        }
      }
    });
  }

  @Override
  public List<Cookie> loadForRequest(HttpUrl url) {
    String host = url.host();
    int i = host.indexOf("www.");
    if (i >= 0) {
      host = host.substring(i + 4);
    }
    CookieDomain cookieDomain = cookieMap.get(host);
    if (cookieDomain == null) {
      return Collections.emptyList();
    } else {
      List<Cookie> cookies = cookieDomain.getCookies();
      CookieDomain it = cookieDomain;
      while (true) {
        CookieDomain parent = it.getParent();
        if (parent != null) {
          cookies.addAll(it.getCookies());
          it = parent;
        } else {
          break;
        }
      }
      logger.debug("Request-Cookie: url: {}, cookies: {}", url, cookies);
      return cookies;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class CookieDomain {

    private final String name;
    private CookieDomain parent;
    private List<CookieDomain> children = new ArrayList<>();
    private List<Cookie> cookies = new ArrayList<>();

    public CookieDomain(String name) {
      this.name = name;
    }

    public void setParent(CookieDomain parent) {
      this.parent = parent;
    }

    public CookieDomain getParent() {
      return parent;
    }

    public List<CookieDomain> getChildren() {
      return children;
    }

    public CookieDomain addChild(CookieDomain cookieDomain) {
      children.add(cookieDomain);
      return this;
    }

    public List<Cookie> getCookies() {
      return cookies;
    }

    public synchronized CookieDomain addCookie(Cookie cookie) {
      for (Cookie exists : cookies) {
        if (exists.name().equals(cookie.name())) {
          cookies.remove(exists);
          break;
        }
      }
      cookies.add(cookie);
      return this;
    }

    public String getName() {
      return name;
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CookieDomain) {
        return ((CookieDomain) obj).getName().equals(name);
      } else {
        return false;
      }
    }
  }
}
