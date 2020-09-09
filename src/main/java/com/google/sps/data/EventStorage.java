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

import com.google.appengine.api.datastore.EntityNotFoundException;

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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EventStorage {
  public static Event getEvent(String eventId) {
    Entity eventEntity = null;
    try {
      eventEntity = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.stringToKey(eventId));
    } catch (EntityNotFoundException e) {
      System.err.println("Cannot get event " + eventId + ": " + e.getMessage());
      return null;
    }
    return eventEntity != null ? Event.fromDatastoreEntity(eventEntity) : null;
  }

  public static List<Event> getSearchedEvents(String search, String searchCategory, String searchStart, String searchEnd, String searchDuration, String searchLocation) {
    Query query = new Query("Event");

    if (searchDuration != null && !searchDuration.isEmpty()) {
      Long searchDurationLong = Long.parseLong(searchDuration);
      if (searchDurationLong != null) {
        Filter durationFilter =
        new FilterPredicate("duration", FilterOperator.LESS_THAN_OR_EQUAL, searchDurationLong);
        query = query.setFilter(durationFilter);
      }
    }

    if (searchLocation != null && !searchLocation.isEmpty() && !searchLocation.equals("all")) {
      Filter locationFilter =
      new FilterPredicate("location-id", FilterOperator.EQUAL, searchLocation);
      query = query.setFilter(locationFilter);
    }
 
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String title = (String) entity.getProperty("title");
      String description = (String) entity.getProperty("description");
      DateTimeRange dateTimeRange = new Gson().fromJson((String) entity.getProperty("date-time-range"), DateTimeRange.class);

      if (!eventInRange(searchStart, searchEnd, dateTimeRange)) {
        continue;
      }
 
      if (search == null || search.isEmpty() || EventStorage.isTextMatch(search, title) || EventStorage.isTextMatch(search, description)) {
        String category = (String) entity.getProperty("category");

        if (searchCategory == null || searchCategory.equals("all") || category.equals(searchCategory)) {
          events.add(Event.fromDatastoreEntity(entity));
        }
      }
    }

    return events;
  }

  private static boolean isTextMatch(String search, String text) {
    return text.toLowerCase().contains(search.toLowerCase());
  }

  private static boolean eventInRange(String start, String end, DateTimeRange range) {
    return range == null || ((start == null || start.isEmpty() || range.getStartDate() == null 
    || start.compareTo(range.getStartDate()) <= 0) && (end == null || end.isEmpty() 
    || range.getEndDate() == null || end.compareTo(range.getEndDate()) >= 0));
  }

  public static String addOrUpdateEvent(Event event) {
    // Make an Entity of event.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity eventEntity = null;
    if (event.getID() == null) {
      eventEntity = new Entity("Event");
    } else {
      try {
        eventEntity = datastore.get(KeyFactory.createKey("Event", event.getID()));
      } catch (EntityNotFoundException e) {
        System.err.println("Cannot get event " + event.getID() + ": " + e.getMessage());
        return null;
      }
    }

    eventEntity.setProperty("gcalendar-id", event.getGCalendarID());
    eventEntity.setProperty("title", event.getTitle());
    eventEntity.setProperty("description", event.getDescription());
    eventEntity.setProperty("category", event.getCategory());
    eventEntity.setProperty("tags", event.getTags());
    eventEntity.setProperty("date", event.getDate());
    eventEntity.setProperty("time", event.getTime());
    eventEntity.setProperty("date-time-range", event.getDateTimeRangeAsJSON());
    eventEntity.setProperty("duration", event.getDuration());
    eventEntity.setProperty("location", event.getLocation());
    eventEntity.setProperty("location-id", event.getLocationId());
    eventEntity.setProperty("links", event.getLinks());
    eventEntity.setProperty("fields", event.getFieldsAsJSON());
    eventEntity.setProperty("owner", event.getOwnerID());
    eventEntity.setProperty("invited-users", event.getInvitedIDs());
    eventEntity.setProperty("joined-users", event.getJoinedIDs());
    eventEntity.setProperty("declined-users", event.getDeclinedIDs());

    // Store Entities to datastore.
    datastore.put(eventEntity);
    return KeyFactory.keyToString(eventEntity.getKey());
  }

  public static void deleteEvent(String eventId) {
    DatastoreServiceFactory.getDatastoreService().delete(KeyFactory.createKey("Event", eventId));
  }

  public static void joinEvent(String userId, String eventId) {
    Event event = getEvent(eventId);
    if (event == null)
      return;
    
    event.joinEvent(userId);
    addOrUpdateEvent(event);
  }

  public static boolean hasUserAccessToEvent(String userId, String eventId) {
    Event event = getEvent(eventId);
    if (event == null)
      return false;
    return event.hasUserAccessToEvent(userId);
  }
}
