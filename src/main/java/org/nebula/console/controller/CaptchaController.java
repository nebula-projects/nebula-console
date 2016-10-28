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

import org.nebula.console.service.CaptchaService;
import org.nebula.console.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/code")
public class CaptchaController {

  private final static Logger logger = LoggerFactory
      .getLogger(CaptchaController.class);

  @Autowired
  private CaptchaService captchaService;

  @RequestMapping(value = "captcha-image")
  public ModelAndView getKaptchaImage(HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    response.setHeader("Pragma", "no-cache");
    response.setContentType("image/jpeg");

    String sessionId = CookieUtil.getCookieValue(request, CookieUtil.SESSION_KEY);

    logger.info("sessionId {}.", sessionId);

    if (sessionId != null) {
      try {
        BufferedImage bi = captchaService.createCaptcha(sessionId);

        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        try {
          out.flush();
        } finally {
          out.close();
        }
      } catch (Exception e) {
        logger.error("Failed to get captcha.", e);
      }
    }

    return null;
  }
}