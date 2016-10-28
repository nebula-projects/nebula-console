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

import org.nebula.admin.client.model.admin.DomainSummary;
import org.nebula.admin.client.response.admin.GetDomainsResponse;
import org.nebula.console.exception.AjaxException;
import org.nebula.console.service.DomainService;
import org.nebula.console.service.StatisticsService;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DomainController {

  private final static Logger logger = LoggerFactory.getLogger(DomainController.class);

  @Autowired
  private DomainService domainService;

  @Autowired
  private StatisticsService statisticsService;


  @RequestMapping(value = "/domains", method = RequestMethod.GET)
  public String get(String name, Pagination pagination, ModelMap model) throws Exception {

    logger.info("get domains request: pagination {}", JsonUtils.toJson(pagination));

    return list(name, pagination, model);
  }

  @RequestMapping(value = "/domains", method = RequestMethod.POST)
  public String search(String name, Pagination pagination, ModelMap model) throws Exception {

    logger
        .info("search domain request: name {}, pagination: {}", name, JsonUtils.toJson(pagination));

    return list(name, pagination, model);
  }

  private String list(String name, Pagination pagination, ModelMap model) throws Exception {
    logger.info("list domain request: name {}, pagination: {}", name, JsonUtils.toJson(pagination));

    GetDomainsResponse
        response =
        domainService.searchDomains(name, pagination.getStart(), pagination.getPageSize());

    logger.info("list domain total:{}, size: {}", response.getTotal(),
                response.getDomainSummaries().size());

    pagination.setTotal((long) response.getTotal());
    model.addAttribute("pagination", pagination);
    model.addAttribute("domains", response.getDomainSummaries());
    model.addAttribute("name", name);

    return "viewDomains";
  }

  @ResponseBody
  @RequestMapping(value = "/domain/{name}", method = RequestMethod.DELETE)
  public String delete(@PathVariable("name") String name) throws AjaxException {

    logger.info("delete domain {}", name);

    try {
      domainService.deleteDomain(name);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/domain/new", method = RequestMethod.GET)
  public String newDomain() {

    logger.info("get new domain page.");

    return "viewDomain";
  }

  @RequestMapping(value = "/domain", method = RequestMethod.POST)
  @ResponseBody
  public String addDomain(DomainSummary domainSummary) {

    logger.info("addDomain {}", JsonUtils.toJson(domainSummary));

    try {

      domainService.saveDomain(domainSummary);

    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/domain/{name}", method = RequestMethod.GET)
  public String editDomain(@PathVariable("name") String name, ModelMap model) throws Exception {

    logger.info("view domain {}", name);

    DomainSummary domainSummary = domainService.getDomain(name);

    if (domainSummary != null) {
      model.addAttribute("domain", domainSummary);
    }

    return "viewDomain";
  }

}
