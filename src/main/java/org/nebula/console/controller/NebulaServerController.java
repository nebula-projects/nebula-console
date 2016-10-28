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

import org.nebula.admin.client.model.admin.NebulaServerSummary;
import org.nebula.admin.client.response.admin.GetNebulaServersResponse;
import org.nebula.console.exception.AjaxException;
import org.nebula.console.service.NebulaServerService;
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

import java.util.List;

@Controller
public class NebulaServerController {

  private final static Logger logger = LoggerFactory.getLogger(NebulaServerController.class);

  @Autowired
  private NebulaServerService nebulaServerService;

  @RequestMapping(value = "/nebulaservers", method = RequestMethod.GET)
  public String get(String nebulaServerHost, Pagination pagination, ModelMap model)
      throws Exception {

    logger.info("get nebula servers request: host {}, pagination {}", nebulaServerHost,
                JsonUtils.toJson(pagination));

    return list(nebulaServerHost, pagination, model);
  }

  @RequestMapping(value = "/nebulaservers", method = RequestMethod.POST)
  public String search(String nebulaServerHost, Pagination pagination, ModelMap model)
      throws Exception {

    logger.info("search nebula server request: host {}, pagination: {}", nebulaServerHost,
                JsonUtils.toJson(pagination));

    return list(nebulaServerHost, pagination, model);
  }

  private String list(String nebulaServerHost, Pagination pagination, ModelMap model)
      throws Exception {

    GetNebulaServersResponse response = nebulaServerService.searchNebulaServers(
        nebulaServerHost, pagination.getStart(), pagination.getPageSize());

    logger.info("list nebula servers total:{}, size: {}", response.getTotal(),
                response.getNebulaServerSummaries().size());

    pagination.setTotal((long) response.getTotal());

    model.addAttribute("pagination", pagination);
    model.addAttribute("nebulaservers", response.getNebulaServerSummaries());
    model.addAttribute("host", nebulaServerHost);

    return "viewNebulaServers";
  }

  @ResponseBody
  @RequestMapping(value = "/nebulaserver/{host}/", method = RequestMethod.DELETE)
  public String delete(@PathVariable("host") String host) throws Exception {

    logger.info("delete nebulaserver {}", host);

    try {
      nebulaServerService.delete(host);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/nebulaserver/{host}/", method = RequestMethod.GET)
  public String view(@PathVariable("host") String nebulaServerHost, ModelMap model)
      throws Exception {

    logger.info("view nebulaserver {}", nebulaServerHost);

    NebulaServerSummary nebulaServerSummary = nebulaServerService.getNebulaServer(nebulaServerHost);

    if (nebulaServerSummary != null) {
      model.addAttribute("nebulaserver", nebulaServerSummary);
    }

    return "viewNebulaServer";
  }

  @RequestMapping(value = "/nebulaserver", method = RequestMethod.POST)
  @ResponseBody
  public String save(NebulaServerSummary nebulaServerSummary) throws Exception {

    logger.info("save Nebula {}", JsonUtils.toJson(nebulaServerSummary));

    try {

      nebulaServerService.saveNebulaServer(nebulaServerSummary);

    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @ResponseBody
  @RequestMapping(value = "/nebulaservers/search", method = RequestMethod.GET)
  public String searchNebulaServersForDomain(String nebulaServerHost) throws Exception {

    logger.info("search nebula server {} for domain.", nebulaServerHost);

    try {
      List<String> neublaHosts = nebulaServerService.searchUnusedNebulaServers(nebulaServerHost);

      logger.info("domains: {}", JsonUtils.toJson(neublaHosts));

      return new SearchResult(neublaHosts).getResult();
    } catch (Exception e) {
      throw new AjaxException(e);
    }

  }

}
