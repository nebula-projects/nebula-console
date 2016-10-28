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

import org.apache.commons.lang.StringUtils;
import org.nebula.admin.client.model.admin.NebulaServerSummary;
import org.nebula.admin.client.request.admin.GetNebulaServersRequest;
import org.nebula.admin.client.response.admin.GetNebulaServersResponse;
import org.nebula.console.facade.AdminNebulaServerFacade;
import org.nebula.framework.utils.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NebulaServerService {

  @Autowired
  private AdminNebulaServerFacade adminNebulaServerFacade;

  public GetNebulaServersResponse searchNebulaServers(String nebulaServerHost, int startPage, int pageSize) throws Exception{
    return adminNebulaServerFacade.getNebulaServers(
        nebulaServerHost, GetNebulaServersRequest.SEARCH_SCOPE_ALL, false, startPage,
        pageSize);
  }

  public List<String> searchUnusedNebulaServers(String nebulaServerHost) throws Exception{

    return getNebulaServerHosts(adminNebulaServerFacade.getNebulaServers(
        nebulaServerHost, GetNebulaServersRequest.SEARCH_SCOPE_UNUSED, false, 0, 10)
                                    .getNebulaServerSummaries());
  }

  public NebulaServerSummary getNebulaServer(String nebulaServerHost) throws Exception{

    GetNebulaServersResponse response = adminNebulaServerFacade.getNebulaServers(
        nebulaServerHost, GetNebulaServersRequest.SEARCH_SCOPE_ALL, true, 0, 1);

    return response.getNebulaServerSummaries().size() ==0? null : response.getNebulaServerSummaries().get(0);
  }

  public void delete(String nebulaServerHost) throws Exception {
    adminNebulaServerFacade.delete(nebulaServerHost);
  }

  public void saveNebulaServer(NebulaServerSummary nebulaServerSummary) throws Exception {
    validate(nebulaServerSummary);
    adminNebulaServerFacade.save(nebulaServerSummary);
  }

  private List<String> getNebulaServerHosts(List<NebulaServerSummary> neublaServerSummaries) {

    List<String> neublaHosts = new ArrayList<String>();

    for (NebulaServerSummary nebulaServerSummary : neublaServerSummaries) {
      neublaHosts.add(nebulaServerSummary.getHost());
    }

    return neublaHosts;
  }

  private void validate(NebulaServerSummary nebulaServerSummary) {

    Validate.notEmpty(nebulaServerSummary.getHost(), "The host can't be empty.");
    Validate.isTrue(nebulaServerSummary.getHost().length() <= 255,
                    "The length of host can't exceed 255.");

    if (StringUtils.isNotBlank(nebulaServerSummary.getDescription())) {
      Validate.isTrue(nebulaServerSummary.getDescription().length() <= 255,
                      "The length of description can't exceed 255.");
    }
  }
}