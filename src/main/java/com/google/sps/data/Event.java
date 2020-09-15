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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Event {
  private String id;
  private String gcalendarId;
  private String title;
  private String description;
  private String category;
  private List<String> tags;
  private DateTimeRange dateTimeRange;
  private Long duration; // in minutes
  private String location;
  private String locationId;
  private List<String> links;
  private Map<String, String> fields;
  private String ownerId;
  private List<String> invitedUsersId = new ArrayList<>();
  private List<String> joinedUsersId = new ArrayList<>();
  private List<String> declinedUsersId = new ArrayList<>();

  private Event() {
  }

  @Override
  public boolean equals(Object other_object) {
    if (!(other_object instanceof Event))
        return false;
    Event other = (Event) other_object;
    return Objects.equals(id, other.id)
        && Objects.equals(gcalendarId, other.gcalendarId)
        && Objects.equals(title, other.title)
        && Objects.equals(description, other.description)
        && Objects.equals(category, other.category)
        && Objects.equals(tags, other.tags)
        && Objects.equals(dateTimeRange, other.dateTimeRange)
        && Objects.equals(duration, other.duration)
        && Objects.equals(location, other.location)
        && Objects.equals(links, other.links)
        && Objects.equals(fields, other.fields)
        && Objects.equals(ownerId, other.ownerId)
        && Objects.equals(invitedUsersId, other.invitedUsersId)
        && Objects.equals(joinedUsersId, other.joinedUsersId)
        && Objects.equals(declinedUsersId, other.declinedUsersId);
  }

  public static Event fromDatastoreEntity(Entity eventEntity) {
    return Event.newBuilder()
        .setID(KeyFactory.keyToString(eventEntity.getKey()))
        .setGCalendarID((String) eventEntity.getProperty("gcalendar-id"))
        .setTitle((String) eventEntity.getProperty("title"))
        .setDescription((String) eventEntity.getProperty("description"))
        .setCategory((String) eventEntity.getProperty("category"))
        .setTags((ArrayList) eventEntity.getProperty("tags"))
        .setDateTimeRange(new Gson().fromJson((String) eventEntity.getProperty("date-time-range"), DateTimeRange.class))
        .setDuration((Long) eventEntity.getProperty("duration"))
        .setLocation((String) eventEntity.getProperty("location"))
        .setLocationId((String) eventEntity.getProperty("location-id"))
        .setLinks((ArrayList) eventEntity.getProperty("links"))
        .setFields(new Gson().fromJson((String) eventEntity.getProperty("fields"), Map.class))
        .setOwnerID((String) eventEntity.getProperty("owner"))
        .setInvitedIDs((ArrayList) eventEntity.getProperty("invited-users"))
        .setJoinedIDs((ArrayList) eventEntity.getProperty("joined-users"))
        .setDeclinedIDs((ArrayList) eventEntity.getProperty("declined-users"))
        .build();
  }

  public String getID() {
    return id;
  }

  public String getGCalendarID() {
    return gcalendarId;
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

  public boolean isDateTimeSet() {
    return dateTimeRange != null && dateTimeRange.isDateTimeSet();
  }

  public String getDate() {
    return dateTimeRange != null && dateTimeRange.isDateSet() ? dateTimeRange.getDate() : null;
  }

  public DateTimeRange getDateTimeRange() {
    return this.dateTimeRange;
  }

  public String getTime() {
    return dateTimeRange != null && dateTimeRange.isTimeSet() ? dateTimeRange.getTime() : null;
  }

  public String getDateTimeAsString() { // Convert DateTime to UTC String
    return dateTimeRange != null && dateTimeRange.isDateTimeSet() ? dateTimeRange.getDate() + "T" + dateTimeRange.getTime() + ":00Z" : null;
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

  public String getLocationId() {
    return locationId;
  }

  public List<String> getLinks() {
    return links;
  }

  public Map<String, String> getFields() {
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

  public boolean joinEvent(String userId) {
    if (invitedUsersId == null)
        return false;
    if (joinedUsersId == null)
        joinedUsersId = new ArrayList<>();
    if (invitedUsersId.contains(userId)) {
      invitedUsersId.remove(userId);
      joinedUsersId.add(userId);
      return true;
    }
    return false;
  }

  public boolean hasUserAccessToEvent(String userId) {
    // TODO: add isPublic
    return ownerId.equals(userId) || invitedUsersId.contains(userId) || joinedUsersId.contains(userId) || declinedUsersId.contains(userId);
  }

  public static Builder newBuilder() {
    return new Event().new Builder();
  }

  public class Builder {
    private Builder() {
    }

    public Builder setID(String id) {
      Event.this.id = id;
      return this;
    }

    public Builder setGCalendarID(String gcalendarId) {
      Event.this.gcalendarId = gcalendarId;
      return this;
    }

    public Builder setTitle(String title) {
      Event.this.title = title;
      return this;
    }

    public Builder setDescription(String description) {
      Event.this.description = description;
      return this;
    }

    public Builder setCategory(String category) {
      Event.this.category = category;
      return this;
    }

    public Builder setTags(List<String> tags) {
      Event.this.tags = new ArrayList<>();
      if (tags != null) {
        Event.this.tags.addAll(tags);
      }
      return this;
    }

    public Builder setDateTimeRange(DateTimeRange dateTimeRange) {
      Event.this.dateTimeRange = dateTimeRange;
      return this;
    }

    public Builder setDuration(Long duration) {
      Event.this.duration = duration;
      return this;
    }

    public Builder setLocation(String location) {
      Event.this.location = location;
      return this;
    }

    public Builder setLocationId(String locationId) {
      Event.this.locationId = locationId;
      return this;
    }

    public Builder setLinks(List<String> links) {
      Event.this.links = new ArrayList<>();
      if (links != null) {
        Event.this.links.addAll(links);
      }
      return this;
    }

    public Builder setFields(Map<String, String> fields) {
      Event.this.fields = new HashMap<>();
      if (fields != null) {
        Event.this.fields.putAll(fields);
      }
      return this;
    }

    public Builder setOwnerID(String ownerId) {
      Event.this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
      return this;
    }

    public Builder setInvitedIDs(List<String> invitedUsersId) {
      Event.this.invitedUsersId = new ArrayList<>();
      if (invitedUsersId != null) {
        Event.this.invitedUsersId.addAll(invitedUsersId);
      }
      return this;
    }

    public Builder setJoinedIDs(List<String> joinedUsersId) {
      Event.this.joinedUsersId = new ArrayList<>();
      if (joinedUsersId != null) {
        Event.this.joinedUsersId.addAll(joinedUsersId);
      }
      return this;
    }

    public Builder setDeclinedIDs(List<String> declinedUsersId) {
      Event.this.declinedUsersId = new ArrayList<>();
      if (declinedUsersId != null) {
        Event.this.declinedUsersId.addAll(declinedUsersId);
      }
      return this;
    }
    
    public Event build() {
      if (Event.this.ownerId == null)
        throw new IllegalArgumentException("Owner of event should be specified"); 
      return Event.this;
    }
  }

  public void setRange(DateTimeRange dateTimeRange) {
    this.dateTimeRange = dateTimeRange;
  }

  public void setGCalendarId(String id) {
    this.gcalendarId = id;
  }

  public void setInvitedIDs(List<String> invitedUsersId) {
    Event.this.invitedUsersId = new ArrayList<>();
    if (invitedUsersId != null) {
      Event.this.invitedUsersId.addAll(invitedUsersId);
    }
  }

  public void setJoinedIDs(List<String> joinedUsersId) {
    Event.this.joinedUsersId = new ArrayList<>();
    if (joinedUsersId != null) {
      Event.this.joinedUsersId.addAll(joinedUsersId);
    }
  }

  public void setDeclinedIDs(List<String> declinedUsersId) {
    Event.this.declinedUsersId = new ArrayList<>();
    if (declinedUsersId != null) {
      Event.this.declinedUsersId.addAll(declinedUsersId);
    }
  }
}
