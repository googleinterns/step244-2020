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

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class User {
  private final String id;
  private String email;
  private String username;
  private List<String> invitedEventsId = new ArrayList<String>();
  private List<String> joinedEventsId = new ArrayList<String>();
  private List<String> declinedEventsId = new ArrayList<String>();

  public User(String id, String email, String username, List<String> invitedEventsId, List<String> joinedEventsId, List<String> declinedEventsId) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.email = email;
    this.username = Objects.requireNonNull(username, "username cannot be null");
    this.invitedEventsId.addAll(invitedEventsId);
    this.joinedEventsId.addAll(joinedEventsId);
    this.declinedEventsId.addAll(declinedEventsId);
  }

  public void joinEvent(String eventId) {
    if (invitedEventsId.contains(eventId)) {
      this.invitedEventsId.remove(eventId);
      this.joinedEventsId.add(eventId);
    }
  }

  public String getID() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public List<String> getInvitedEventsID() {
    return invitedEventsId;
  }

  public List<String> getJoinedEventsID() {
    return joinedEventsId;
  }

  public List<String> getDeclinedEventsID() {
    return declinedEventsId;
  }
}
