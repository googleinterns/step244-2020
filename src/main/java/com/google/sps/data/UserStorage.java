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
import java.util.List;
import java.util.ArrayList;

public class UserStorage {
  public static User getUser(Long user_id) {
    // TODO: Query in datastore.
    return null;
  }

  public static void addUser(User user) {
    // Make an Entity of user.
    Entity userEntity = new Entity("User", user.getID());

    Key userEntityKey = userEntity.getKey();
    // Make an Entity of user info.
    Entity userInfoEntity = new Entity("UserInfo", userEntityKey);

    // Make an Entity of user users.
    Entity userEventsEntity = new Entity("UserEvents", userEntityKey);

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userEntity);
    datastore.put(userInfoEntity);
    datastore.put(userEventsEntity);
  }

  public static void editUser(User user) {
    // TODO: Edit event in datastore.
  }

  public static void deleteUser(Long user_id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userEntityKey = KeyFactory.createKey("User", user_id);
    datastore.delete(userEntityKey);
  }

  public static void joinEvent(Long event_id) {
    // TODO: edit info in datastore
    EventStorage.joinEvent(event_id);
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
