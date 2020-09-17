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
  fetch('/events/' + event_id).then(response => response.json()).then((event) => {
    document.getElementById('duration-info').hidden = true;
    document.getElementById('location-info').hidden = true;
    
    document.getElementById('title-info').innerText = event.title;
    document.getElementById('start-date-info').innerText = event.dateTimeRange.startDate;
    document.getElementById('start-time-info').innerText = event.dateTimeRange.startTime;
    if (event.duration != null && event.duration != "") {
      document.getElementById('duration-info').hidden = false;
      document.getElementById('duration-info').innerText = 'Duration: ' + event.duration + 'minutes';
    }

    document.getElementById('category-info').innerText = event.category;
    for (tag in event.tags) {
      const tagLI = document.createElement('li');
      tagLI.innerText = event.tags[tag];
      document.getElementById('tags-info').appendChild(tagLI);
    }

    document.getElementById('description-info').innerText = event.description;
    if (event.location != null && event.location != "") {
        document.getElementById('location-info').hidden = false;
        document.getElementById('location-info').innerText = 'Location: ' + event.location;
    }

    var today = new Date();
    fetch('/weather?' + new URLSearchParams({
        location: event.location,
      }) + '&' + new URLSearchParams({
        hours: today.getUTCHours() - event.dateTimeRange.startTime.split(':')[0],
      }) + '&' + new URLSearchParams({
        days: today.getDay() - event.dateTimeRange.startDate.split('-')[2],
      })).then(weatherResponse => weatherResponse.json()).then((weather) => {
        document.getElementById('weather-type-info').innerText = weather.type;
        document.getElementById('weather-temperature-info').innerText = weather.temperature;
        document.getElementById('weather-temperaturefeels-like-info').innerText = weather.temperatureFeelsLike;
        document.getElementById('weather-pressure-info').innerText = weather.pressure;
        document.getElementById('weather-humidity-info').innerText = weather.humidity;
        document.getElementById('weather-clouds-info').innerText = weather.clouds;
        document.getElementById('weather-icon').src = `"http://openweathermap.org/img/wn/${weather.iconId}.@2x.png"`;
    });

    for (link in event.links) {
      const linkA = document.createElement('a');
      linkA.innerText = event.links[link];
      linkA.href = "https://" + event.links[link];
      document.getElementById('links-info').appendChild(linkA);
    }
    if (typeof event.gcalendarId === "undefined") {
      var doodleLink = document.createElement("a");
      doodleLink.href = getCurrentUrl() + "/doodle.html?eventId=" + event.id;
      doodleLink.innerText = "Click to select a time";
      document.getElementById("links-info").appendChild(doodleLink);
    }
    for (field in event.fields) {
      const fieldLI = document.createElement('li');
      fieldLI.innerText = field + ': ' + event.fields[field];
      document.getElementById('fields-info').appendChild(fieldLI);
    }

    document.getElementById('owner-info').innerText = 'Owner of event: ' + event.ownerId;
    for (person in event.joinedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.joinedUsersId[person];
      personLI.setAttribute('class', 'joined');
      document.getElementById('people-list-info').appendChild(personLI);
    }
    for (person in event.invitedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.invitedUsersId[person];
      personLI.setAttribute('class', 'invited');
      document.getElementById('people-list-info').appendChild(personLI);
    }
    for (person in event.declinedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.declinedUsersId[person];
      personLI.setAttribute('class', 'declined');
      document.getElementById('people-list-info').appendChild(personLI);
    }
  });
}

function getUser() {
  fetch('/users').then(response => response.json());
}

function prepareSearch() {
  document.getElementById("start-date").min = new Date().toISOString().slice(0, 10);
  document.getElementById("end-date").min = new Date().toISOString().slice(0, 10);
  searchEvents();
}

