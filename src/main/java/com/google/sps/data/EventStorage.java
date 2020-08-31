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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventStorage {
  public static Event getEvent(String event_id) {
    // TODO: Query in datastore.
    return null;
  }

  public static List<Event> getSearchedEvents(String search, String searchTags, String searchDuration, String searchLocation) {
    Query query = new Query("Event");

    if (searchDuration != null && !searchDuration.isEmpty()) {
        Filter durationFilter =
        new FilterPredicate("duration", FilterOperator.LESS_THAN_OR_EQUAL, Long.parseLong(searchDuration));
        query = query.setFilter(durationFilter);
    }

    if (searchLocation != null && !searchLocation.isEmpty() && !searchLocation.equals("Everywhere")) {
        Filter locationFilter =
        new FilterPredicate("location", FilterOperator.EQUAL, searchLocation);
        query = query.setFilter(locationFilter);
    }
 
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String title = (String) entity.getProperty("title");
      String description = (String) entity.getProperty("description");
 
      if (search == null || search.isEmpty() || EventStorage.isTextMatch(search, title) || EventStorage.isTextMatch(search, description)) {
        String id = (String) entity.getKey().getAppId();
        List<String> tags = (List<String>) entity.getProperty("tags");

        if (searchTags == null || searchTags.equals("all") || tags.contains(searchTags)) {
          String date_range = (String) entity.getProperty("date-range");
          String time_range = (String) entity.getProperty("time-range");
          Long duration = (Long) entity.getProperty("duration");
          String location = (String) entity.getProperty("location");
          List<String> links = (List<String>) entity.getProperty("links");
          String fieldsJson = (String) entity.getProperty("fields");
          Map<String, String> fields = new Gson().fromJson(
          fieldsJson, new TypeToken<HashMap<String, String>>() {}.getType()
          );
          String owner_id = (String) entity.getProperty("owner");
          List<String> invited_participant_id = (List<String>) entity.getProperty("invited-users");
          List<String> joined_participant_id = (List<String>) entity.getProperty("joined-users");
          List<String> declined_participant_id = (List<String>) entity.getProperty("declined-users");
          Event event = new Event(id, title, description, tags, date_range, time_range, duration,
            location, links, fields, 
            owner_id, invited_participant_id, joined_participant_id, declined_participant_id);

          events.add(event);
        }
      }
    }

    return events;
  }

  private static boolean isTextMatch(String search, String text) {
    return text.toLowerCase().contains(search.toLowerCase());
  }

  public static void addEvent(Event event) {
    // Make an Entity of event.
    Entity eventEntity = new Entity("Event", event.getID());

    eventEntity.setProperty("title", event.getTitle());
    eventEntity.setProperty("description", event.getDescription());
    eventEntity.setProperty("tags", event.getTags());
    eventEntity.setProperty("date-range", event.getDateRange());
    eventEntity.setProperty("time-range", event.getTimeRange());
    eventEntity.setProperty("duration", event.getDuration());
    eventEntity.setProperty("location", event.getLocation());
    eventEntity.setProperty("links", event.getLinks());
    eventEntity.setProperty("fields", event.getFields());
    eventEntity.setProperty("owner", event.getOwnerID());
    eventEntity.setProperty("invited-users", event.getInvitedIDs());
    eventEntity.setProperty("joined-users", event.getJoinedIDs());
    eventEntity.setProperty("declined-users", event.getDeclinedIDs());

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);
  }

  public static void editEvent(Event event) {
    // TODO: Edit event in datastore.
  }

  public static void deleteEvent(Event event) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key eventEntityKey = KeyFactory.createKey("Event", event.getID());
    datastore.delete(eventEntityKey);
  }

  public static void joinEvent(String event_id) {
    // TODO: Add current user to EventUsers
  }
}
