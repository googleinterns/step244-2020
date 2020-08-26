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
  fetch('/events?' + new URLSearchParams({
    search: search,
  })).then(response => response.json());
}

function joinEvent(event_id) {
  fetch('/events/' + event_id + '/join').then(response => response.json());

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

/** Add custom field to form while creating an event. */
function showCustomFieldName() {
  document.getElementById('add-event-custom-fields').hidden = true;
  document.getElementById('add-event-custom-field').hidden = false;
}

function addCustomField() {
  document.getElementById('add-event-custom-fields').hidden = false;
  document.getElementById('add-event-custom-field').hidden = true;

  var form = document.getElementById('add-event-form');
  var fieldName = document.getElementById('field-name').value;
  document.getElementById('field-name').value = '';

  const customFieldInput = document.createElement('input');
  customFieldInput.setAttribute('type', 'hidden');
  customFieldInput.setAttribute('name', 'custom-fields');
  customFieldInput.setAttribute('value', fieldName);
  
  const fieldLabel = document.createElement('label');
  fieldLabel.setAttribute('for', fieldName);
  fieldLabel.innerText = fieldName;

  const fieldInput = document.createElement('input');
  fieldInput.setAttribute('type', 'text');
  fieldInput.setAttribute('id', fieldName);
  fieldInput.setAttribute('name', fieldName);

  var button = document.getElementById('add-event-custom-fields');
  form.insertBefore(customFieldInput, button);
  form.insertBefore(fieldLabel, button);
  form.insertBefore(fieldInput, button);
}

function addPerson() {
  var person = document.getElementById('person').value;
  document.getElementById('person').value = '';

  const personInput = document.createElement('input');
  personInput.setAttribute('type', 'hidden');
  personInput.setAttribute('name', 'people');
  personInput.setAttribute('value', person);
  document.getElementById('add-event-form').insertBefore(personInput, document.getElementById('event-people'));

  const personLI = document.createElement('li');
  personLI.innerText = person;
  document.getElementById('event-people-list').appendChild(personLI);
}
