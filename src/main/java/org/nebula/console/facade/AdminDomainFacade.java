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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nebula.admin.client.NebulaAdminClient;
import org.nebula.admin.client.model.admin.DomainSummary;
import org.nebula.admin.client.request.admin.DeleteDomainRequest;
import org.nebula.admin.client.request.admin.GetDomainNamesRequest;
import org.nebula.admin.client.request.admin.GetDomainsRequest;
import org.nebula.admin.client.request.admin.SaveDomainRequest;
import org.nebula.admin.client.request.admin.UserDomainRequest;
import org.nebula.admin.client.response.admin.DeleteDomainResponse;
import org.nebula.admin.client.response.admin.GetDomainNamesResponse;
import org.nebula.admin.client.response.admin.GetDomainsResponse;
import org.nebula.admin.client.response.admin.SaveDomainResponse;
import org.nebula.framework.utils.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDomainFacade {

  @Autowired
  private NebulaAdminClient nebulaAdminClient;

  @Value("${nebula.user.max.domains}")
  private int userMaxDomains;

  public String selectNebulaServer(String domainName) throws Exception {

    DomainSummary domainSummary = getDomainSummary(domainName);

    if(domainSummary != null && ArrayUtils.isNotEmpty(domainSummary.getServers())) {
      return domainSummary.getServers()[0];
    }

    return null;
  }

  public DomainSummary getDomainSummary(String domainName) throws Exception {
    GetDomainsRequest request = new GetDomainsRequest();
    request.setPageNo(0);
    request.setPageSize(1);
    request.setDomainName(domainName);
    request.setExactMatch(true);

    GetDomainsResponse response = nebulaAdminClient.getDomains(request);

    return (response.getDomainSummaries() == null || response.getDomainSummaries().size() == 0)
           ? null : response.getDomainSummaries().get(0);
  }

  public List<DomainSummary> getDomainSummaries(String username) throws Exception {

    List<DomainSummary> domainSummaries = new ArrayList<DomainSummary>();

    List<String> domainNames = getDomainNamesForUser(username);

    for (String domainName : domainNames) {

      DomainSummary domainSummary = getDomainSummary(domainName);

      if (domainSummary != null) {
        domainSummaries.add(domainSummary);
      }
    }

    return domainSummaries;
  }

  public List<String> getDomainNamesForUser(String username) throws Exception {
    return getDomainNamesWithExcludedDomains(username, null, null, 0, userMaxDomains);
  }

  public List<String> getDomainNamesWithExcludedDomains(String username, String domainName,
                                     List<String> excludedDomains, int pageNo, int pageSize)
      throws Exception {
    GetDomainNamesRequest request = new GetDomainNamesRequest();
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    request.setUsername(username);
    request.setDomainName(domainName);
    request.setExcludedDomains(excludedDomains);

    GetDomainNamesResponse response = nebulaAdminClient.getDomainNames(request);

    return response.getNames();
  }


  public int countAllDomains() throws Exception {
    GetDomainsRequest request = new GetDomainsRequest();
    request.setPageNo(0);
    request.setPageSize(1);

    return nebulaAdminClient.getDomains(request).getTotal();
  }

  public GetDomainsResponse getDomains(String domainName, int startPage, int pageSize,
                                       boolean exactMatch) throws Exception {
    GetDomainsRequest request = new GetDomainsRequest();
    request.setPageNo(startPage);
    request.setPageSize(pageSize);
    request.setDomainName(domainName);
    request.setExactMatch(exactMatch);

    return nebulaAdminClient.getDomains(request);
  }

  public DeleteDomainResponse deleteDomain(String domainName) throws Exception {

    DeleteDomainRequest request = new DeleteDomainRequest();
    request.setDomainName(domainName);

    return nebulaAdminClient.deleteDomain(request);
  }

  public SaveDomainResponse saveDomain(DomainSummary domainSummary) throws Exception {

    SaveDomainRequest request = new SaveDomainRequest();
    request.setDomainSummary(domainSummary);

    return nebulaAdminClient.saveDomain(request);
  }


}
