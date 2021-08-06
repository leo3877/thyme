package org.yixi.thyme.core;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 所有实体类的基类。
 *
 * @author yixi
 * @since 1.0.0
 */
@Data
public class BaseEntity<PK> implements Serializable {

  public static final String CREATE_TIME = "create_time";

  public static final String UPDATE_TIME = "update_time";

  @Column(name = "id")
  @JsonSerialize(using = ToStringSerializer.class)
  protected PK id;

  /**
   * 更科学的命名应该是: createdAt, updatedAt, 但国内大部分取名是 createTime, updateTime
   */
  @Column(name = CREATE_TIME)
  private Date createTime;
  @Column(name = UPDATE_TIME)
  private Date updateTime;

  /**
   * 数据是否有效：0-有效, 未被删除  1-无效，已被删除
   */
  private Integer isDeleted;

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