function searchEvents() {
  document.getElementById('events-container').innerText = "";
  var search = document.getElementById('search').value;
  var category = document.getElementById('category').value;
  var start = document.getElementById('start-date').value;
  var end = document.getElementById('end-date').value;
  var duration = document.getElementById('duration').value;
  var location = document.getElementById('location-id').value;
  var tags = $('#tags option:selected').toArray().map(item => item.value).join();
  fetch('/events?' + new URLSearchParams({
    search: search,
  }) + '&' + new URLSearchParams({
    category: category,
  }) + '&' + new URLSearchParams({
    start: start,
  }) + '&' + new URLSearchParams({
    end: end,
  }) + '&' + new URLSearchParams({
    duration: duration,
  }) + '&' + new URLSearchParams({
    location: location,
  }) + '&' + new URLSearchParams({
    tags: tags,
  })).then(handleError).then(response => response.json()).then(jsonObject => {
    jsonObject.searched.forEach(function (event) {
      showEvent(event, jsonObject.alreadyJoined.includes(event.id));
    });
  }).catch(error => {
    if (error == 401) {
      alert("You are not logged in and you cannot see this page. You will be redirected to login");
      fetch("/auth").then(authResponse => authResponse.json()).then(authData => {
        window.location.href = authData.authLink;
      });
    }
  });
  document.getElementById('location-id').value = "all";
}

function showEvent(event, alreadyJoined) {
  var divElement = document.createElement("div");
  var h1Element = document.createElement("h1");
  var h2Element = document.createElement("h2");
  var h3Element = document.createElement("h3");
  var brElement = document.createElement("br");
  var pElement = document.createElement("p");
  var iElement = document.createElement("i");
  var formElement = document.createElement("form");
  var inputElement = document.createElement("input");
  var buttonElement = document.createElement("button");

  inputElement.type = "submit";
  inputElement.classList.add("btn", "btn-success");
  buttonElement.classList.add("btn", "btn-success");

  formElement.action = "/events/" + event.id;
  formElement.method = "POST";
  if (!alreadyJoined)
    inputElement.value = "Join Event!";
  else {
    buttonElement.innerText = "Joined";
    buttonElement.onclick = function () { window.location.href = getCurrentUrl() + "/event.html?event_id=" + event.id; };
  }

  formElement.appendChild(inputElement);
  iElement.classList.add("fas", "fa-map-marker-alt");
  pElement.innerText = event.location;
  pElement.appendChild(iElement);

  h3Element.innerText = event.description;
  h2Element.innerText = event.duration;
  h1Element.innerText = event.title;

  divElement.appendChild(h1Element);
  divElement.appendChild(brElement);
  divElement.appendChild(h2Element);
  divElement.appendChild(h3Element);
  divElement.appendChild(brElement);
  divElement.appendChild(pElement);
  divElement.appendChild(brElement);
  if (!alreadyJoined)
    divElement.appendChild(formElement);
  else divElement.appendChild(buttonElement);
  document.getElementById("events-container").appendChild(divElement);
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
            var start, end, allDay = false;
            if (event.start.dateTime) {
              start = event.start.dateTime.value;
              end = event.end.dateTime.value;
            } else {
              start = event.start.date.value;
              end = event.end.date.value;
              allDay = true;
            }
            var shared = null, private = null;
            if (event.extendedProperties)
              shared = event.extendedProperties.shared, private = event.extendedProperties.private;
            fullcalendarEvents.push({
              id: event.id,
              title: event.summary,
              start: start,
              end: end,
              allDay: allDay,
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
      fetch("/auth?origin=calendar").then(authResponse => authResponse.json()).then(authInfo => {
        if (authInfo.isLoggedIn) {
          window.location.href = getCurrentUrl() + "/token?origin=calendar";
        } else {
          window.location.href = authInfo.authLink;
        }
      });

    }
  });
}

function getCurrentUrl() {
  var currentUrl = window.location.href;
  var currentUrlSlices = currentUrl.split("/");
  return currentUrlSlices[0] + "//" + currentUrlSlices[2];
}

