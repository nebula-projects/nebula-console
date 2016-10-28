/*
 * Copyright 2002-2015 the original author or authors.
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

package org.nebula.console.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextHolderUtil {

  private static ThreadLocal<String> USERNAME = new ThreadLocal<String>();

  public static String getCurrentUsername() {
    return USERNAME.get();
  }

  public static void setCurrentUsername(String username) {
    USERNAME.set(username);
  }

}
