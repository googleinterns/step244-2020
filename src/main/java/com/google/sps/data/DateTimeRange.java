// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

public final class DateTimeRange {
  private final String startDate; // yyyy-mm-dd
  private final String endDate;  
  private final String startTime; // hh:mm
  private final String endTime;

  public DateTimeRange(String startDate, String endDate, String startTime, String endTime) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public DateTimeRange(String date, String time) {
    this.startDate = date;
    this.endDate = date;
    this.startTime = time;
    this.endTime = time;
  }

  public Boolean isDateSet() {
    return startDate != null && startDate.equals(endDate);
  }

  public Boolean isTimeSet() {
    return startTime != null && startTime.equals(endTime);
  }

  public Boolean isDateTimeSet() {
    return isDateSet() && isTimeSet();
  }

  public String getDate() {
    return isDateSet() ? startDate : null;
  }

  public String getStartDate() {
    return startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public String getTime() {
    return isTimeSet() ? startTime : null;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public String convertStartTimeToString() {
    return this.startDate + "T" + this.startTime + ":00Z";
  }

  public String convertEndTimeToString() {
    return this.endDate + "T" + this.endTime + ":00Z";
  }
}
