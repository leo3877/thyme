package org.yixi.thyme.tool.gen.entity;

import org.yixi.thyme.http.HttpRequest;
import org.yixi.thyme.http.ThymeHttpClient;
import org.yixi.thyme.http.ThymeOkHttp3;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class RestRequestEntityGen implements EntityGen {

  private static final Logger logger = LoggerFactory.getLogger(RestRequestEntityGen.class);
  private final JsonStringEntityGen jsonStringEntityGen;
  private final ThymeHttpClient thymeHttpClient;

  public RestRequestEntityGen(JsonStringEntityGen jsonStringEntityGen) {
    this(jsonStringEntityGen, ThymeOkHttp3.Builder.builder().httpsNoVerify().build());
  }

  public RestRequestEntityGen(JsonStringEntityGen jsonStringEntityGen,
    ThymeHttpClient thymeHttpClient) {
    this.jsonStringEntityGen = jsonStringEntityGen;
    this.thymeHttpClient = thymeHttpClient;
  }

  public void get(String url, String className, Profile profile) {
    get(url, className, null, profile);
  }

  public void get(String url, String className, String match, Profile profile) {
    get(url, null, className, match, profile);
  }

  public void get(String url, Map<String, Object> header,
    String className, String match, Profile profile) {
    String res = thymeHttpClient.get(url, header, String.class);
    jsonStringEntityGen.gen(res, match, className, profile);
  }

  public void formPost(String url, String className, Profile profile) {
    formPost(url, null, className, null, profile);
  }

  public void post(String url, Map<String, Object> params, String className, Profile profile) {
    formPost(url, null, className, null, profile);
  }

  public void formPost(String url, Map<String, Object> params, String className, String match,
    Profile profile) {
    formPost(url, null, params, className, match, profile);
  }

  public void formPost(String url, Map<String, Object> header, Map<String, Object> params,
    String className, String match, Profile profile) {
    jsonStringEntityGen.gen(thymeHttpClient.postForm(url, header, params, String.class), match,
      className, profile);
  }

  public void call(HttpRequest httpRequest, String className, String match, Profile profile) {
    jsonStringEntityGen.gen(thymeHttpClient.call(httpRequest, String.class), match, className,
      profile);
  }
}


