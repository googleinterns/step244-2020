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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.List;

public class User {
  private final String uid;
  private final String email;
  private final String nickname;

  public User(String email, String nickname) {
    this.uid = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    this.email = email;
    this.nickname = nickname;
  }

  public static User get() {
  }

  public static void add(parameters) {
    // Make an Entity of event.
    Entity userEntity = new Entity("User", uid);

    // Make an Entity of user info.
    Entity userInfoEntity = new Entity("UserInfo", uid);

    // Make an Entity of user users.
    Entity userEventsEntity = new Entity("UserEvents", uid);

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userEntity);
    datastore.put(userInfoEntity);
    datastore.put(userEventsEntity);

    update(parameters);
  }

  public static void update(parameters) {
    // TODO: update user info with parameters.

    // Store Entities to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userInfoEntity);
    datastore.put(userEventsEntity);
  }

  public static void join(event_id) {
    // TODO: edit info in datastore
    Event.join(event_id);
  }

  public static List<Event> search(parameters) {
    Query query = new Query("EventInfo");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (...) {
        events.add(...);
      }
    }
    //TODO: Search with parameters.
    return events;
  }
}
