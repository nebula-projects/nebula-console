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

package org.nebula.console.controller;

import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.console.exception.AjaxException;
import org.nebula.console.service.CaptchaService;
import org.nebula.console.service.LoginService;
import org.nebula.console.service.SessionService;
import org.nebula.console.service.UserService;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.console.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IndexController {

  private final static Logger logger = LoggerFactory
      .getLogger(IndexController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private LoginService loginService;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private CaptchaService captchaService;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index(ModelMap model)
      throws Exception {

    logger.info("{} visit index.", ContextHolderUtil.getCurrentUsername());

    return "dashboard";
  }

  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public String login(HttpServletResponse response, ModelMap model) {

    logger.info("get login page.");

    String cookieValue = UUID.randomUUID().toString();

    CookieUtil.addCookie(response, CookieUtil.SESSION_KEY, cookieValue, Integer.MAX_VALUE);

    model.addAttribute("loginActive", "active");

    return "login";
  }

  @RequestMapping(value = "/signup", method = RequestMethod.GET)
  public String signup(HttpServletResponse response, ModelMap model) throws Exception {

    logger.info("get signup page.");

    String cookieValue = UUID.randomUUID().toString();

    CookieUtil.addCookie(response, CookieUtil.SESSION_KEY, cookieValue, Integer.MAX_VALUE);
    model.addAttribute("signupActive", "active");

    return "login";
  }

  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  @ResponseBody
  public String signup(UserSummary userSummary, @RequestParam("captcha") String captcha,
                       HttpServletRequest request) {
    logger.info("The user {} signup with captcha {}.", userSummary.getUsername(), captcha);

    try {

      validateCaptcha(request, captcha);

      userService.signup(userSummary);

    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  private void validateCaptcha(HttpServletRequest request, String captcha) {
    String sessionId = CookieUtil.getCookieValue(request, CookieUtil.SESSION_KEY);

    if (!captchaService.isCorrectCaptcha(sessionId, captcha)) {
      throw new IllegalArgumentException("The captcha is not incorrect.");
    }
  }

  @RequestMapping(value = "/login", method = RequestMethod.POST)
  @ResponseBody
  public String login(String username, String password, String captcha,
                      HttpServletResponse response) {

    logger.info("The user {} try to login with captcha {}.", username, captcha);

    try {

      loginService.login(username, password);

      String cookieValue = username + ":" + UUID.randomUUID().toString();

      CookieUtil.addCookie(response, CookieUtil.USER_KEY, cookieValue, Integer.MAX_VALUE);

      sessionService.add(cookieValue, SessionService.DEFAULT_TIME_OUT);

      CookieUtil.addCookie(response, CookieUtil.IS_ADMIN_KEY,
                           userService.isAdmin(username) + "",
                           Integer.MAX_VALUE);

    } catch (Exception e) {
      throw new AjaxException(e);
    }
    return "success";
  }


  @RequestMapping(value = "/logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
    logger.info("The user logout.");

    String value = CookieUtil.deleteCookie(request, response, CookieUtil.USER_KEY);

    if (value != null) {
      sessionService.remove(value);
    }

    ContextHolderUtil.setCurrentUsername(null);

    CookieUtil.deleteCookie(request, response, CookieUtil.IS_ADMIN_KEY);

    return "redirect:login";
  }


}
