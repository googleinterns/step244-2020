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

public class Event {
  private final Long id;
  private final String title;
  private final String date;
  private final List<String> tags = new ArrayList<String>();
  private final Map<String, String> fields = new HashMap<String, String>();
  private final String description;

  public Event(Long id, String title, String date, List<String> tags, Map<String, String> fields, String description) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    if (title == null) {
      throw new IllegalArgumentException("title cannot be null");
    }

    if (date == null) {
      throw new IllegalArgumentException("date cannot be null");
    }

    if (tags == null) {
      throw new IllegalArgumentException("tags cannot be null");
    }

    if (fields == null) {
      throw new IllegalArgumentException("tags cannot be null");
    }

    if (description == null) {
      throw new IllegalArgumentException("description cannot be null");
    }

    this.id = id;
    this.title = title;
    this.date = date;

    this.tags.addAll(tags);
    this.fields.putAll(fields);
    this.description = description;
  }

  public Long getID() {
    return id;
  }
}
