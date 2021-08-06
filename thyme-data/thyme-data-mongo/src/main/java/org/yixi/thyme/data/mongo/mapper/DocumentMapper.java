package org.yixi.thyme.data.mongo.mapper;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.yixi.thyme.data.mongo.MongoUpdate;

/**
 * @author yixi
 * @see DefaultMongoDocMapper
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface DocumentMapper {

  <T> T toObject(Document doc, Class<T> clazz);

  Document toDocument(Object obj);

  List<Document> toDocuments(List<Object> objects);

  MongoUpdate toMongoUpdate(Object obj);

  /**
   * @author sneaky
   * @since 1.0.0
   */
  @Setter
  @Getter
  class Options {

    /**
     * 忽略空属性
     */
    private boolean ignoreNull;
    /**
     * 下划线
     */
    private boolean lowerUnderscore;

  }

  /**
   * @author sneaky
   * @since 1.0.0
   */
  class OptionsBuilder {

    private Options options;

    public OptionsBuilder(Options options) {
      this.options = options;
    }

    public static OptionsBuilder builder() {
      return new OptionsBuilder(new Options());
    }

    public OptionsBuilder ignoreNull() {
      options.setIgnoreNull(true);
      return this;
    }

    public OptionsBuilder lowerUnderscore() {
      options.setLowerUnderscore(true);
      return this;
    }

    public Options build() {
      return options;
    }
  }
}
