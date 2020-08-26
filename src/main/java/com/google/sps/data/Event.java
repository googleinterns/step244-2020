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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Event {
  private final String id;
  private final String title;
  private final String description;
  private final List<String> tags = new ArrayList<String>();
  private final Date start_date;
  private final Date start_time;
  private final Date end_date;
  private final Date end_time;
  private final String location;
  private final List<String> links = new ArrayList<String>();
  private final Map<String, String> fields = new HashMap<String, String>();
  private final Map<String, String> participants_status_by_id = new HashMap<String, String>();

  public Event(String id, String title, String description, List<String> tags, Date start_date, Date start_time, Date end_date, Date end_time,
               String location, List<String> links, Map<String, String> fields, Map<String, String> participants_status_by_id) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.title = Objects.requireNonNull(title, "title cannot be null");
    this.description = Objects.requireNonNull(description, "description cannot be null");
    this.tags.addAll(Objects.requireNonNull(tags, "tags cannot be null"));
    this.start_date = start_date;
    this.start_time = start_time;
    this.end_date = end_date;
    this.end_time = end_time;
    this.location = Objects.requireNonNull(location, "location cannot be null");
    this.links.addAll(Objects.requireNonNull(links, "links cannot be null"));
    this.fields.putAll(Objects.requireNonNull(fields, "fields cannot be null"));
    this.participants_status_by_id.putAll(Objects.requireNonNull(participants_status_by_id, "participants_status_by_id cannot be null"));
  }

  public String getID() {
    return id;
  }
}
