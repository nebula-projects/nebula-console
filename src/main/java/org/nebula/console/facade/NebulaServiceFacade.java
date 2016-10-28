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

import org.nebula.admin.client.NebulaMgmtClient;
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.request.mgmt.GetHistoryEventsRequest;
import org.nebula.admin.client.request.mgmt.GetInstancesRequest;
import org.nebula.admin.client.request.mgmt.GetRegistrationRequest;
import org.nebula.admin.client.request.mgmt.GetRegistrationsRequest;
import org.nebula.admin.client.response.mgmt.GetHistoryEventsResponse;
import org.nebula.admin.client.response.mgmt.GetRegistrationResponse;
import org.nebula.admin.client.response.mgmt.GetRegistrationsResponse;
import org.nebula.framework.client.request.GetEventsRequest;
import org.nebula.framework.client.request.RegisterRequest;
import org.nebula.framework.client.response.GetEventsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NebulaServiceFacade {

  private final static int PAGE_SIZE = 20;

  @Value("${nebula.service.mgmt.port:80}")
  private int nebulaServiceMgmtPort;

  @Value("${nebula.service.mgmt.contextPath:}")
  private String nebulaServiceMgmtContextPath;

  public NebulaMgmtClient buildNebulaMgmtClient(String username, String secretKey, String nebulaServer){
    return new NebulaMgmtClient(username, secretKey, nebulaServer, nebulaServiceMgmtPort,
                                nebulaServiceMgmtContextPath);
  }

  public GetEventsResponse getEvents(NebulaMgmtClient nebulaMgmtClient, String instanceId,
                                     int startPage, int pageSize) throws Exception {

    GetEventsRequest request = new GetEventsRequest();
    request.setInstanceId(instanceId);
    request.setPageNo(startPage);
    request.setPageSize(pageSize);

    return nebulaMgmtClient.getEvents(request);
  }

  public GetHistoryEventsResponse getHistoryEvents(NebulaMgmtClient nebulaMgmtClient,
                                                   String instanceId, int startPage, int pageSize)
      throws Exception {

    GetHistoryEventsRequest request = new GetHistoryEventsRequest();
    request.setInstanceId(instanceId);
    request.setPageNo(startPage);
    request.setPageSize(pageSize);

    return nebulaMgmtClient.getHistoryEvents(request);
  }

  public GetRegistrationResponse getRegistration(NebulaMgmtClient nebulaMgmtClient,
                                                 String registrationId) throws Exception {

    GetRegistrationRequest request = new GetRegistrationRequest();
    request.setId(registrationId);

    return nebulaMgmtClient.getRegistration(request);
  }

  public GetRegistrationsResponse getActivityRegistrationsForUser(NebulaMgmtClient nebulaMgmtClient,
                                                                  String targetUsername, int pageNo) throws Exception {

    GetRegistrationsRequest request = new GetRegistrationsRequest();
    request.setUsername(targetUsername);
    request.setPageNo(pageNo);
    request.setPageSize(PAGE_SIZE);
    request.setNodeType(RegisterRequest.NodeType.ACTIVITY);

    return nebulaMgmtClient.getRegistrations(request);
  }

  public GetRegistrationsResponse getRegistrations(
      NebulaMgmtClient nebulaMgmtClient,  String targetUsername, String workflowName,
      int pageNo, int pageSize, RegisterRequest.NodeType nodeType) throws Exception {

    GetRegistrationsRequest request = new GetRegistrationsRequest();
    request.setUsername(targetUsername);
    request.setWorkflowName(workflowName);
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    request.setNodeType(nodeType);

    return nebulaMgmtClient.getRegistrations(request);
  }

  public int countInstances(NebulaMgmtClient nebulaMgmtClient,  String targetUser, String registrationId,
      GetInstancesRequest.SearchMode searchMode) throws Exception {

    GetInstancesRequest request = new GetInstancesRequest();
    request.setPageNo(0);
    request.setPageSize(1);
    request.setUsername(targetUser);
    request.setRegistrationId(registrationId);
    request.setSearchMode(searchMode);

    return nebulaMgmtClient.getInstances(request).getTotal();
  }
}
