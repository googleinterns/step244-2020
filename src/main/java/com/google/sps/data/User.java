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

public class User {
  private final Long id;
  private final String email;
  private final String nickname;

  public User(Long id, String email, String nickname) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    if (email == null) {
      throw new IllegalArgumentException("email cannot be null");
    }

    if (nickname == null) {
      throw new IllegalArgumentException("nickname cannot be null");
    }

    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }
  
  public Long getID() {
    return id;
  }
}
