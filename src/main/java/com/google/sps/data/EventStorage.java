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

public class EventStorage {
  public static Event getEvent(String event_id) {
    // TODO: Query in datastore.
    return null;
  }

  public static void addEvent(Event event) {
    // Make an Entity of event.
    Entity eventEntity = new Entity("Event", event.getID());

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
