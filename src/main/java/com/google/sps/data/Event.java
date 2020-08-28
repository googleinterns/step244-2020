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
  private final String title;
  private final String description;
  private final List<String> tags = new ArrayList<String>();
  private final String date_range;
  private final String time_range;
  private final Long duration; // in minutes
  private final String location;
  private final List<String> links = new ArrayList<String>();
  private final Map<String, String> fields = new HashMap<String, String>();
  private final String owner_id;
  private final List<String> invited_participants_id = new ArrayList<String>();
  private final List<String> joined_participants_id = new ArrayList<String>();
  private final List<String> declined_participants_id = new ArrayList<String>();

  public Event(String id, String title, String description, List<String> tags, String date_range, String time_range, Long duration,
               String location, List<String> links, Map<String, String> fields, 
               String owner_id, List<String> invited_participant_id, List<String> joined_participant_id, List<String> declined_participant_id) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.title = Objects.requireNonNull(title, "title cannot be null");
    this.description = Objects.requireNonNull(description, "description cannot be null");
    this.tags.addAll(Objects.requireNonNull(tags, "tags cannot be null"));
    this.date_range = Objects.requireNonNull(date_range, "date_range cannot be null");
    this.time_range = Objects.requireNonNull(time_range, "time_range cannot be null");
    this.duration = duration;
    this.location = Objects.requireNonNull(location, "location cannot be null");
    this.links.addAll(Objects.requireNonNull(links, "links cannot be null"));
    this.fields.putAll(Objects.requireNonNull(fields, "fields cannot be null"));
    this.owner_id = Objects.requireNonNull(owner_id, "owner_id cannot be null");
    this.invited_participants_id.addAll(Objects.requireNonNull(invited_participants_id, "invited_participants_id cannot be null"));
    this.joined_participants_id.addAll(Objects.requireNonNull(joined_participants_id, "joined_participants_id cannot be null"));
    this.declined_participants_id.addAll(Objects.requireNonNull(declined_participants_id, "declined_participants_id cannot be null"));
  }

  public String getID() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getTags() {
    return tags;
  }

  public String getTimeRange() {
    return time_range;
  }

  public String getDateRange() {
    return date_range;
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

  public String getFields() { // Map -> gson
    return new Gson().toJson(fields);
  }

  public String getOwnerID() {
    return owner_id;
  }

  public List<String> getInvitedIDs() {
    return invited_participants_id;
  }

  public List<String> getJoinedIDs() {
    return joined_participants_id;
  }

  public List<String> getDeclinedIDs() {
    return declined_participants_id;
  }
}
