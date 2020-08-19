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

import java.util.Objects;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.apache.commons.validator.routines.EmailValidator;

public class User {
  /**
   * Returns the nickname of the user with id, or empty String if the user has not set a nickname.
   */
  public static String getUID(String username) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    if (isEmail(username)) {
      Query query = new Query("UserInfo").setFilter(new Query.FilterPredicate("e-mail", Query.FilterOperator.EQUAL, username));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        return "";
      }
      return (String) entity.getProperty("uid");
    }
    else {
      Query query = new Query("UserInfo").setFilter(new Query.FilterPredicate("nickname", Query.FilterOperator.EQUAL, username));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        return "";
      }
      return (String) entity.getProperty("uid");
    }
  }

  public static boolean isEmail(String email) {
    EmailValidator validator = EmailValidator.getInstance();
    return validator.isValid(email);
  }
}
