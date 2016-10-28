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

package org.nebula.console.facade;

import org.nebula.admin.client.NebulaMonitorClient;
import org.nebula.admin.client.model.monitor.StatisticSummary;
import org.nebula.admin.client.request.monitor.GetStatisticsRequest;
import org.nebula.admin.client.response.monitor.GetStatisticsResponse;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorServiceFacade {

  private final static Logger logger = LoggerFactory
      .getLogger(MonitorServiceFacade.class);

  @Autowired
  private NebulaMonitorClient nebulaMonitorClient;

  public List<StatisticSummary> getStatistics(String measurement, String domainName,
                                              String username, Long startTimestamp,
                                              Long endTimestamp) throws Exception {

    GetStatisticsRequest request = new GetStatisticsRequest();
    request.setMeasurement(measurement);
    request.setUsername(username);
    request.setDomainName(domainName);
    request.setStartTimestamp(startTimestamp);
    request.setEndTimestamp(endTimestamp);

    GetStatisticsResponse response = nebulaMonitorClient.getStatistics(request);
    logger.info("response=" + JsonUtils.toJson(response));

    return response.getStatisticSummaries();

  }
}
