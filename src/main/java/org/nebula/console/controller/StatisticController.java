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

import org.nebula.admin.client.model.monitor.StatisticSummary;
import org.nebula.console.exception.AjaxException;
import org.nebula.console.service.StatisticsService;
import org.nebula.console.service.UserService;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.nebula.framework.utils.JsonUtils.toJson;

@Controller
public class StatisticController {

  private final static Logger logger = LoggerFactory
      .getLogger(StatisticController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private StatisticsService statisticsService;

  @RequestMapping(value = "/statistics", method = RequestMethod.GET)
  @ResponseBody
  public String getStatistics() {

    String username = ContextHolderUtil.getCurrentUsername();

    boolean isAdmin = userService.isAdmin(username);

    logger.info("getStatistics for user " + username);

    try {
      if (isAdmin) {
        return toJson(statisticsService.collectAdminStatistics());
      } else {
        return toJson(statisticsService.collectUserStatistics(username));
      }
    } catch(Exception e) {
      throw new AjaxException("error", e);
    }

  }

  @RequestMapping(value = "/statistics/{measurement}", method = RequestMethod.GET)
  @ResponseBody
  public String getStatistics(@PathVariable("measurement") String measurement,
                              @RequestParam("startTimestamp") Long startTimestamp,
                              @RequestParam("endTimestamp") Long endTimestamp) {

    String currentUsername = ContextHolderUtil.getCurrentUsername();

    logger.info("getStatistics username {}, measurement {}, startTimestamp {}, endTimestamp {}.",
                currentUsername,
                measurement, startTimestamp, endTimestamp);
    try {

      String
          statistics =
          toJson(statisticsService.collectStatistics(measurement, startTimestamp, endTimestamp));

      logger.info("getStatistics response for {} with result {}",
                  currentUsername, statistics);

      return statistics;

    } catch(Exception e) {
      throw new AjaxException("error", e);
    }
  }

  @ResponseBody
  @RequestMapping(value = "/statistics/domain/{domainName}", method = RequestMethod.GET)
  public String getDomainStatistics(@PathVariable("domainName") String domainName) {

    logger.info("collectStatistics for domain {}", domainName);

    List<StatisticSummary> statisticSummaries = new ArrayList<StatisticSummary>();
    try {

      statisticSummaries = statisticsService.collectDomainStatistics(domainName);

      logger.info("statisticSummaries: {}", JsonUtils.toJson(statisticSummaries));
    } catch (Exception e) {
      throw new AjaxException("error", e);
    }

    return JsonUtils.toJson(statisticSummaries);
  }

}