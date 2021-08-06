package org.yixi.thyme.data.mongo.biz;

import static org.yixi.thyme.data.mongo.Conditions.and;
import static org.yixi.thyme.data.mongo.Conditions.eq;
import static org.yixi.thyme.data.mongo.Conditions.gt;
import static org.yixi.thyme.data.mongo.Conditions.gte;
import static org.yixi.thyme.data.mongo.Conditions.ne;
import static org.yixi.thyme.data.mongo.Conditions.or;

import java.util.Date;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.data.mongo.Conditions.Or;

/**
 * @author yixi
 * @since 1.0.0
 */
public class FilterTest {

  public static void main(String[] args) {
    Or or = or(and(eq("b", "b"), ne("a", 1), gt("b", 1), gte("c", 2)), eq("a", 3));
    System.out.println(Thyme.DATE_TIME_FORMAT_GMT_8.format(new Date(1617930903000L)));
  }

}
