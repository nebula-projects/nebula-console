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

import org.nebula.admin.client.model.nebula.RegistrationSummary;

public class Workflow {

  private RegistrationSummary registrationSummary;

  private int runningInstances;

  private int historyInstances;

  public RegistrationSummary getRegistrationSummary() {
    return registrationSummary;
  }

  public void setRegistrationSummary(RegistrationSummary registrationSummary) {
    this.registrationSummary = registrationSummary;
  }

  public int getRunningInstances() {
    return runningInstances;
  }

  public void setRunningInstances(int runningInstances) {
    this.runningInstances = runningInstances;
  }

  public int getHistoryInstances() {
    return historyInstances;
  }

  public void setHistoryInstances(int historyInstances) {
    this.historyInstances = historyInstances;
  }
}
