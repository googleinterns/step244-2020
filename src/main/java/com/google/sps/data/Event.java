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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.gson.Gson;

public class Event {
  private final String id;
  private String gcalendarId;
  private final String title;
  private final String description;
  private final String category;
  private final List<String> tags = new ArrayList<String>();
  private final DateTimeRange dateTimeRange;
  private final Long duration; // in minutes
  private final String location;
  private final List<String> links = new ArrayList<String>();
  private final Map<String, String> fields = new HashMap<String, String>();
  private final String ownerId;
  private List<String> invitedUsersId = new ArrayList<String>();
  private List<String> joinedUsersId = new ArrayList<String>();
  private List<String> declinedUsersId = new ArrayList<String>();


  public Event(String id, String gcalendarId, 
               String title, String description, String category, List<String> tags, 
               DateTimeRange dateTimeRange, Long duration,
               String location, List<String> links, Map<String, String> fields, 
               String ownerId, List<String> invitedUsersId, List<String> joinedUsersId, List<String> declinedUsersId) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.gcalendarId = gcalendarId;
    this.title = Objects.requireNonNull(title, "title cannot be null");
    this.description = Objects.requireNonNull(description, "description cannot be null");
    this.category = Objects.requireNonNull(category, "category cannot be null");
    this.tags.addAll(Objects.requireNonNull(tags, "tags cannot be null"));
    this.dateTimeRange = dateTimeRange;
    this.duration = duration;
    this.location = Objects.requireNonNull(location, "location cannot be null");
    this.links.addAll(Objects.requireNonNull(links, "links cannot be null"));
    this.fields.putAll(Objects.requireNonNull(fields, "fields cannot be null"));
    this.ownerId = Objects.requireNonNull(ownerId, "ownerId cannot be null");
    this.invitedUsersId.addAll(Objects.requireNonNull(invitedUsersId, "invitedUsersId cannot be null"));
    this.joinedUsersId.addAll(Objects.requireNonNull(joinedUsersId, "joinedUsersId cannot be null"));
    this.declinedUsersId.addAll(Objects.requireNonNull(declinedUsersId, "declinedUsersId cannot be null"));
  }

  public String getID() {
    return id;
  }

  public String getGCalendarID() {
    return gcalendarId;
  }

  public void setGCalendarID(String newGCalendarId) {
    this.gcalendarId = newGCalendarId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public List<String> getTags() {
    return tags;
  }

  public Boolean isDateTimeSet() {
    return dateTimeRange != null && dateTimeRange.isDateTimeSet();
  }

  public String getDate() {
    return dateTimeRange != null ? dateTimeRange.getDate() : null;
  }

  public String getTime() {
    return dateTimeRange != null ? dateTimeRange.getTime() : null;
  }

  public String getDateTimeAsString() { // Convert DateTime to UTC String
    if (dateTimeRange != null && dateTimeRange.isDateTimeSet()) {
      return dateTimeRange.getDate() + "T" + dateTimeRange.getTime() + ":00Z";
    }
    return null;
  }

  public String getDateTimeRangeAsJSON() { // Convert fields of DateTimeRange to gson string
    return new Gson().toJson(dateTimeRange);
  }

  public Long getDuration() {
    return duration;
  }

  public String getLocation() {
    return location;
  }

  public List<String> getLinks() {
    return links;
  }

  public Map<String, String> getFields() { // Convert fields map to gson string
    return fields;
  }

  public String getFieldsAsJSON() { // Convert fields map to gson string
    return new Gson().toJson(fields);
  }

  public String getOwnerID() {
    return ownerId;
  }

  public List<String> getInvitedIDs() {
    return invitedUsersId;
  }

  public List<String> getJoinedIDs() {
    return joinedUsersId;
  }

  public List<String> getDeclinedIDs() {
    return declinedUsersId;
  }

  public void joinEvent(String userId) {
    if (invitedUsersId.contains(userId)) {
      this.invitedUsersId.remove(userId);
      this.joinedUsersId.add(userId);
    }
  }
}
