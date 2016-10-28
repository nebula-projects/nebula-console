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

package org.nebula.console.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

  public static Date todayMiddleNight() {

    GregorianCalendar now = new GregorianCalendar();
    now.set(Calendar.HOUR_OF_DAY, 23);
    now.set(Calendar.MINUTE, 59);
    now.set(Calendar.SECOND, 59);
    now.set(Calendar.MILLISECOND, 999);

    return now.getTime();
  }

  public static Date beforeToday(int days) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(todayMiddleNight());
    calendar.add(Calendar.DATE, -days);
    return calendar.getTime();
  }
}
