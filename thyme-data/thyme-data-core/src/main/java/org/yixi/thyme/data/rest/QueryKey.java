package org.yixi.thyme.data.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryKey {

  Condition require() default Condition.NONE;

  Operator[] operators() default {};

  /**
   * @author yixi
   * @since 1.0.0
   */
  enum Condition {
    NONE,

    ANY, // 满足其中1个

    ANY2, // 满足其中2个

    ANY3, // 满足其中3个

    ALL // 必须都满足
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  @SuppressWarnings("UniformEnumConstantName")
  enum Operator {
    $eq,

    $gt,

    $gte,

    $in,

    $nin,

    $lt,

    $lte,

    $or,

    $and,

    $exists,

    $regex
  }
}
