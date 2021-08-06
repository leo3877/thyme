package org.yixi.thyme.data.mongo.mapper;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.data.mongo.mapper.DefaultDocumentMapperTest.TestType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class A extends BaseEntity<String> {

  private int i;
  private float f;
  private double d;
  private long l;
  private String string;
  private Date date;
  private TestType testType;
  private B b;

}