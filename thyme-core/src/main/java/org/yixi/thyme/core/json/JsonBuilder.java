package org.yixi.thyme.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons.ThymeObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class JsonBuilder {

  private static final ObjectMapper objectMapper = ThymeObjectMapper.defaultObjectMapper();

  private final StringWriter writer = new StringWriter();
  private JsonGenerator generator;

  public JsonBuilder() {
    try {
      generator = objectMapper.getFactory().createGenerator(writer);
      generator.writeStartObject();
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e.getCause());
    }
  }

  public static JsonBuilder builder() {
    return new JsonBuilder();
  }

  public JsonBuilder object(String key, Object val) {
    try {
      generator.writeObjectField(key, val);
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e.getCause());
    }
    return this;
  }

  public JsonBuilder number(String key, Number val) {
    try {
      if (val instanceof Long) {
        generator.writeNumberField(key, (Long) val);
      } else if (val instanceof Integer) {
        generator.writeNumberField(key, (Integer) val);
      } else if (val instanceof Double) {
        generator.writeNumberField(key, (Double) val);
      } else if (val instanceof Float) {
        generator.writeNumberField(key, (Float) val);
      } else if (val instanceof BigDecimal) {
        generator.writeNumberField(key, (BigDecimal) val);
      } else if (val instanceof Short) {
        generator.writeNumberField(key, (Short) val);
      } else if (val instanceof Byte) {
        generator.writeNumberField(key, (Byte) val);
      } else {
        throw new ThymeException("mismatch type: " + val.getClass().getName());
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage(), e.getCause());
    }
    return this;
  }

  public JsonBuilder string(String key, String val) {
    try {
      generator.writeStringField(key, val);
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e.getCause());
    }
    return this;
  }

  public JsonBuilder bool(String key, Boolean val) {
    try {
      generator.writeBooleanField(key, val);
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e.getCause());
    }
    return this;
  }

  public static String jsonString(String key, Object value) {
    return JsonBuilder.builder().object(key, value).build();
  }

  public String build() {
    try {
      generator.close();
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e.getCause());
    }
    return writer.toString();
  }
}
