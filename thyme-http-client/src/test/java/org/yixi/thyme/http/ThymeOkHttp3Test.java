package org.yixi.thyme.http;

import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.Codecs;
import org.yixi.thyme.core.util.Rsa;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ThymeOkHttp3Test {

  private ThymeHttpClient thymeHttpClient = ThymeOkHttp3.Builder.builder()
    .connectTimeout(5000)
    .writeTimeout(5000)
    .httpsNoVerify()
    .followRedirects(false)
    .cookie(true)
    .build();

  @Test
  public void test() {
    String string = Codecs.decodeUrlToString(
      ("%7B%22datasource%22%3A"
        + "%223__table%22%2C%22viz_type%22%3A%22table%22%2C%22slice_id%22%3A44%2C"
        + "%22granularity_sqla%22%3A%22ds%22%2C%22time_grain_sqla%22%3Anull%2C"
        + "%22since%22%3A%22101+years+ago%22%2C%22until%22%3A%22now%22%2C"
        + "%22groupby%22%3A%5B%5D%2C%22metrics%22%3A%5B%7B%22expressionType%22%3A"
        + "%22SQL%22%2C%22sqlExpression%22%3A%22num%22%2C%22column%22%3Anull%2C"
        + "%22aggregate%22%3Anull%2C%22hasCustomLabel%22%3Afalse%2C"
        + "%22fromFormData%22%3Atrue%2C%22label%22%3A%22num%22%2C%22optionName%22"
        + "%3A%22metric_cd186co5soi_tza2xmfdipb%22%7D%2C%7B%22expressionType%22"
        + "%3A%22SQL%22%2C%22sqlExpression%22%3A%22gender%22%2C%22column%22"
        + "%3Anull%2C%22aggregate%22%3Anull%2C%22hasCustomLabel%22%3Afalse%2C"
        + "%22fromFormData%22%3Atrue%2C%22label%22%3A%22gender%22%2C%22optionName"
        + "%22%3A%22metric_9vpdwtu7x28_g2xf8k8oll9%22%7D%2C%7B%22expressionType"
        + "%22%3A%22SQL%22%2C%22sqlExpression%22%3A%22name%22%2C%22column%22"
        + "%3Anull%2C%22aggregate%22%3Anull%2C%22hasCustomLabel%22%3Afalse%2C"
        + "%22fromFormData%22%3Atrue%2C%22label%22%3A%22name%22%2C%22optionName"
        + "%22%3A%22metric_osdrimimy0b_esqllsu1ao7%22%7D%5D%2C"
        + "%22timeseries_limit_metric%22%3Anull%2C%22row_limit%22%3A500%2C"
        + "%22include_time%22%3Afalse%2C%22order_desc%22%3Atrue%2C%22all_columns"
        + "%22%3A%5B%5D%2C%22order_by_cols%22%3A%5B%5D%2C"
        + "%22table_timestamp_format%22%3A%22%25Y-%25m-%25d+%25H%3A%25M%3A%25S%22"
        + "%2C%22page_length%22%3A0%2C%22include_search%22%3Afalse%2C"
        + "%22table_filter%22%3Afalse%2C%22align_pn%22%3Afalse%2C%22color_pn%22"
        + "%3Atrue%2C%22where%22%3A%22%22%2C%22having%22%3A%22%22%2C%22filters%22"
        + "%3A%5B%5D%2C%22url_params%22%3A%7B%7D%7D")
        .getBytes());
    System.out.println(string);
    System.out.println(Jsons.encodePretty(Jsons.decode(string, Map.class)));

    String x = Codecs.encodeUrlToString(
      ("%7B%22datasource%22%3A%2212__table%22%2C%22viz_type%22%3A%22big_number_total%22"
        + "%2C%22slice_id%22%3A47%2C%22granularity_sqla%22%3A%22created_time%22"
        + "%2C%22time_grain_sqla%22%3Anull%2C%22since%22%3A%227+years+ago%22%2C"
        + "%22until%22%3A%22now%22%2C%22metric%22%3A%7B%22expressionType%22%3A"
        + "%22SQL%22%2C%22sqlExpression%22%3A%22SUM%28balance%29+%2F+100%22%2C"
        + "%22column%22%3Anull%2C%22aggregate%22%3Anull%2C%22hasCustomLabel%22"
        + "%3Atrue%2C%22fromFormData%22%3Atrue%2C%22label%22%3A%22sum_total%22%2C"
        + "%22optionName%22%3A%22metric_rpsnbwz5hrm_8ligailcxns%22%7D%2C"
        + "%22y_axis_format%22%3A%22%2C.3f%22%2C%22where%22%3A%22%22%2C%22having"
        + "%22%3A%22%22%2C%22filters%22%3A%5B%5D%2C%22url_params%22%3A%7B%7D%7D")
        .getBytes());
    System.out.println(Jsons.encodePretty(Jsons.decode(x, Map.class)));
    System.out.println(x);
  }

  @Test
  public void testGet() {
    String res = thymeHttpClient.get("https://www.okex.me");
    System.out.println(res);
  }

  @Test
  public void test001() {
    DatabaseUser databaseUser = get("superset", "s:wac_loan_backend");
    System.out.println(databaseUser);
  }

  public DatabaseUser get(String projectId, String databaseName) {
    String domain = "https://strongbox.test.wacai.info";
    String publicKey = thymeHttpClient.get(String.format("%s/key/%s", domain, projectId));
    DatabaseUser databaseUser = thymeHttpClient.get(
      String.format("%s/props/%s/%s", domain, projectId, databaseName),
      DatabaseUser.class);
    String password = Rsa.Cipher.decryptToString(Rsa.loadPublicKey(publicKey),
      Codecs.decodeBase64(databaseUser.getPassword()));
    databaseUser.setPassword(password); // 解密后的明文密码
    return databaseUser;
  }

  @Test
  public void testGetReturnResponse() {
    ThymeHttpClient.ThymeResponse response = thymeHttpClient.get("http://www.baidu.com",
      ThymeHttpClient.ThymeResponse.class);
    String string = response.readData(String.class);
    System.out.println("type: " + response.getMediaType().type());
    System.out.println("subtype: " + response.getMediaType().subtype());
    System.out.println("charset:" + response.getMediaType().charset());
    System.out.println(string);
  }

  @Test
  public void testGet302Redirect() {
    String res = thymeHttpClient.get("http://suo.im/3OSL3w");
    System.out.println(res);
  }

  @Test
  public void testGet302Redirect1() {
    String targetUrl = "http://localhost:8080/aaaa/bbbb/cccc?a=3";
    HttpUrl httpUrl = HttpUrl.parse(targetUrl);
    List<String> list = httpUrl.pathSegments();
    httpUrl.queryParameterNames();
    System.out.println(
      "pattern: " + HttpUrl.parse("http://localhost:8080/aaaa/{ab}/cccc/{c}?a=4")
        .pathSegments());
    System.out.println("target: " + list);

    String pattern = "http://localhost:8080/aaaa/{111}/{2222}";

    httpUrl.newBuilder().host(httpUrl.host());
  }

  @Test
  public void testFormPostLogin() {
    String res = thymeHttpClient.get("http://localhost:8088/login/");
    int i = res.indexOf(
      "<input id=\"csrf_token\" name=\"csrf_token\" type=\"hidden\" value=\"");
    res = res.substring(i);
    res = res.substring(0, res.indexOf(">") - 1);
    String csrfToken = res.substring(res.lastIndexOf("\"") + 1);
    Document params = new Document()
      .append("csrf_token", csrfToken)
      .append("username", "admin")
      .append("username", "admin");

    String res1 = thymeHttpClient.postForm("http://localhost:8088/login/", params);

  }

  @Test
  public void testDownloadImg() {
    ThymeHttpClient.FileMemoryInputStream fileMemoryInputStream = thymeHttpClient.get(
      "https://img.zcool.cn/community/0174295541fe180000019ae91f2478.jpg",
      ThymeHttpClient.FileMemoryInputStream.class);

    fileMemoryInputStream.save(); // 保存到当前目录
    // fileMemoryInputStream.save("/Users/air/download"); // 保存到指定目录
  }

  @Test
  public void test002() {
    String s = thymeHttpClient.get("http://www.wangdaibus.com/");
  }

  @Test
  public void testDownloadHtml() {
    ThymeHttpClient.FileMemoryInputStream fileMemoryInputStream = thymeHttpClient.get(
      "https://www.baidu.com", ThymeHttpClient.FileMemoryInputStream.class);

    fileMemoryInputStream
      .rename("baidu_home.html")
      .save("/Users/air/Downloads/html"); // 保存到指定目录
  }

  @Test
  public void testDownloadLargeFile() {
    ThymeHttpClient.FileMemoryInputStream memoryTomcat = thymeHttpClient.get(
      "http://mirrors.tuna.tsinghua.edu"
        + ".cn/apache/tomcat/tomcat-9/v9.0.8/bin/apache-tomcat-9.0.8.zip",
      ThymeHttpClient.FileMemoryInputStream.class);
    memoryTomcat.save(state -> {
      System.out.println(String.format("文件大小：%d M, 已经下载：%d M, 下载速度：%d KB/s",
        state.getTotalSize() / 1024 / 1024, state.getReceivedSize() / 1024 / 1024,
        state.getReceiveRate() / 1024));
      if (state.isEnd()) {
        System.out.println("下载成功!");
      }
    });
  }

  @Test
  public void testCreateShortUrl() {
    thymeHttpClient.get("http://dwz.wailian.work"); // 1. 需要先调用主网页，获取 session token
    String originUrl = "http://www.baidu.com";
    String url = "http://dwz.wailian.work/api.php?from=w&url=" + Codecs.encodeBase64ToString(
      originUrl.getBytes()) + " &site=sina"; // 2. 生成短链接
    Map res = thymeHttpClient.get(url, Map.class);
    if ("error".equals(res.get("result"))) {
      System.out.println(res.get("data"));
    }
    System.out.println("短网址：" + res);
  }

  @Test
  public void testFormPost() {
    String res = thymeHttpClient.postForm("http://www.baidu.com");
    System.out.println(res);
  }

  @Test
  public void testJsonPost() {
    String body = "{\n"
      + "            \"id\": \"test\",\n"
      + "            \"updatedTime\": \"2018-04-18 11:39:35\",\n"
      + "            \"marketingPlanId\": \"marketind0001\",\n"
      + "            \"mobile\": \"1861831131\",\n"
      + "            \"createdTime\": \"2018-04-18 11:39:35\",\n"
      + "            \"type\": \"SMS\",\n"
      + "            \"ctrScore\": 0.819\n"
      + "        }";
    String res = thymeHttpClient.postJson(
      "http://api.test-ofa.k2.test.wacai"
        + ".info/finance-dmp/api/v1/open-data/objects/smash-user",
      body);
    System.out.println(res);
  }

  @Test
  public void testCall() {
    HttpRequest httpRequest = HttpRequest.post("http://api.creditcard.staging.wacai"
      + ".info/finance-dmp/api/v1/marketing-plan-tasks/query");

    // 添加 header
    httpRequest.addHeader("test", "test");
    httpRequest.addHeader("test", "test");
    httpRequest.addHeader("test1", "test");

    Map body = new HashMap();
    body.put("limit", 2);

    httpRequest.requestBody(HttpRequest.Body.jsonBody(Jsons.encode(body)));
    Map res = thymeHttpClient.call(httpRequest, Map.class);
    // 设置 requestBody
    System.out.println(Jsons.encodePretty(res));
  }

  @Test
  public void testFormRequestBody() {
    HttpRequest httpRequest = HttpRequest.post("url");

    // 添加 header
    httpRequest.addHeader("test", "test")
      .addHeader("test", "test")
      .addHeader("test1", "test");

    // 设置 requestBody
    HttpRequest.FormRequestBody formRequestBody = HttpRequest.Body.formBody()
      .addParam("name", "小红")
      .addParam("hobbies", "网球")
      .addParam("hobbies", "篮球")
      .addParam("hobbies", "足球");

    httpRequest.requestBody(formRequestBody);

    thymeHttpClient.call(httpRequest);
  }

  @Test
  public void testPk() {
    HttpRequest httpRequest = HttpRequest.post("https://x.pps.tv/api/highladder/getPkResult")
      .addHeader("Host", "x.pps.tv")
      .addHeader("Referer", "https://x.pps.tv/room/122864")
      .addHeader("User-Agent",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) "
          + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 "
          + "Mobile Safari/537.36")
      .addHeader("Origin", "https://x.pps.tv");
  }

  @Test
  public void doTestCookie() {
    HttpRequest httpRequest = HttpRequest.post("https://x.pps.tv/buy/buySendGift");

    // 添加 header
    httpRequest.addHeader("Cookie", "QC005=48d2b2fdf1c6841dd40493dafb064ded; "
      + "__uuid=1425d759-59b4-ce52-f982-eb9bfcfc64aa; QC006=jcqm816prpqp78zuxa74kkds; "
      + "P00004=1879738810.1534343187.9cc9095ef4; "
      + "QC008=1534343186.1534343186.1536588554.2; nu=0; "
      + "QC160=%7B%22u%22%3A%2217707420888%22%2C%22lang%22%3A%22%22%2C%22local%22%3A%7B"
      + "%22name%22%3A%22%E4%B8%AD%E5%9B%BD%E5%A4%A7%E9%99%86%22%2C%22init%22%3A%22Z%22"
      + "%2C%22rcode%22%3A48%2C%22acode%22%3A%2286%22%7D%2C%22type%22%3A%22p1%22%7D; "
      + "QC010=58052329; "
      +
      "P00001=eab5l3awTkm1CjcsDYm2tjeVBIlcgLsPMenZoVtewD7lKUm2PKVTC19jhSnsEJGRE4dPo0f; "
      + "P00003=2338093524; P00010=2338093524; P01010=1539705600; "
      +
      "P00007=eab5l3awTkm1CjcsDYm2tjeVBIlcgLsPMenZoVtewD7lKUm2PKVTC19jhSnsEJGRE4dPo0f; "
      + "P00PRU=2338093524; "
      + "P00002=%7B%22uid%22%3A%222338093524%22%2C%22pru%22%3A2338093524%2C%22user_name"
      + "%22%3A%2218682750689%22%2C%22nickname%22%3A%22sneaky99%22%2C%22pnickname%22%3A"
      + "%22sneaky99%22%2C%22type%22%3A11%2C%22email%22%3A%22%22%7D; "
      + "P000email=18682750689; QC007=DIRECT; "
      + "Hm_lvt_0f5556da646371aeac76715b71f140dd=1540479595; "
      + "Hm_lpvt_0f5556da646371aeac76715b71f140dd=1540479595; "
      + "x_8a9c1=sha1_jnopryuq_lTrwDV_jordtfd2_8c84c_15d0f; "
      + "__dfp=a0ed449a669903428ab853108bdfe0dccd38ef0701dfb2c5d8a4815e52dc284348"
      + "@1540991300101@1539695300101")
      .addHeader("Host", "x.pps.tv")
      .addHeader("Referer", "https://x.pps.tv/room/122864")
      .addHeader("User-Agent",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) "
          + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 "
          + "Mobile Safari/537.36")
      .addHeader("Origin", "https://x.pps.tv");

    HttpRequest.FormRequestBody formRequestBody = HttpRequest.Body.formBody()
      .addParam("room_id", "122864")
      .addParam("to_uid", "2366524090")
      .addParam("product_id", "781")
      // .addParam("product_id", "46")
      .addParam("money_type", "3")
      .addParam("swf_type", "1")
      .addParam("2366524090", "10");

    httpRequest.requestBody(formRequestBody);
    httpRequest.setUrl("https://x.pps.tv/buy/buySendGift");
    for (int i = 0; i < 1000000; i++) {
      long duration = Thyme.duration(() -> {
        String call = thymeHttpClient.call(httpRequest);
      });
      long sleep = 220 - duration / 1000;
      if (sleep > 0) {
        try {
          Thread.sleep(sleep);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }

  @Test
  public void testMultiPartUploadFile() {
    HttpRequest httpRequest = HttpRequest.post("url");

    // 添加 header
    httpRequest.addHeader("test", "test");
    httpRequest.addHeader("test", "test");
    httpRequest.addHeader("test1", "test");

    Map<String, Object> params = new HashMap<>();
    params.put("name", "小明");
    params.put("sex", "1");

    HttpRequest.MultipartFormRequestBody multipartRequestBody = HttpRequest.Body
      .multipartBody();
    multipartRequestBody.addFormData(params); // 提交表单字段

    multipartRequestBody.addFile("pic", new File("/user/test.png")); // 上传图片
    multipartRequestBody.addFile("pic2", new File("/user/test2.png")); // 上传图片

    // 设置 requestBody
    httpRequest.requestBody(multipartRequestBody);

    thymeHttpClient.call(httpRequest);
  }

  @Test
  public void test00132() throws Exception {
    org.jsoup.nodes.Document document = Jsoup.connect("https://btc.com/stats/diff").get();
    Elements trs = document.select("tr");
    TreeMap treeMap = new TreeMap();
    trs.forEach(tr -> {
      try {
        Date date = Jsons.decode(tr.children().get(1).text().trim(), Date.class);
        String[] strings = tr.children().get(3).text().split(" ");
        double percent = Double.parseDouble(strings[1]);
        if (strings[0].equals("-")) {
          percent = -percent;
        }
//        System.out.println(date);
//        System.out.println(percent);
        treeMap.put(date, percent);

      } catch (Exception e) {
        // ignore
//        e.printStackTrace();
      }
    });

    Date date = Jsons.decode("2017-12-01 00:00:00", Date.class);
    Entry<Date, Double> entry = treeMap.lowerEntry(date);
    double shouyi = 0.00017;
    double total = 0;

    for (int i = 0; i < 600; i++) {
      date = DateUtils.addDays(date, 1);
      Entry newEntry = treeMap.lowerEntry(date);
      if (newEntry.getKey() != entry.getKey()) {
        entry = newEntry;
        shouyi = shouyi / (1 + entry.getValue() / 100);
        System.out
          .println(
            String.format("%s %f %.8f", Jsons.encode(entry.getKey()), entry.getValue(), shouyi));
      }
      total += shouyi * 13.5;
    }
    System.out.println(total);

    System.out.println("btc: " + 6 * 12.5 * 24 * 365 * 8);

  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class DatabaseUser {

    private String name; // 数据库名称
    private String address; // 数据地址
    private String username; // 用户名
    private String password; // 密码

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public String toString() {
      return "DatabaseUser{" +
        "name='" + name + '\'' +
        ", address='" + address + '\'' +
        ", username='" + username + '\'' +
        ", password='" + password + '\'' +
        '}';
    }
  }
}
