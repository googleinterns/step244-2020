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
}

function addEventToGCalendar() { //To be modified to get fields
  var resp = verifyCredentials().then(validCredential => {
    if (validCredential == true) {
      fetch("/events/gcalendar", { method: "POST" }).then(response => {
        window.location.href = getCurrentUrl() + "/calendar.html";
      }).catch(error => alert(error));
    } else {
      window.location.href = getCurrentUrl() + "/token?origin=calendar";
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

function getGCalendarEvents(calendar, startTime, endTime) {
  verifyCredentials().then(validCredential => {
    if (validCredential) {
      fetch("/events/gcalendar?startEpochInSeconds=" + startTime + "&endEpochInSeconds=" + endTime)
        .then(response => response.json()).then(events => {
          var fullcalendarEvents = [];
          events.forEach(event => {
            var start, end;
            if (event.start.dateTime) {
              start = event.start.dateTime.value;
              end = event.end.dateTime.value;
            } else {
              start = event.start.date.value;
              end = event.end.date.value;
            }
            var shared = null, private = null;
            if (event.extendedProperties)
              shared = event.extendedProperties.shared, private = event.extendedProperties.private;
            fullcalendarEvents.push({
              id: event.id,
              title: event.summary,
              start: start,
              end: end,
              location: event.location,
              description: event.description,
              shared: shared,
              private: private
            });
          });
          if (calendar.getEventSources().length)
            calendar.getEventSources()[0].remove();
          calendar.addEventSource(fullcalendarEvents);
        });
    } else {
      window.location.href = getCurrentUrl() + "/token?origin=calendar";
    }
  });
}

function getCurrentUrl(){
  var currentUrl = window.location.href;
  var currentUrlSlices = currentUrl.split("/");
  return currentUrlSlices[0] + "//" + currentUrlSlices[2];
}

function createCalendarElements(givenProperties) {
  var eventWrapper = document.createElement("div");
  eventWrapper.classList.add("event-wrapper");

  var contentWrapper = document.createElement("div");
  contentWrapper.classList.add("content-wrapper");

  var descriptionElement = document.createElement("div");
  descriptionElement.classList.add("description-wrapper");
  if (givenProperties.description) {
    var descriptionContent = document.createElement("div");
    descriptionContent.innerHTML = givenProperties.description;
    descriptionContent.classList.add("right-item");
    var descriptionIcon = document.createElement("i");
    descriptionIcon.classList.add("fa", "fa-bars", "left-item");
    descriptionElement.appendChild(descriptionIcon);
    descriptionElement.appendChild(descriptionContent);
  }

  var locationElement = document.createElement("div");
  locationElement.classList.add("extendedprop-wrapper");
  if (givenProperties.location) {
    var locationIcon = document.createElement("i");
    locationIcon.classList.add("fa", "fa-location-arrow", "left-item");
    var locationContent = document.createElement("div");
    locationContent.innerHTML = givenProperties.location;
    locationContent.classList.add("right-item");
    locationElement.appendChild(locationIcon);
    locationElement.appendChild(locationContent);
  }

  eventWrapper.appendChild(descriptionElement);
  eventWrapper.appendChild(locationElement);

  if (givenProperties.shared) {
    for (const [key, value] of Object.entries(givenProperties.shared)) {
      var extendedPropertyElement = document.createElement("div");
      extendedPropertyElement.classList.add("extendedprop-wrapper");
      var extendedPropertyName = document.createElement("div");
      extendedPropertyName.innerHTML = key;
      extendedPropertyName.classList.add("left-item");
      var extendedPropertyValue = document.createElement("div");
      extendedPropertyValue.innerHTML = value;
      extendedPropertyValue.classList.add("right-item");
      extendedPropertyElement.appendChild(extendedPropertyName);
      extendedPropertyElement.appendChild(extendedPropertyValue);
      eventWrapper.appendChild(extendedPropertyElement);
    }
  }

  var buttonDivElement = document.createElement("div");
  buttonDivElement.classList.add("add-button");
  var buttonElement = document.createElement("button");
  buttonElement.classList.add("btn", "btn-primary");
  var plusIcon = document.createElement("i");
  plusIcon.classList.add("fas", "fa-edit");

  buttonElement.appendChild(plusIcon);
  buttonDivElement.appendChild(buttonElement);
  eventWrapper.appendChild(buttonDivElement);
  return eventWrapper;
}

function hasAnyParentWithGivenId(parent, id) {
  while (parent != null && parent.nodeName != "BODY") {
    if (parent.id === id)
      return true;
    parent = parent.parentElement;
  }
  return false;
}

function isIdValid(givenId) {
  return givenId.length && document.getElementById(givenId);
}

function calendarRender() {
  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'timeGridDay,timeGridWeek,dayGridMonth'
    },
    eventDidMount: function (info) {
      $(info.el).popover({
        animation: true,
        html: true,
        title: info.event.title,
        content: createCalendarElements(info.event.extendedProps),
        trigger: 'click',
        container: 'body',
        placement: 'top',
      });
    },
    datesSet: function (info) {
      var viewUTCStartTime = info.view.activeStart.getTime() - info.view.activeStart.getTimezoneOffset();
      var viewUTCEndTime = info.view.activeEnd.getTime() - info.view.activeEnd.getTimezoneOffset();
      getGCalendarEvents(calendar, viewUTCStartTime, viewUTCEndTime);
    },
    fixedWeekCount: false,
    eventClick: function (event) {
      event.jsEvent.preventDefault();
    },
    eventTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    },
    displayEventEnd: true
  });
  calendar.render();
}

/** Add custom field to form while creating an event. */
function showFieldName() {
  document.getElementById('add-event-fields').hidden = true;
  document.getElementById('add-event-field').hidden = false;
}

function addField() {
  document.getElementById('add-event-fields').hidden = false;
  document.getElementById('add-event-field').hidden = true;

  var form = document.getElementById('add-event-form');
  var fieldName = document.getElementById('field-name').value;
  document.getElementById('field-name').value = '';

  const FieldInput = document.createElement('input');
  FieldInput.setAttribute('type', 'hidden');
  FieldInput.setAttribute('name', 'fields');
  FieldInput.setAttribute('value', fieldName);

  const fieldLabel = document.createElement('label');
  fieldLabel.setAttribute('for', fieldName);
  fieldLabel.innerText = fieldName;

  const fieldInput = document.createElement('input');
  fieldInput.setAttribute('type', 'text');
  fieldInput.setAttribute('id', fieldName);
  fieldInput.setAttribute('name', fieldName);

  var button = document.getElementById('add-event-fields');
  form.insertBefore(FieldInput, button);
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

function setMinDateToToday() {
  document.getElementById("event-start-date").min = new Date().toISOString().slice(0, 10);
}
