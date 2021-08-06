/*
 * Copyright 2014-2015 MongoDB, Inc.
 * Copyright (c) 2008-2014 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yixi.thyme.core.util;


import org.yixi.thyme.core.Thyme;

/**
 * 从 mongo 中移植而来
 *
 * @author mongo driver
 * @author yixi
 */
public final class Assertions {


  public static void main(String[] args) {
    notNull0(null, "test");
  }

  public static <T> T notNull(final String name, final T value) {
    if (value == null) {
      throw Thyme.exBiz(name + " can not be null");
    }
    return value;
  }

  public static <T> T notNull0(final T value, String format, Object... params) {
    if (value == null) {
      throw Thyme.exBiz(String.format(format, params));
    }
    return value;
  }

  public static <T> void null0(final T value, String format, Object... params) {
    if (value != null) {
      throw Thyme.exBiz(String.format(format, params));
    }
  }

  public static String notEmpty(final String name, final String value) {
    if (value == null) {
      throw Thyme.exBiz(name + " can not be empty");
    }
    return value;
  }

  public static void isTrue(final String name, final boolean condition) {
    if (!condition) {
      throw Thyme.exBiz(name);
    }
  }

  private Assertions() {
  }
}
