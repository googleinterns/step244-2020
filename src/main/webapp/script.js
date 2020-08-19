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

/* Add all fetched events to the page. */
function getEvents() {
  document.getElementById('events-container').innerText = "";
  fetch('/event').then(response => response.json()).then(events => events.forEach(addEvent));
}

/* Add all fetched events which match the search to the page. */
function getSearchEvents() {
  var search = document.getElementById('search').value;
  document.getElementById('events-container').innerText = "";
  fetch('/event?' + new URLSearchParams({
    search: search,
})).then(response => response.json()).then(events => events.forEach(addEvent));
}

/* Add name, text, and image of event fields to the page. */
function addEvent(event) {
  document.getElementById('events-container').innerHTML += "<hr><h1>" + event.name + "</h1>";
  document.getElementById('events-container').innerHTML += "<h2>" + event.date + "</h2>";
  document.getElementById('events-container').innerHTML += "<h3>" + event.description + "</h3>";
  document.getElementById('events-container').innerHTML += "<button type=\"button\" onclick=\"joinEvent()\">Join event!</button>";
}

function joinEvent() {

}
