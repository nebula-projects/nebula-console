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

import org.apache.commons.lang.Validate;
import org.nebula.admin.client.NebulaMgmtClient;
import org.nebula.admin.client.model.admin.DomainSummary;
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.model.nebula.RegistrationSummary;
import org.nebula.admin.client.request.mgmt.GetInstancesRequest;
import org.nebula.admin.client.response.mgmt.GetRegistrationResponse;
import org.nebula.admin.client.response.mgmt.GetRegistrationsResponse;
import org.nebula.console.controller.Registration;
import org.nebula.console.controller.Workflow;
import org.nebula.console.controller.Workflows;
import org.nebula.console.facade.AdminDomainFacade;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.facade.NebulaServiceFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowService {

  @Autowired
  private UserService userService;

  @Autowired
  private AdminDomainFacade adminDomainFacade;

  @Autowired
  private NebulaServiceFacade nebulaServiceFacade;

  @Autowired
  private AdminUserFacade adminUserFacade;

  public Workflows searchWorkflows(String username, String workflowName, int startPage, int pageSize) throws Exception {

    String currentUser = ContextHolderUtil.getCurrentUsername();

    String secretKey = adminUserFacade.getSecretKey(currentUser);

    String searchUser = currentUserIfNotAdmin(currentUser, username);

    Validate.notEmpty(searchUser, "The username can't be empty.");

    List<DomainSummary> domainSummaries = adminDomainFacade.getDomainSummaries(searchUser);

    Workflows workflows = new Workflows();

    for (DomainSummary domainSummary : domainSummaries) {

      if (hasServer(domainSummary)) {

        NebulaMgmtClient
            nebulaMgmtClient = nebulaServiceFacade.buildNebulaMgmtClient(
            currentUser, secretKey, domainSummary
            .getServers()[0]);

        GetRegistrationsResponse
            response = nebulaServiceFacade.getRegistrations(nebulaMgmtClient, searchUser,
                                                            workflowName,
                                                            startPage, pageSize,
                                                            RegisterRequest.NodeType.WORKFLOW);

        if (hasRegistration(response)) {

          workflows.setWorkflows(composeWorkflowsWithStatistics(nebulaMgmtClient, response
              .getRegistrationSummaries()));
          workflows.setTotal(response.getTotal());

          return workflows;
        }
      }

    }

    return workflows;
  }

  private boolean hasServer(DomainSummary domainSummary) {
    return domainSummary.getServers().length !=0;
  }

  private boolean hasRegistration(GetRegistrationsResponse response) {
    return response.getRegistrationSummaries().size() > 0;
  }

  private List<Workflow> composeWorkflowsWithStatistics(NebulaMgmtClient nebulaMgmtClient, List<RegistrationSummary> registrationSummaries) throws Exception {

    List<Workflow> workflows = new ArrayList<Workflow>();

    for(RegistrationSummary rs : registrationSummaries) {
      Workflow workflow = new Workflow();


      workflow.setRunningInstances(nebulaServiceFacade
                                       .countInstances(nebulaMgmtClient, null,
                                                       rs.getId(),
                                                       GetInstancesRequest.SearchMode.RUNNING));
      workflow.setHistoryInstances(nebulaServiceFacade
                                       .countInstances(nebulaMgmtClient, null,
                                                       rs.getId(),
                                                       GetInstancesRequest.SearchMode.HISTORY));
      workflow.setRegistrationSummary(rs);

      workflows.add(workflow);
    }

    return workflows;
  }

  private String currentUserIfNotAdmin(String currentUser, String inputUser) {
    return userService.isAdmin(currentUser) ? inputUser : currentUser;
  }

  public WorkflowManager createWorkflowManager(String workflowId) throws Exception {
    return new WorkflowManager(workflowId);
  }

  public class WorkflowManager {

    private final static int PAGE_SIZE = 20;

    private UserSummary user;
    private String nebulaServer;
    private String workflowId;
    private String loginUsername;
    private String secretKey;

    private GetRegistrationResponse workflowRegistration;

    private List<Registration> activityRegistrations = new ArrayList<Registration>();

    public WorkflowManager(String workflowId) throws Exception {
      Validate.notEmpty(workflowId, "The id can't be empty.");

      this.workflowId = workflowId;
      this.loginUsername = ContextHolderUtil.getCurrentUsername();
      this.secretKey = adminUserFacade.getSecretKey(loginUsername);

      fetchRegistration();

      fetchCorrespondingActivityRegistrations();

    }

    public GetRegistrationResponse getWorkflowRegistration() {
      return workflowRegistration;
    }

    public List<Registration> getActivityRegistrations() {
      return activityRegistrations;
    }

    private void fetchRegistration() throws Exception {

      nebulaServer = getServer(workflowId);

      workflowRegistration =
          nebulaServiceFacade
              .getRegistration(nebulaServiceFacade.buildNebulaMgmtClient(
                                   loginUsername, secretKey,nebulaServer),
                               workflowId);

      Validate.isTrue(hasProcess(loginUsername, workflowRegistration),
                      "The user " + loginUsername + " doesn't own the process " + workflowId);
    }

    private boolean hasProcess(String username, GetRegistrationResponse response) {

      return userService.isAdmin(username) || username.equals(response.getUser());
    }

    private String getServer(String registrationId) throws Exception {

      String domainName = registrationId.split("-")[0];

      return adminDomainFacade.selectNebulaServer(domainName);
    }

    private void fetchCorrespondingActivityRegistrations() throws Exception {

      int pageNo = 1;

      while (true) {
        GetRegistrationsResponse
            response =
            nebulaServiceFacade
                .getActivityRegistrationsForUser(
                    nebulaServiceFacade.buildNebulaMgmtClient(loginUsername, secretKey, nebulaServer),
                    workflowRegistration.getUser(), pageNo);
        findCorrespondingActivityRegistrations(response);

        if (!hasMorePage(response)) {
          return;
        }

        pageNo++;
      }
    }

    private void findCorrespondingActivityRegistrations(GetRegistrationsResponse response) {
      List<RegistrationSummary> registrationSummaries = response.getRegistrationSummaries();

      for (RegistrationSummary summary : registrationSummaries) {

        RegisterRequest.RegistrationInfo
            registrationInfo =
            JsonUtils.toObject(summary.getData(), RegisterRequest.RegistrationInfo.class);

        for (String realm : registrationInfo.getRealms()) {
          if (workflowRegistration.getRegistrationInfo().getRealms().contains(realm)) {
            activityRegistrations.add(new Registration(summary, registrationInfo));
            break;
          }
        }
      }
    }

    private boolean hasMorePage(GetRegistrationsResponse response) {
      int pageNo = response.getPageNo();

      int numberOfCurrentPage = response.getRegistrationSummaries().size();

      return numberOfCurrentPage != 0 && (pageNo - 1) * PAGE_SIZE + numberOfCurrentPage < response
          .getTotal();

    }

  }
}