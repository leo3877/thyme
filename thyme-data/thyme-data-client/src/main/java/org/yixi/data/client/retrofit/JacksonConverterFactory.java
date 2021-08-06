/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yixi.data.client.retrofit;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@linkplain Converter.Factory converter} which uses Jackson.
 *
 * <p>Because Jackson is so flexible in the types it supports, this converter assumes that it can
 * handle all types. If you are mixing JSON serialization with something else (such as protocol
 * buffers), you must {@linkplain Retrofit.Builder#addConverterFactory(Converter.Factory) add this
 * instance} last to allow the other converters a chance to see their types.
 *
 * @author retrofit2
 */
@SuppressWarnings("all")
public final class JacksonConverterFactory extends Converter.Factory {

  /**
   * Create an instance using a default {@link ObjectMapper} instance for conversion.
   */
  public static JacksonConverterFactory create() {
    return create(new ObjectMapper());
  }

  /**
   * Create an instance using {@code mapper} for conversion.
   */
  @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
  public static JacksonConverterFactory create(ObjectMapper mapper) {
    if (mapper == null) {
      throw new NullPointerException("mapper == null");
    }
    return new JacksonConverterFactory(mapper);
  }

  private final ObjectMapper mapper;

  private JacksonConverterFactory(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
    Retrofit retrofit) {
    if (type == String.class) {
      return null;
    }
    if (type == Boolean.class || type == boolean.class) {
      return null;
    }
    if (type == Byte.class || type == byte.class) {
      return null;
    }
    if (type == Character.class || type == char.class) {
      return null;
    }
    if (type == Double.class || type == double.class) {
      return null;
    }
    if (type == Float.class || type == float.class) {
      return null;
    }
    if (type == Integer.class || type == int.class) {
      return null;
    }
    if (type == Long.class || type == long.class) {
      return null;
    }
    if (type == Short.class || type == short.class) {
      return null;
    }
    JavaType javaType = mapper.getTypeFactory().constructType(type);
    ObjectReader reader = mapper.readerFor(javaType);
    return new JacksonResponseBodyConverter<>(reader);
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
    Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    if (type == String.class
      || type == boolean.class
      || type == Boolean.class
      || type == byte.class
      || type == Byte.class
      || type == char.class
      || type == Character.class
      || type == double.class
      || type == Double.class
      || type == float.class
      || type == Float.class
      || type == int.class
      || type == Integer.class
      || type == long.class
      || type == Long.class
      || type == short.class
      || type == Short.class) {
      return null;
    }
    JavaType javaType = mapper.getTypeFactory().constructType(type);
    ObjectWriter writer = mapper.writerFor(javaType);
    return new JacksonRequestBodyConverter<>(writer);
  }
}
