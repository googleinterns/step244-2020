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

import java.sql.Timestamp;

public final class TimeRange {
  private final Timestamp start; // in milliseconds
  private final Timestamp end;   // in milliseconds

  private TimeRange(Timestamp start, Timestamp end) {
    this.start = start;
    this.end = end;
  }

  public TimeRange(Timestamp time) {
    this.start = time;
    this.end = time;
  }

  public Timestamp start() {
    return start;
  }

  public Timestamp end() {
    return end;
  }
}
