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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventStorage {
  public static Event getEvent(Long event_id) {
    // TODO: Query in datastore.
    return null;
  }

  public static List<Event> getSearchedEvents(String search) {
    Query query = new Query("Event");
 
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String title = (String) entity.getProperty("title");
      String description = (String) entity.getProperty("description");
 
      if (search == null || search.isEmpty() || EventStorage.isTextMatch(search, title) || EventStorage.isTextMatch(search, description)) {
        String id = (String) entity.getProperty("id");
        String date = (String) entity.getProperty("date");
        List<String> tags = (List<String>) entity.getProperty("tags");
        Map<String, String> fields = (Map<String, String>) entity.getProperty("fields");
        Event event = new Event(id, title, date, tags, fields, description); 

        events.add(event);
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
    eventEntity.setProperty("id", event.getID());
    eventEntity.setProperty("title", event.getTitle());
    eventEntity.setProperty("date", event.getDate());
    eventEntity.setProperty("tags", event.getTags());
    eventEntity.setProperty("description", event.getDescription());


    Key eventEntityKey = eventEntity.getKey();
    // Make an Entity of event info.
    Entity eventInfoEntity = new Entity("EventInfo", eventEntityKey);

    // Make an Entity of event users.
    Entity eventUsersEntity = new Entity("EventUsers", eventEntityKey);

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);
    datastore.put(eventInfoEntity);
    datastore.put(eventUsersEntity);
  }

  public static void editEvent(Event event) {
    // TODO: Edit event in datastore.
  }

  public static void deleteEvent(Event event) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key eventEntityKey = KeyFactory.createKey("Event", event.getID());
    datastore.delete(eventEntityKey);
  }

  public static void joinEvent(Long event_id) {
    // TODO: Add current user to EventUsers
  }
}
