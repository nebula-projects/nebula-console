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

import org.nebula.admin.client.NebulaMgmtClient;
import org.nebula.admin.client.model.admin.DomainSummary;
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.model.monitor.StatisticSummary;
import org.nebula.admin.client.request.mgmt.GetInstancesRequest;
import org.nebula.admin.client.response.mgmt.GetRegistrationsResponse;
import org.nebula.console.facade.AdminDomainFacade;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.facade.MonitorServiceFacade;
import org.nebula.console.facade.NebulaServiceFacade;
import org.nebula.console.facade.AdminUserDomainFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.console.util.DateUtil;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.utils.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

  private final static String[] MEASUREMENTS = {StatisticSummary.WORKFLOWS_MEASUREMENT,
                                                StatisticSummary.ACTIVITIES_MEASUREMENT,
                                                StatisticSummary.RUNNING_INSTANCES_MEASUREMENT,
                                                StatisticSummary.COMPLETED_INSTANCES_MEASUREMENT};

  @Autowired
  private MonitorServiceFacade monitorServiceFacade;

  @Autowired
  private AdminUserFacade adminUserFacade;

  @Autowired
  private AdminUserDomainFacade adminUserDomainFacade;

  @Autowired
  private AdminDomainFacade adminDomainFacade;

  @Autowired
  private NebulaServiceFacade nebulaServiceFacade;

  public List<StatisticSummary> collectStatistics(String measurement, Long startTimestamp, Long endTimestamp) throws Exception{

     return monitorServiceFacade.getStatistics(measurement, null,
                                                  null,
                                                  startTimestamp == null ? DateUtil
                                                      .beforeToday(7).getTime()
                                                                         : startTimestamp,
                                                  endTimestamp == null ? DateUtil
                                                      .todayMiddleNight().getTime()
                                                                       : endTimestamp);

  }


  public List<StatisticSummary> collectAdminStatistics() throws Exception {
    List<StatisticSummary> allStatisticsSummaries = new ArrayList<StatisticSummary>();

    for (String measurement : MEASUREMENTS) {

      List<StatisticSummary>
          statisticSummaries =
          monitorServiceFacade.getStatistics(measurement, null, null,
                                                DateUtil.beforeToday(2).getTime(),
                                                DateUtil.todayMiddleNight().getTime());

      allStatisticsSummaries.add(statisticSummaries.get(0));

    }

    StatisticSummary userSummary = new StatisticSummary();
    userSummary.setMeasurement(StatisticSummary.USERS_MEASUREMENT);
    userSummary.setValue(new Long(adminUserFacade.countAllUsers()));


    StatisticSummary domainSummary = new StatisticSummary();
    domainSummary.setMeasurement(StatisticSummary.DOMAINS_MEASUREMENT);
    domainSummary.setValue(new Long(adminDomainFacade.countAllDomains()));

    allStatisticsSummaries.add(userSummary);
    allStatisticsSummaries.add(domainSummary);

    return allStatisticsSummaries;
  }

  public List<StatisticSummary>  collectUserStatistics(String username) throws Exception{

    int workflowNumbers = 0;
    int activityNumbers = 0;
    int unCompletedInstanceNumbers = 0;
    int completedInstanceNumbers = 0;

    String secretKey = adminUserFacade.getSecretKey(username);

    List<DomainSummary> domainSummaries = adminDomainFacade.getDomainSummaries(username);
    for (DomainSummary domainSummary : domainSummaries) {

      if (hasServer(domainSummary)) {
        NebulaMgmtClient
            nebulaMgmtClient =
            nebulaServiceFacade.buildNebulaMgmtClient(username,secretKey, domainSummary.getServers()[0]);

        workflowNumbers += countWorkflowNumbers(nebulaMgmtClient, username);
        activityNumbers += countActivityNumbers(nebulaMgmtClient, username);
        unCompletedInstanceNumbers += countUnCompletedInstanceNumbers(nebulaMgmtClient, username);
        completedInstanceNumbers += countCompletedInstanceNumbers(nebulaMgmtClient, username);

      }

    }

    List<StatisticSummary> statisticSummaries = new ArrayList<StatisticSummary>();
    statisticSummaries.add(createStatisticSummary(StatisticSummary.WORKFLOWS_MEASUREMENT, username, workflowNumbers));
    statisticSummaries.add(createStatisticSummary(StatisticSummary.ACTIVITIES_MEASUREMENT, username, activityNumbers));
    statisticSummaries.add(createStatisticSummary(StatisticSummary.RUNNING_INSTANCES_MEASUREMENT, username, unCompletedInstanceNumbers));
    statisticSummaries.add(createStatisticSummary(StatisticSummary.COMPLETED_INSTANCES_MEASUREMENT, username, completedInstanceNumbers));

    return statisticSummaries;
  }

  private boolean hasServer(DomainSummary domainSummary) {
    return domainSummary.getServers().length >0;
  }


  private int countWorkflowNumbers(NebulaMgmtClient nebulaMgmtClient, String username) throws Exception{
    return nebulaServiceFacade.getRegistrations(
        nebulaMgmtClient, username, null,
        0, 1, RegisterRequest.NodeType.WORKFLOW).getTotal();
  }

  private int countActivityNumbers(NebulaMgmtClient nebulaMgmtClient, String username) throws Exception{
    return nebulaServiceFacade.getRegistrations(
        nebulaMgmtClient, username, null,
        0, 1, RegisterRequest.NodeType.ACTIVITY).getTotal();
  }

  private int countUnCompletedInstanceNumbers(NebulaMgmtClient nebulaMgmtClient, String username) throws Exception{
    return nebulaServiceFacade
        .countInstances(nebulaMgmtClient,
                        username, null,
                        GetInstancesRequest.SearchMode.RUNNING);
  }

  private int countCompletedInstanceNumbers(NebulaMgmtClient nebulaMgmtClient, String username) throws Exception{
    return nebulaServiceFacade
        .countInstances(nebulaMgmtClient,
                        username, null,
                        GetInstancesRequest.SearchMode.HISTORY);
  }


  public List<StatisticSummary> collectDomainStatistics(String domainName) throws Exception{

    List<StatisticSummary> statisticSummaries = new ArrayList<StatisticSummary>();

    String nebulaServer = adminDomainFacade.selectNebulaServer(domainName);

    if(nebulaServer!=null) {

      String username = ContextHolderUtil.getCurrentUsername();
      String secretKey = adminUserFacade.getSecretKey(username);

      NebulaMgmtClient nebulaMgmtClient = nebulaServiceFacade.buildNebulaMgmtClient(username, secretKey,nebulaServer);

      statisticSummaries.add(collectWorkflows(nebulaMgmtClient));
      statisticSummaries.add(collectActivities(nebulaMgmtClient));
      statisticSummaries.add(collectUnCompletedInstances(nebulaMgmtClient));
      statisticSummaries.add(collectCompletedInstances(nebulaMgmtClient));

    }

    statisticSummaries.add(collectUsers(domainName));

    return statisticSummaries;

  }

  private StatisticSummary createStatisticSummary(String measurement, String username, int value) {

    StatisticSummary statisticSummary = new StatisticSummary();
    statisticSummary.setMeasurement(measurement);
    statisticSummary.setUsername(username);
    statisticSummary.setValue(new Long(value));

    return statisticSummary;
  }

  private StatisticSummary collectUsers(String domainName) throws Exception{
    int userNumbers = adminUserDomainFacade.getUserNumbersForDomain(domainName);
    return new StatisticSummary(StatisticSummary.USERS_MEASUREMENT, new Long(userNumbers));
  }

  private StatisticSummary collectWorkflows(NebulaMgmtClient nebulaMgmtClient) throws Exception{


    GetRegistrationsResponse response = nebulaServiceFacade.getRegistrations(nebulaMgmtClient, null,
                                                                             null, 0, 0,
                                                                             RegisterRequest.NodeType.WORKFLOW);


    return new StatisticSummary(StatisticSummary.WORKFLOWS_MEASUREMENT, new Long(response.getTotal()));
  }

  private StatisticSummary collectActivities(NebulaMgmtClient nebulaMgmtClient) throws Exception{


    GetRegistrationsResponse response = nebulaServiceFacade.getRegistrations(nebulaMgmtClient, null,
                                                                             null, 0, 0,
                                                                             RegisterRequest.NodeType.ACTIVITY);


    return new StatisticSummary(StatisticSummary.ACTIVITIES_MEASUREMENT, new Long(response.getTotal()));
  }

  private StatisticSummary collectUnCompletedInstances(NebulaMgmtClient nebulaMgmtClient) throws Exception{


    int unCompletedInstances = nebulaServiceFacade.countInstances(nebulaMgmtClient, null,
                                                                  null,
                                                                  GetInstancesRequest.SearchMode.RUNNING);


    return new StatisticSummary(StatisticSummary.RUNNING_INSTANCES_MEASUREMENT, new Long(unCompletedInstances));
  }

  private StatisticSummary collectCompletedInstances(NebulaMgmtClient nebulaMgmtClient) throws Exception{


    int completedInstances = nebulaServiceFacade.countInstances(nebulaMgmtClient, null,
                                                                  null,
                                                                  GetInstancesRequest.SearchMode.HISTORY);


    return new StatisticSummary(StatisticSummary.COMPLETED_INSTANCES_MEASUREMENT, new Long(completedInstances));
  }

}