function getCurrentLocation() {
  var currentUrl = window.location.pathname;
  var currentUrlSlices = currentUrl.split("/");
  return currentUrlSlices[1].split(".")[0];
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

function loadFreeTimes() {
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = urlParams.get('eventId');
  if (eventId == null) {
    alert("Your request is incomplete. Please access this page from the event page!");
    return;
  }

  fetch("/events/schedule?eventId=" + eventId).then(response => response.json()).then(freeTimes => {
    freeTimes.forEach(freeTime => {
      var toStartDate = new Date(freeTime.start);
      var toEndDate = new Date(freeTime.end);
      var buttonElem = document.createElement("button");
      buttonElem.classList.add("btn", "btn-success");
      buttonElem.innerText = toStartDate.toLocaleString() + "  ///  " + toEndDate.toLocaleString();
      buttonElem.setAttribute("onclick", "setTime('" + eventId + "','" + toStartDate.toISOString() + "')");
      document.getElementById("page-content-wrapper").appendChild(buttonElem);
    })
  }).catch(error => alert(error));
}

function setTime(eventId, start) {
  fetch("/events?" + new URLSearchParams({ start: start }) + "&" + new URLSearchParams({ eventId: eventId }), { method: 'PUT' }).then(response => {
    window.location.href = getCurrentUrl() + "/event.html?event_id=" + eventId;
  }).catch(error => alert(error));
}

function fetchUserInfo() {
  fetch("/users").then(handleError).then(response => response.json()).then(userInfo => {
    const email = userInfo.email;
    var username = userInfo.username;
    if (username == null) {
      document.getElementById("username-placeholder").innerText = "You currently do not have an username. If you want to set one, click ";
      username = email;
    } else {
      document.getElementById("username-placeholder").innerText = "Your username is currently: " + username + ". If you want to change it click ";
    }
    var displayBoxButton = document.createElement("a");
    displayBoxButton.setAttribute("href", "#");
    displayBoxButton.setAttribute("onclick", "hideElementById('username-placeholder'); showElementById('username-setter')");
    displayBoxButton.innerText = "here";
    document.getElementById("username-placeholder").appendChild(displayBoxButton);
    document.getElementById("user-header").innerText = "Hello, " + username + "!";
    createPopoverForEventTypes("invited-events", userInfo.invitedEvents, "Events you are invited to");
    createPopoverForEventTypes("joined-events", userInfo.joinedEvents, "Events you joined");
    createPopoverForEventTypes("declined-events", userInfo.declinedEvents, "Events you declined");
  }).catch(error => {
    if (error == 401)
      alert("Please login first, using the button on the sidebar");
    else alert(error);
  });
}

function handleError(response) {
  if (!response.ok)
    throw response.status;
  return response;
}

function createPopoverForEventTypes(givenId, givenContent, givenTitle) {
  $("#" + givenId).popover({
    animation: true,
    html: true,
    title: givenTitle,
    content: createElementsForEvents(givenContent),
    trigger: 'click',
    container: 'body',
    placement: 'top',
  });
}

function createElementsForEvents(givenElements) {
  var ulElement = document.createElement("ul");
  var eventsExist = false;
  givenElements.forEach(event => {
    eventsExist = true;
    var liElement = document.createElement("li");
    var aElement = document.createElement("a");
    aElement.href = getCurrentUrl() + "/event.html?event_id=" + event.id;
    aElement.innerText = event.title;
    liElement.appendChild(aElement);
    ulElement.appendChild(liElement);
  });
  if (!eventsExist) {
    ulElement.innerText = "There are no events to be displayed";
  }
  return ulElement;
}

function showElementById(Id) {
  document.getElementById(Id).style.display = "";
}

function hideElementById(Id) {
  document.getElementById(Id).style.display = "none";
}

function changePopoverColorTo(color) {
  var styleNode = document.getElementById("stylesheetId");
  if (styleNode == null) {
    styleNode = document.createElement("style");
    styleNode.id = "stylesheetId";
  } else {
    document.body.removeChild(styleNode);
  }
  styleNode.innerHTML = ".popover-header {background: " + color + ";}";
  document.body.appendChild(styleNode);
}

function getCredentialIfNeeded() {
  verifyCredentials().then(validCredential => {
    if (!validCredential) {
      fetch("/auth?origin=add_event").then(authResponse => authResponse.json()).then(authInfo => {
        if (authInfo.isLoggedIn) {
          window.location.href = getCurrentUrl() + "/token?origin=add_event";
        } else {
          window.location.href = authInfo.authLink;
        }
      });
    }
  })
}
