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
import org.nebula.admin.client.NebulaAdminClient;
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.request.admin.VerifyUserRequest;
import org.nebula.admin.client.response.admin.VerifyUserResponse;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class LoginService {

  private final static Logger logger = Logger.getLogger(LoginService.class);
  private final static String FAIL_TO_LOGIN_HASH = "FAIL_TO_LOGIN_HASH";
  private final static int LOGIN_RETRY_THRESHOLD = 5;

  @Autowired
  private JedisPool jedisPool;

  @Autowired
  private NebulaAdminClient nebulaAdminClient;

  @Autowired
  private AdminUserFacade adminUserFacade;

  public void login(String username, String password) throws Exception {

    VerifyUserRequest request = new VerifyUserRequest();
    request.setUsername(username);
    request.setPassword(password);

    VerifyUserResponse response = nebulaAdminClient.verifyUser(request);

    if (response.getResult() == VerifyUserResponse.USER_NOT_EXIST
        || response.getResult() == VerifyUserResponse.USER_LOCKED_DISABLED) {
      throw new IllegalArgumentException(response.getDescription());
    }

    if (response.getResult() == VerifyUserResponse.USER_INCORRECT_PWD) {

      if (exceedMaxLoginRetry(username)) {
        lockLoginUser(username);
      }

      incrLoginFailure(username);
      throw new IllegalArgumentException(response.getDescription());
    }

    ContextHolderUtil.setCurrentUsername(username);

    clearLoginFailure(username);
  }

  private boolean exceedMaxLoginRetry(String username) {
    return getLoginFailure(username) > LOGIN_RETRY_THRESHOLD;
  }

  private void lockLoginUser(String username) throws Exception {

    UserSummary userSummary = adminUserFacade.getUser(username);
    userSummary.setStatus(UserSummary.LOCKED);
    adminUserFacade.saveUser(userSummary);
  }

  private void clearLoginFailure(String username) {
    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      jedis.hdel(FAIL_TO_LOGIN_HASH, username);
    } catch (Exception e) {
      logger.error("Failed to get login failure for user " + username, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }
  }

  private int getLoginFailure(String username) {
    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      String count = jedis.hget(FAIL_TO_LOGIN_HASH, username);
      if (count == null) {
        return 0;
      }

      return Integer.parseInt(count);
    } catch (Exception e) {
      logger.error("Failed to get login failure for user " + username, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }
    return 0;
  }

  private void incrLoginFailure(String username) {
    Jedis jedis = null;

    try {
      jedis = jedisPool.getResource();

      jedis.hincrBy(FAIL_TO_LOGIN_HASH, username, 1);
    } catch (Exception e) {
      logger.error("Failed to incr login failure for user " + username, e);
      jedisPool.returnBrokenResource(jedis);
    } finally {
      jedisPool.returnResource(jedis);
    }

  }

}
