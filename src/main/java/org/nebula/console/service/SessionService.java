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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class SessionService {

  public final static int DEFAULT_TIME_OUT = 30 * 60 * 1000;
  private final static Logger logger = Logger.getLogger(SessionService.class);
  private final static String SESSION_KEY = "SESSIONKEY";
  @Autowired
  private JedisPool jedisPool;

  public boolean exists(String sessionId) {

    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      return jedis.zrank(SESSION_KEY, sessionId) != null;
    } catch (Exception e) {
      logger.error("Failed to get rank for member " + sessionId, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }

    return false;
  }

  public void add(String sessionId, int timeout) {

    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      jedis.zadd(SESSION_KEY, System.currentTimeMillis() + timeout, sessionId);
    } catch (Exception e) {
      logger.error("Failed to add member " + sessionId, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }
  }

  public void remove(String sessionId) {

    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      jedis.zrem(SESSION_KEY, sessionId);
    } catch (Exception e) {
      logger.error("Failed to remove member " + sessionId, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }
  }
}
