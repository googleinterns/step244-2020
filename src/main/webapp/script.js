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
    document.getElementById('title-info').innerText = event.title;
    document.getElementById('start-date-info').innerText = event.dateTimeRange.startDate;
    document.getElementById('start-time-info').innerText = event.dateTimeRange.startTime;
    if (event.duration != null && event.duration != "") {
      document.getElementById('duration-info').hidden = false;
      document.getElementById('duration-info').innerText = event.duration + ' minutes';
    }

    document.getElementById('category-info').innerText = event.category;
    for (tag in event.tags) {
      const tagLI = document.createElement('li');
      tagLI.innerText = event.tags[tag];
      document.getElementById('tags-info').appendChild(tagLI);
    }

    document.getElementById('description-info').innerText = event.description;
    if (event.location != null && event.location != "") {
      document.getElementById('location-wrapper').hidden = false;
      document.getElementById('location-info').innerText = event.location;
    }

    for (link in event.links) {
      document.getElementById('links-info').hidden = false;
      const linkA = document.createElement('a');
      linkA.innerText = event.links[link];
      linkA.href = "https://" + event.links[link];
      document.getElementById('links-info').appendChild(linkA);
    }

    if (typeof event.gcalendarId === "undefined") {
      document.getElementById("doodle-link-info").hidden = false;
      document.getElementById("doodle-link-info").href = getCurrentUrl() + "/doodle.html?eventId=" + event.id;
    }

    for (field in event.fields) {
      const fieldLI = document.createElement('li');
      fieldLI.innerText = field + ': ' + event.fields[field];
      document.getElementById('fields-info').appendChild(fieldLI);
    }

    document.getElementById('owner-info').innerText = event.ownerId;
    var peopleList = document.getElementById('people-list-info');
    for (person in event.joinedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.joinedUsersId[person];
      personLI.setAttribute('class', 'joined');
      peopleList.appendChild(personLI);
    }
    for (person in event.invitedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.invitedUsersId[person];
      personLI.setAttribute('class', 'invited');
      peopleList.appendChild(personLI);
    }
    for (person in event.declinedUsersId) {
      const personLI = document.createElement('li');
      personLI.innerText = event.declinedUsersId[person];
      personLI.setAttribute('class', 'declined');
      peopleList.appendChild(personLI);
    }

    var today = new Date();
    var startHours = parseInt(event.dateTimeRange.startTime.split(':')[0]);
    var startYear = parseInt(event.dateTimeRange.startDate.split('-')[0]);
    var startMonth = parseInt(event.dateTimeRange.startDate.split('-')[1]);
    var startDate = parseInt(event.dateTimeRange.startDate.split('-')[2]);
    var days = null;
    var hours = null;
    if (startYear == today.getFullYear() && startMonth == today.getMonth() + 1) {
      days = startDate - today.getDate();
      if (days == 0) {
        hours = startHours - today.getUTCHours();
      }
    }
    if (days == null || days > 7)
      return;
    
    fetch('/weather?' + new URLSearchParams({
        location: event.location,
      }) + '&' + new URLSearchParams({
        hours: hours,
      }) + '&' + new URLSearchParams({
        days: days,
      })).then(weatherResponse => weatherResponse.json()).then((weather) => {
      document.getElementById('weather-info').hidden = false;
      document.getElementById('weather-type-info').innerText = weather.type;
      document.getElementById('weather-temperature-info').innerText = "Temperature: " + weather.temperature + "\u00B0C, feels like " + 
                                                                                        weather.temperatureFeelsLike + "\u00B0C";
      document.getElementById('weather-pressure-info').innerText = "Pressure: " + weather.pressure;
      document.getElementById('weather-humidity-info').innerText = "Humidity: " + weather.humidity;
      document.getElementById('weather-clouds-info').innerText = "Clouds: " + weather.clouds;
      document.getElementById('weather-icon').src = `https://openweathermap.org/img/wn/${weather.iconId}@2x.png`;
    });
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
  fetch("/auth?origin=search").then(authResponse => authResponse.json()).then(responseJson => {
    if (!responseJson.isLoggedIn) {
      window.location.href = responseJson.authLink;
      return;
    }
    verifyCredentials().then(validCredential => {
      if (!validCredential) {
        window.location.href = getCurrentUrl() + "/token?origin=search";
        return;
      }
      var search = document.getElementById('search').value;
      var category = document.getElementById('category').value;
      var start = document.getElementById('start-date').value;
      var end = document.getElementById('end-date').value;
      var duration = document.getElementById('duration').value;
      var location = document.getElementById('location-id').value;
      var tags = $('#tags option:selected').toArray().map(item => "tags=" + item.value).join('&');
  
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
      }) + '&' + tags
      ).then(handleError).then(response => response.json()).then(jsonObject => {
        var userId = responseJson.userId;
        jsonObject.forEach(function (event) {
          showEvent(event, event.joinedUsersId.includes(userId));
        });
      });
    });
  });
  document.getElementById('location-id').value = "all";
}

