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

package org.nebula.console.service;

import com.google.code.kaptcha.Producer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class CaptchaService {

  public final static int DEFAULT_EXPIRE_OUT = 1 * 60 * 1000;

  private final static Logger logger = Logger.getLogger(SessionService.class);

  private final static String SIGNUP_CAPTCHA_HASH_KEY = "org.nebula.console.signup.captcha.hash";

  @Autowired
  private Producer captchaProducer;
  @Autowired
  private JedisPool jedisPool;

  public boolean isCorrectCaptcha(String sessionId, String captcha) {
    return captcha.equals(getCaptcha(sessionId));
  }

  private String getCaptcha(String sessionId) {
    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      return jedis.hget(SIGNUP_CAPTCHA_HASH_KEY, sessionId);
    } catch (Exception e) {
      logger.error("Failed to get captcha for session {} " + sessionId, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }
    return null;
  }

  public BufferedImage createCaptcha(String sessionId) {

    String capText = captchaProducer.createText();

    BufferedImage img = captchaProducer.createImage(capText);

    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      jedis.hset(SIGNUP_CAPTCHA_HASH_KEY, sessionId, capText);
    } catch (Exception e) {
      logger.error("Failed to create captcha for session {} " + sessionId, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }

    return img;
  }

}
