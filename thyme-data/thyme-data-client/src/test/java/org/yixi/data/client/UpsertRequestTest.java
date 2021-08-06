package org.yixi.data.client;

import org.yixi.data.client.UpsertRequest.Builder;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.json.Jsons;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class UpsertRequestTest {

  @Test
  public void test() {
    UpsertRequest upsertRequest = Builder.builder()
      .filter("moible", "18681231832")
      .doc(new Document<>("a", "b"))
      .build();
    System.out.println(Jsons.encodePretty(upsertRequest));
  }
}