function showEvent(event, alreadyJoined) {
  var divElement = document.createElement("div");
  var h1Element = document.createElement("h1");
  var h2Element = document.createElement("h2");
  var h3Element = document.createElement("h3");
  var brElement = document.createElement("br");
  var hrElement = document.createElement("hr");
  var pElement1 = document.createElement("p");
  var pElement2 = document.createElement("p");
  var pElement3 = document.createElement("p");
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
  pElement1.innerHTML = '<i class="fas fa-clock"></i> ' + event.date + ' ' + event.time;
  pElement2.innerHTML = '<i class="fas fa-stopwatch"></i> ' + event.duration + ' minutes';
  pElement3.innerHTML = '<i class="fas fa-map-marker-alt"></i> ' + event.location;
  h3Element.innerText = event.description;
  h1Element.innerText = event.title;

  divElement.appendChild(h1Element);
  divElement.appendChild(brElement);
  divElement.appendChild(h2Element);
  divElement.appendChild(brElement);
  divElement.appendChild(pElement1);
  divElement.appendChild(pElement2);
  divElement.appendChild(pElement3);
  divElement.appendChild(hrElement);
  divElement.appendChild(brElement);
  divElement.appendChild(h3Element);
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
  fetch("/auth?origin=calendar").then(authResponse => authResponse.json()).then(responseJson => {
    if (!responseJson.isLoggedIn) {
      window.location.href = responseJson.authLink;
      return;
    }
    var userId = responseJson.userId;
    verifyCredentials().then(validCredential => {
      if (validCredential) {
        fetch("/events/gcalendar?startEpochInSeconds=" + startTime + "&endEpochInSeconds=" + endTime)
          .then(response => response.json()).then(events => {
            var gcalendarEvents = [];
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
              var className, omitEvent = false, color;
              if (shared != null && shared.seeYouId) {
                className = "see-you-event";
                color = "#28a745";
              } else {
                if (event.attendees) {
                  event.attendees.forEach(eventAttendee => {
                    if (eventAttendee.self === true) {
                      if (eventAttendee.responseStatus === "accepted") {
                        className = "accepted-event";
                      }
                      else if (eventAttendee.responseStatus === "needsAction") {
                        className = "transparent-event";
                      }
                      else {
                        omitEvent = true;
                      }
                      color = "#007bff";
                    }
                  })
                } else {
                  className = "accepted-event";
                  color = "#007bff";
                }
              }
              if (!omitEvent) {
                gcalendarEvents.push({
                  id: event.id,
                  color: color,
                  classNames: [className],
                  title: event.summary,
                  start: start,
                  end: end,
                  allDay: allDay,
                  location: event.location,
                  description: event.description,
                  shared: shared,
                  private: private
                });
              }
            });
            var startDateTimeRange = new Date(startTime - 48 * 60 * 60 * 1000).toISOString().slice(0, 10);
            var endDateTimeRange = new Date(endTime + 48 * 60 * 60 * 1000).toISOString().slice(0, 10);
            var storedEventsSource = [];
            fetch("events?start=" + startDateTimeRange + "&end=" + endDateTimeRange).then(newResponse => newResponse.json())
              .then(storedEvents => {
                storedEvents.forEach(storedEvent => {
                  if (!storedEvent.joinedUsersId.includes(userId) || typeof storedEvent.gcalendarId !== "undefined")
                    return;
                  var startTime = new Date(storedEvent.dateTimeRange.startDate + "T" + storedEvent.dateTimeRange.startTime + ":00Z");
                  var endTime = new Date(storedEvent.dateTimeRange.endDate + "T" + storedEvent.dateTimeRange.endTime + ":00Z");
                  var startTimeWithShift = new Date(startTime.getTime() + storedEvent.dateTimeRange.tzShift).getTime();
                  var endTimeWithShift = new Date(endTime.getTime() + storedEvent.dateTimeRange.tzShift).getTime() + 24 * 60 * 60 * 1000;

                  storedEventsSource.push({
                    id: storedEvent.id,
                    storageId: storedEvent.id,
                    title: storedEvent.title,
                    description: storedEvent.description,
                    shared: storedEvent.fields,
                    location: storedEvent.location,
                    start: startTimeWithShift,
                    end: endTimeWithShift,
                    color: "#dc3545",
                    classNames: ["no-time-set-event"],
                    allDay: true
                  });
                });

                while (calendar.getEventSources().length)
                  calendar.getEventSources()[0].remove();
                calendar.addEventSource(gcalendarEvents);
                calendar.addEventSource(storedEventsSource);
              });
          });
      } else {
        window.location.href = getCurrentUrl() + "/token?origin=calendar";
      }
    });
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
  var localStorageEventId = null;
  if (givenProperties.shared) {
    for (const [key, value] of Object.entries(givenProperties.shared)) {
      if (key === "seeYouId") {
        localStorageEventId = value;
        continue;
      }
      var extendedPropertyElement = document.createElement("div");
      extendedPropertyElement.classList.add("extendedprop-wrapper");
      var extendedIcon = document.createElement("i");
      extendedIcon.classList.add("fa", "fa-plus-circle");
      var extendedPropertyName = document.createElement("div");
      extendedPropertyName.appendChild(extendedIcon);
      var textNode = document.createTextNode(key + ": ");
      extendedPropertyName.appendChild(textNode);
      extendedPropertyName.classList.add("left-item");
      var extendedPropertyValue = document.createElement("div");
      extendedPropertyValue.innerHTML = value;
      extendedPropertyValue.classList.add("right-item");
      extendedPropertyElement.appendChild(extendedPropertyName);
      extendedPropertyElement.appendChild(extendedPropertyValue);
      eventWrapper.appendChild(extendedPropertyElement);
    }
  }
  if (givenProperties.storageId != null)
    localStorageEventId = givenProperties.storageId;
  if (localStorageEventId != null) {
    var buttonDivElement = document.createElement("div");
    buttonDivElement.classList.add("add-button");
    var buttonElement = document.createElement("button");
    buttonElement.onclick = function () { window.location.href = getCurrentUrl() + "/event.html?event_id=" + localStorageEventId; }
    buttonElement.classList.add("btn", "btn-primary");
    var plusIcon = document.createElement("i");
    plusIcon.classList.add("fas", "fa-edit");

    buttonElement.appendChild(plusIcon);
    buttonDivElement.appendChild(buttonElement);
    eventWrapper.appendChild(buttonDivElement);
  }
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
      changePopoverColorTo(event.event.backgroundColor);
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

function addTag() {
  var eventTag = document.getElementById('event-custom-tag').value;
  document.getElementById('event-custom-tag').value = '';

  const tagInput = document.createElement('input');
  tagInput.setAttribute('type', 'hidden');
  tagInput.setAttribute('name', 'tags');
  tagInput.setAttribute('value', eventTag);
  document.getElementById('add-event-form').insertBefore(tagInput, document.getElementById('event-custom-tags'));

  const tagLI = document.createElement('li');
  tagLI.innerText = eventTag;
  document.getElementById('event-custom-tags-list').appendChild(tagLI);
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

  fetch("/events/schedule?eventId=" + eventId).then(handleError).then(response => response.json()).then(freeTimes => {
    freeTimes.forEach(freeTime => {
      var toStartDate = new Date(freeTime.start);
      var toEndDate = new Date(freeTime.end);
      var buttonElem = document.createElement("button");
      buttonElem.classList.add("btn", "btn-success");
      buttonElem.innerText = toStartDate.toLocaleString() + "  ///  " + toEndDate.toLocaleString();
      buttonElem.setAttribute("onclick", "setTime('" + eventId + "','" + toStartDate.toISOString() + "')");
      document.getElementById("page-content-wrapper").appendChild(buttonElem);
    })
  }).catch(error => {
    if (error == 401)
      alert("You are not the owner of the event. You cannot select the time.");
  });
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
