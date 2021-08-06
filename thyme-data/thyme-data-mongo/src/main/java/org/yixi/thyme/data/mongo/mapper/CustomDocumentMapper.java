package org.yixi.thyme.data.mongo.mapper;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.mongo.MongoUpdate;

/**
 * @author yixi
 * @since 1.0.0
 */
public class CustomDocumentMapper implements DocumentMapper {

  private DocumentMapper documentMapper;

  public CustomDocumentMapper() {
    documentMapper = new DefaultDocumentMapper();
  }

  public CustomDocumentMapper(Options options) {
    documentMapper = new DefaultDocumentMapper(options);
  }

  public CustomDocumentMapper(DocumentMapper documentMapper) {
    this.documentMapper = documentMapper;
  }

  @Override
  public Document toDocument(Object obj) {
    Document doc = documentMapper.toDocument(obj);
    Object id = doc.get("id");
    if (id != null) {
      doc.put("_id", id);
      doc.remove("id");
    }
    return doc;
  }

  @Override
  public List<Document> toDocuments(List<Object> objects) {
    return objects.stream().map(this::toDocument).collect(Collectors.toList());
  }

  @Override
  public MongoUpdate toMongoUpdate(Object obj) {
    return documentMapper.toMongoUpdate(obj);
  }

  @Override
  public <T> T toObject(Document doc, Class<T> clazz) {
    T obj = documentMapper.toObject(doc, clazz);
    Object id = doc.get("_id");
    if (id != null) {
      if (Map.class.isAssignableFrom(clazz)) {
        Map<String, Object> map = (Map) obj;
        map.put("id", id);
        map.remove("_id");
      } else {
        try {
          Method idMethod = obj.getClass().getMethod("setId", Object.class);
          idMethod.invoke(obj, id);
        } catch (Exception e) {
          throw new ThymeException(e.getMessage(), e);
        }
      }
    }
    return obj;
  }
}
