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

package com.google.sps.servlets;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.Time;
import com.google.sps.data.DateTimeRange;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import com.google.sps.data.Search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpHeaders;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/events/*")
public class EventServlet extends HttpServlet {
  UserStorage userStorageObject;
  EventStorage eventStorageObject;
  UserService userService;
  Utils utilsObject;
  AuthorizationCodeFlow flow;

  @Inject
  EventServlet(UserStorage userStorageObject, EventStorage eventStorageObject, UserService userService,
      Utils utilsObject, AuthorizationCodeFlow flow) {
    this.userStorageObject = userStorageObject;
    this.eventStorageObject = eventStorageObject;
    this.userService = userService;
    this.utilsObject = utilsObject;
    this.flow = flow;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String pathName = request.getPathInfo();
    
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (pathName == null || pathName.isEmpty() || pathName.equals("/")) {
      String text = request.getParameter("search");
      String category = request.getParameter("category");
      String start = request.getParameter("start");
      String end = request.getParameter("end");
      String duration = request.getParameter("duration");
      String location = request.getParameter("location");
      String tagsString = request.getParameter("tags");
      if (tagsString == null) {
        tagsString = "";
      }
      List<String> tags = Arrays.asList(tagsString.split("\\s*,\\s*"));

      Search search = new Search(text, category, start, end, duration, location, tags);

      List<Event> events = eventStorageObject.getSearchedEvents(search);

      Gson gson = new Gson();

      JsonObject wrapper = new JsonObject();
      JsonElement eventsJson = gson.toJsonTree(events);
      JsonElement userJoinedEventsJson = gson
          .toJsonTree(userStorageObject.getUser(userService.getCurrentUser().getUserId()).getJoinedEventsID());
      wrapper.getAsJsonObject().add("alreadyJoined", userJoinedEventsJson);
      wrapper.getAsJsonObject().add("searched", eventsJson);
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(events));

      return;
    }

    if (pathName.equals("/schedule")) {
      getFreeTimesForEvent(request, response);
      return;
    }

    if (pathName.equals("/gcalendar")) {
      getEvents(request, response, userService);
      return;
    }

    String[] pathParts = pathName.split("/");
    String eventId = pathParts[1];

    if (!getEvent(request, response, userService.getCurrentUser().getUserId(), eventId)) {
      // TODO: Write message to user
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (!UserServiceFactory.getUserService().isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String currentUserId = UserServiceFactory.getUserService().getCurrentUser().getUserId();

    String pathName = request.getPathInfo();

    if (pathName == null || pathName.isEmpty() || pathName.equals("/")) {
      String eventId = addEvent(request, response, currentUserId);
      if (eventId != null) {
        response.sendRedirect("/event.html?event_id=" + eventId);
      } else {
        // TODO: Write message to user
      }
      return;
    }

    String[] pathParts = pathName.split("/");
    String eventId = pathParts[1];

    if (joinEvent(request, response, currentUserId, eventId)) {
      response.sendRedirect("/event.html?event_id=" + eventId);
    } else {
      // TODO: Write message to user
    }
  }

  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String eventId = request.getParameter("eventId");
    if (eventId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Event event = eventStorageObject.getEvent(eventId);
    if (event == null || event.isDateTimeSet()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (!event.getOwnerID().equals(userService.getCurrentUser().getUserId())) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String start = request.getParameter("start");
    if (start == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String startDate = start.split("T")[0];
    String startTime = start.split("T")[1].split("Z")[0];
    startTime = String.valueOf(startTime.subSequence(0, 5));
    DateTimeRange newRange = new DateTimeRange(startDate, startTime, Long.valueOf(0));
    event.setRange(newRange);
    event.setGCalendarId(Utils.createGCalendarEvent(event).getId());
    eventStorageObject.addOrUpdateEvent(event);
    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String addEvent(HttpServletRequest request, HttpServletResponse response, String currentUserId)
      throws IOException {
    Long duration = parseLongFromString(request.getParameter("duration"));
    if (duration == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }

    Map<String, String> fields = null;
    if (request.getParameterValues("fields") != null) {
      fields = new HashMap<String, String>();
      for (String field : request.getParameterValues("fields")) {
        if (request.getParameter(field) != null) {
          fields.put(field.trim(), request.getParameter(field).trim());
        }
      }
    }
    boolean isPublic = true;
    if (request.getParameter("is-public") == null) {
      isPublic = false;
    }
    Long tzShift = parseLongFromString(request.getParameter("tzShift"));
    if (tzShift == null) {
      tzShift = Long.valueOf(0);
    }

    Event.Builder eventBuilder = Event.newBuilder()
        .setOwnerID(currentUserId)
        .setTitle(request.getParameter("title"))
        .setDescription(request.getParameter("description"))
        .setCategory(request.getParameter("category"))
        .setTags(parseTags(request.getParameterValues("tags")))
        .setLocation(request.getParameter("location"))
        .setLocationId(request.getParameter("location-id"))
        .setDateTimeRange(formatDateTimeRange(request.getParameter("start-date"), request.getParameter("start-time"),
            request.getParameter("end-date"), request.getParameter("end-time"), tzShift))
        .setDuration(duration)
        .setLinks(parseLinks(request.getParameter("links")))
        .setFields(fields)
        .setIsPublic(isPublic)
        .setInvitedIDs(parseInvitedIDs(request.getParameterValues("people")));

    Event event = eventBuilder.build();

    String gcalendarId = null;
    if (event.isDateTimeSet()) {
      com.google.api.services.calendar.model.Event newEvent = Utils.createGCalendarEvent(event);
      if (newEvent == null) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return null;
      }
      gcalendarId = newEvent.getId();
    }
    if (gcalendarId != null) {
      event = eventBuilder.setGCalendarID(gcalendarId).build();
    }

    String eventId = null;
    try {
      eventId = eventStorageObject.addOrUpdateEvent(event);
      userStorageObject.joinEvent(currentUserId, eventId, /*isPublic=*/true);
    } catch (Exception e) {
      System.err.println("Can't add new event to storage: " + e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    return eventId;
  }

  private void getFreeTimesForEvent(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String eventId = request.getParameter("eventId");
    if (eventId == null || eventStorageObject.getEvent(eventId) == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DateTimeRange timeRange = eventStorageObject.getEvent(eventId).getDateTimeRange();
    Long duration = eventStorageObject.getEvent(eventId).getDuration();
    if (timeRange.isDateTimeSet()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DateTime startDateTime = new DateTime(timeRange.convertStartTimeToUTCString());
    DateTime startDateTimeWithShift = new DateTime(startDateTime.getValue() + timeRange.getShift());
    DateTime endDateTime = new DateTime(timeRange.convertEndTimeToUTCString());
    DateTime endDateTimeWithShift = new DateTime(endDateTime.getValue() + timeRange.getShift());
    String ownerId = eventStorageObject.getEvent(eventId).getOwnerID();

    if (userService.getCurrentUser() == null || !userService.getCurrentUser().getUserId().equals(ownerId)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    List<String> joinedParticipantsIds = eventStorageObject.getEvent(eventId).getJoinedIDs();
    if (!joinedParticipantsIds.contains(ownerId)) {
      joinedParticipantsIds.add(ownerId);
    }

    List<Credential> usersCredentials;
    try {
      usersCredentials = getCredentialsFromUserList(joinedParticipantsIds);
    } catch (InterruptedException e2) {
      e2.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    Vector<Time> busyTimesForAttendees = utilsObject.getBusyTimesForAttendees(response, flow, usersCredentials,
        startDateTimeWithShift, endDateTimeWithShift);
    writeFreeTimesFromInterval(startDateTimeWithShift.getValue(), endDateTimeWithShift.getValue(), duration,
        busyTimesForAttendees, response);
  }

  private void writeFreeTimesFromInterval(Long start, Long end, Long duration, Vector<Time> busyTimes,
      HttpServletResponse response) throws IOException {
    List<Time> freeTimes = new ArrayList<>();
    final Long SHIFT = Long.valueOf(1000 * 60 * 15);
    Long currentStart = start;
    Long currentEnd = start + duration * 60 * 1000;

    while (currentEnd <= end) {
      final Long innerCurrentStart = Long.valueOf(currentStart);
      final Long innerCurrentEnd = Long.valueOf(currentEnd);
      if (!busyTimes.stream().anyMatch(time -> time.overlaps(innerCurrentStart, innerCurrentEnd))) {
        freeTimes.add(new Time(currentStart, currentEnd));
      }
      currentStart = currentStart + SHIFT;
      currentEnd += SHIFT;
    }
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(freeTimes));

    return;
  }

  private List<Credential> getCredentialsFromUserList(List<String> userIds) throws InterruptedException {
    List<Credential> usersCredential = new ArrayList<>();
    ExecutorService executorService = Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory());
    List<Callable<Credential>> callables = new ArrayList<>();
    userIds.forEach(userId -> {
      callables.add(() -> {
        return flow.loadCredential(userId);
      });
    });
    List<Future<Credential>> futureCredentials = executorService.invokeAll(callables);
    futureCredentials.stream().forEach(futureCredential -> {
      try {
        usersCredential.add(futureCredential.get(700, TimeUnit.MILLISECONDS));
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        e.printStackTrace();
      }
    });
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS); // Await max 10 seconds before returning
    return usersCredential;
  }

  private boolean getEvent(HttpServletRequest request, HttpServletResponse response, String currentUserId,
      String eventId) throws IOException {
    Event event = eventStorageObject.getEvent(eventId);
    if (event == null) {
      System.err.println("Can't find event with id " + eventId);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return false;
    }

    if (!event.hasUserAccessToEvent(currentUserId)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }
    response.setContentType("application/json;");
    
    event.setInvitedIDs(IDsToUsernames(event.getInvitedIDs()));
    event.setJoinedIDs(IDsToUsernames(event.getJoinedIDs()));
    event.setDeclinedIDs(IDsToUsernames(event.getDeclinedIDs()));
    response.getWriter().println(new Gson().toJson(event));
    return true;
  }

  private void getEvents(HttpServletRequest request, HttpServletResponse response, UserService userService)
      throws IOException {
    String startEpochInSeconds = request.getParameter("startEpochInSeconds");
    String endEpochInSeconds = request.getParameter("endEpochInSeconds");

    if (startEpochInSeconds == null || endEpochInSeconds == null || !startEpochInSeconds.matches("^[0-9]+$")
        || !endEpochInSeconds.matches("^[0-9]+$")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Long startEpochInSecondsParsed = parseLongFromString(startEpochInSeconds);
    Long endEpochInSecondsParsed = parseLongFromString(endEpochInSeconds);

    if (startEpochInSecondsParsed == null || endEpochInSecondsParsed == null
        || startEpochInSecondsParsed > endEpochInSecondsParsed) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String eventsInJson = getGCalendarEventsByInterval(startEpochInSecondsParsed, endEpochInSecondsParsed);
    if (eventsInJson == null)
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    else
      response.getWriter().println(eventsInJson);
    return;
  }

  private boolean joinEvent(HttpServletRequest request, HttpServletResponse response, String currentUserId,
      String eventId) throws IOException {
    if (!eventStorageObject.hasUserAccessToEvent(currentUserId, eventId)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }

    try {
      Event event = eventStorageObject.getEvent(eventId);
      if (event == null) {
        System.err.println("Can't find event with id " + eventId);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      if (event.getJoinedIDs().contains(currentUserId)) {
        return true;
      }
      userStorageObject.joinEvent(currentUserId, eventId, event.isPublic());
      User user = userStorageObject.getUser(currentUserId);
      if (user == null) {
        System.err.println("Can't find user with id " + currentUserId);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      if (event.isDateTimeSet())
        Utils.joinGCalendarEvent(event.getOwnerID(), event.getGCalendarID(), user.getEmail());
    } catch (Exception e) {
      // TODO: specify exception
      System.err.println("Can't add new event to storage: " + e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return false;
    }

    return true;
  }

  private List<String> IDsToUsernames(List<String> IDs) {
    return IDs != null
        ? IDs.stream().map(id -> userStorageObject.getUsernameByID(id))
            .filter(Objects::nonNull).collect(Collectors.toList())
        : null;
  }

  private List<String> parseInvitedIDs(String[] invitedIDs) {
    return invitedIDs != null
        ? Arrays.asList(invitedIDs).stream().map(person -> userStorageObject.getIDbyUsername(person))
            .filter(Objects::nonNull).collect(Collectors.toList())
        : null;
  }

  private List<String> parseTags(String[] tags) {
    return tags != null ? Arrays.asList(Arrays.stream(tags).map(String::trim).toArray(String[]::new)) : null;
  }

  private List<String> parseLinks(String links) {
    return Arrays.asList(links.split(","));
  }

  private DateTimeRange formatDateTimeRange(String startDate, String startTime, String endDate, String endTime, Long tzShift) {
    if (endDate == null || endTime == null)
      return new DateTimeRange(startDate, startTime, tzShift);
    return new DateTimeRange(startDate, endDate, startTime, endTime, tzShift);
  }

  private Long parseLongFromString(String str) {
    try {
      return Long.parseLong(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String getGCalendarEventsByInterval(Long startEpochInSeconds, Long endEpochInSeconds) throws IOException {
    DateTime startDateTime = new DateTime(startEpochInSeconds);
    DateTime endDateTime = new DateTime(endEpochInSeconds);
    try {
      Calendar service = Utils.loadCalendarClient();
      HttpHeaders headers = new com.google.api.client.http.HttpHeaders().setAcceptEncoding("gzip").setUserAgent("gzip");
      List<com.google.api.services.calendar.model.Event> events = service.events().list("primary")
          .setFields("items(summary,start,end,description,extendedProperties,location)").setSingleEvents(true)
          .setTimeMin(startDateTime).setTimeMax(endDateTime).setRequestHeaders(headers).execute().getItems();
      return new Gson().toJson(events);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }
}
