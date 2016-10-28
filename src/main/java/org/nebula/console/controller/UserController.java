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

import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.response.admin.GetUsersResponse;
import org.nebula.console.exception.AjaxException;
import org.nebula.console.service.DomainService;
import org.nebula.console.service.UserService;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.framework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.nebula.framework.utils.JsonUtils.toJson;

@Controller
public class UserController {

  private final static Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private DomainService domainService;

  @RequestMapping(value = "/user/profile", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
  @ResponseBody
  public String getProfile() throws Exception {

    String currentUsername = ContextHolderUtil.getCurrentUsername();

    logger.info("get user {}'s profile.", currentUsername);

    return toJson(getUserSummary(currentUsername));
  }

  @RequestMapping(value = "/user/accessCredentials", method = RequestMethod.GET, produces = "text/plain;charset=utf-8")
  @ResponseBody
  public String getAccessCredentails() throws Exception {

    String currentUsername = ContextHolderUtil.getCurrentUsername();

    logger.info("get user {}'s profile.", currentUsername);

    UserSummary userSummary = getUserSummary(currentUsername);

    userSummary.setSecretKey(userService.getSecretKey(currentUsername));

    return toJson(userSummary);
  }

  private UserSummary getUserSummary(String username) {
    try {
      UserSummary userSummary = userService.getUser(username);

      if (userSummary == null) {
        throw new AjaxException("The user " + username + " doesn't exist.");
      }
      return userSummary;

    } catch (Exception e) {
      throw new AjaxException("error", e);
    }
  }

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public String get(String username, Pagination pagination, ModelMap model) throws Exception {

    logger.info("get users request: pagination {}", JsonUtils.toJson(pagination));

    return list(username, pagination, model);
  }

  @RequestMapping(value = "/users/search", method = RequestMethod.POST)
  public String search(String username, Pagination pagination, ModelMap model) throws Exception {

    logger.info("search user request: username {}, pagination: {}", username,
                JsonUtils.toJson(pagination));

    return list(username, pagination, model);
  }

  private String list(String username, Pagination pagination, ModelMap model) throws Exception {
    logger.info("list user request: username {}, pagination: {}", username,
                JsonUtils.toJson(pagination));

    GetUsersResponse
        response = userService.searchUsers(username, pagination.getStart(),
                                           pagination.getPageSize());

    logger.info("list users total:{}, size: {}", response.getTotal(),
                response.getUserSummaries().size());

    pagination.setTotal((long) response.getTotal());
    model.addAttribute("pagination", pagination);
    model.addAttribute("users", response.getUserSummaries());
    model.addAttribute("username", username);

    return "viewUsers";
  }

  @ResponseBody
  @RequestMapping(value = "/users/{name}", method = RequestMethod.DELETE)
  public String delete(@PathVariable("name") String name) {

    logger.info("delete user {}", name);

    try {
      userService.deleteUser(name);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/users/{username}", method = RequestMethod.GET)
  public String viewUser(@PathVariable("username") String username, ModelMap model)
      throws Exception {

    logger.info("view user {}", username);

    UserSummary userSummary = userService.getUser(username);

    if (userSummary != null) {
      model.addAttribute("user", userSummary);

      List<String> domains = domainService.getDomainNamesForUser(username);

      model.addAttribute("myDomains", domains);
    }

    return "viewUser";
  }

  @RequestMapping(value = "/user", method = RequestMethod.POST)
  @ResponseBody
  public String saveUser(UserSummary userSummary) throws Exception {

    logger.info("save user {}", JsonUtils.toJson(userSummary));

    try {
      userService.saveUser(userSummary);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/user/password", method = RequestMethod.POST)
  @ResponseBody
  public String changePassword(@RequestParam("oldPassword") String oldPassword,
                               @RequestParam("password") String password) throws Exception {

    String username = ContextHolderUtil.getCurrentUsername();

    logger.info("user {} change password", username);

    try {
      userService.changePassword(username, oldPassword, password);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @RequestMapping(value = "/user/accessCredentials", method = RequestMethod.POST)
  @ResponseBody
  public String changeSecretKey(@RequestParam("secretKey") String secretKey,
                               @RequestParam("password") String password) throws Exception {

    String username = ContextHolderUtil.getCurrentUsername();

    logger.info("user {} change secret key", username);

    try {
      userService.changeSecretKey(username, password, secretKey);

    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

  @ResponseBody
  @RequestMapping(value = "/users/{username}/domains", method = RequestMethod.GET)
  public String findUserNotOwnedDomains(@PathVariable("username") String username,
                                        String domainNameForQuery)
      throws Exception {

    logger.info("user {} search domain {}", username, domainNameForQuery);

    try {

      List<String>
          domainNames = domainService.findUserNotOwnedDomains(username, domainNameForQuery);

      logger.info("domains: {}", JsonUtils.toJson(domainNames));

      return new SearchResult(domainNames).getResult();

    } catch (Exception e) {
      throw new AjaxException(e);
    }
  }

  @ResponseBody
  @RequestMapping(value = "/users/{username}/domain/{domainName}", method = RequestMethod.POST)
  public String addDomainForUser(@PathVariable("username") String username,
                                 @PathVariable("domainName") String domainName) throws Exception {

    logger.info("addDomainForUser user {}, domain {}", username, domainName);

    try {
      userService.addDomainForUser(username, domainName);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    logger.info("addDomainForUser completed");

    return domainName;
  }

  @ResponseBody
  @RequestMapping(value = "/users/{username}/domain/{domainName}", method = RequestMethod.DELETE)
  public String removeDomainFromUser(@PathVariable("username") String username,
                                     @PathVariable("domainName") String domainName)
      throws Exception {
    logger.info("removeDomainFromUser user {}, domain {}", username, domainName);

    try {
      userService.removeDomainFromUser(username, domainName);
    } catch (Exception e) {
      throw new AjaxException(e);
    }

    return "success";
  }

}
