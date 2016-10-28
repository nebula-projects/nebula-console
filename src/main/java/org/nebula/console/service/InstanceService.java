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
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.response.mgmt.GetHistoryEventsResponse;
import org.nebula.admin.client.response.mgmt.GetRegistrationResponse;
import org.nebula.console.facade.AdminDomainFacade;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.facade.NebulaServiceFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.framework.client.response.GetEventsResponse;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {

  private final static Logger logger = LoggerFactory.getLogger(InstanceService.class);

  @Autowired
  private AdminUserFacade adminUserFacade;

  @Autowired
  private AdminDomainFacade adminDomainFacade;

  @Autowired
  private NebulaServiceFacade nebulaServiceFacade;

  public GetEventsResponse search(String instanceId, Integer currentPage, Integer pageSize)
      throws Exception {

    String nebulaServer = getNebulaServer(instanceId);

    if (nebulaServer == null) {
      return new GetEventsResponse();
    }

    String currentUsername = ContextHolderUtil.getCurrentUsername();

    String secretKey = adminUserFacade.getSecretKey(currentUsername);

    NebulaMgmtClient
        nebulaMgmtClient =
        nebulaServiceFacade.buildNebulaMgmtClient(
            currentUsername, secretKey, nebulaServer);

    GetEventsResponse
        response = getEvents(nebulaMgmtClient, instanceId, currentPage,
                             pageSize);

    boolean isAdmin = adminUserFacade.isAdmin(currentUsername);
    if (!isAdmin) {
      if (!DoesUserOwnTheRegistration(nebulaMgmtClient, response)) {
        response.setTotal(0);
        response.setEvents(null);
      }
    }

    logger.info("list response:" + JsonUtils.toJson(response));

    return response;
  }

  private boolean DoesUserOwnTheRegistration(NebulaMgmtClient nebulaMgmtClient,
                                             GetEventsResponse response) throws Exception {
    if (response.getEvents().size() > 0) {
      GetRegistrationResponse
          registrationResponse =
          nebulaServiceFacade.getRegistration(nebulaMgmtClient, response.getEvents().get(0)
              .getRegistrationId());
      return registrationResponse.getId() != null;
    }
    return false;
  }


  private GetEventsResponse getEvents(NebulaMgmtClient nebulaMgmtClient, String instanceId,
                                      int currentPage, int pageSize) throws Exception {

    GetEventsResponse
        response =
        nebulaServiceFacade.getEvents(nebulaMgmtClient, instanceId, currentPage,
                                      pageSize);

    if (response.getEvents().size() == 0) {
      GetHistoryEventsResponse
          getHistoryEventsResponse =
          nebulaServiceFacade.getHistoryEvents(nebulaMgmtClient, instanceId, currentPage,
                                               pageSize);
      BeanUtils.copyProperties(getHistoryEventsResponse, response);
    }

    return response;
  }

  private String getNebulaServer(String registrationId) throws Exception {

    String domainName = registrationId.split("-")[0];

    return adminDomainFacade.selectNebulaServer(domainName);
  }

}