// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function getEvent() {
  const urlParams = new URLSearchParams(window.location.search);
  const event_id = urlParams.get('event_id');
  fetch('/events?event_id=' + event_id).then(response => response.json());
}

function getUser() {
  const urlParams = new URLSearchParams(window.location.search);
  const user_id = urlParams.get('user_id');
  fetch('/users?user_id=' + user_id).then(response => response.json());
}

function searchEvents() {
  fetch('/events?' + new URLSearchParams({
    search: search,
  })).then(response => response.json());
}

function joinEvent(event_id) {
  fetch('/join?event_id=' + event_id).then(response => response.json());
}
