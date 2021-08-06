package org.yixi.thyme.proxy;

import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.http.HttpRequest;
import org.yixi.thyme.http.ThymeHttpClient;
import org.yixi.thyme.http.ThymeHttpClient.ThymeResponse;
import org.yixi.thyme.proxy.RouterConfig.Operator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ProxyTest {

  @Bean
  public FilterRegistrationBean proxyFilter() {
    ProxyFilter filter = new ProxyFilter();
    filter.addProxyRouter("/cdp", Operator.startsWith, "http://localhost:8080");

    filter.addListener("/cdp", Operator.startsWith, new ProxyEventListener() {
      @Override
      public HttpServletRequest preRequest(HttpServletRequest httpServletRequest,
        HttpRequest proxyRequest) {
        proxyRequest.setHeader("X-App-Id",
          httpServletRequest.getSession().getAttribute("___key_login_basic_user_info___")
            .toString());
        if (httpServletRequest.getRequestURI().contains("/cdp/user_profile")) {
          return new MultipleReadHttpRequest(httpServletRequest);
        } else {
          return httpServletRequest;
        }

      }

      @Override
      public void postResponse(HttpServletRequest httpServletRequest, HttpRequest proxyRequest,
        ThymeResponse thymeResponse) {
        try {
          if (httpServletRequest.getRequestURI().contains("/cdp/user_profile")) {
            YixiQuery yixiQuery = Jsons.decode(ThymeHttpClient
                .readString(httpServletRequest.getInputStream(), Charset.forName("utf-8")),
              YixiQuery.class);
          }
        } catch (IOException e) {
          throw new ThymeException(e.getMessage(), e);
        }
        Map wrapperResponse = new HashMap();
        if (thymeResponse.getCode() >= 400) {
          ThymeException exception = thymeResponse.readData(ThymeException.class);
          wrapperResponse.put("msg", exception.getMessage());
          wrapperResponse.put("code", exception.getCode());
        } else {
          wrapperResponse.put("code", 0);
          wrapperResponse.put("data", thymeResponse.readData(Map.class));
        }
        byte[] bytes = Jsons.encode(wrapperResponse).getBytes();
        thymeResponse.setDataStream(new ByteArrayInputStream(bytes));
        thymeResponse.setContentLength((long) bytes.length);
        thymeResponse.setHeader("Content-Length", bytes.length + "");
      }
    });

    FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
    filterRegBean.setFilter(filter);
    filterRegBean.addUrlPatterns("/*");
    filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

    return filterRegBean;
  }

}
