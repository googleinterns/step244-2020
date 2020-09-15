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
  private String id;
  private String email;
  private String username;
  private List<String> invitedEventsId = new ArrayList<String>();
  private List<String> joinedEventsId = new ArrayList<String>();
  private List<String> declinedEventsId = new ArrayList<String>();

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User))
      return false;
    User other = (User) obj;
    return Objects.equals(this.id, other.id) && Objects.equals(this.email, other.email)
        && Objects.equals(this.username, other.username) && Objects.equals(this.invitedEventsId, other.invitedEventsId)
        && Objects.equals(this.joinedEventsId, other.joinedEventsId) && Objects.equals(this.declinedEventsId, other.declinedEventsId);
  }

  public class Builder {
    public Builder setId(String id) {
      User.this.id = id;
      return this;
    }

    public Builder setEmail(String email) {
      User.this.email = email;
      return this;
    }

    public Builder setUsername(String username) {
      User.this.username = username;
      return this;
    }

    public Builder setInvitedEventsId(List<String> invitedIds) {
      addAllIfNotNull(User.this.invitedEventsId, invitedIds);
      return this;
    }

    public Builder setJoinedEventsId(List<String> joinedIds) {
      addAllIfNotNull(User.this.joinedEventsId, joinedIds);
      return this;
    }

    public Builder setDeclinedEventsId(List<String> declinedIds) {
      addAllIfNotNull(User.this.declinedEventsId, declinedIds);
      return this;
    }

    public User build() {
      return User.this;
    }
  }

  public static Builder newBuilder() {
    return new User().new Builder();
  }

  public void joinEvent(String eventId, boolean isEventPublic) {
    if (invitedEventsId.contains(eventId) || isEventPublic) {
      this.invitedEventsId.remove(eventId);
      this.joinedEventsId.add(eventId);
    }
  }

  private void addAllIfNotNull(List<String> list1, List<String> list2) {
    if (list2 != null) {
      list1.addAll(list2);
    }
    return;
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

  public void setUsername(String username) {
    this.username = username;
  }
}
