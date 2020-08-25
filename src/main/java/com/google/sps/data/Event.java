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

public final class Event {

  private final String name;
  private final String date;
  private final String location;
  private final String description;
  private final String category;
  private final String tags;

  public Event(String name, String date, String location, String description, String category, String tags) {
    this.name = name;
    this.date = date;
    this.location = location;
    this.description = description;
    this.category = category;
    this.tags = tags;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public String getTags() {
    return tags;
  }
}
