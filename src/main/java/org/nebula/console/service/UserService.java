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

import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.response.admin.GetUsersResponse;
import org.nebula.console.facade.AdminUserFacade;
import org.nebula.console.facade.AdminUserDomainFacade;
import org.nebula.console.util.ContextHolderUtil;
import org.nebula.framework.utils.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private AdminUserFacade adminUserFacade;

  @Autowired
  private AdminUserDomainFacade adminUserDomainFacade;

  private final static String ADMIN_USER_NAME = "admin";

  public static boolean isAdmin(String username) {
    return username.equals(ADMIN_USER_NAME);
  }

  public UserSummary getUser(String username) throws Exception{
    GetUsersResponse response = adminUserFacade.getUsers(username, true, 0, 1);

    return response.getUserSummaries().size() ==0?  null : response.getUserSummaries().get(0);

  }

  public GetUsersResponse searchUsers(String username, int startPage, int pageSize) throws Exception {
    return adminUserFacade.getUsers(username, false, startPage, pageSize);
  }

  public void deleteUser(String username) throws Exception {
    adminUserFacade.deleteUser(username);
  }

  public void addDomainForUser(String username, String domainName) throws Exception {
    adminUserDomainFacade.addDomainForUser(username, domainName);
  }

  public void removeDomainFromUser(String username, String domainName) throws Exception {
    adminUserDomainFacade.removeDomainFromUser(username, domainName);
  }

  public String getSecretKey(String username) throws Exception{
    return adminUserFacade.getSecretKey(username);
  }

  public void signup(UserSummary userSummary) throws Exception {

    validateParameters(userSummary);

    validateExists(userSummary.getUsername());

    userSummary.setStatus(UserSummary.ENABLED);
    userSummary.setAdmin(false);

    adminUserFacade.saveUser(userSummary);
  }

  public void changePassword(String username, String oldPassword, String newPassword) throws Exception{
    adminUserFacade.changePassword(username, oldPassword, newPassword);
  }

  public void changeSecretKey(String username, String password, String newSecretKey) throws Exception{
    adminUserFacade.changeSecretKey(username, password, newSecretKey);
  }

  public void saveUser(UserSummary userSummary) throws Exception{

    validateIllegalChange(userSummary);

    adminUserFacade.saveUser(userSummary);
  }

  private void validateIllegalChange(UserSummary userSummary) throws Exception {

    String username = ContextHolderUtil.getCurrentUsername();

    UserSummary oldUserSummary = getUser(username);

    if(oldUserSummary==null) {
      throw new IllegalArgumentException("The user " + username + " doesn't exist.");
    }

    validateAdminFlagNotChanged(oldUserSummary, userSummary);

    validateUserIdNotChanged(oldUserSummary, userSummary);

    validateUsernameNotChanged(oldUserSummary, userSummary);

    if(!isAdmin(username)) {
      validateStatusNotChanged(oldUserSummary, userSummary);
    }
  }

  private void validateAdminFlagNotChanged(UserSummary oldUserSummary, UserSummary userSummary) {
    if(oldUserSummary.isAdmin() != userSummary.isAdmin()) {
      throw new IllegalArgumentException("The admin flag should not be changed.");
    }
  }

  private void validateUserIdNotChanged(UserSummary oldUserSummary, UserSummary userSummary) {
    if(!oldUserSummary.getId().equals(userSummary.getId())) {
      throw new IllegalArgumentException("The User id should not be changed.");
    }
  }

  private void validateUsernameNotChanged(UserSummary oldUserSummary, UserSummary userSummary) {
    if(!oldUserSummary.getUsername().equals(userSummary.getUsername())) {
      throw new IllegalArgumentException("The username should not be changed.");
    }
  }

  private void validateStatusNotChanged(UserSummary oldUserSummary, UserSummary userSummary) {
    if(oldUserSummary.getStatus() != userSummary.getStatus()) {
      throw new IllegalArgumentException("The user status should not be changed.");
    }
  }

  private void validateParameters(UserSummary user) {

    Validate.notEmpty(user.getUsername(), "Username can't be empty");

    //user signup
    if(user.getId()==null) {
      Validate.notEmpty(user.getPassword(), "Password can't be empty");
    }
    Validate.notEmpty(user.getEmail(), "Email can't be empty");
  }

  private void validateExists(String username) throws Exception{

    if (adminUserFacade.getUser(username) != null) {
      throw new IllegalArgumentException("The user " + username + " already exists. Please change.");
    }
  }


}