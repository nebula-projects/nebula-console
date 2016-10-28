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
import org.nebula.admin.client.model.admin.UserSummary;
import org.nebula.admin.client.request.admin.ChangePasswordRequest;
import org.nebula.admin.client.request.admin.ChangeSecretKeyRequest;
import org.nebula.admin.client.request.admin.DeleteUserRequest;
import org.nebula.admin.client.request.admin.GetSecretKeyRequest;
import org.nebula.admin.client.request.admin.GetUsersRequest;
import org.nebula.admin.client.request.admin.SaveUserRequest;
import org.nebula.admin.client.response.admin.GetSecretKeyResponse;
import org.nebula.admin.client.response.admin.GetUsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminUserFacade {

  @Autowired
  private NebulaAdminClient nebulaAdminClient;

  public void saveUser(UserSummary userSummary) throws Exception {
    SaveUserRequest request = new SaveUserRequest();

    request.setUserSummary(userSummary);

    nebulaAdminClient.saveUser(request);
  }

  public boolean isAdmin(String username) {
    return username.equals("admin");
  }

  public int countAllUsers() throws Exception {
    GetUsersRequest request = new GetUsersRequest();
    request.setPageNo(0);
    request.setPageSize(1);
    request.setExactMatch(false);

    return nebulaAdminClient.getUsers(request).getTotal();
  }

  public UserSummary getUser(String username) throws Exception {
    GetUsersRequest request = new GetUsersRequest();
    request.setPageNo(0);
    request.setPageSize(1);
    request.setName(username);
    request.setExactMatch(true);

    GetUsersResponse response = nebulaAdminClient.getUsers(request);

    if (response.getUserSummaries().size() > 0) {
      return response.getUserSummaries().get(0);
    }
    return null;
  }

  public GetUsersResponse getUsers(String username, boolean exactMatch, int pageNo, int pageSize)
      throws Exception {

    GetUsersRequest request = new GetUsersRequest();
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    request.setName(username);
    request.setExactMatch(exactMatch);

    return nebulaAdminClient.getUsers(request);

  }

  public void deleteUser(String username) throws Exception {
    DeleteUserRequest request = new DeleteUserRequest();
    request.setName(username);

    nebulaAdminClient.deleteUser(request);
  }

  public void changePassword(String username, String oldPassword, String newPassword)
      throws Exception {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setUsername(username);
    request.setOldPassword(oldPassword);
    request.setNewPassword(newPassword);

    nebulaAdminClient.changePassword(request);
  }

  public String getSecretKey(String username)
      throws Exception {
    GetSecretKeyRequest request = new GetSecretKeyRequest();
    request.setUsername(username);

    GetSecretKeyResponse response = nebulaAdminClient.getSecretKey(request);

    return response.getSecretKey();
  }

  public void changeSecretKey(String username, String password, String newSecretKey)
      throws Exception {
    ChangeSecretKeyRequest request = new ChangeSecretKeyRequest();
    request.setUsername(username);
    request.setPassword(password);
    request.setSecretKey(newSecretKey);

    nebulaAdminClient.changeSecretKey(request);
  }

}
