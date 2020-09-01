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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EventStorage {
  public static Event getEvent(String eventId) {
    Query query = new Query("Event").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, KeyFactory.createKey("Event", eventId)));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity eventEntity = datastore.prepare(query).asSingleEntity();
    if (eventEntity != null) {
      return new Event(
        eventId, (String) eventEntity.getProperty("gcalendar-id"),
        (String) eventEntity.getProperty("title"),
        (String) eventEntity.getProperty("description"),
        (String) eventEntity.getProperty("category"),
        (ArrayList) eventEntity.getProperty("tags"),
        new Gson().fromJson((String) eventEntity.getProperty("date-time-range"), DateTimeRange.class),
        (Long) eventEntity.getProperty("duration"),
        (String) eventEntity.getProperty("location"),
        (ArrayList) eventEntity.getProperty("links"),
        new Gson().fromJson((String) eventEntity.getProperty("fields"), Map.class),
        (String) eventEntity.getProperty("owner"),
        (ArrayList) eventEntity.getProperty("invited-users"),
        (ArrayList) eventEntity.getProperty("joined-users"),
        (ArrayList) eventEntity.getProperty("declined-users")
      );
    }
    return null;
  }

  public static List<Event> getSearchedEvents(String search, String searchCategory, String searchDuration, String searchLocation) {
    Query query = new Query("Event");

    if (searchDuration != null && !searchDuration.isEmpty()) {
      Long searchDurationLong = Long.parseLong(searchDuration);
      if (searchDurationLong != null) {
        Filter durationFilter =
        new FilterPredicate("duration", FilterOperator.LESS_THAN_OR_EQUAL, searchDurationLong);
        query = query.setFilter(durationFilter);
      }
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
        // TODO: Likely this is an incorrect way to get id
        String id = (String) entity.getKey().getAppId();
        String category = (String) entity.getProperty("category");

        if (searchCategory == null || searchCategory.equals("all") || category.equals(searchCategory)) {
          String gcalendarId = (String) entity.getProperty("gcalendar-id");
          List<String> tags = (List<String>) entity.getProperty("tags");
          String dateTimeRangeJson = (String) entity.getProperty("date-time-range");
          // TODO: Get actual data structure from Json
          DateTimeRange dateTimeRange = null;
          Long duration = (Long) entity.getProperty("duration");
          String location = (String) entity.getProperty("location");
          List<String> links = (List<String>) entity.getProperty("links");
          String fieldsJson = (String) entity.getProperty("fields");
          Map<String, String> fields = new Gson().fromJson(
          fieldsJson, new TypeToken<HashMap<String, String>>() {}.getType()
          );
          String ownerId = (String) entity.getProperty("owner");
          List<String> invitedParticipantsId = (List<String>) entity.getProperty("invited-users");
          List<String> joinedParticipantsId = (List<String>) entity.getProperty("joined-users");
          List<String> declinedParticipantsId = (List<String>) entity.getProperty("declined-users");
          Event event = new Event(id, gcalendarId, title, description, category, tags, 
               dateTimeRange, duration, location, links, fields, ownerId, invitedParticipantsId, 
               joinedParticipantsId, declinedParticipantsId);
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
    eventEntity.setProperty("links", event.getLinks());
    eventEntity.setProperty("fields", event.getFieldsAsJSON());
    eventEntity.setProperty("owner", event.getOwnerID());
    eventEntity.setProperty("invited-users", event.getInvitedIDs());
    eventEntity.setProperty("joined-users", event.getJoinedIDs());
    eventEntity.setProperty("declined-users", event.getDeclinedIDs());

    // Store Entities to datastore.
    DatastoreServiceFactory.getDatastoreService().put(eventEntity);
  }

  public static void editEvent(Event event) {
    // TODO: Edit event in datastore.
  }

  public static void deleteEvent(Event event) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key eventEntityKey = KeyFactory.createKey("Event", event.getID());
    datastore.delete(eventEntityKey);
  }

  public static void joinEvent(String userId, String eventId) {
    Event event = getEvent(eventId);
    
  }
}
