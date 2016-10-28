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

import org.nebula.admin.client.NebulaAdminClient;
import org.nebula.admin.client.request.admin.GetUserDomainsRequest;
import org.nebula.admin.client.request.admin.UserDomainRequest;
import org.nebula.admin.client.response.admin.GetUserDomainsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDomainFacade {

  @Autowired
  private NebulaAdminClient nebulaAdminClient;

  public int getUserNumbersForDomain(String domainName) throws Exception{

    GetUserDomainsRequest request = new GetUserDomainsRequest();
    request.setDomainName(domainName);

    GetUserDomainsResponse response = nebulaAdminClient.getUserDomains(request);

    return response.getTotal();
  }

  public void addDomainForUser(String username, String domainName) throws Exception {
    UserDomainRequest request = new UserDomainRequest();
    request.setUsername(username);
    request.setDomainName(domainName);

    nebulaAdminClient.addDomainForUser(request);
  }

  public void removeDomainFromUser(String username, String domainName) throws Exception {
    UserDomainRequest request = new UserDomainRequest();
    request.setUsername(username);
    request.setDomainName(domainName);

    nebulaAdminClient.removeDomainFromUser(request);
  }
}