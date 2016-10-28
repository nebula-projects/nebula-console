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

package org.nebula.console.config;

import org.nebula.admin.client.NebulaAdminClient;
import org.nebula.admin.client.NebulaMonitorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class ApplicationConfiguration {

  @Value("${redis.host}")
  String redisHost;

  @Value("${redis.port}")
  int redisPort;

  @Value("${redis.maxTotal}")
  int redisMaxTotal;

  @Value("${redis.maxIdle}")
  int redisMaxIdle;

  @Value("${redis.maxWaitSecs}")
  int redisMaxWaitSecs;

  @Value("${nebula.monitor.service.host}")
  String nebulaMonitorHost;

  @Value("${nebula.monitor.service.port}")
  int nebulaMonitorPort;

  @Value("${nebula.monitor.service.contextPath}")
  String nebulaMonitorContextPath;

  @Value("${nebula.admin.service.host}")
  String nebulaAdminServiceHost;

  @Value("${nebula.admin.service.port}")
  int nebulaAdminServicePort;

  @Value("${nebula.admin.service.contextPath}")
  String nebulaAdminServiceContextPath;

  @Value("${nebula.console.username}")
  String nebulaConsoleUsername;

  @Value("${nebula.console.password}")
  String nebulaConsolePassword;


  @Bean
  JedisPool jedisPool() {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(redisMaxTotal);
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
    config.setMaxIdle(redisMaxIdle);
    //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
    config.setMaxWaitMillis(1000 * redisMaxWaitSecs);
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    config.setTestOnBorrow(true); 
    return new JedisPool(config, redisHost, redisPort);
  }

  @Bean
  NebulaMonitorClient nebulaMonitorClient() {
    return new NebulaMonitorClient(nebulaConsoleUsername, nebulaConsolePassword,
                                   nebulaMonitorHost, nebulaMonitorPort, nebulaMonitorContextPath);
  }

  @Bean
  NebulaAdminClient nebulaAdminClient() {
    return new NebulaAdminClient(nebulaConsoleUsername, nebulaConsolePassword,
                                 nebulaAdminServiceHost, nebulaAdminServicePort,
                                 nebulaAdminServiceContextPath);
  }
}
