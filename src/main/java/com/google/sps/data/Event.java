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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.List;

public class Event {
  private final String id;
  private final String title;
  private final String date;
  private final List<String> tags;
  private final Map<String, String> fields;
  private final String description;

  public Event(String title, String date, List<String> tags, String description) {
    this.title = title;
    this.date = date;
    this.tags = tags;
    this.description = description;
  }

  public static Event get(event_id) {
    // TODO: Search in datastore.
  }

  public static void add(parameters) {
    // Make an Entity of event.
    Entity eventEntity = new Entity("Event");

    // Make an Entity of event info.
    Entity eventInfoEntity = new Entity("EventInfo", eventEntity.getKey());

    // Make an Entity of event users.
    Entity eventUsersEntity = new Entity("EventUsers", eventEntity.getKey());

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);
    datastore.put(eventInfoEntity);
    datastore.put(eventUsersEntity);

    update(parameters);
  }

  public static void update(parameters) {
    // TODO: Update event info with parameters.

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventInfoEntity);
    datastore.put(eventUsersEntity);
  }

  public static void join(event_id) {
    // TODO: edit info in datastore
  }
}
