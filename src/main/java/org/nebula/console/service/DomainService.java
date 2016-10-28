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
import org.nebula.admin.client.model.admin.DomainSummary;
import org.nebula.admin.client.response.admin.DeleteDomainResponse;
import org.nebula.admin.client.response.admin.GetDomainsResponse;
import org.nebula.admin.client.response.admin.SaveDomainResponse;
import org.nebula.console.facade.AdminDomainFacade;
import org.nebula.framework.utils.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DomainService {

  @Autowired
  private AdminDomainFacade adminDomainFacade;

  public GetDomainsResponse searchDomains(String domainName, int startPage, int pageSize) throws Exception {
    return adminDomainFacade.getDomains(domainName, startPage, pageSize, false);
  }

  public DomainSummary getDomain(String domainName) throws Exception {
    GetDomainsResponse response = adminDomainFacade.getDomains(domainName, 0, 1, true);

    return response.getDomainSummaries().size()==0? null : response.getDomainSummaries().get(0);
  }

  public DeleteDomainResponse deleteDomain(String domainName) throws Exception {
    return adminDomainFacade.deleteDomain(domainName);
  }

  public SaveDomainResponse saveDomain(DomainSummary domainSummary) throws Exception {

    validate(domainSummary);

    return adminDomainFacade.saveDomain(domainSummary);
  }

  public List<String> getDomainNamesForUser(String username) throws Exception{
    return adminDomainFacade.getDomainNamesForUser(username);
  }

  public List<String> findUserNotOwnedDomains(String username, String domainNameForSearch) throws Exception{

    List<String> myOwnedDomains = getDomainNamesForUser(username);

    return adminDomainFacade.getDomainNamesWithExcludedDomains(null, domainNameForSearch,
                                                               myOwnedDomains.isEmpty() ? null
                                                                                        : myOwnedDomains,
                                                               0, 10);
  }

  private void validate(DomainSummary domainSummary) {

    Validate.notEmpty(domainSummary.getName(), "The domain name must exist.");
    Validate.isTrue(domainSummary.getName().length() <= 10,
                    "The length of domain name can't exceed 10.");

    Validate.notEmpty(domainSummary.getDbUrl(), "The dbUrl must exist.");
    Validate
        .isTrue(domainSummary.getDbUrl().length() <= 100, "The length of DB URL can't exceed 100.");

    Validate.notEmpty(domainSummary.getRedisHost(), "The redis host must exist.");
    Validate.isTrue(domainSummary.getRedisHost().length() <= 100,
                    "The length of redis host can't exceed 100.");

    Validate.isTrue(domainSummary.getRedisPort() <= 65535, "The redis port can't exceed 65535.");

    if (StringUtils.isNotBlank(domainSummary.getDescription())) {
      Validate.isTrue(domainSummary.getRedisHost().length() <= 200,
                      "The length of description can't exceed 200.");
    }

    Validate.isTrue(domainSummary.getStatus() == DomainSummary.ENABLED
                    || domainSummary.getStatus() == DomainSummary.DISABLED,
                    "The status of domain must be 1 - enabled or 0 - disabled.");
  }
}