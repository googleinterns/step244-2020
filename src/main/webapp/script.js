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

function getEvent(event_id) {
  fetch('/events/' + event_id).then(response => response.json());
}

function getUser() {
  fetch('/users').then(response => response.json());
}

function searchEvents() {
  document.getElementById('events-container').innerText = "";
  var search = document.getElementById('search').value;
  fetch('/events?' + new URLSearchParams({
    search: search,
  })).then(response => response.json()).then(events => events.forEach(showEvent));
}

function showEvent(event) {
  document.getElementById('events-container').innerHTML += "<div><h1>" + event.title + "</h1><hr><br><h2>" + event.date + "</h2><h3>" + event.description + "</h3><br><button type=\"button\" onclick=\"joinEvent()\">Join event!</button><br><br></div>";
}

function joinEvent(event_id) {
  fetch('/events/' + event_id + '/join').then(response => response.json());
}

function addEventToGCalendar() { //To be modified to get fields
  var resp = verifyCredentials().then(validCredential => {
    if (validCredential == true) {
      fetch("/events/gcalendar", { method: "POST" }).then(response => {
        window.location.href = "http://localhost:8080/calendar.html";
      }).catch(error => alert(error));
    } else {
      window.location.href = "http://localhost:8080/token?origin=calendar"; //To be modified to get current location
    }
  });
}

function verifyCredentials() {
  return fetch("/credentials").then(response => response.text()).then(responseText => {
    return (responseText == "true");
  }).catch(error => {
    alert(error);
    return false;
  });
}
