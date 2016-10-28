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

package org.nebula.console.interceptor;

import org.nebula.console.service.SessionService;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.console.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

  private final static Logger logger = LoggerFactory
      .getLogger(LoginInterceptor.class);

  @Autowired
  private SessionService sessionService;
  @Autowired
  private AdminUserFacade adminUserFacade;
  private List<String> excludedUrls;
  private List<String> adminUrls;
  private List<String> apiUrls;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String contextPath = request.getContextPath();
    request.setAttribute("contextPath", contextPath);

    String requestUri = request.getRequestURI();


    logger.info("requestUri=" + requestUri);

    for (String url : excludedUrls) {
      if (requestUri.endsWith(url)) {
        return true;
      }
    }

    String username = null;
    try {
      String userValue = CookieUtil.getCookieValue(request, CookieUtil.USER_KEY);
      logger.info("userValue=" + userValue);
      if (userValue != null) {
        boolean exists = sessionService.exists(userValue);
        logger.info("userValue {} exists {}", userValue, exists);
        if (!exists) {
          response.sendRedirect(request.getContextPath() + "/login");
          return true;
        }
      } else {
        response.sendRedirect(request.getContextPath() + "/login");
        return true;
      }

      username = userValue.split(":")[0];

      if (!adminUserFacade.isAdmin(username)) {
        for (String url : adminUrls) {
          if (requestUri.startsWith(contextPath + url)) {
            return false;
          }
        }
      }

    } catch (Throwable e) {
      logger.error("Failed to login", e);
      return false;
    }

    ContextHolderUtil.setCurrentUsername(username);

    return true;
  }


  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                         ModelAndView modelAndView) throws Exception {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) throws Exception {
  }

  public void setExcludedUrls(List<String> excludedUrls) {
    this.excludedUrls = excludedUrls;
  }

  public void setAdminUrls(List<String> adminUrls) {
    this.adminUrls = adminUrls;
  }

}

