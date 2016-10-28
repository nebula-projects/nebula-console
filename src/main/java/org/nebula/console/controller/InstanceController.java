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

import org.apache.commons.lang.StringUtils;
import org.nebula.console.service.InstanceService;
import org.nebula.framework.client.response.GetEventsResponse;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class InstanceController {

  private final static Logger logger = LoggerFactory.getLogger(InstanceController.class);

  @Autowired
  private InstanceService instanceService;

  @RequestMapping(value = "/instance", method = RequestMethod.GET)
  public String get(String instanceId, Pagination pagination, ModelMap model) throws Exception {

    logger.info("get instance {} with pagination {}", instanceId, JsonUtils.toJson(pagination));

    return list(instanceId, pagination, model);
  }

  @RequestMapping(value = "/instance", method = RequestMethod.POST)
  public String search(String instanceId, Pagination pagination, ModelMap model) throws Exception {
    logger.info("search instance {} with pagination {}", instanceId, JsonUtils.toJson(pagination));

    return list(instanceId, pagination, model);
  }

  private String list(String instanceId, Pagination pagination, ModelMap model) throws Exception {

    if (StringUtils.isBlank(instanceId)) {
      return "viewInstance";
    }

    GetEventsResponse
        response =
        instanceService.search(instanceId, pagination.getCurrentPage(), pagination.getPageSize());

    pagination.setTotal(Long.valueOf(response.getTotal()));
    model.addAttribute("pagination", pagination);
    model.addAttribute("events", response.getEvents());
    model.addAttribute("instanceId", instanceId);

    return "viewInstance";
  }
}
