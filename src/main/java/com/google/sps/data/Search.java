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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.*;

import org.apache.commons.lang3.StringUtils;

public class Search {

  private final int MAX_DISTANCE = 3;

  private final String text;
  private final String category;
  private final String start;
  private final String end;
  private final String duration;
  private final String location;
  private final List<String> tags;

  public Search() {
    this(null, null, null, null, null, null, null);
  }

  public Search(String text, String category, String start, String end, String duration, String location, List<String> tags) {
    this.text = text;
    this.category = category;
    this.start = start;
    this.end = end;
    this.duration = duration;
    this.location = location;
    this.tags = tags;
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

  public List<String> getTags() {
    return tags;
  }

  public String getTagsAsStringForParameters() {
    return listToParameterString(tags);
  }

  private String listToParameterString(List<String> list) {
    if (list == null || list.size() == 0) {
      return "";
    }
    return String.join(",", list);
  }

  public boolean isSearchedTextMatching(String title, String description) {
    return text == null || text.isEmpty() || isTextMatching(title) || isTextMatching(description);
  }

  public boolean isTextMatching(String stringToMatch) {
    List<String> stringList = Arrays.asList(stringToMatch.toLowerCase().split("\\s+"));
    List<String> textList = Arrays.asList(this.text.toLowerCase().split("\\s+"));

    for (String string : stringList) {
      int length = string.length();
      for (String text : textList) {
        int distance = Math.min(Math.min(text.length(), length) / 2, MAX_DISTANCE);
        if (StringUtils.getLevenshteinDistance(string, text) <= distance) {
          return true;
        }
      }
    }
    return false;
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

  public int countMatchingTags(List<String> tags) {
    if (this.tags == null || this.tags.size() == 0) {
      return -1;
    }
    if (tags == null || tags.size() == 0) {
      return 0;
    }
    return tags.stream().filter(tag -> this.tags.contains(tag)).collect(Collectors.toList()).size();
  }

  @Override
  public boolean equals(Object other_object) {
    if (!(other_object instanceof Search))
        return false;
    Search other = (Search) other_object;
    return Objects.equals(text, other.getText())
        && Objects.equals(category, other.getCategory())
        && Objects.equals(start, other.getStart())
        && Objects.equals(end, other.getEnd())
        && Objects.equals(duration, other.getDuration())
        && Objects.equals(location, other.getLocation())
        && Objects.equals(tags, other.getTags());
  }
}
