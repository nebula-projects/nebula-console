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

import com.fasterxml.jackson.annotation.JsonValue;

import org.nebula.framework.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

  private Result result = new Result();

  public SearchResult(List<String> results) {

    List<ValueData> valueDataList = new ArrayList<ValueData>();
    for (String result : results) {
      ValueData vd = new ValueData();
      vd.setData(result);
      vd.setValue(result);
      valueDataList.add(vd);
    }

    Suggestions suggestions = new Suggestions();

    suggestions.setValueData(valueDataList);
    result.setSuggestions(suggestions);
  }

  public String getResult() {
    return JsonUtils.toJson(result);
  }

  static class Suggestions {

    List<ValueData> valueData;

    @JsonValue
    public List<ValueData> getValueData() {
      return valueData;
    }

    public void setValueData(List<ValueData> valueData) {
      this.valueData = valueData;
    }
  }

  class Result {

    private Suggestions suggestions;

    public Suggestions getSuggestions() {
      return suggestions;
    }

    public void setSuggestions(Suggestions suggestions) {
      this.suggestions = suggestions;
    }
  }

  class ValueData {

    private String value;
    private String data;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }
  }


}
