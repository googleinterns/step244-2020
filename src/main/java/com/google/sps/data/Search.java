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

public class Search {
  private final String text;
  private final String category;
  private final String start;
  private final String end;
  private final String duration;
  private final String location;

  public Search() {
    this(null, null, null, null, null, null);
  }

  public Search(String text, String category, String start, String end, String duration, String location) {
    this.text = text;
    this.category = category;
    this.start = start;
    this.end = end;
    this.duration = duration;
    this.location = location;
  }

  public String getText() {
    return text;
  }

  public String getCategory() {
    return category;
  }

  public String getStart() {
    return start;
  }

  public String getEnd() {
    return end;
  }

  public String getDuration() {
    return duration;
  }

  public String getLocation() {
    return location;
  }

  public boolean isSearchedTextMatching(String title, String description) {
    return text == null || text.isEmpty() || isTextMatching(title) || isTextMatching(description);
  }

  public boolean isTextMatching(String string) {
    return string.toLowerCase().contains(text.toLowerCase());
  }

  public boolean eventInRange(DateTimeRange range) {
    return range == null || ((start == null || start.isEmpty() || range.getStartDate() == null 
    || start.compareTo(range.getStartDate()) <= 0) && (end == null || end.isEmpty() 
    || range.getEndDate() == null || end.compareTo(range.getEndDate()) >= 0));
  }

  public boolean eventInCategory(String category) {
    return this.category == null || category == null || this.category.equals("all") 
    || category.equals(this.category);
  }
}
