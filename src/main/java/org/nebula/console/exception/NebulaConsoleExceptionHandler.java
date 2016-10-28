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

package org.nebula.console.exception;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;

@ControllerAdvice
public class NebulaConsoleExceptionHandler {

  private final static Logger logger = Logger.getLogger(NebulaConsoleExceptionHandler.class);

  @ExceptionHandler(AjaxException.class)
  @ResponseBody
  public String handleAjaxException(AjaxException e) {

    logger.error("Failed to process the request.", e);

    //Don't return the stacktrace to web.
    return e.getMessage();
  }

  @ExceptionHandler(Exception.class)
  public String exception(Exception e) {
    logger.error("Failed to process the request.", e);
    return "error";
  }

}

