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
import org.nebula.admin.client.model.admin.NebulaServerSummary;
import org.nebula.admin.client.request.admin.DeleteNebulaServerRequest;
import org.nebula.admin.client.request.admin.GetNebulaServersRequest;
import org.nebula.admin.client.request.admin.SaveNebulaServerRequest;
import org.nebula.admin.client.response.admin.GetNebulaServersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminNebulaServerFacade {

  @Autowired
  private NebulaAdminClient nebulaAdminClient;

  public GetNebulaServersResponse getNebulaServers(String nebulaServerHost, int searchScope, boolean exactMatch,
                                                   int pageNo, int pageSize) throws Exception {

    GetNebulaServersRequest request = new GetNebulaServersRequest();
    request.setPageNo(pageNo);
    request.setPageSize(pageSize);
    request.setHost(nebulaServerHost);
    request.setSearchScope(searchScope);
    request.setExactMatch(exactMatch);

    return nebulaAdminClient.getNebulaServers(request);
  }

  public void delete(String nebulaServerHost) throws Exception {

    DeleteNebulaServerRequest request = new DeleteNebulaServerRequest();
    request.setHost(nebulaServerHost);

    nebulaAdminClient.deleteNebulaServer(request);
  }

  public void save(NebulaServerSummary nebulaServerSummary) throws Exception {

    SaveNebulaServerRequest request = new SaveNebulaServerRequest();
    request.setNebulaServerSummary(nebulaServerSummary);

    nebulaAdminClient.saveNebulaServer(request);
  }


}
