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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Event {
  private final String id;
  private final String title;
  private final String date;
  private final List<String> tags = new ArrayList<String>();
  private final Map<String, String> fields = new HashMap<String, String>();
  private final String description;

  public Event(String id, String title, String date, List<String> tags, Map<String, String> fields, String description) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.title = Objects.requireNonNull(title, "title cannot be null");
    this.date = Objects.requireNonNull(date, "date cannot be null");

    this.tags.addAll(Objects.requireNonNull(tags, "tags cannot be null"));
    this.fields.putAll(Objects.requireNonNull(fields, "fields cannot be null"));
    this.description = Objects.requireNonNull(description, "description cannot be null");
  }

  public String getID() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDate() {
    return date;
  }

  public List<String> getTags() {
    return tags;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public String getDescription() {
    return description;
  }
}
