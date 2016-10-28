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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

  public final static String USER_KEY = "UserKey";

  public final static String IS_ADMIN_KEY = "IsAdminKey";

  public final static String SESSION_KEY = "SessionKey";

  public static String getCookieValue(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();

    if(cookies!=null) {

      for (Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public static String addCookie(HttpServletResponse response, String cookieName,
                                 String cookieValue, int maxAge) {

    Cookie cookie = new Cookie(cookieName, cookieValue);
    response.addCookie(cookie);
    cookie.setMaxAge(maxAge);

    return cookieValue;
  }

  public static String deleteCookie(HttpServletRequest request, HttpServletResponse response,
                                    String cookieName) {
    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return cookie.getValue();
      }
    }
    return null;
  }
}
