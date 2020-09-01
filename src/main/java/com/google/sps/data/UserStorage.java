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
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.List;
import java.util.ArrayList;

public class UserStorage {
  public static User getUser(String userId) {
    Query query = new Query("User").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, "User", userId));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = datastore.prepare(query).asSingleEntity();
    if (userEntity == null) {
      return null;
    }
    return (String) userEntity.getProperty("id");
  }

  public static User getUserByUsername(String username) {
    // TODO: Query in datastore.
    return null;
  }

  public static void addUser(User user) {
    // Make an Entity of user.
    Entity userEntity = new Entity("User", user.getID());

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userEntity);
  }

  public static void editUser(User user) {
    // TODO: Edit event in datastore.
  }

  public static void deleteUser(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userEntityKey = KeyFactory.createKey("User", userId);
    datastore.delete(userEntityKey);
  }

  public static String getIDbyUsername(String username) {
    Query query = new Query("User").setFilter(new FilterPredicate("username", FilterOperator.EQUAL, username));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = datastore.prepare(query).asSingleEntity();
    if (userEntity == null) {
      return null;
    }
    return (String) userEntity.getProperty("id");
  }

  public static void joinEvent(String userId, String eventId) {
    User user = getUser(userId);
    if (!user.getInvitaions().containd(eventId)) return;
    user.addParticipation(eventId);
    EventStorage.joinEvent(userId, eventId);
  }

  public static List<Event> search() {
    Query query = new Query("EventInfo");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
    }
    //TODO: Search with parameters.
    return events;
  }
}
