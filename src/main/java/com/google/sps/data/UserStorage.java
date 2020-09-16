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
  public User getUser(String userId) {
    Query query = new Query("User").setFilter(
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, KeyFactory.createKey("User", userId)));
    Entity userEntity = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
    if (userEntity != null) {
      return User.newBuilder().setId(userId).setEmail((String) userEntity.getProperty("email"))
          .setUsername((String) userEntity.getProperty("username"))
          .setInvitedEventsId(convertPropertyToList(userEntity.getProperty("invited-events")))
          .setJoinedEventsId(convertPropertyToList(userEntity.getProperty("joined-events")))
          .setDeclinedEventsId(convertPropertyToList(userEntity.getProperty("declined-events"))).build();
    }
    return null;
  }

  private List<String> convertPropertyToList(Object toConvert) {
    if (toConvert == null)
      return null;
    return (List) toConvert;
  }

  public void addOrUpdateUser(User user) {
    if (user == null)
      return;

    // Make an Entity of user.
    Entity userEntity = new Entity("User", user.getID());

    userEntity.setProperty("email", user.getEmail());
    userEntity.setProperty("username", user.getUsername());
    userEntity.setProperty("invited-events", user.getInvitedEventsID());
    userEntity.setProperty("joined-events", user.getJoinedEventsID());
    userEntity.setProperty("declined-events", user.getDeclinedEventsID());

    // Store Entities to datastore.
    DatastoreServiceFactory.getDatastoreService().put(userEntity);
  }

  public void deleteUser(String userId) {
    DatastoreServiceFactory.getDatastoreService().delete(KeyFactory.createKey("User", userId));
  }

  public String getIDbyUsername(String username) {
    Query query = new Query("User").setFilter(new FilterPredicate("username", FilterOperator.EQUAL, username));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = datastore.prepare(query).asSingleEntity();
    return userEntity != null ? (String) userEntity.getProperty("id") : null;
  }

  public String getUsernameByID(String userId) {
    Query query = new Query("User").setFilter(
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, KeyFactory.createKey("User", userId)));
    Entity userEntity = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
    return userEntity != null ? (String) userEntity.getProperty("email") : null;
  }

  public void joinEvent(String userId, String eventId) {
    User user = getUser(userId);
    if (user == null)
      return;
    user.joinEvent(eventId);
    addOrUpdateUser(user);
    new EventStorage().joinEvent(userId, eventId);
  }

  public List<Event> search() {
    Query query = new Query("EventInfo");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
    }
    // TODO: Search with parameters.
    return events;
  }
}
