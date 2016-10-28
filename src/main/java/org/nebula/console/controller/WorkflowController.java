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

import org.nebula.admin.client.response.mgmt.GetRegistrationResponse;
import org.nebula.console.service.WorkflowService;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.nebula.framework.utils.JsonUtils.toJson;

@Controller
public class WorkflowController {

  private final static Logger logger = LoggerFactory.getLogger(WorkflowController.class);

  @Autowired
  private WorkflowService workflowService;

  @RequestMapping(value = "/workflows", method = RequestMethod.GET)
  public String get() {

    logger.info("get workflow request");

    return "viewWorkflows";
  }

  @RequestMapping(value = "/workflows", method = RequestMethod.POST)
  public String search(String username, String workflowname, Pagination pagination, ModelMap model)
      throws Exception {

    logger.info("search workflow request: username {}, workflow {}, pagination: {}", username,
                workflowname, JsonUtils.toJson(pagination));

    try {
      Workflows
          workflows =
          workflowService.searchWorkflows(username, workflowname, pagination.getCurrentPage(),
                                          pagination.getPageSize());

      if (workflows.getWorkflows().size() > 0) {

        pagination.setTotal(Long.valueOf(workflows.getTotal()));
        model.addAttribute("pagination", pagination);
        model.addAttribute("workflows", workflows.getWorkflows());
        model.addAttribute("workflowname", workflowname);
        model.addAttribute("username", username);
      }
    } catch (Exception e) {
      logger.error("Failed to search workflow for user " + username + ", workflow " + workflowname,
                   e);
    }

    return "viewWorkflows";
  }

  @RequestMapping(value = "/workflow/{id}", method = RequestMethod.GET)
  public String viewWorkflow(@PathVariable("id") String workflowId, ModelMap model) {

    logger.info("view workflow {}", workflowId);

    try {

      WorkflowService.WorkflowManager
          workflowManager = workflowService.createWorkflowManager(workflowId);

      GetRegistrationResponse workflowRegistration = workflowManager.getWorkflowRegistration();
      List<Registration> activityRegistrations = workflowManager.getActivityRegistrations();

      logger.info("WorkflowRegistration {}", toJson(workflowRegistration));
      logger.info("ActivityRegistrations {}", toJson(activityRegistrations));

      model.addAttribute("workflow", workflowRegistration);
      model.addAttribute("activities", activityRegistrations);

    } catch (Exception e) {
      logger.error("Failed to view workflow for Id " + workflowId, e);
    }

    return "viewWorkflow";
  }

}
